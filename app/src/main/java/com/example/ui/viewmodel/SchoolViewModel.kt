package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.model.UserRole
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class SchoolViewModel(
    private val repository: SchoolRepository
) : ViewModel() {

    private val _activeRole = MutableStateFlow(UserRole.ADMIN)
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<String?>(null)
    val selectedStudentId: StateFlow<String?> = _selectedStudentId.asStateFlow()

    private val _selectedClassFilter = MutableStateFlow("الكل")
    val selectedClassFilter: StateFlow<String> = _selectedClassFilter.asStateFlow()

    private val _selectedDayFilter = MutableStateFlow("الأحد")
    val selectedDayFilter: StateFlow<String> = _selectedDayFilter.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val schoolConfigState: StateFlow<UiState<SchoolConfig?>> = repository.observeSchoolConfig()
        .map<SchoolConfig?, UiState<SchoolConfig?>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing school config", e)
            emit(UiState.Error("تعذر تحميل إعدادات المدرسة"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val studentsState: StateFlow<UiState<List<StudentRecord>>> = repository.observeStudents()
        .map<List<StudentRecord>, UiState<List<StudentRecord>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing students", e)
            emit(UiState.Error("تعذر تحميل قائمة الطلاب"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val teachersState: StateFlow<UiState<List<TeacherRecord>>> = repository.observeTeachers()
        .map<List<TeacherRecord>, UiState<List<TeacherRecord>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing teachers", e)
            emit(UiState.Error("تعذر تحميل قائمة المعلمين"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val lessonsState: StateFlow<UiState<List<LessonPrep>>> = repository.observeLessons()
        .map<List<LessonPrep>, UiState<List<LessonPrep>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing lessons", e)
            emit(UiState.Error("تعذر تحميل جدول الدروس والتحضير"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val gradesState: StateFlow<UiState<List<GradeEntry>>> = repository.observeGrades()
        .map<List<GradeEntry>, UiState<List<GradeEntry>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing grades", e)
            emit(UiState.Error("تعذر تحميل سجل الدرجات"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val noticesState: StateFlow<UiState<List<SchoolNotice>>> = repository.observeNotices()
        .map<List<SchoolNotice>, UiState<List<SchoolNotice>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing notices", e)
            emit(UiState.Error("تعذر تحميل البلاغات والرسائل"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val eventsState: StateFlow<UiState<List<SchoolEvent>>> = repository.observeEvents()
        .map<List<SchoolEvent>, UiState<List<SchoolEvent>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing events", e)
            emit(UiState.Error("تعذر تحميل فعاليات المدرسة"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    fun setRole(role: UserRole) {
        _activeRole.value = role
    }

    fun selectStudent(studentId: String?) {
        _selectedStudentId.value = studentId
    }

    fun applyAccessCode(accessCode: String, students: List<StudentRecord>, asParent: Boolean): Boolean {
        val matched = students.firstOrNull {
            it.accessCode.equals(accessCode.trim(), ignoreCase = true)
        }
        return if (matched != null) {
            _selectedStudentId.value = matched.id
            _activeRole.value = if (asParent) UserRole.PARENT else UserRole.STUDENT
            showMessage("تم الدخول بنجاح لحساب الطالب: ${matched.fullName}")
            true
        } else {
            showMessage("كود الدخول غير مطابق لأي طالب مسجل")
            false
        }
    }

    fun setClassFilter(gradeClass: String) {
        _selectedClassFilter.value = gradeClass
    }

    fun setDayFilter(dayName: String) {
        _selectedDayFilter.value = dayName
    }

    fun clearBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun showMessage(msg: String) {
        _statusBannerMessage.value = msg
    }

    fun initializeNewSchool(
        name: String,
        address: String,
        schoolType: SchoolType,
        emblemTitle: String,
        includeSampleRecords: Boolean
    ) {
        if (name.isBlank()) {
            showMessage("يرجى إدخال اسم المدرسة")
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.initializeSchoolWithSampleData(
                name = name,
                address = address,
                schoolType = schoolType,
                emblemTitle = emblemTitle,
                includeSampleRecords = includeSampleRecords
            )
            _isBusy.value = false
            result.onSuccess {
                showMessage("تم تأسيس منصة (${name.trim()}) بنجاح (${schoolType.labelAr})")
            }.onFailure {
                showMessage("حدث خطأ أثناء حفظ إعدادات المدرسة")
            }
        }
    }

    fun updateSchoolConfig(
        name: String,
        address: String,
        schoolType: SchoolType,
        emblemTitle: String,
        academicYear: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val existingId = (schoolConfigState.value as? UiState.Success)?.data?.id
            val result = repository.saveSchoolConfig(
                name = name,
                address = address,
                schoolType = schoolType,
                emblemTitle = emblemTitle,
                academicYear = academicYear,
                existingSchoolId = existingId
            )
            _isBusy.value = false
            result.onSuccess {
                showMessage("تم تحديث إعدادات ونوع المدرسة إلى (${schoolType.labelAr})")
            }.onFailure {
                showMessage("تعذر تحديث إعدادات المدرسة")
            }
        }
    }

    fun addStudent(
        fullName: String,
        gradeClass: String,
        section: String,
        parentName: String,
        parentPhone: String,
        isHonorRoll: Boolean,
        honorReason: String,
        totalTuition: Long,
        paidTuition: Long,
        nextInstallmentDate: String
    ) {
        if (fullName.isBlank() || parentName.isBlank()) {
            showMessage("يرجى إدخال اسم الطالب واسم ولي الأمر")
            return
        }
        viewModelScope.launch {
            val result = repository.addStudent(
                fullName = fullName,
                gradeClass = gradeClass,
                section = section,
                parentName = parentName,
                parentPhone = parentPhone,
                isHonorRoll = isHonorRoll,
                honorReason = honorReason,
                totalTuition = totalTuition,
                paidTuition = paidTuition,
                nextInstallmentDate = nextInstallmentDate
            )
            result.onSuccess {
                showMessage("تمت إضافة الطالب وتوليد كود الدخول السريع بنجاح")
            }.onFailure {
                showMessage("تعذر إضافة الطالب")
            }
        }
    }

    fun markAttendance(student: StudentRecord, status: AttendanceStatus) {
        viewModelScope.launch {
            val result = repository.updateStudentAttendance(student, status, autoSendAlert = true)
            result.onSuccess {
                val extraMsg = if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.LATE) {
                    " - تم إرسال تنبيه فوري لحساب ولي الأمر"
                } else ""
                showMessage("تم تسجيل حالة الطالب (${student.fullName}): ${status.labelAr}$extraMsg")
            }.onFailure {
                showMessage("تعذر تحديث حالة الحضور")
            }
        }
    }

    fun toggleHonorRoll(student: StudentRecord, isHonor: Boolean, reason: String) {
        viewModelScope.launch {
            val result = repository.updateStudentHonorRoll(student.id, isHonor, reason)
            result.onSuccess {
                val msg = if (isHonor) "تم تكريم الطالب (${student.fullName}) في لوحة الشرف الذهبية"
                else "تم تحديث حالة لوحة الشرف للطالب"
                showMessage(msg)
            }
        }
    }

    fun updateTuitionPayment(
        studentId: String,
        totalTuition: Long,
        paidTuition: Long,
        nextInstallmentDate: String
    ) {
        viewModelScope.launch {
            val result = repository.updateStudentTuition(
                studentId = studentId,
                totalTuition = totalTuition,
                paidTuition = paidTuition,
                nextInstallmentDate = nextInstallmentDate
            )
            result.onSuccess {
                showMessage("تم تحديث سجل الأقساط والدفعات الدراسية بنجاح")
            }
        }
    }

    fun addTeacher(fullName: String, subject: String, assignedClasses: String, phone: String) {
        if (fullName.isBlank() || subject.isBlank()) {
            showMessage("يرجى إدخال اسم المعلم والاختصاص")
            return
        }
        viewModelScope.launch {
            repository.addTeacher(fullName, subject, assignedClasses, phone).onSuccess {
                showMessage("تمت إضافة المعلم ($fullName) إلى الكادر التدريسي")
            }
        }
    }

    fun addLessonPrep(
        dayName: String,
        periodNumber: Long,
        gradeClass: String,
        section: String,
        subject: String,
        teacherName: String,
        preparationTopic: String,
        homeworkText: String
    ) {
        if (subject.isBlank() || preparationTopic.isBlank()) {
            showMessage("يرجى إدخال المادة وموضوع التحضير القادم")
            return
        }
        viewModelScope.launch {
            repository.addLessonPrep(
                dayName = dayName,
                periodNumber = periodNumber,
                gradeClass = gradeClass,
                section = section,
                subject = subject,
                teacherName = teacherName,
                preparationTopic = preparationTopic,
                homeworkText = homeworkText
            ).onSuccess {
                showMessage("تم نشر التحضير اليومي والواجب البيتي للصف ($gradeClass)")
            }
        }
    }

    fun addGradeEntry(
        student: StudentRecord,
        subject: String,
        examDate: String,
        dailyScore: Long,
        monthlyScore: Long,
        midyearScore: Long,
        annualEffortScore: Long
    ) {
        if (subject.isBlank()) {
            showMessage("يرجى تحديد اسم المادة الدراسية")
            return
        }
        viewModelScope.launch {
            repository.addGradeEntry(
                studentId = student.id,
                studentName = student.fullName,
                gradeClass = student.gradeClass,
                subject = subject,
                examDate = examDate,
                dailyScore = dailyScore,
                monthlyScore = monthlyScore,
                midyearScore = midyearScore,
                annualEffortScore = annualEffortScore
            ).onSuccess {
                showMessage("تم رصد درجات مادة ($subject) للطالب (${student.fullName})")
            }
        }
    }

    fun sendParentNotice(
        student: StudentRecord,
        noticeType: NoticeType,
        title: String,
        message: String,
        senderRole: String
    ) {
        if (title.isBlank() || message.isBlank()) {
            showMessage("يرجى كتابة عنوان ونص التبليغ")
            return
        }
        viewModelScope.launch {
            repository.sendNotice(
                studentId = student.id,
                studentName = student.fullName,
                parentPhone = student.parentPhone,
                noticeType = noticeType,
                title = title,
                message = message,
                senderRole = senderRole
            ).onSuccess {
                showMessage("تم إرسال التبليغ إلى ولي أمر الطالب (${student.fullName})")
            }
        }
    }

    fun replyToNotice(noticeId: String, replyText: String) {
        if (replyText.isBlank()) return
        viewModelScope.launch {
            repository.replyToNotice(noticeId, replyText).onSuccess {
                showMessage("تم إرسال رد ولي الأمر إلى إدارة المدرسة بنجاح")
            }
        }
    }

    fun addSchoolEvent(
        title: String,
        description: String,
        eventDate: String,
        category: String,
        visualTheme: String
    ) {
        if (title.isBlank() || description.isBlank()) {
            showMessage("يرجى إدخال عنوان وتفاصيل الفعالية")
            return
        }
        viewModelScope.launch {
            repository.addSchoolEvent(
                title = title,
                description = description,
                eventDate = eventDate,
                category = category,
                visualTheme = visualTheme
            ).onSuccess {
                showMessage("تم نشر الفعالية في معرض أخبار المدرسة")
            }
        }
    }

    companion object {
        private const val TAG = "SchoolViewModel"
    }
}
