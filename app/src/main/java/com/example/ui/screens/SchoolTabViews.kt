package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceStatus
import com.example.data.model.GradeEntry
import com.example.data.model.LessonPrep
import com.example.data.model.NoticeType
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolEvent
import com.example.data.model.SchoolNotice
import com.example.data.model.StudentRecord
import com.example.data.model.TeacherRecord
import com.example.data.model.UserRole
import com.example.ui.components.openWhatsAppWithMessage
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.ExcusedBlue
import com.example.ui.theme.ExcusedBlueBg
import com.example.ui.theme.HonorGold
import com.example.ui.theme.HonorGoldBright
import com.example.ui.theme.HonorGoldContainer
import com.example.ui.theme.LateAmber
import com.example.ui.theme.LateAmberBg
import com.example.ui.theme.OnHonorGoldContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg

/**
 * Tab 1: Smart Daily Attendance Tracker + Role Overview & Staff/Student Management
 */
@Composable
fun AttendanceAndManagementTab(
    schoolConfig: SchoolConfig,
    activeRole: UserRole,
    students: List<StudentRecord>,
    teachers: List<TeacherRecord>,
    selectedClassFilter: String,
    onSelectClassFilter: (String) -> Unit,
    onMarkAttendance: (StudentRecord, AttendanceStatus) -> Unit,
    onToggleHonorRoll: (StudentRecord, Boolean, String) -> Unit,
    onOpenAddStudent: () -> Unit,
    onOpenAddTeacher: () -> Unit
) {
    val context = LocalContext.current
    val availableClasses = remember(students) {
        listOf("الكل") + students.map { it.gradeClass }.distinct()
    }
    val filteredStudents = remember(students, selectedClassFilter) {
        if (selectedClassFilter == "الكل") students
        else students.filter { it.gradeClass == selectedClassFilter }
    }

    val presentCount = students.count { it.attendanceStatus == AttendanceStatus.PRESENT.code }
    val absentCount = students.count { it.attendanceStatus == AttendanceStatus.ABSENT.code }
    val lateCount = students.count { it.attendanceStatus == AttendanceStatus.LATE.code }
    val excusedCount = students.count { it.attendanceStatus == AttendanceStatus.EXCUSED.code }

    var studentToHonor by remember { mutableStateOf<StudentRecord?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Instant Alert Banner for Parents when any viewed student is Absent or Late
        if (activeRole == UserRole.PARENT) {
            val absentOrLate = filteredStudents.filter {
                it.attendanceStatus == AttendanceStatus.ABSENT.code ||
                    it.attendanceStatus == AttendanceStatus.LATE.code
            }
            if (absentOrLate.isNotEmpty()) {
                item {
                    absentOrLate.forEach { st ->
                        val statusAr = AttendanceStatus.fromCode(st.attendanceStatus).labelAr
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AbsentRedBg),
                            border = BorderStroke(1.5.dp, AbsentRed)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = AbsentRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تنبيه فوري لولي الأمر: الطالب ($statusAr)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AbsentRed
                                    )
                                    Text(
                                        text = "تم تسجيل ${st.fullName} ($statusAr) اليوم. إجمالي الغياب الشهري: ${st.absenceDaysMonth} أيام.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // Daily Attendance Summary Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الحضور والغياب اليومي الذكي",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "العدد الكلي: ${students.size}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttendanceMetricPill("حاضر", presentCount, PresentGreen, PresentGreenBg, Modifier.weight(1f))
                        AttendanceMetricPill("غائب", absentCount, AbsentRed, AbsentRedBg, Modifier.weight(1f))
                        AttendanceMetricPill("متأخر", lateCount, LateAmber, LateAmberBg, Modifier.weight(1f))
                        AttendanceMetricPill("مجاز", excusedCount, ExcusedBlue, ExcusedBlueBg, Modifier.weight(1f))
                    }
                }
            }
        }

        // Admin Quick Actions Row (Add Student / Add Teacher)
        if (activeRole == UserRole.ADMIN) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenAddStudent,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_student_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة طالب وكود", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = onOpenAddTeacher,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_teacher_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة معلم (${teachers.size})", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Class Filter Chips
        if (availableClasses.size > 2) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableClasses.forEach { cls ->
                        FilterChip(
                            selected = selectedClassFilter == cls,
                            onClick = { onSelectClassFilter(cls) },
                            label = { Text(cls, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }
        }

        // Student Attendance Cards
        items(filteredStudents, key = { it.id }) { student ->
            val currentStatus = AttendanceStatus.fromCode(student.attendanceStatus)
            val canEditAttendance = activeRole == UserRole.ADMIN || activeRole == UserRole.TEACHER

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (student.isHonorRoll) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = HonorGoldContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = HonorGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "متفوق",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = OnHonorGoldContainer,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "${student.gradeClass} - شعبة (${student.section}) | ولي الأمر: ${student.parentName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Access Code Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = student.accessCode,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Monthly Absence Stat + Honor Roll Quick Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إحصائية الغياب الشهري: ${student.absenceDaysMonth} أيام",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (student.absenceDaysMonth > 2) AbsentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )

                        if (canEditAttendance) {
                            TextButton(
                                onClick = { studentToHonor = student },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = HonorGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (student.isHonorRoll) "تعديل وسام الشرف" else "ترشيح للوحة الشرف",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HonorGold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // 4 Attendance Status Buttons (حاضر، غائب، متأخر، مجاز)
                    if (canEditAttendance) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AttendanceActionChip(
                                label = "حاضر ✅",
                                isSelected = currentStatus == AttendanceStatus.PRESENT,
                                activeColor = PresentGreen,
                                activeBg = PresentGreenBg,
                                onClick = { onMarkAttendance(student, AttendanceStatus.PRESENT) },
                                modifier = Modifier.weight(1f)
                            )
                            AttendanceActionChip(
                                label = "غائب ❌",
                                isSelected = currentStatus == AttendanceStatus.ABSENT,
                                activeColor = AbsentRed,
                                activeBg = AbsentRedBg,
                                onClick = { onMarkAttendance(student, AttendanceStatus.ABSENT) },
                                modifier = Modifier.weight(1f)
                            )
                            AttendanceActionChip(
                                label = "متأخر ⏰",
                                isSelected = currentStatus == AttendanceStatus.LATE,
                                activeColor = LateAmber,
                                activeBg = LateAmberBg,
                                onClick = { onMarkAttendance(student, AttendanceStatus.LATE) },
                                modifier = Modifier.weight(1f)
                            )
                            AttendanceActionChip(
                                label = "مجاز 📄",
                                isSelected = currentStatus == AttendanceStatus.EXCUSED,
                                activeColor = ExcusedBlue,
                                activeBg = ExcusedBlueBg,
                                onClick = { onMarkAttendance(student, AttendanceStatus.EXCUSED) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Instant WhatsApp Notification Button when Student is Absent or Late
                    if (currentStatus == AttendanceStatus.ABSENT || currentStatus == AttendanceStatus.LATE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val statusWord = if (currentStatus == AttendanceStatus.ABSENT) "غائب" else "متأخر عن الدوام"
                                val whatsappMsg = "تحية طيبة من ${schoolConfig.name} (${schoolConfig.address})\n" +
                                    "إلى ولي أمر الطالب: ${student.fullName} (${student.gradeClass} - شعبة ${student.section})\n" +
                                    "نود إعلامكم بتسجيل الطالب اليوم كـ ($statusWord).\n" +
                                    "إجمالي أيام الغياب الشهري: ${student.absenceDaysMonth} يوم."
                                openWhatsAppWithMessage(context, student.parentPhone, whatsappMsg)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرسال تبليغ الغياب عبر واتساب لولي الأمر فوراً",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Teachers Roster Section for Admin
        if (activeRole == UserRole.ADMIN && teachers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الكادر التدريسي في المدرسة (${teachers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(teachers, key = { it.id }) { teacher ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = teacher.fullName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "الاختصاص: ${teacher.subject} | الصفوف: ${teacher.assignedClasses}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (studentToHonor != null) {
        val st = studentToHonor!!
        var isHonor by remember(st) { mutableStateOf(st.isHonorRoll) }
        var reason by remember(st) {
            mutableStateOf(st.honorReason.ifBlank { "تفوق علمي والتزام مثالي في الصف" })
        }
        AlertDialog(
            onDismissRequest = { studentToHonor = null },
            title = { Text("تكريم الطالب في لوحة الشرف الذهبية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الطالب: ${st.fullName}")
                    FilterChip(
                        selected = isHonor,
                        onClick = { isHonor = !isHonor },
                        label = {
                            Text(
                                text = if (isHonor) "مدرج في لوحة الشرف الذهبية ⭐" else "إدراج في لوحة الشرف",
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("سبب التكريم أو الوسام") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleHonorRoll(st, isHonor, reason)
                        studentToHonor = null
                    }
                ) {
                    Text("حفظ التكريم", maxLines = 1, softWrap = false)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToHonor = null }) {
                    Text("إلغاء", maxLines = 1, softWrap = false)
                }
            }
        )
    }
}

@Composable
private fun AttendanceMetricPill(
    label: String,
    count: Int,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun AttendanceActionChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Tab 2: Daily Schedule, Preparation & Homework (جدول الدروس والتحضير اليومي والواجبات)
 */
@Composable
fun ScheduleAndPrepTab(
    activeRole: UserRole,
    lessons: List<LessonPrep>,
    selectedDayFilter: String,
    onSelectDayFilter: (String) -> Unit,
    onOpenAddLesson: () -> Unit
) {
    val schoolDays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس")
    val filteredLessons = remember(lessons, selectedDayFilter) {
        lessons.filter { it.dayName == selectedDayFilter }.sortedBy { it.periodNumber }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جدول الدروس والتحضير اليومي",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "التحضير القادم والواجبات البيتية حسب الصفوف والأيام",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (activeRole == UserRole.ADMIN || activeRole == UserRole.TEACHER) {
                    Button(
                        onClick = onOpenAddLesson,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_lesson_prep_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نشر تحضير", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        // Days Selector Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                schoolDays.forEach { day ->
                    FilterChip(
                        selected = selectedDayFilter == day,
                        onClick = { onSelectDayFilter(day) },
                        label = {
                            Text(
                                text = "يوم $day",
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }
        }

        if (filteredLessons.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا يوجد تحضير مسجل ليوم $selectedDayFilter حالياً",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "يمكن للمعلم أو الإدارة إضافة الحصص والتحضير القادم بضغطة زر",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredLessons, key = { it.id }) { lesson ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "الحصة ${lesson.periodNumber}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = lesson.subject,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${lesson.gradeClass} (${lesson.section})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "مدرس المادة: ${lesson.teacherName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📖 التحضير القادم:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = lesson.preparationTopic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✍️ الواجب البيتي:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = HonorGold
                                )
                                Text(
                                    text = lesson.homeworkText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Exams & Student Grades Sheet (الامتحانات والدرجات)
 */
@Composable
fun ExamsAndGradesTab(
    activeRole: UserRole,
    grades: List<GradeEntry>,
    onOpenAddGrade: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جدول الامتحانات وكشف الدرجات",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "الدرجات اليومية والشهرية ونصف السنة والسعي السنوي",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (activeRole == UserRole.ADMIN || activeRole == UserRole.TEACHER) {
                    Button(
                        onClick = onOpenAddGrade,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_grade_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("رصد درجة", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        items(grades, key = { it.id }) { grade ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${grade.subject} - ${grade.studentName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${grade.gradeClass} | موعد الامتحان: ${grade.examDate}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (grade.finalScore >= 90) HonorGoldContainer else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${grade.evaluationText} (${grade.finalScore}%)",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (grade.finalScore >= 90) OnHonorGoldContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreCell("يومي", grade.dailyScore, Modifier.weight(1f))
                        ScoreCell("شهري", grade.monthlyScore, Modifier.weight(1f))
                        ScoreCell("نصف السنة", grade.midyearScore, Modifier.weight(1f))
                        ScoreCell("سعي سنوي", grade.annualEffortScore, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCell(
    label: String,
    score: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Tab 4: Honor Roll (الطلبة المجتهدون ولوحة الشرف) & School Gallery (فعاليات وصور المدرسة)
 */
@Composable
fun HonorRollAndGalleryTab(
    activeRole: UserRole,
    students: List<StudentRecord>,
    events: List<SchoolEvent>,
    onOpenAddEvent: () -> Unit
) {
    val honorStudents = remember(students) { students.filter { it.isHonorRoll } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Golden Honor Roll Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF92400E), Color(0xFFD97706), Color(0xFFF59E0B))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "وسام لوحة الشرف",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "لوحة الشرف والطلبة المجتهدون",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "تكريم أوائل الطلبة والمتميزين علمياً وأخلاقياً بالأوسمة الذهبية",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            }
        }

        // Honor Roll Student Cards
        items(honorStudents, key = { "honor_${it.id}" }) { student ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, HonorGoldBright)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(HonorGoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = HonorGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "${student.gradeClass} - شعبة (${student.section})",
                            style = MaterialTheme.typography.labelMedium,
                            color = HonorGold,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (student.honorReason.isNotBlank()) {
                            Text(
                                text = student.honorReason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // School Gallery & News Section Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "فعاليات وصور المدرسة",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "أخبار المهرجانات العلمية والأنشطة الطلابية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (activeRole == UserRole.ADMIN || activeRole == UserRole.TEACHER) {
                    Button(
                        onClick = onOpenAddEvent,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نشر فعالية", maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        items(events, key = { it.id }) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Decorative Illustrated Event Banner
                    EventVisualBanner(visualTheme = event.visualTheme, category = event.category)

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = event.eventDate,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventVisualBanner(
    visualTheme: String,
    category: String
) {
    val gradientColors = when (visualTheme) {
        "HONOR" -> listOf(Color(0xFF78350F), Color(0xFFD97706))
        "SPORTS" -> listOf(Color(0xFF065F46), Color(0xFF10B981))
        else -> listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Brush.linearGradient(colors = gradientColors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = size.height * 0.65f,
                center = Offset(size.width * 0.2f, size.height * 0.3f)
            )
            drawCircle(
                color = Color(0xFFF59E0B).copy(alpha = 0.22f),
                radius = size.height * 0.45f,
                center = Offset(size.width * 0.85f, size.height * 0.75f)
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Tab 5: Parent Notices & Direct Communication + Tuition & Installments (when Private School)
 */
@Composable
fun NoticesAndTuitionTab(
    schoolConfig: SchoolConfig,
    activeRole: UserRole,
    students: List<StudentRecord>,
    notices: List<SchoolNotice>,
    onOpenSendNotice: () -> Unit,
    onReplyToNotice: (String, String) -> Unit,
    onUpdateTuition: (String, Long, Long, String) -> Unit
) {
    val context = LocalContext.current
    var showTuitionSubTab by remember { mutableStateOf(false) }
    var editingTuitionStudent by remember { mutableStateOf<StudentRecord?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-tab selector if school is PRIVATE (مدرسة أهلية) and user is Admin or Parent
        if (schoolConfig.isPrivateSchool && (activeRole == UserRole.ADMIN || activeRole == UserRole.PARENT)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showTuitionSubTab,
                        onClick = { showTuitionSubTab = false },
                        label = {
                            Text("البلاغات والرسائل (${notices.size})", maxLines = 1, softWrap = false)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = showTuitionSubTab,
                        onClick = { showTuitionSubTab = true },
                        label = {
                            Text("الأقساط الدراسية والدفعات 🏫", maxLines = 1, softWrap = false)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("private_school_tuition_tab")
                    )
                }
            }
        }

        if (showTuitionSubTab && schoolConfig.isPrivateSchool) {
            // Private School Tuition & Installments Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "قسم متابعة الأقساط الدراسية والدفعات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "مفعل تلقائياً لأن المدرسة مسجلة كـ (مدرسة أهلية)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            items(students, key = { "tuition_${it.id}" }) { student ->
                val progress = if (student.totalTuition > 0L) {
                    (student.paidTuition.toFloat() / student.totalTuition.toFloat()).coerceIn(0f, 1f)
                } else 1f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "${student.gradeClass} | ولي الأمر: ${student.parentName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            if (activeRole == UserRole.ADMIN) {
                                OutlinedButton(
                                    onClick = { editingTuitionStudent = student },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تسجيل دفعة", maxLines = 1, softWrap = false)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = PresentGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الكلي: ${student.totalTuition} د.ع",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "المدفوع: ${student.paidTuition} د.ع",
                                style = MaterialTheme.typography.labelLarge,
                                color = PresentGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "المتبقي: ${student.remainingTuition} د.ع",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (student.remainingTuition > 0) AbsentRed else PresentGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "موعد القسط القادم: ${student.nextInstallmentDate.ifBlank { "غير محدد" }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Parent Notices & Direct Communication Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "نظام البلاغات والتواصل مع ولي الأمر",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "شكر وتقدير، استدعاء، وملاحظات علمية وسلوكية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (activeRole == UserRole.ADMIN || activeRole == UserRole.TEACHER) {
                        Button(
                            onClick = onOpenSendNotice,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("send_notice_button")
                        ) {
                            Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إرسال تبليغ", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            items(notices, key = { it.id }) { notice ->
                val typeEnum = NoticeType.fromCode(notice.noticeType)
                var replyDraft by remember(notice.id) { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when (typeEnum) {
                                    NoticeType.APPRECIATION -> HonorGoldContainer
                                    NoticeType.ABSENCE_ALERT, NoticeType.SUMMONS -> AbsentRedBg
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                Text(
                                    text = typeEnum.labelAr,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when (typeEnum) {
                                        NoticeType.APPRECIATION -> OnHonorGoldContainer
                                        NoticeType.ABSENCE_ALERT, NoticeType.SUMMONS -> AbsentRed
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "الطالب: ${notice.studentName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (notice.parentReply.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = PresentGreenBg
                            ) {
                                Text(
                                    text = "رد ولي الأمر: ${notice.parentReply}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PresentGreen,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Parent Direct Reply Input Box
                        if (activeRole == UserRole.PARENT && notice.parentReply.isBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = replyDraft,
                                    onValueChange = { replyDraft = it },
                                    placeholder = { Text("اكتب ردك المباشر لإدارة المدرسة...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        onReplyToNotice(notice.id, replyDraft)
                                        replyDraft = ""
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال الرد")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Direct WhatsApp Button
                        OutlinedButton(
                            onClick = {
                                val text = "*${schoolConfig.name}*\n" +
                                    "تبليغ (${typeEnum.labelAr}) بخصوص الطالب: ${notice.studentName}\n" +
                                    "${notice.title}\n${notice.message}"
                                openWhatsAppWithMessage(context, notice.parentPhone, text)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مشاركة أو متابعة التبليغ عبر واتساب",
                                color = Color(0xFF059669),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingTuitionStudent != null) {
        val st = editingTuitionStudent!!
        var totalText by remember(st) { mutableStateOf(st.totalTuition.toString()) }
        var paidText by remember(st) { mutableStateOf(st.paidTuition.toString()) }
        var nextDate by remember(st) { mutableStateOf(st.nextInstallmentDate) }

        AlertDialog(
            onDismissRequest = { editingTuitionStudent = null },
            title = { Text("تحديث الأقساط والدفعات الدراسية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الطالب: ${st.fullName}")
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("القسط الكلي (د.ع)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("المبلغ المدفوع (د.ع)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nextDate,
                        onValueChange = { nextDate = it },
                        label = { Text("موعد القسط القادم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTuition(
                            st.id,
                            totalText.toLongOrNull() ?: st.totalTuition,
                            paidText.toLongOrNull() ?: st.paidTuition,
                            nextDate
                        )
                        editingTuitionStudent = null
                    }
                ) {
                    Text("حفظ الدفعة", maxLines = 1, softWrap = false)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTuitionStudent = null }) {
                    Text("إلغاء", maxLines = 1, softWrap = false)
                }
            }
        )
    }
}
