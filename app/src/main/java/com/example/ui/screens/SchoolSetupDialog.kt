package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.NoticeType
import com.example.data.model.SchoolConfig
import com.example.data.model.SchoolType
import com.example.data.model.StudentRecord
import com.example.ui.components.DeveloperSignatureBar

/**
 * First-Time School Platform Setup Screen ("تأسيس منصة مدرسة جديدة")
 * Allows selecting between Government School (مدرسة حكومية) and Private School (مدرسة أهلية).
 */
@Composable
fun InitialSchoolSetupScreen(
    isBusy: Boolean,
    onCreateSchool: (
        name: String,
        address: String,
        schoolType: SchoolType,
        emblemTitle: String,
        includeSampleRecords: Boolean
    ) -> Unit,
    onSignOut: () -> Unit
) {
    var schoolName by rememberSaveable { mutableStateOf("ثانوية زمار النموذجية") }
    var schoolAddress by rememberSaveable { mutableStateOf("الموصل - زمار") }
    var emblemTitle by rememberSaveable { mutableStateOf("العلم نور والاجتهاد رفعة") }
    var selectedType by rememberSaveable { mutableStateOf(SchoolType.GOVERNMENT) }
    var includeSampleData by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                        )
                    )
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "تأسيس مدرسة",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "تأسيس منصة مدرسة جديدة",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أدخل بيانات المدرسة وحدد نوعها (حكومية أو أهلية) لتخصيص الأقسام تلقائياً",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("اسم المدرسة") },
                    placeholder = { Text("مثال: ثانوية زمار النموذجية") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_school_name_input")
                )

                OutlinedTextField(
                    value = schoolAddress,
                    onValueChange = { schoolAddress = it },
                    label = { Text("عنوان المدرسة") },
                    placeholder = { Text("الموصل - زمار") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_school_address_input")
                )

                OutlinedTextField(
                    value = emblemTitle,
                    onValueChange = { emblemTitle = it },
                    label = { Text("شعار أو رسالة المدرسة") },
                    placeholder = { Text("العلم نور والاجتهاد رفعة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "تحديد نوع المدرسة:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SchoolTypeSelectionCard(
                        title = "مدرسة حكومية",
                        subtitle = "الواجهة التعليمية والإدارية الكاملة بدون أقساط مالية",
                        isSelected = selectedType == SchoolType.GOVERNMENT,
                        isPrivate = false,
                        onClick = { selectedType = SchoolType.GOVERNMENT },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_government_school")
                    )

                    SchoolTypeSelectionCard(
                        title = "مدرسة أهلية",
                        subtitle = "تفعيل قسم متابعة الأقساط الدراسية والدفعات تلقائياً",
                        isSelected = selectedType == SchoolType.PRIVATE,
                        isPrivate = true,
                        onClick = { selectedType = SchoolType.PRIVATE },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_private_school")
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeSampleData = !includeSampleData }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeSampleData,
                            onCheckedChange = { includeSampleData = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "إدراج بيانات نموذجية جاهزة للتجربة الفورية",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "تجهيز صفوف ومعلمين وطلاب وجدول دروس ودرجات ولوحة شرف",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        onCreateSchool(
                            schoolName,
                            schoolAddress,
                            selectedType,
                            emblemTitle,
                            includeSampleData
                        )
                    },
                    enabled = !isBusy && schoolName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("confirm_create_school_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأسيس المنصة والبدء الآن",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "تسجيل الخروج أو تبديل الحساب",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DeveloperSignatureBar(
            modifier = Modifier.widthIn(max = 580.dp)
        )
    }
}

@Composable
private fun SchoolTypeSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPrivate: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isPrivate) Icons.Default.Apartment else Icons.Default.AccountBalance,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditSchoolSettingsDialog(
    currentConfig: SchoolConfig,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String, SchoolType, String, String) -> Unit,
    onSignOut: () -> Unit
) {
    var name by remember { mutableStateOf(currentConfig.name) }
    var address by remember { mutableStateOf(currentConfig.address) }
    var emblem by remember { mutableStateOf(currentConfig.emblemTitle) }
    var year by remember { mutableStateOf(currentConfig.academicYear) }
    var selectedType by remember { mutableStateOf(SchoolType.fromCode(currentConfig.schoolType)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعدادات المنصة والمدرسة",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المدرسة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emblem,
                    onValueChange = { emblem = it },
                    label = { Text("شعار المدرسة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("العام الدراسي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "نوع المدرسة:",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == SchoolType.GOVERNMENT,
                        onClick = { selectedType = SchoolType.GOVERNMENT },
                        label = {
                            Text(
                                text = "مدرسة حكومية",
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == SchoolType.PRIVATE,
                        onClick = { selectedType = SchoolType.PRIVATE },
                        label = {
                            Text(
                                text = "مدرسة أهلية",
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = onToggleDarkTheme,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isDarkTheme) "التبديل إلى الوضع النهاري" else "التبديل إلى الوضع الليلي",
                        maxLines = 1,
                        softWrap = false
                    )
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "تسجيل الخروج من الحساب",
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                DeveloperSignatureBar()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, address, selectedType, emblem, year)
                    onDismiss()
                }
            ) {
                Text("حفظ التعديلات", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun QuickAccessCodeDialog(
    students: List<StudentRecord>,
    onDismiss: () -> Unit,
    onCodeSubmit: (code: String, asParent: Boolean) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var asParent by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("الدخول السريع بكود الطالب / ولي الأمر") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أدخل كود الدخول السريع الذي زودتك به إدارة المدرسة (مثل: ST-1234):",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("كود الدخول السريع") },
                    placeholder = {
                        Text(students.firstOrNull()?.accessCode ?: "ST-1001")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = asParent,
                        onClick = { asParent = true },
                        label = { Text("دخول كولي أمر", maxLines = 1, softWrap = false) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !asParent,
                        onClick = { asParent = false },
                        label = { Text("دخول كطالب", maxLines = 1, softWrap = false) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (students.isNotEmpty()) {
                    Text(
                        text = "أكواد متوفرة للتجربة السريعة:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        students.take(3).forEach { student ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { codeInput = student.accessCode }
                            ) {
                                Text(
                                    text = "${student.accessCode} (${student.fullName.substringBefore(" ")})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCodeSubmit(codeInput, asParent)
                    onDismiss()
                }
            ) {
                Text("دخول الآن", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun AddStudentDialog(
    isPrivateSchool: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        fullName: String,
        gradeClass: String,
        section: String,
        parentName: String,
        parentPhone: String,
        isHonor: Boolean,
        honorReason: String,
        totalTuition: Long,
        paidTuition: Long,
        nextDate: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var gradeClass by remember { mutableStateOf("الصف الأول المتوسط") }
    var section by remember { mutableStateOf("أ") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("07512362567") }
    var isHonor by remember { mutableStateOf(false) }
    var honorReason by remember { mutableStateOf("") }
    var totalTuitionText by remember { mutableStateOf(if (isPrivateSchool) "1500000" else "0") }
    var paidTuitionText by remember { mutableStateOf(if (isPrivateSchool) "500000" else "0") }
    var nextDate by remember { mutableStateOf(if (isPrivateSchool) "2026/12/01" else "غير مطلوب") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة طالب جديد وتوليد كود دخول") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل للطالب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradeClass,
                        onValueChange = { gradeClass = it },
                        label = { Text("الصف الدراسي") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("الشعبة") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("اسم ولي الأمر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("رقم هاتف ولي الأمر (لإشعارات واتساب)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHonor, onCheckedChange = { isHonor = it })
                    Text("إدراج الطالب في لوحة الشرف الذهبية للمتفوقين")
                }
                if (isHonor) {
                    OutlinedTextField(
                        value = honorReason,
                        onValueChange = { honorReason = it },
                        label = { Text("سبب التكريم أو الوسام الذهبي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isPrivateSchool) {
                    Text(
                        text = "بيانات الأقساط الدراسية (للمدرسة الأهلية):",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalTuitionText,
                            onValueChange = { totalTuitionText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("القسط الكلي (د.ع)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paidTuitionText,
                            onValueChange = { paidTuitionText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("المدفوع (د.ع)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = nextDate,
                        onValueChange = { nextDate = it },
                        label = { Text("تاريخ القسط القادم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        fullName,
                        gradeClass,
                        section,
                        parentName,
                        parentPhone,
                        isHonor,
                        honorReason,
                        totalTuitionText.toLongOrNull() ?: 0L,
                        paidTuitionText.toLongOrNull() ?: 0L,
                        nextDate
                    )
                    onDismiss()
                }
            ) {
                Text("حفظ وتوليد الكود", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var assignedClasses by remember { mutableStateOf("الصف الأول المتوسط (أ، ب)") }
    var phone by remember { mutableStateOf("07512362567") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة معلم جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("اسم المعلم / المدرس") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة الدراسية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedClasses,
                    onValueChange = { assignedClasses = it },
                    label = { Text("الصفوف والشعب المسندة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(fullName, subject, assignedClasses, phone)
                    onDismiss()
                }
            ) {
                Text("إضافة المعلم", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun AddLessonPrepDialog(
    defaultDay: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String, String, String, String, String, String) -> Unit
) {
    var dayName by remember { mutableStateOf(defaultDay) }
    var periodText by remember { mutableStateOf("1") }
    var gradeClass by remember { mutableStateOf("الصف الأول المتوسط") }
    var section by remember { mutableStateOf("أ") }
    var subject by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("أ. خالد الجبوري") }
    var preparationTopic by remember { mutableStateOf("") }
    var homeworkText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نشر التحضير اليومي والواجب البيتي") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dayName,
                        onValueChange = { dayName = it },
                        label = { Text("اليوم") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = periodText,
                        onValueChange = { periodText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("الحصة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradeClass,
                        onValueChange = { gradeClass = it },
                        label = { Text("الصف") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("الشعبة") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة الدراسية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("اسم المعلم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = preparationTopic,
                    onValueChange = { preparationTopic = it },
                    label = { Text("موضوع التحضير القادم") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = homeworkText,
                    onValueChange = { homeworkText = it },
                    label = { Text("الواجب البيتي المطلوب") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        dayName,
                        periodText.toLongOrNull() ?: 1L,
                        gradeClass,
                        section,
                        subject,
                        teacherName,
                        preparationTopic,
                        homeworkText
                    )
                    onDismiss()
                }
            ) {
                Text("نشر التحضير", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun AddGradeDialog(
    students: List<StudentRecord>,
    onDismiss: () -> Unit,
    onConfirm: (StudentRecord, String, String, Long, Long, Long, Long) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var subject by remember { mutableStateOf("الرياضيات") }
    var examDate by remember { mutableStateOf("2026/11/15") }
    var dailyText by remember { mutableStateOf("95") }
    var monthlyText by remember { mutableStateOf("92") }
    var midyearText by remember { mutableStateOf("94") }
    var annualText by remember { mutableStateOf("95") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رصد درجات الطالب وموعد الامتحان") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("اختر الطالب:", style = MaterialTheme.typography.titleSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    students.forEach { st ->
                        FilterChip(
                            selected = selectedStudent?.id == st.id,
                            onClick = { selectedStudent = st },
                            label = {
                                Text(
                                    text = "${st.fullName} (${st.gradeClass})",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة الدراسية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    label = { Text("تاريخ الامتحان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dailyText,
                        onValueChange = { dailyText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("يومي (100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = monthlyText,
                        onValueChange = { monthlyText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("شهري (100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = midyearText,
                        onValueChange = { midyearText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("نصف السنة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = annualText,
                        onValueChange = { annualText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("السعي السنوي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val st = selectedStudent
                    if (st != null) {
                        onConfirm(
                            st,
                            subject,
                            examDate,
                            dailyText.toLongOrNull() ?: 0L,
                            monthlyText.toLongOrNull() ?: 0L,
                            midyearText.toLongOrNull() ?: 0L,
                            annualText.toLongOrNull() ?: 0L
                        )
                        onDismiss()
                    }
                },
                enabled = selectedStudent != null
            ) {
                Text("حفظ الدرجات", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun SendNoticeDialog(
    students: List<StudentRecord>,
    senderRoleLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (StudentRecord, NoticeType, String, String, String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var selectedType by remember { mutableStateOf(NoticeType.APPRECIATION) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إرسال تبليغ خاص لولي الأمر") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("اختر الطالب:", style = MaterialTheme.typography.titleSmall)
                students.forEach { st ->
                    FilterChip(
                        selected = selectedStudent?.id == st.id,
                        onClick = { selectedStudent = st },
                        label = {
                            Text(
                                text = "${st.fullName} - ولي الأمر: ${st.parentName}",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("نوع التبليغ:", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(NoticeType.APPRECIATION, NoticeType.SUMMONS).forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.labelAr, maxLines = 1, softWrap = false) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(NoticeType.ACADEMIC, NoticeType.BEHAVIORAL).forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.labelAr, maxLines = 1, softWrap = false) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التبليغ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("تفاصيل الرسالة الموجهة لولي الأمر") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val st = selectedStudent
                    if (st != null) {
                        onConfirm(st, selectedType, title, message, senderRoleLabel)
                        onDismiss()
                    }
                },
                enabled = selectedStudent != null
            ) {
                Text("إرسال التبليغ", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("2026/11/01") }
    var category by remember { mutableStateOf("مهرجان علمي") }
    var visualTheme by remember { mutableStateOf("SCIENCE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نشر فعالية أو خبر مدرسي جديد") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الفعالية أو الخبر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف (مهرجان علمي، تكريم، رياضة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("تاريخ الفعالية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل الخبر أو الفعالية") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("الطابع البصري للبطاقة:", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SCIENCE" to "علمي", "HONOR" to "تكريمي", "SPORTS" to "رياضي").forEach { (code, label) ->
                        FilterChip(
                            selected = visualTheme == code,
                            onClick = { visualTheme = code },
                            label = { Text(label, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(title, description, eventDate, category, visualTheme)
                    onDismiss()
                }
            ) {
                Text("نشر في المعرض", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", maxLines = 1, softWrap = false)
            }
        }
    )
}
