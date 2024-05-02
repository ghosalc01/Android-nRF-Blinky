package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

abstract class IncomingCallCallback: ProfileReadResponse() {

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() == 1) {
            val incomingcallState = data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01
            onIncomingCallStateChanged(device, incomingcallState)
        } else {
            onInvalidDataReceived(device, data)
        }
    }

    abstract fun onIncomingCallStateChanged(device: BluetoothDevice, state: Boolean)
}