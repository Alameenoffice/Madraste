package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class SchoolType(val code: String, val labelAr: String, val badgeAr: String) {
    GOVERNMENT("GOVERNMENT", "مدرسة حكومية", "حكومية"),
    PRIVATE("PRIVATE", "مدرسة أهلية", "أهلية");

    companion object {
        fun fromCode(code: String): SchoolType =
            entries.find { it.code == code } ?: GOVERNMENT
    }
}

enum class UserRole(val code: String, val titleAr: String, val subtitleAr: String) {
    ADMIN("ADMIN", "إدارة المدرسة", "المدير والتحكم الشامل"),
    TEACHER("TEACHER", "المعلم", "الحضور والتحضير والدرجات"),
    PARENT("PARENT", "ولي الأمر", "متابعة الأبناء والتبليغات"),
    STUDENT("STUDENT", "الطالب", "الجدول والواجبات ولوحة الشرف")
}

enum class AttendanceStatus(val code: String, val labelAr: String) {
    PRESENT("PRESENT", "حاضر"),
    ABSENT("ABSENT", "غائب"),
    LATE("LATE", "متأخر"),
    EXCUSED("EXCUSED", "مجاز");

    companion object {
        fun fromCode(code: String): AttendanceStatus =
            entries.find { it.code == code } ?: PRESENT
    }
}

enum class NoticeType(val code: String, val labelAr: String) {
    APPRECIATION("APPRECIATION", "شكر وتقدير"),
    SUMMONS("SUMMONS", "استدعاء ولي أمر"),
    BEHAVIORAL("BEHAVIORAL", "ملاحظة سلوكية"),
    ACADEMIC("ACADEMIC", "ملاحظة علمية"),
    ABSENCE_ALERT("ABSENCE_ALERT", "تنبيه غياب أو تأخر");

    companion object {
        fun fromCode(code: String): NoticeType =
            entries.find { it.code == code } ?: ACADEMIC
    }
}

@IgnoreExtraProperties
data class SchoolConfig(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val address: String = "الموصل - زمار",
    val schoolType: String = "GOVERNMENT",
    val emblemTitle: String = "العلم نور والاجتهاد رفعة",
    val academicYear: String = "2026 - 2027",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isPrivateSchool: Boolean
        get() = schoolType == SchoolType.PRIVATE.code
}

@IgnoreExtraProperties
data class StudentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val gradeClass: String = "",
    val section: String = "أ",
    val parentName: String = "",
    val parentPhone: String = "",
    val accessCode: String = "",
    val attendanceStatus: String = "PRESENT",
    val absenceDaysMonth: Long = 0L,
    val isHonorRoll: Boolean = false,
    val honorReason: String = "",
    val totalTuition: Long = 0L,
    val paidTuition: Long = 0L,
    val nextInstallmentDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val remainingTuition: Long
        get() = (totalTuition - paidTuition).coerceAtLeast(0L)
}

@IgnoreExtraProperties
data class TeacherRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val subject: String = "",
    val assignedClasses: String = "",
    val phone: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LessonPrep(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dayName: String = "الأحد",
    val periodNumber: Long = 1L,
    val gradeClass: String = "",
    val section: String = "أ",
    val subject: String = "",
    val teacherName: String = "",
    val preparationTopic: String = "",
    val homeworkText: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GradeEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val gradeClass: String = "",
    val subject: String = "",
    val examDate: String = "",
    val dailyScore: Long = 0L,
    val monthlyScore: Long = 0L,
    val midyearScore: Long = 0L,
    val annualEffortScore: Long = 0L,
    val finalScore: Long = 0L,
    val evaluationText: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        fun computeEvaluation(score: Long): String = when {
            score >= 90L -> "ممتاز متفوق"
            score >= 80L -> "جيد جداً"
            score >= 70L -> "جيد"
            score >= 50L -> "متوسط"
            else -> "يحتاج متابعة"
        }
    }
}

@IgnoreExtraProperties
data class SchoolNotice(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val parentPhone: String = "",
    val noticeType: String = "ACADEMIC",
    val title: String = "",
    val message: String = "",
    val senderRole: String = "إدارة المدرسة",
    val parentReply: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SchoolEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val eventDate: String = "",
    val category: String = "",
    val visualTheme: String = "SCIENCE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
