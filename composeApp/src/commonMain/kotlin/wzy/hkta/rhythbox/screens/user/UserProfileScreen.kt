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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.AsyncImage
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.filekit.encodeToByteArray
import com.attafitamim.krop.filekit.toImageSrc
import com.attafitamim.krop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import rhythbox.composeapp.generated.resources.Res
import rhythbox.composeapp.generated.resources.artist
import rhythbox.composeapp.generated.resources.right
import wzy.hkta.rhythbox.components.CalendarBottomSheet
import wzy.hkta.rhythbox.components.CalendarSelectMode
import wzy.hkta.rhythbox.components.Input
import wzy.hkta.rhythbox.components.InputType
import wzy.hkta.rhythbox.components.NavBar
import wzy.hkta.rhythbox.components.Textarea
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.UserManager
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.MUSIC_BASE_URL
import wzy.hkta.rhythbox.fatchMusic.UserInfo

class UserProfileScreen : Screen {

    @Composable
    override fun Content() {
        val vm = rememberScreenModel { UserProfileViewModel() }
        val userInfo by vm.userInfo.collectAsState()
        val loading by vm.loading.collectAsState()
        var showCalendar by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val imageCropper = rememberImageCropper()

        LaunchedEffect(Unit) {
            UserManager.getUserInfo()
        }

        // 图片选择器

        val imagePicker = rememberFilePickerLauncher(
            type = FileKitType.Image,
            onResult = { image ->
                image?.let {
                    scope.launch {
                        try {
                            val result = imageCropper.crop { it.toImageSrc() }
                            when (result) {
                                CropResult.Cancelled -> ToastManager.show("图片上传取消")
                                is CropError -> ToastManager.show("图片裁剪失败")
                                is CropResult.Success -> {
                                    ToastManager.showLoading("正在上传头像...")
                                    val bytes = result.bitmap.encodeToByteArray()
                                    val data = FetchMusic.updateAvatar(bytes, it.name)
                                    vm.setUserInfo(userInfo.copy(avatar = data.url))
                                    vm.submit()
                                    ToastManager.show("头像上传成功")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ToastManager.show("图片处理异常：${e.message}")
                        }
                    }
                }
            }
        )
        imageCropper.cropState?.let {
            ImageCropperDialog(
                state = it,
                style = cropperStyle(
                    overlay = Color.Gray.copy(alpha = .5f),
                    autoZoom = false,
                    guidelines = null,
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.05f))
        ) {
            // 顶部导航栏
            NavBar("个人资料")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 整体圆角大卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp).padding(bottom = 20.dp)
                    ) {
                        // 1. 头像行
                        ProfileAvatarItem(
                            title = "头像",
                            avatar = userInfo.avatar,
                            onClick = {
                                imagePicker.launch()
                            }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 2. 昵称
                        ProfileItem(
                            title = "账户",
                        ) {
                            Input(
                                value = userInfo.username,
                                textAlign = TextAlign.End,
                                enabled = false
                            )
                        }


                        // 2. 昵称
                        ProfileItem(
                            title = "昵称",
                        ) {
                            Input(
                                value = userInfo.nickname,
                                textAlign = TextAlign.End,
                                onValueChange = { vm.setUserInfo(userInfo.copy(nickname = it)) }
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 3. 手机号
                        ProfileItem(
                            title = "手机号码",
                        ) {
                            Input(
                                value = userInfo.phone ?: "",
                                textAlign = TextAlign.End,
                                type = InputType.Number,
                                onValueChange = {
                                    vm.setUserInfo(userInfo.copy(phone = it))
                                }
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 4. 用户名/ID号
                        ProfileItem(
                            title = "ID号",
                        ) {
                            Text(
                                userInfo.id.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth().padding(end = 10.dp)
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 5. 邮箱
                        ProfileItem(
                            title = "邮箱",
                        ) {
                            Input(
                                value = userInfo.email ?: "",
                                textAlign = TextAlign.End,
                                onValueChange = { vm.setUserInfo(userInfo.copy(email = it)) }
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 6. 性别（行内直接单选样式）
                        ProfileGenderRow(
                            title = "性别",
                            selectedGender = userInfo.gender,
                            onGenderSelected = { vm.setUserInfo(userInfo.copy(gender = it)) }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 7. 生日
                        ProfileItem(
                            title = "生日",
                        ) {
                            Input(
                                value = userInfo.birthday ?: "",
                                textAlign = TextAlign.End,
                                onClick = {
                                    showCalendar = true
                                },
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )

                        // 8. 个性签名
                        ProfileItem(title = "个性签名", labelPosition = LabelPosition.Top) {
                            Textarea(
                                value = userInfo.signature ?: "",
                                textAlign = TextAlign.Start,
                                onValueChange = { vm.setUserInfo(userInfo.copy(signature = it)) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                // 底部保存修改按钮
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { vm.submit() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = "保存修改", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }

        CalendarBottomSheet(
            show = showCalendar,
            mode = CalendarSelectMode.Single,
            onDismissRequest = {
                showCalendar = false
            },
            onConfirm = {
                vm.setUserInfo(userInfo.copy(birthday = it.single.toString()))
                showCalendar = false
            }
        )
    }
}

@Composable
private fun ProfileAvatarItem(
    title: String,
    avatar: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when (avatar) {
                null -> Icon(
                    painter = painterResource(Res.drawable.artist),
                    contentDescription = "用户头像",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> AsyncImage(
                    model = "$MUSIC_BASE_URL$avatar",
                    contentDescription = "用户头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            }
        }
    }
}


enum class LabelPosition {
    Top,
    Start,
}

@Composable
private fun ProfileItem(
    title: String,
    editable: Boolean = true,
    labelPosition: LabelPosition = LabelPosition.Start,
    showArrow: Boolean = false,
    content: @Composable () -> Unit,
) {

    when (labelPosition) {
        LabelPosition.Top -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, lineHeight = 40.sp)
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }

        LabelPosition.Start -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                content()
            }
            if (editable && showArrow) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(Res.drawable.right), // 可替换为你的右箭头图标
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }


}

@Composable
private fun ProfileGenderRow(
    title: String,
    selectedGender: Int,
    onGenderSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("男", "女", "保密").forEachIndexed { index, genderOption ->
                Row(
                    modifier = Modifier.clickable { onGenderSelected(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGender == index,
                        onClick = { onGenderSelected(index) },
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = genderOption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

class UserProfileViewModel : ScreenModel {

    private val _userInfo = MutableStateFlow(UserManager.userInfo.value!!.copy())
    private val _loading = MutableStateFlow(false)

    val userInfo = _userInfo.asStateFlow()
    val loading = _loading.asStateFlow()

    fun setUserInfo(userInfo: UserInfo) {
        _userInfo.value = userInfo
    }

    fun validation() {
        if (userInfo.value.nickname.isEmpty()) {
            throw Exception("昵称不能为空")
        }
        if (userInfo.value.phone?.length != 11) {
            throw Exception("手机号必须为11位")
        }

    }

    fun submit() {
        screenModelScope.launch {
            try {
                validation()
                _loading.value = true
                FetchMusic.user.updateUserInfo(userInfo.value)
                UserManager.getUserInfo()
                ToastManager.show("更新成功")
            } catch (e: Exception) {
                ToastManager.show(e.message ?: "")
            } finally {
                _loading.value = false
            }
        }
    }

}