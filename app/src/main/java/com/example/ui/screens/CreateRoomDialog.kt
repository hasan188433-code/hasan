package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreateRoom: (title: String, desc: String, category: String, isPrivate: Boolean, passcode: String?, maxCapacity: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("گفتگو و گپ") }
    var isPrivate by remember { mutableStateOf(false) }
    var passcode by remember { mutableStateOf("") }
    var maxCapacity by remember { mutableFloatStateOf(50f) }

    val categories = listOf("گفتگو و گپ", "موسیقی", "فناوری", "پادکست", "گیمینگ")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1B1736),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ایجاد اتاق صوتی جدید 🎙️",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_create_room")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان اتاق", color = Color(0xFFA5A0CA)) },
                    placeholder = { Text("مثلاً: گپ آزاد و آهنگ‌های درخواستی", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color(0xFF382F66),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات یا قوانین اتاق", color = Color(0xFFA5A0CA)) },
                    placeholder = { Text("درباره چه موضوعاتی صحبت می‌کنیم...", color = Color.Gray) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color(0xFF382F66),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_desc_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "دسته‌بندی موضوعی:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA5A0CA),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C4DFF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF262049),
                                labelColor = Color(0xFFA5A0CA)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Public / Private Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF262049))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.Public,
                            contentDescription = null,
                            tint = if (isPrivate) Color(0xFFFF4081) else Color(0xFF00E676)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPrivate) "اتاق خصوصی (کد ورود)" else "اتاق عمومی (همه)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isPrivate) "نیازمند وارد کردن رمز برای ورود" else "قابل مشاهده برای همه کاربران",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF4081)
                        ),
                        modifier = Modifier.testTag("private_switch")
                    )
                }

                if (isPrivate) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { passcode = it },
                        label = { Text("رمز ورود به اتاق (PIN)", color = Color(0xFFA5A0CA)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF4081),
                            unfocusedBorderColor = Color(0xFF382F66),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_pin_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Max Capacity Slider
                Text(
                    text = "حداکثر ظرفیت: ${maxCapacity.toInt()} نفر",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA5A0CA)
                )

                Slider(
                    value = maxCapacity,
                    onValueChange = { maxCapacity = it },
                    valueRange = 10f..200f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E676),
                        activeTrackColor = Color(0xFF00E676),
                        inactiveTrackColor = Color(0xFF382F66)
                    ),
                    modifier = Modifier.testTag("capacity_slider")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateRoom(
                                title.trim(),
                                description.trim(),
                                selectedCategory,
                                isPrivate,
                                passcode.takeIf { isPrivate && it.isNotBlank() },
                                maxCapacity.toInt()
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C4DFF),
                        disabledContainerColor = Color(0xFF382F66)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_create_room_button")
                ) {
                    Text(
                        text = "ایجاد و شروع اتاق صوتی 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
