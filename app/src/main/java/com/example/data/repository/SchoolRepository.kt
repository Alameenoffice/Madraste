package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AttendanceStatus
import com.example.data.model.GradeEntry
import com.example.data.model.LessonPrep
import com.example.data.model.NoticeType
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolEvent
import com.example.data.model.SchoolNotice
import com.example.data.model.SchoolType
import com.example.data.model.StudentRecord
import com.example.data.model.TeacherRecord
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SchoolRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً للوصول إلى بيانات المدرسة")
    }

    // 1. Observe School Config
    fun observeSchoolConfig(): Flow<SchoolConfig?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/schools"
        emitAll(
            db.collection("users").document(uid).collection("schools")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.firstOrNull()?.toObject(
                        SchoolConfig::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // 2. Save or Update School Config
    suspend fun saveSchoolConfig(
        name: String,
        address: String,
        schoolType: SchoolType,
        emblemTitle: String,
        academicYear: String,
        existingSchoolId: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val isUpdate = !existingSchoolId.isNullOrBlank()
        val docId = if (isUpdate) existingSchoolId!! else "sch_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("schools").document(docId)
        try {
            if (isUpdate) {
                val updates = mapOf(
                    "name" to name.trim().take(120),
                    "address" to address.trim().ifBlank { "الموصل - زمار" }.take(200),
                    "schoolType" to schoolType.code,
                    "emblemTitle" to emblemTitle.trim().ifBlank { "العلم نور والاجتهاد رفعة" }.take(120),
                    "academicYear" to academicYear.trim().ifBlank { "2026 - 2027" }.take(40),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "name" to name.trim().take(120),
                    "address" to address.trim().ifBlank { "الموصل - زمار" }.take(200),
                    "schoolType" to schoolType.code,
                    "emblemTitle" to emblemTitle.trim().ifBlank { "العلم نور والاجتهاد رفعة" }.take(120),
                    "academicYear" to academicYear.trim().ifBlank { "2026 - 2027" }.take(40),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isUpdate) OperationType.UPDATE else OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // 3. Students Observation & CRUD
    fun observeStudents(): Flow<List<StudentRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/students"
        emitAll(
            db.collection("users").document(uid).collection("students")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StudentRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getStudentById(studentId: String): Result<StudentRecord?> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("students").document(studentId)
        try {
            docRef.get().await().toObject(
                StudentRecord::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun addStudent(
        fullName: String,
        gradeClass: String,
        section: String,
        parentName: String,
        parentPhone: String,
        isHonorRoll: Boolean = false,
        honorReason: String = "",
        totalTuition: Long = 0L,
        paidTuition: Long = 0L,
        nextInstallmentDate: String = "غير محدد"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "st_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val codeSuffix = (1000..9999).random()
        val accessCode = "ST-$codeSuffix"
        val docRef = db.collection("users").document(uid).collection("students").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().take(120),
            "gradeClass" to gradeClass.trim().take(60),
            "section" to section.trim().ifBlank { "أ" }.take(20),
            "parentName" to parentName.trim().take(120),
            "parentPhone" to parentPhone.trim().ifBlank { "07512362567" }.take(30),
            "accessCode" to accessCode,
            "attendanceStatus" to AttendanceStatus.PRESENT.code,
            "absenceDaysMonth" to 0L,
            "isHonorRoll" to isHonorRoll,
            "honorReason" to honorReason.trim().take(200),
            "totalTuition" to totalTuition.coerceIn(0L, 100000000L),
            "paidTuition" to paidTuition.coerceIn(0L, 100000000L),
            "nextInstallmentDate" to nextInstallmentDate.trim().take(60),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStudentAttendance(
        student: StudentRecord,
        newStatus: AttendanceStatus,
        autoSendAlert: Boolean = true
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("students").document(student.id)
        val updatedAbsenceCount = when {
            newStatus == AttendanceStatus.ABSENT && student.attendanceStatus != AttendanceStatus.ABSENT.code ->
                (student.absenceDaysMonth + 1L).coerceAtMost(31L)
            newStatus != AttendanceStatus.ABSENT && student.attendanceStatus == AttendanceStatus.ABSENT.code ->
                (student.absenceDaysMonth - 1L).coerceAtLeast(0L)
            else -> student.absenceDaysMonth
        }
        try {
            docRef.update(
                mapOf(
                    "attendanceStatus" to newStatus.code,
                    "absenceDaysMonth" to updatedAbsenceCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (autoSendAlert && (newStatus == AttendanceStatus.ABSENT || newStatus == AttendanceStatus.LATE)) {
                val statusLabel = if (newStatus == AttendanceStatus.ABSENT) "غائب" else "متأخر عن الدوام"
                sendNotice(
                    studentId = student.id,
                    studentName = student.fullName,
                    parentPhone = student.parentPhone,
                    noticeType = NoticeType.ABSENCE_ALERT,
                    title = "إشعار فوري: تسجيل الطالب ($statusLabel)",
                    message = "نود إعلامكم بأن الطالب (${student.fullName}) في ${student.gradeClass} - شعبة (${student.section}) تم تسجيله اليوم كـ ($statusLabel). إجمالي أيام الغياب الشهري: $updatedAbsenceCount يوم.",
                    senderRole = "نظام الحضور الذكي"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStudentHonorRoll(
        studentId: String,
        isHonor: Boolean,
        honorReason: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("students").document(studentId)
        try {
            docRef.update(
                mapOf(
                    "isHonorRoll" to isHonor,
                    "honorReason" to honorReason.trim().take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStudentTuition(
        studentId: String,
        totalTuition: Long,
        paidTuition: Long,
        nextInstallmentDate: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("students").document(studentId)
        try {
            docRef.update(
                mapOf(
                    "totalTuition" to totalTuition.coerceIn(0L, 100000000L),
                    "paidTuition" to paidTuition.coerceIn(0L, 100000000L),
                    "nextInstallmentDate" to nextInstallmentDate.trim().take(60),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteStudent(studentId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("students").document(studentId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // 4. Teachers Observation & CRUD
    fun observeTeachers(): Flow<List<TeacherRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/teachers"
        emitAll(
            db.collection("users").document(uid).collection("teachers")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TeacherRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addTeacher(
        fullName: String,
        subject: String,
        assignedClasses: String,
        phone: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "tc_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("teachers").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().take(120),
            "subject" to subject.trim().take(80),
            "assignedClasses" to assignedClasses.trim().take(150),
            "phone" to phone.trim().ifBlank { "07512362567" }.take(30),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTeacher(teacherId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("teachers").document(teacherId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // 5. Lessons & Daily Preparation Observation & CRUD
    fun observeLessons(): Flow<List<LessonPrep>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/lessons"
        emitAll(
            db.collection("users").document(uid).collection("lessons")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LessonPrep::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.periodNumber }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addLessonPrep(
        dayName: String,
        periodNumber: Long,
        gradeClass: String,
        section: String,
        subject: String,
        teacherName: String,
        preparationTopic: String,
        homeworkText: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "ls_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("lessons").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "dayName" to dayName.trim().take(30),
            "periodNumber" to periodNumber.coerceIn(1L, 8L),
            "gradeClass" to gradeClass.trim().take(60),
            "section" to section.trim().ifBlank { "أ" }.take(20),
            "subject" to subject.trim().take(80),
            "teacherName" to teacherName.trim().take(120),
            "preparationTopic" to preparationTopic.trim().take(300),
            "homeworkText" to homeworkText.trim().take(300),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteLessonPrep(lessonId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("lessons").document(lessonId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // 6. Grades Observation & CRUD
    fun observeGrades(): Flow<List<GradeEntry>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/grades"
        emitAll(
            db.collection("users").document(uid).collection("grades")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GradeEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addGradeEntry(
        studentId: String,
        studentName: String,
        gradeClass: String,
        subject: String,
        examDate: String,
        dailyScore: Long,
        monthlyScore: Long,
        midyearScore: Long,
        annualEffortScore: Long
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "gr_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("grades").document(docId)
        val safeDaily = dailyScore.coerceIn(0L, 100L)
        val safeMonthly = monthlyScore.coerceIn(0L, 100L)
        val safeMidyear = midyearScore.coerceIn(0L, 100L)
        val safeAnnual = annualEffortScore.coerceIn(0L, 100L)
        val finalAvg = ((safeDaily + safeMonthly + safeMidyear + safeAnnual) / 4L).coerceIn(0L, 100L)
        val evaluation = GradeEntry.computeEvaluation(finalAvg)

        val payload = mapOf(
            "userId" to uid,
            "studentId" to studentId.trim().take(128),
            "studentName" to studentName.trim().take(120),
            "gradeClass" to gradeClass.trim().take(60),
            "subject" to subject.trim().take(80),
            "examDate" to examDate.trim().ifBlank { "2026/11/15" }.take(60),
            "dailyScore" to safeDaily,
            "monthlyScore" to safeMonthly,
            "midyearScore" to safeMidyear,
            "annualEffortScore" to safeAnnual,
            "finalScore" to finalAvg,
            "evaluationText" to evaluation,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // 7. Notices Observation & CRUD
    fun observeNotices(): Flow<List<SchoolNotice>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notices"
        emitAll(
            db.collection("users").document(uid).collection("notices")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SchoolNotice::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun sendNotice(
        studentId: String,
        studentName: String,
        parentPhone: String,
        noticeType: NoticeType,
        title: String,
        message: String,
        senderRole: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "nt_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("notices").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "studentId" to studentId.trim().take(128),
            "studentName" to studentName.trim().take(120),
            "parentPhone" to parentPhone.trim().ifBlank { "07512362567" }.take(30),
            "noticeType" to noticeType.code,
            "title" to title.trim().take(150),
            "message" to message.trim().take(600),
            "senderRole" to senderRole.trim().take(60),
            "parentReply" to "",
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun replyToNotice(noticeId: String, replyText: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notices").document(noticeId)
        try {
            docRef.update(
                mapOf(
                    "parentReply" to replyText.trim().take(500),
                    "isRead" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // 8. School Gallery & Events Observation & CRUD
    fun observeEvents(): Flow<List<SchoolEvent>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/events"
        emitAll(
            db.collection("users").document(uid).collection("events")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SchoolEvent::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addSchoolEvent(
        title: String,
        description: String,
        eventDate: String,
        category: String,
        visualTheme: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "ev_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("users").document(uid).collection("events").document(docId)
        val safeTheme = if (visualTheme in listOf("SCIENCE", "HONOR", "SPORTS", "CULTURE")) visualTheme else "SCIENCE"
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(150),
            "description" to description.trim().take(600),
            "eventDate" to eventDate.trim().ifBlank { "2026/10/28" }.take(60),
            "category" to category.trim().take(60),
            "visualTheme" to safeTheme,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // 9. Seed Initial Model School Data (when user initializes a new school)
    suspend fun initializeSchoolWithSampleData(
        name: String,
        address: String,
        schoolType: SchoolType,
        emblemTitle: String,
        includeSampleRecords: Boolean
    ): Result<Unit> = runCatching {
        saveSchoolConfig(
            name = name,
            address = address,
            schoolType = schoolType,
            emblemTitle = emblemTitle,
            academicYear = "2026 - 2027"
        ).getOrThrow()

        if (!includeSampleRecords) return@runCatching

        // Add sample teachers
        addTeacher("أ. خالد الجبوري", "الرياضيات", "الأول المتوسط (أ، ب) - الثاني المتوسط (أ)", "07512362567")
        addTeacher("أ. سارة الموصلي", "اللغة العربية", "الأول المتوسط (أ) - الثالث المتوسط (أ)", "07512362567")
        addTeacher("أ. عمر الدليمي", "الفيزياء والعلوم", "الثاني المتوسط (أ) - الثالث المتوسط (أ)", "07512362567")

        val isPrivate = schoolType == SchoolType.PRIVATE
        // Add sample students
        val st1Id = addStudent(
            fullName = "يوسف رائد الحمداني",
            gradeClass = "الصف الأول المتوسط",
            section = "أ",
            parentName = "رائد الحمداني",
            parentPhone = "07512362567",
            isHonorRoll = true,
            honorReason = "المركز الأول في الرياضيات والعلوم بمعدل 98%",
            totalTuition = if (isPrivate) 1500000L else 0L,
            paidTuition = if (isPrivate) 1000000L else 0L,
            nextInstallmentDate = if (isPrivate) "2026/12/01" else "غير مطلوب"
        ).getOrThrow()

        val st2Id = addStudent(
            fullName = "مريم سعد الطائي",
            gradeClass = "الصف الأول المتوسط",
            section = "أ",
            parentName = "سعد الطائي",
            parentPhone = "07512362567",
            isHonorRoll = true,
            honorReason = "تفوق متميز في اللغة العربية والالتزام المثالي",
            totalTuition = if (isPrivate) 1500000L else 0L,
            paidTuition = if (isPrivate) 1500000L else 0L,
            nextInstallmentDate = if (isPrivate) "مسدد بالكامل" else "غير مطلوب"
        ).getOrThrow()

        val st3Id = addStudent(
            fullName = "عبدالله مازن السبعاوي",
            gradeClass = "الصف الثاني المتوسط",
            section = "ب",
            parentName = "مازن السبعاوي",
            parentPhone = "07512362567",
            isHonorRoll = false,
            honorReason = "",
            totalTuition = if (isPrivate) 1750000L else 0L,
            paidTuition = if (isPrivate) 750000L else 0L,
            nextInstallmentDate = if (isPrivate) "2026/11/15" else "غير مطلوب"
        ).getOrThrow()

        // Add sample daily lessons & homework
        addLessonPrep(
            dayName = "الأحد",
            periodNumber = 1L,
            gradeClass = "الصف الأول المتوسط",
            section = "أ",
            subject = "الرياضيات",
            teacherName = "أ. خالد الجبوري",
            preparationTopic = "الفصل الثاني: الأعداد النسبية والعمليات عليها (ص 34 - 38)",
            homeworkText = "حل تمارين تأكد من فهمك صفحة 39 في الدفتر البيتي"
        )
        addLessonPrep(
            dayName = "الأحد",
            periodNumber = 2L,
            gradeClass = "الصف الأول المتوسط",
            section = "أ",
            subject = "اللغة العربية",
            teacherName = "أ. سارة الموصلي",
            preparationTopic = "قواعد اللغة العربية: أقسام الكلام وعلامات الإعراب",
            homeworkText = "كتابة خمس جمل مفيدة مع إعراب الكلمات المحددة"
        )
        addLessonPrep(
            dayName = "الإثنين",
            periodNumber = 1L,
            gradeClass = "الصف الثاني المتوسط",
            section = "ب",
            subject = "الفيزياء والعلوم",
            teacherName = "أ. عمر الدليمي",
            preparationTopic = "الحركة الخطية وقوانين نيوتن في الحركة",
            homeworkText = "مراجعة مسائل السرعة والتعجيل مع رسم المخطط البياني"
        )

        // Add sample grades
        addGradeEntry(
            studentId = st1Id,
            studentName = "يوسف رائد الحمداني",
            gradeClass = "الصف الأول المتوسط",
            subject = "الرياضيات",
            examDate = "2026/11/10",
            dailyScore = 98L,
            monthlyScore = 97L,
            midyearScore = 99L,
            annualEffortScore = 98L
        )
        addGradeEntry(
            studentId = st1Id,
            studentName = "يوسف رائد الحمداني",
            gradeClass = "الصف الأول المتوسط",
            subject = "اللغة العربية",
            examDate = "2026/11/12",
            dailyScore = 95L,
            monthlyScore = 94L,
            midyearScore = 96L,
            annualEffortScore = 95L
        )
        addGradeEntry(
            studentId = st2Id,
            studentName = "مريم سعد الطائي",
            gradeClass = "الصف الأول المتوسط",
            subject = "اللغة العربية",
            examDate = "2026/11/12",
            dailyScore = 100L,
            monthlyScore = 98L,
            midyearScore = 99L,
            annualEffortScore = 99L
        )

        // Add sample notices
        sendNotice(
            studentId = st1Id,
            studentName = "يوسف رائد الحمداني",
            parentPhone = "07512362567",
            noticeType = NoticeType.APPRECIATION,
            title = "كتاب شكر وتقدير لتفوق الطالب",
            message = "تتقدم إدارة المدرسة بجزيل الشكر والتقدير لولي أمر الطالب يوسف رائد الحمداني لحصوله على المرتبة الأولى في الاختبار الشهري لمادة الرياضيات.",
            senderRole = "إدارة المدرسة"
        )
        sendNotice(
            studentId = st3Id,
            studentName = "عبدالله مازن السبعاوي",
            parentPhone = "07512362567",
            noticeType = NoticeType.ACADEMIC,
            title = "متابعة الواجب البيتي لمادة العلوم",
            message = "يرجى متابعة الطالب في إكمال دفتر التجارب العلمية ومراجعة الفصل الثاني قبل الامتحان الشهري القادم.",
            senderRole = "أ. عمر الدليمي"
        )

        // Add sample events
        addSchoolEvent(
            title = "حفل تكريم الطلبة الأوائل والمتفوقين",
            description = "أقامت إدارة المدرسة احتفالية تكريمية لتوزيع الأوسمة الذهبية وشهادات التقدير على الطلبة المجتهدين بحضور الكادر التدريسي وأولياء الأمور.",
            eventDate = "2026/10/25",
            category = "تكريم المتفوقين",
            visualTheme = "HONOR"
        )
        addSchoolEvent(
            title = "المعرض العلمي السنوي للابتكارات الطلابية",
            description = "مشاركة متميزة لطلبة الصفوف المتوسطة في عرض التجارب الفيزيائية والكيميائية والنماذج الهندسية المبتكرة داخل مختبرات المدرسة.",
            eventDate = "2026/10/18",
            category = "مهرجان علمي",
            visualTheme = "SCIENCE"
        )
    }
}
