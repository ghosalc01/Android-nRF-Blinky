package no.nordicsemi.android.blinky.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.blinky.ble.data.ButtonCallback
import no.nordicsemi.android.blinky.ble.data.ButtonState
import no.nordicsemi.android.blinky.ble.data.TimeModeCallback
import no.nordicsemi.android.blinky.ble.data.TimeModeData
import no.nordicsemi.android.blinky.ble.data.dstCallback
import no.nordicsemi.android.blinky.ble.data.dstData
import no.nordicsemi.android.blinky.spec.Blinky
import no.nordicsemi.android.blinky.spec.BlinkySpec
import timber.log.Timber

class BlinkyManager(
    context: Context,
    device: BluetoothDevice
): Blinky by BlinkyManagerImpl(context, device)

private class BlinkyManagerImpl(
    context: Context,
    private val device: BluetoothDevice,
): BleManager(context), Blinky {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var timeMode: BluetoothGattCharacteristic? = null
    private var buttonCharacteristic: BluetoothGattCharacteristic? = null
    private var dstMode: BluetoothGattCharacteristic? = null

    private val _dstState = MutableStateFlow(false)
    override val dstState = _dstState.asStateFlow()

    private val _timeModeState = MutableStateFlow(false)
    override val timeModeState = _timeModeState.asStateFlow()

    private val _buttonState = MutableStateFlow(false)
    override val buttonState = _buttonState.asStateFlow()

    override val state = stateAsFlow()
        .map {
            when (it) {
                is ConnectionState.Connecting,
                is ConnectionState.Initializing -> Blinky.State.LOADING
                is ConnectionState.Ready -> Blinky.State.READY
                is ConnectionState.Disconnecting,
                is ConnectionState.Disconnected -> Blinky.State.NOT_AVAILABLE
            }
        }
        .stateIn(scope, SharingStarted.Lazily, Blinky.State.NOT_AVAILABLE)


    private val buttonCallback by lazy {
        object : ButtonCallback() {
            override fun onButtonStateChanged(device: BluetoothDevice, state: Boolean) {
                _buttonState.tryEmit(state)
            }
        }
    }

    private val timeModeCallback by lazy {
        object : TimeModeCallback() {
            override fun onTimeModeStateChanged(device: BluetoothDevice, state: Boolean) {
                _timeModeState.tryEmit(state)
            }
        }
    }
    private val dstCallback by lazy {
        object : dstCallback() {
            override fun onDstStateChanged(device: BluetoothDevice, state: Boolean) {
                _dstState.tryEmit(state)
            }
        }
    }


    override suspend fun connect() = connect(device)
        .retry(3, 300)
        .useAutoConnect(false)
        .timeout(3000)
        .suspend()

    override fun release() {
        // Cancel all coroutines.
        scope.cancel()

        val wasConnected = isReady
        // If the device wasn't connected, it means that ConnectRequest was still pending.
        // Cancelling queue will initiate disconnecting automatically.
        cancelQueue()

        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    override suspend fun timeModeUpdate(state: Boolean) {
        // Write the value to the characteristic.
        writeCharacteristic(
            timeMode,
            TimeModeData.from(state),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()
        // Update the state flow with the new value.
        _timeModeState.value = state
    }
    override suspend fun dstUpdate(state: Boolean) {
        // Write the value to the characteristic.
        writeCharacteristic(
            dstMode,
            dstData.from(state),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()
        // Update the state flow with the new value.
        _dstState.value = state
    }

    override fun log(priority: Int, message: String) {
        Timber.log(priority, message)
    }

    override fun getMinLogPriority(): Int {
        // By default, the library logs only INFO or
        // higher priority messages. You may change it here.
        return Log.VERBOSE
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        // Get the LBS Service from the gatt object.
        gatt.getService(BlinkySpec.KEPLER_CLOCK_SERVICE_UUID)?.apply {
            // Get the LED characteristic.
            timeMode = getCharacteristic(
                BlinkySpec.KEPLER_TIME_MODE_CHARACTERISTIC_UUID,
                // Mind, that below we pass required properties.
                // If your implementation supports only WRITE_NO_RESPONSE,
                // change the property to BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE.
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            dstMode = getCharacteristic(
                BlinkySpec.KEPLER_DAYLIGHT_SAVING_CHARACTERISTIC_UUID,
                // Mind, that below we pass required properties.
                // If your implementation supports only WRITE_NO_RESPONSE,
                // change the property to BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE.
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )

            // Get the Button characteristic.

            buttonCharacteristic = getCharacteristic(
                BlinkySpec.BLINKY_BUTTON_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY
            )

            // Return true if all required characteristics are supported.
            return timeMode != null
        }
        return false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        // Enable notifications for the button characteristic.
        val flow: Flow<ButtonState> = setNotificationCallback(buttonCharacteristic)
            .asValidResponseFlow()

        // Forward the button state to the buttonState flow.
        scope.launch {
            flow.map { it.state }.collect { _buttonState.tryEmit(it) }
        }

        enableNotifications(buttonCharacteristic)
            .enqueue()

        // Read the initial value of the button characteristic.
        readCharacteristic(buttonCharacteristic)
            .with(buttonCallback)
            .enqueue()

        // Read the initial value of the LED characteristic.
        readCharacteristic(timeMode)
            .with(timeModeCallback)
            .enqueue()

        readCharacteristic(dstMode)
            .with(dstCallback)
            .enqueue()
    }

    override fun onServicesInvalidated() {
        timeMode = null
        dstMode = null
        buttonCharacteristic = null
    }
}