package dev.alimmz.atlasfly

import android.app.Activity
import android.os.Bundle
import com.chuckerteam.chucker.api.Chucker

/** Debug launcher that provides reliable access when Chucker notifications are hidden. */
class ChuckerLauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Chucker.getLaunchIntent(this))
        finish()
    }
}
