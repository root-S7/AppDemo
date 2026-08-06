package ren.icraft.library.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

object ContextUtils {

    /**
     * 调起系统默认浏览器打开 URL
     */
    @JvmStatic
    fun Context.openBrowser(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                if(this@openBrowser !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    @JvmStatic
    fun Context.openIntentOrWeb(targetUri: String, targetPackage: String? = null, fallbackUrl: String? = null) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, targetUri.toUri()).apply {
                if(!targetPackage.isNullOrEmpty()) setPackage(targetPackage)

                if(this@openIntentOrWeb !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }.onFailure {
            if(!fallbackUrl.isNullOrEmpty()) openBrowser(fallbackUrl)
        }
    }

    /**
     * 跳转到当前应用的系统设置详情页
     */
    @JvmStatic
    fun Context.openAppDetailsSettings() {
        runCatching {
            val intent = Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            ).apply {
                if (this@openAppDetailsSettings !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            startActivity(intent)
        }
    }

    @JvmStatic
    fun Context.openAction(action: String) {
        runCatching {
            val intent = Intent(action).apply {
                if (this@openAction !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            startActivity(intent)
        }
    }
}