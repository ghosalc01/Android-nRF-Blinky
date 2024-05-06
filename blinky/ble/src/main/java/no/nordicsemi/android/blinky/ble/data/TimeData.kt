package no.nordicsemi.android.blinky.ble.data

import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.data.MutableData
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.time.Instant



class TimeData private constructor() {

    companion object {
        val data get() = MutableData().apply {
            setValue(Instant.now().epochSecond, Data.FORMAT_UINT32_LE, 0)
        }
    }

}