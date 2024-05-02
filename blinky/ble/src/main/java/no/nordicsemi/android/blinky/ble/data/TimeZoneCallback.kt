package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class TimeZoneCallback: ProfileReadResponse() {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val timeZoneState = data.getIntValue(Data.FORMAT_UINT16_LE, 0) == 0x01
            onTimeZoneStateChanged(device, timeZoneState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onTimeZoneStateChanged(device: BluetoothDevice, state: Boolean)
}