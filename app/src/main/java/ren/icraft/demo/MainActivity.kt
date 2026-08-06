package ren.icraft.demo

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ren.icraft.demo.databinding.ActivityMainBinding
import ren.icraft.demo.databinding.IncludeMainCard2Binding
import ren.icraft.demo.databinding.IncludeMainCardBinding
import ren.icraft.library.utils.ContextUtils.openAction
import ren.icraft.library.utils.ContextUtils.openAppDetailsSettings
import ren.icraft.library.utils.ContextUtils.openBrowser
import ren.icraft.library.utils.ContextUtils.openIntentOrWeb


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val cardBinding: IncludeMainCardBinding by lazy { IncludeMainCardBinding.bind(binding.root) }
    private val bottomCardBinding: IncludeMainCard2Binding by lazy { IncludeMainCard2Binding.bind(binding.root) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cardBinding.apply {
            netflix.setOnClickListener(this@MainActivity)
            chrome.setOnClickListener(this@MainActivity)
            youtube.setOnClickListener(this@MainActivity)
            googlePlay.setOnClickListener(this@MainActivity)
        }

        bottomCardBinding.apply {
            keystone.setOnClickListener(this@MainActivity)
            miracast.setOnClickListener(this@MainActivity)
            signalSource.setOnClickListener(this@MainActivity)
            myApps.setOnClickListener(this@MainActivity)
            settings.setOnClickListener(this@MainActivity)
        }

        binding.apply {
            wifi.setOnClickListener(this@MainActivity)
            usb.setOnClickListener(this@MainActivity)
            time.setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(v: View?) {
        Log.d("事件", "onClick: ")
        v ?: return

        when(v.id) {
            cardBinding.googlePlay.id -> openIntentOrWeb("https://play.google.com/store", "com.android.vending")
            cardBinding.youtube.id -> openIntentOrWeb("https://www.youtube.com", "com.google.android.youtube")
            cardBinding.chrome.id -> openBrowser("https://www.baidu.com")
            cardBinding.netflix.id -> openIntentOrWeb("nflx://www.netflix.com/browse", "com.netflix.mediaclient", "https://www.netflix.com")
            bottomCardBinding.settings.id -> openAction(Settings.ACTION_SETTINGS)
            bottomCardBinding.myApps.id -> openAppDetailsSettings()
            bottomCardBinding.signalSource.id, binding.wifi.id -> openAction(Settings.ACTION_WIFI_SETTINGS)
            bottomCardBinding.miracast.id -> openAction(Settings.ACTION_CAST_SETTINGS,)
            bottomCardBinding.keystone.id -> openAction(Settings.ACTION_SECURITY_SETTINGS)
            binding.time.id -> openAction(Settings.ACTION_DATE_SETTINGS)
            binding.usb.id -> openAction(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        }
    }
}