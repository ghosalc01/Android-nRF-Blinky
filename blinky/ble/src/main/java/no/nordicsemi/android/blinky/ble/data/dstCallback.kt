package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class DSTCallback: ProfileReadResponse() {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val dstState = data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01
            onDSTStateChanged(device, dstState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onDSTStateChanged(device: BluetoothDevice, state: Boolean)
}