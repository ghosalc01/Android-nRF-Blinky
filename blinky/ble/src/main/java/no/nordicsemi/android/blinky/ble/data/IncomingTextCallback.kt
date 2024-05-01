package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class IncomingTextCallback: ProfileReadResponse() {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val incomingtextState = data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01
            onIncomingTextStateChanged(device, incomingtextState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onIncomingTextStateChanged(device: BluetoothDevice, state: Boolean)
}