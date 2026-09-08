package com.example.scamshield.shieldscan

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ShieldScanTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        try {
            qsTile?.apply {
                state = Tile.STATE_INACTIVE
                label = "SCAN WITH SCAMSHIELD"
                updateTile()
            }
        } catch (_: Throwable) {
        }
    }

    override fun onClick() {
        super.onClick()

        val intent = Intent(this, ShieldScanActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        } catch (_: Throwable) {
            try {
                startActivity(intent)
            } catch (_: Throwable) {
            }
        }
    }
}
