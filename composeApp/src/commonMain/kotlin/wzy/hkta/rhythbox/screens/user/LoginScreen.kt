package wzy.hkta.rhythbox.screens.user

import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.email
import rhythbox.composeapp.generated.resources.lock
import rhythbox.composeapp.generated.resources.music
import rhythbox.composeapp.generated.resources.visibility
import rhythbox.composeapp.generated.resources.visibility_off

private val CardBackground = Color(0xFF1B1D25)
private val AccentColor = Color(0xFF9B7BFF)
private val SecondaryText = Color(0xFF92939F)

class LoginScreen : Screen {

    @Composable
    override fun Content() {

        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var rememberLogin by remember { mutableStateOf(true) }
        var loading by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {

            // 背景装饰
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = 120.dp, y = (-260).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AccentColor.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Logo
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFB69AFF),
                                    Color(0xFF7553E8)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.music),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    text = "Rhythbox",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "让音乐，成为生活的节奏",
                    fontSize = 14.sp,
                    color = SecondaryText
                )

                Spacer(Modifier.height(38.dp))

                // 登录卡片
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {

                    Text(
                        text = "欢迎回来",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "登录你的账号，继续享受音乐",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = "账号",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "用户名 / 邮箱",
                                color = SecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.email),
                                null,
                                tint = SecondaryText
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        colors = loginFieldColors()
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "密码",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "请输入密码",
                                color = SecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.lock),
                                null,
                                tint = SecondaryText
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(if (passwordVisible) Res.drawable.visibility else Res.drawable.visibility_off),
                                contentDescription = "显示密码",
                                tint = SecondaryText,
                                modifier = Modifier.size(30.dp).clickable {
                                    passwordVisible = !passwordVisible
                                }
                            )
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        colors = loginFieldColors()
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberLogin,
                            onCheckedChange = { rememberLogin = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentColor
                            )
                        )

                        Text(
                            "记住登录状态",
                            color = SecondaryText,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // 登录按钮
                    Button(
                        onClick = {
                            // TODO: 调用登录接口
                        },
                        enabled = username.isNotBlank() &&
                                password.isNotBlank() &&
                                !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            disabledContainerColor = AccentColor.copy(
                                alpha = 0.35f
                            )
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "登 录/注 册",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Rhythbox · Music for your life",
                    color = SecondaryText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = Color(0xFF353743),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = AccentColor,
    focusedContainerColor = Color(0xFF22242D),
    unfocusedContainerColor = Color(0xFF22242D)
)
