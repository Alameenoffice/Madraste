package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AttendanceStatus
import com.example.data.model.SchoolType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveSchoolConfig_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = SchoolRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveSchoolConfig(
                name = "ثانوية زمار النموذجية",
                address = "الموصل - زمار",
                schoolType = SchoolType.GOVERNMENT,
                emblemTitle = "العلم نور",
                academicYear = "2026 - 2027"
            )
        }
        assertTrue(saveResult.isSuccess)

        val config = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSchoolConfig().first { it != null }
        }
        assertNotNull(config)
        assertEquals("ثانوية زمار النموذجية", config?.name)
    }

    @Test
    fun addAndUpdateStudentAttendance_authenticatedOwner_createsAlertAndUpdatesStatus() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = SchoolRepository(firestore)

        val studentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addStudent(
                fullName = "أحمد خليل الموصلي",
                gradeClass = "الصف الأول المتوسط",
                section = "أ",
                parentName = "خليل الموصلي",
                parentPhone = "07512362567"
            ).getOrThrow()
        }

        val students = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudents().first { list -> list.any { it.id == studentId } }
        }
        val createdStudent = students.first { it.id == studentId }

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateStudentAttendance(createdStudent, AttendanceStatus.ABSENT, autoSendAlert = true)
        }
        assertTrue(updateResult.isSuccess)
    }

    @Test
    fun crossUserAccess_bobReadingAliceStudent_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SchoolRepository(firestore)
        val studentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addStudent(
                fullName = "سارة العبيدي",
                gradeClass = "الصف الثاني المتوسط",
                section = "أ",
                parentName = "محمد العبيدي",
                parentPhone = "07512362567"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        var caughtException: Exception? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("students").document(studentId)
                    .get().await()
            }
        } catch (e: Exception) {
            caughtException = e
        }
        assertNotNull(caughtException)
        val firestoreEx = caughtException?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()

        var caughtException: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("students")
                    .whereEqualTo("userId", aliceUid)
                    .get().await()
            }
        } catch (e: Exception) {
            caughtException = e
        }
        assertNotNull(caughtException)
        val firestoreEx = caughtException?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_school@test.com"
        const val BOB_EMAIL = "bob_school@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
