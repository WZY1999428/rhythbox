package wzy.hkta.rhythbox.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.utils.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.utils.fatchMusic.LoginRequest
import wzy.hkta.rhythbox.utils.fatchMusic.UserInfo

object UserManager {
    private val _userInfo = MutableStateFlow(
        StorageManager.getItem<UserInfo>("userInfo")
    )

    val userInfo = _userInfo.asStateFlow()

    fun getUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            _userInfo.value = FetchMusic.getUserInfo()
            userInfo.value?.let {
                StorageManager.setItem<UserInfo>("userInfo", it)
            }
        }
    }

    fun login(params: LoginRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            FetchMusic.login(params)
        }
    }
}