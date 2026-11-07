package wzy.hkta.rhythbox.fatchMusic

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import org.apache.http.HttpStatus
import wzy.hkta.rhythbox.utils.ToastManager
import wzy.hkta.rhythbox.utils.UserManager

val TokenPlugin = createClientPlugin("TokenPlugin") {

    onRequest { request, _ ->
        val token = UserManager.getToken()
        if (!token.isNullOrBlank()) {
            request.headers.append("authorization", token)
        }
    }
    onResponse { res ->
        if (res.status == HttpStatusCode.Unauthorized) {
            UserManager.removeToken()
            ToastManager.show("登录已过期")
        }

    }
}