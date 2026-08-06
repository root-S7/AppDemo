package ren.icraft.library

import android.app.Activity
import android.app.Application
import android.graphics.Color.TRANSPARENT
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class DemoApplication : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        lateinit var instance: DemoApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as? ComponentActivity)?.enableEdgeToEdge(navigationBarStyle = SystemBarStyle.dark(TRANSPARENT))
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            (activity as? ComponentActivity)?.enableEdgeToEdge(navigationBarStyle = SystemBarStyle.dark(TRANSPARENT))
        }

        hideSystemBars(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        hideSystemBars(activity)
    }

    private fun hideSystemBars(activity: Activity) {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
}