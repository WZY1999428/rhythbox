package wzy.hkta.rhythbox.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wzy.hkta.rhythbox.fatchMusic.FetchMusic
import wzy.hkta.rhythbox.fatchMusic.UserInfo

object UserManager {
    private val _userInfo = MutableStateFlow(
        StorageManager.getItem<UserInfo>("userInfo")
    )

    fun getToken() = StorageManager.getItem<String>("token")
    fun removeToken() {
        println("删除删除 token")
        StorageManager.remove("token")
        clearUserInfo()
    }

    fun clearUserInfo() {
        _userInfo.value = null
    }

    val userInfo = _userInfo.asStateFlow()

    fun getUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getToken()?.let {
                    FetchMusic.user.getUserInfo()?.let { data ->
                        _userInfo.value = data
                        StorageManager.setItem<UserInfo>("userInfo", data)
                    }
                }
            } catch (err: Exception) {
                println("errorerrorerrorerror $err")
            }
        }
    }


    fun isLogin(): Boolean {
        return userInfo.value != null && getToken() != null
    }

}