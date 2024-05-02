package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class TimeModeCallback: ProfileReadResponse() {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val timeModeState = data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01
            onTimeModeStateChanged(device, timeModeState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onTimeModeStateChanged(device: BluetoothDevice, state: Boolean)
}