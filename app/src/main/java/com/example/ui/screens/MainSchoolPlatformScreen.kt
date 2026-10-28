package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolType
import com.example.data.model.StudentRecord
import com.example.data.model.UserRole
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.UiState

private data class PlatformTabItem(
    val index: Int,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainSchoolPlatformScreen(
    viewModel: SchoolViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onSignOut: () -> Unit
) {
    val schoolConfigState by viewModel.schoolConfigState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val teachersState by viewModel.teachersState.collectAsStateWithLifecycle()
    val lessonsState by viewModel.lessonsState.collectAsStateWithLifecycle()
    val gradesState by viewModel.gradesState.collectAsStateWithLifecycle()
    val noticesState by viewModel.noticesState.collectAsStateWithLifecycle()
    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()

    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val selectedStudentId by viewModel.selectedStudentId.collectAsStateWithLifecycle()
    val selectedClassFilter by viewModel.selectedClassFilter.collectAsStateWithLifecycle()
    val selectedDayFilter by viewModel.selectedDayFilter.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    var currentTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccessCodeDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddLessonDialog by remember { mutableStateOf(false) }
    var showAddGradeDialog by remember { mutableStateOf(false) }
    var showSendNoticeDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentTabIndex != 0) {
        currentTabIndex = 0
    }

    val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
    val allTeachers = (teachersState as? UiState.Success)?.data ?: emptyList()
    val allLessons = (lessonsState as? UiState.Success)?.data ?: emptyList()
    val allGrades = (gradesState as? UiState.Success)?.data ?: emptyList()
    val allNotices = (noticesState as? UiState.Success)?.data ?: emptyList()
    val allEvents = (eventsState as? UiState.Success)?.data ?: emptyList()

    when (val configResult = schoolConfigState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            InitialSchoolSetupScreen(
                isBusy = isBusy,
                onCreateSchool = { name, address, type, emblem, sample ->
                    viewModel.initializeNewSchool(name, address, type, emblem, sample)
                },
                onSignOut = onSignOut
            )
        }

        is UiState.Success -> {
            val schoolConfig: SchoolConfig? = configResult.data
            if (schoolConfig == null) {
                InitialSchoolSetupScreen(
                    isBusy = isBusy,
                    onCreateSchool = { name, address, type, emblem, sample ->
                        viewModel.initializeNewSchool(name, address, type, emblem, sample)
                    },
                    onSignOut = onSignOut
                )
            } else {
                // Scope students/grades/notices when viewing as Parent or Student with a selected student
                val effectiveSelectedStudent: StudentRecord? = remember(allStudents, selectedStudentId) {
                    allStudents.find { it.id == selectedStudentId } ?: allStudents.firstOrNull()
                }
                val isParentOrStudent = activeRole == UserRole.PARENT || activeRole == UserRole.STUDENT
                val roleScopedStudents = remember(allStudents, isParentOrStudent, effectiveSelectedStudent) {
                    if (isParentOrStudent && effectiveSelectedStudent != null) {
                        listOf(effectiveSelectedStudent)
                    } else {
                        allStudents
                    }
                }
                val roleScopedGrades = remember(allGrades, isParentOrStudent, effectiveSelectedStudent) {
                    if (isParentOrStudent && effectiveSelectedStudent != null) {
                        allGrades.filter { it.studentId == effectiveSelectedStudent.id }
                    } else {
                        allGrades
                    }
                }
                val roleScopedNotices = remember(allNotices, isParentOrStudent, effectiveSelectedStudent) {
                    if (isParentOrStudent && effectiveSelectedStudent != null) {
                        allNotices.filter { it.studentId == effectiveSelectedStudent.id }
                    } else {
                        allNotices
                    }
                }

                val tabs = remember(schoolConfig.isPrivateSchool) {
                    listOf(
                        PlatformTabItem(0, "الحضور والإدارة", Icons.Default.CheckCircle),
                        PlatformTabItem(1, "الجدول والتحضير", Icons.Default.MenuBook),
                        PlatformTabItem(2, "الدرجات", Icons.Default.Grade),
                        PlatformTabItem(3, "لوحة الشرف", Icons.Default.EmojiEvents),
                        PlatformTabItem(
                            4,
                            if (schoolConfig.isPrivateSchool) "البلاغات والأقساط" else "البلاغات",
                            Icons.Default.Campaign
                        )
                    )
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        SchoolTopHeader(
                            schoolConfig = schoolConfig,
                            activeRole = activeRole,
                            allStudents = allStudents,
                            selectedStudent = effectiveSelectedStudent,
                            isDarkTheme = isDarkTheme,
                            onSelectRole = { viewModel.setRole(it) },
                            onSelectStudent = { viewModel.selectStudent(it.id) },
                            onOpenAccessCode = { showAccessCodeDialog = true },
                            onToggleDarkTheme = onToggleDarkTheme,
                            onOpenSettings = { showSettingsDialog = true }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTabIndex == tab.index,
                                    onClick = { currentTabIndex = tab.index },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Dismissible Status Feedback Banner
                        AnimatedVisibility(visible = statusBannerMessage != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable { viewModel.clearBannerMessage() },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = statusBannerMessage ?: "",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTabIndex) {
                                0 -> AttendanceAndManagementTab(
                                    schoolConfig = schoolConfig,
                                    activeRole = activeRole,
                                    students = roleScopedStudents,
                                    teachers = allTeachers,
                                    selectedClassFilter = selectedClassFilter,
                                    onSelectClassFilter = { viewModel.setClassFilter(it) },
                                    onMarkAttendance = { st, status -> viewModel.markAttendance(st, status) },
                                    onToggleHonorRoll = { st, isHonor, reason ->
                                        viewModel.toggleHonorRoll(st, isHonor, reason)
                                    },
                                    onOpenAddStudent = { showAddStudentDialog = true },
                                    onOpenAddTeacher = { showAddTeacherDialog = true }
                                )

                                1 -> ScheduleAndPrepTab(
                                    activeRole = activeRole,
                                    lessons = allLessons,
                                    selectedDayFilter = selectedDayFilter,
                                    onSelectDayFilter = { viewModel.setDayFilter(it) },
                                    onOpenAddLesson = { showAddLessonDialog = true }
                                )

                                2 -> ExamsAndGradesTab(
                                    activeRole = activeRole,
                                    grades = roleScopedGrades,
                                    onOpenAddGrade = { showAddGradeDialog = true }
                                )

                                3 -> HonorRollAndGalleryTab(
                                    activeRole = activeRole,
                                    students = allStudents,
                                    events = allEvents,
                                    onOpenAddEvent = { showAddEventDialog = true }
                                )

                                else -> NoticesAndTuitionTab(
                                    schoolConfig = schoolConfig,
                                    activeRole = activeRole,
                                    students = roleScopedStudents,
                                    notices = roleScopedNotices,
                                    onOpenSendNotice = { showSendNoticeDialog = true },
                                    onReplyToNotice = { id, reply -> viewModel.replyToNotice(id, reply) },
                                    onUpdateTuition = { stId, total, paid, nextDate ->
                                        viewModel.updateTuitionPayment(stId, total, paid, nextDate)
                                    }
                                )
                            }
                        }
                    }
                }

                // Dialogs
                if (showSettingsDialog) {
                    EditSchoolSettingsDialog(
                        currentConfig = schoolConfig,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onDismiss = { showSettingsDialog = false },
                        onSave = { name, address, type, emblem, year ->
                            viewModel.updateSchoolConfig(name, address, type, emblem, year)
                        },
                        onSignOut = onSignOut
                    )
                }

                if (showAccessCodeDialog) {
                    QuickAccessCodeDialog(
                        students = allStudents,
                        onDismiss = { showAccessCodeDialog = false },
                        onCodeSubmit = { code, asParent ->
                            viewModel.applyAccessCode(code, allStudents, asParent)
                        }
                    )
                }

                if (showAddStudentDialog) {
                    AddStudentDialog(
                        isPrivateSchool = schoolConfig.isPrivateSchool,
                        onDismiss = { showAddStudentDialog = false },
                        onConfirm = { fullName, gradeClass, section, parentName, parentPhone, isHonor, honorReason, totalTuition, paidTuition, nextDate ->
                            viewModel.addStudent(
                                fullName = fullName,
                                gradeClass = gradeClass,
                                section = section,
                                parentName = parentName,
                                parentPhone = parentPhone,
                                isHonorRoll = isHonor,
                                honorReason = honorReason,
                                totalTuition = totalTuition,
                                paidTuition = paidTuition,
                                nextInstallmentDate = nextDate
                            )
                        }
                    )
                }

                if (showAddTeacherDialog) {
                    AddTeacherDialog(
                        onDismiss = { showAddTeacherDialog = false },
                        onConfirm = { name, subject, classes, phone ->
                            viewModel.addTeacher(name, subject, classes, phone)
                        }
                    )
                }

                if (showAddLessonDialog) {
                    AddLessonPrepDialog(
                        defaultDay = selectedDayFilter,
                        onDismiss = { showAddLessonDialog = false },
                        onConfirm = { day, period, cls, sec, sub, teacher, prep, hw ->
                            viewModel.addLessonPrep(day, period, cls, sec, sub, teacher, prep, hw)
                        }
                    )
                }

                if (showAddGradeDialog && allStudents.isNotEmpty()) {
                    AddGradeDialog(
                        students = allStudents,
                        onDismiss = { showAddGradeDialog = false },
                        onConfirm = { st, sub, date, daily, monthly, mid, annual ->
                            viewModel.addGradeEntry(st, sub, date, daily, monthly, mid, annual)
                        }
                    )
                }

                if (showSendNoticeDialog && allStudents.isNotEmpty()) {
                    SendNoticeDialog(
                        students = allStudents,
                        senderRoleLabel = activeRole.titleAr,
                        onDismiss = { showSendNoticeDialog = false },
                        onConfirm = { st, type, title, msg, sender ->
                            viewModel.sendParentNotice(st, type, title, msg, sender)
                        }
                    )
                }

                if (showAddEventDialog) {
                    AddEventDialog(
                        onDismiss = { showAddEventDialog = false },
                        onConfirm = { title, desc, date, cat, theme ->
                            viewModel.addSchoolEvent(title, desc, date, cat, theme)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SchoolTopHeader(
    schoolConfig: SchoolConfig,
    activeRole: UserRole,
    allStudents: List<StudentRecord>,
    selectedStudent: StudentRecord?,
    isDarkTheme: Boolean,
    onSelectRole: (UserRole) -> Unit,
    onSelectStudent: (StudentRecord) -> Unit,
    onOpenAccessCode: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val schoolTypeEnum = SchoolType.fromCode(schoolConfig.schoolType)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top School Identity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = schoolConfig.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (schoolConfig.isPrivateSchool) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                Text(
                                    text = schoolTypeEnum.badgeAr,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (schoolConfig.isPrivateSchool) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${schoolConfig.address} | ${schoolConfig.emblemTitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenAccessCode) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "كود الدخول السريع",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_school_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "إعدادات المدرسة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Role Switcher Bar (المدير، المعلم، ولي الأمر، الطالب)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val isSelected = activeRole == role
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectRole(role) },
                        label = {
                            Text(
                                text = role.titleAr,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.testTag("role_chip_${role.code.lowercase()}")
                    )
                }
            }

            // Student Selector Bar when viewing as Parent or Student
            if ((activeRole == UserRole.PARENT || activeRole == UserRole.STUDENT) && allStudents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (activeRole == UserRole.PARENT) "متابعة الابن:" else "حساب الطالب:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                    allStudents.forEach { st ->
                        FilterChip(
                            selected = selectedStudent?.id == st.id,
                            onClick = { onSelectStudent(st) },
                            label = {
                                Text(
                                    text = "${st.fullName} (${st.accessCode})",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
