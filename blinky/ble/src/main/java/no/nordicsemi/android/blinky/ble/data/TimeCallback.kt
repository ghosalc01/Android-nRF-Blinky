package no.nordicsemi.android.blinky.ble.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class TimeCallback: ProfileReadResponse() {

    @SuppressLint("SuspiciousIndentation")
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val timeState = Data.FORMAT_UINT32_LE
                onTimeStateChanged(device, timeState)

        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onTimeStateChanged(device: BluetoothDevice, state: Int)
}