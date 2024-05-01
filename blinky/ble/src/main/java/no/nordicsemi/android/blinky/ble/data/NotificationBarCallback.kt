package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class NotificationBarCallback: ProfileReadResponse() {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val notificationbarState = data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01
            onNotificaitonBarStateChanged(device, notificationbarState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onNotificaitonBarStateChanged(device: BluetoothDevice, state: Boolean)
}