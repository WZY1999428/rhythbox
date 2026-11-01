
package wzy.hkta.rhythbox.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.artist
import wzy.hkta.rhythbox.components.NavBar

class UserProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // TODO：后续替换成真实用户数据
        var nickname by remember { mutableStateOf("Rhythbox 用户") }
        var username by remember { mutableStateOf("rhythbox_user") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("未设置") }
        var birthday by remember { mutableStateOf("未设置") }
        var signature by remember { mutableStateOf("这个人很懒，什么都没留下") }

        var editingField by remember { mutableStateOf<String?>(null) }
        var editValue by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 顶部导航栏
            NavBar("个人资料")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // 用户头像区域
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                // TODO：打开头像选择器
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
//                            Icons.Default.Person,
                            painter = painterResource(Res.drawable.artist),
                            contentDescription = "用户头像",
                            modifier = Modifier.size(58.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.artist),
//                                Icons.Default.Edit,
                                contentDescription = "修改头像",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = nickname,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(20.dp))
                }

                // 账号信息
                ProfileSection(title = "账号信息") {
                    ProfileItem("用户名", username, editable = false)

                    ProfileItem(
                        title = "昵称",
                        value = nickname,
                        onClick = {
                            editingField = "nickname"
                            editValue = nickname
                        }
                    )

                    ProfileItem(
                        title = "邮箱",
                        value = email.ifBlank { "未绑定" },
                        onClick = {
                            editingField = "email"
                            editValue = email
                        }
                    )

                    ProfileItem(
                        title = "手机号",
                        value = phone.ifBlank { "未绑定" },
                        onClick = {
                            editingField = "phone"
                            editValue = phone
                        }
                    )
                }

                Spacer(Modifier.height(18.dp))

                // 个人资料
                ProfileSection(title = "个人资料") {
                    ProfileItem(
                        title = "性别",
                        value = gender,
                        onClick = {
                            editingField = "gender"
                            editValue = gender
                        }
                    )

                    ProfileItem(
                        title = "生日",
                        value = birthday,
                        onClick = {
                            editingField = "birthday"
                            editValue = birthday
                        }
                    )

                    ProfileItem(
                        title = "个性签名",
                        value = signature,
                        onClick = {
                            editingField = "signature"
                            editValue = signature
                        }
                    )
                }

                Spacer(Modifier.height(30.dp))
            }
        }

        // 简单编辑弹窗
        editingField?.let { field ->
            AlertDialog(
                onDismissRequest = { editingField = null },
                title = { Text("修改${fieldLabel(field)}") },
                text = {
                    if (field == "gender") {
                        var selected by remember(editingField) {
                            mutableStateOf(editValue)
                        }

                        Column {
                            listOf("男", "女", "保密", "未设置").forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selected = item }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected == item,
                                        onClick = { selected = item }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(item)
                                }
                            }
                        }

                        // 将选择结果同步到编辑值
                        LaunchedEffect(selected) {
                            editValue = selected
                        }
                    } else {
                        OutlinedTextField(
                            value = editValue,
                            onValueChange = { editValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = field != "signature",
                            label = { Text(fieldLabel(field)) }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (field) {
                                "nickname" -> nickname = editValue
                                "email" -> email = editValue
                                "phone" -> phone = editValue
                                "gender" -> gender = editValue
                                "birthday" -> birthday = editValue
                                "signature" -> signature = editValue
                            }
                            editingField = null
                        }
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingField = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}

@Composable
private fun ProfileItem(
    title: String,
    value: String,
    editable: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = editable, onClick = onClick)
            .heightIn(min = 54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (editable) {
            Spacer(Modifier.width(6.dp))
            Icon(
//                Icons.Default.ChevronRight,
                painter = painterResource(Res.drawable.artist),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}

private fun fieldLabel(field: String): String {
    return when (field) {
        "nickname" -> "昵称"
        "email" -> "邮箱"
        "phone" -> "手机号"
        "gender" -> "性别"
        "birthday" -> "生日"
        "signature" -> "个性签名"
        else -> field
    }
}
