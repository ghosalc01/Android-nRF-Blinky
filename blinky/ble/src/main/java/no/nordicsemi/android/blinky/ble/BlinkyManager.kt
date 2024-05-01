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
import no.nordicsemi.android.blinky.ble.data.TimeCallback
import no.nordicsemi.android.blinky.ble.data.ButtonState
import no.nordicsemi.android.blinky.ble.data.TimeModeCallback
import no.nordicsemi.android.blinky.ble.data.TimeModeData
import no.nordicsemi.android.blinky.ble.data.TimeData
import no.nordicsemi.android.blinky.ble.data.TimeZoneData
import no.nordicsemi.android.blinky.ble.data.TimeZoneCallback
import no.nordicsemi.android.blinky.ble.data.DSTData
import no.nordicsemi.android.blinky.ble.data.DSTCallback
import no.nordicsemi.android.blinky.ble.data.IncomingCallCallback
import no.nordicsemi.android.blinky.ble.data.IncomingTextCallback
import no.nordicsemi.android.blinky.ble.data.NotificationBarCallback
import no.nordicsemi.android.blinky.spec.Blinky
import no.nordicsemi.android.blinky.spec.BlinkySpec
import timber.log.Timber

class BlinkyManager(
    context: Context,
    device: BluetoothDevice
) : Blinky by BlinkyManagerImpl(context, device)

private class BlinkyManagerImpl(
    context: Context,
    private val device: BluetoothDevice,
) : BleManager(context), Blinky {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var timeCharacteristic: BluetoothGattCharacteristic? = null
    private var timezoneCharacteristic: BluetoothGattCharacteristic? = null
    private var timemodeCharacteristic: BluetoothGattCharacteristic? = null
    private var dstCharacteristic: BluetoothGattCharacteristic? = null
    private var notificationbarCharacteristic: BluetoothGattCharacteristic? = null
    private var incomingcallCharacteristic: BluetoothGattCharacteristic? = null
    private var incomingtextCharacteristic: BluetoothGattCharacteristic? = null

    private var ledCharacteristic: BluetoothGattCharacteristic? = null
    private var buttonCharacteristic: BluetoothGattCharacteristic? = null

    private val _timeState = MutableStateFlow(false)
    override val timeState = _timeState.asStateFlow()

    private val _timemodeState = MutableStateFlow(false)
    override val timemodeState = _timemodeState.asStateFlow()

    private val _timezoneState = MutableStateFlow(false)
    override val timezoneState = _timezoneState.asStateFlow()

    private val _dstState = MutableStateFlow(false)
    override val dstState = _dstState.asStateFlow()

    private val _notificationbarState = MutableStateFlow(false)
    override val notificationbarState = _notificationbarState.asStateFlow()

    private val _incomingcallState = MutableStateFlow(false)
    override val incomingcallState = _incomingcallState.asStateFlow()

    private val _incomingtextState = MutableStateFlow(false)
    override val incomingtextState = _incomingtextState.asStateFlow()




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

/***
    private val buttonCallback by lazy {
        object : ButtonCallback() {
            override fun onButtonStateChanged(device: BluetoothDevice, state: Boolean) {
                _buttonState.tryEmit(state)
            }
        }
    }

    private val ledCallback by lazy {
        object : LedCallback() {
            override fun onLedStateChanged(device: BluetoothDevice, state: Boolean) {
                _ledState.tryEmit(state)
            }
        }
    }
**/
    private val timeCallback by lazy {
        object : TimeCallback() {
            override fun onTimeStateChanged(device: BluetoothDevice, state: Boolean) {
                _timeState.tryEmit(state)
                TODO("Not yet implemented")
            }
        }

    }
    private val timemodeCallback by lazy {
        object : TimeModeCallback() {
            override fun onTimeModeStateChanged(device: BluetoothDevice, state: Boolean) {
                _timemodeState.tryEmit(state)
                TODO("Not yet implemented")
            }
        }

    }
    private val timezoneCallback by lazy {
        object : TimeZoneCallback() {
            override fun onTimeZoneStateChanged(device: BluetoothDevice, state: Boolean) {
                _timezoneState.tryEmit(state)
                TODO("Not yet implemented")
            }
        }

    }
    private val dstCallback by lazy {
        object : DSTCallback() {
            override fun onDSTStateChanged(device: BluetoothDevice, state: Boolean) {
                _dstState.tryEmit(state)
                TODO("Not yet implemented")
            }
        }

    }
    private val notificationBarCallback by lazy {
        object : NotificationBarCallback() {
            override fun onNotificaitonBarStateChanged(device: BluetoothDevice, state: Boolean) {
                TODO("Not yet implemented")
                _notificationbarState.tryEmit(state)
            }
        }

    }

    private val incomingcallCallback by lazy {
        object : IncomingCallCallback() {
            override fun onIncomingCallStateChanged(device: BluetoothDevice, state: Boolean) {
                _incomingcallState.tryEmit(state)
                TODO("Not yet implemented")
            }
        }
    }
    private val incomingtextCallback by lazy {
        object : IncomingTextCallback() {
            override fun onIncomingTextStateChanged(device: BluetoothDevice, state: Boolean) {
                _incomingtextState.tryEmit(state)
                TODO("Not yet implemented")
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

    override suspend fun updateClock(state: Boolean) {
        writeCharacteristic(
            timeCharacteristic,
                    TimeData.from(state),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()

        _timeState.value = state
    }

    override suspend fun turnLed(state: Boolean) {
        // Write the value to the characteristic.
        writeCharacteristic(
            ledCharacteristic,
            LedData.from(state),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()
        // Update the state flow with the new value.
        _ledState.value = state
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
            // get the time characteristic
            timeCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_TIME_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE

            )
            timezoneCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_TIME_ZONE_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            timemodeCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_TIME_MODE_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            dstCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_DAYLIGHT_SAVING_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE


            )
            /***
            ledCharacteristic = getCharacteristic(
                BlinkySpec.BLINKY_LED_CHARACTERISTIC_UUID,
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
**/
            // Return true if all required characteristics are supported.
            return timeCharacteristic != null && timezoneCharacteristic != null && timemodeCharacteristic != null && dstCharacteristic != null
        }
        gatt.getService(BlinkySpec.KEPLER_NOTIFICATION_SERVICE_UUID)?.apply {
            notificationbarCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_NOTIFICATION_BAR_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            incomingcallCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_INCOMING_CALL_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            incomingtextCharacteristic = getCharacteristic(
                BlinkySpec.KEPLER_INCOMING_TEXT_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            return notificationbarCharacteristic != null && incomingcallCharacteristic != null && incomingtextCharacteristic != null
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

        readCharacteristic(timeCharacteristic)
            .with(timeCallback)
            .enqueue()
        readCharacteristic(timezoneCharacteristic)
            .with(timezoneCallback)
            .enqueue()
        readCharacteristic(timemodeCharacteristic)
            .with(timemodeCallback)
            .enqueue()
        readCharacteristic(dstCharacteristic)
            .with(dstCallback)
            .enqueue()
        readCharacteristic(notificationbarCharacteristic)
            .with(notificationBarCallback)
            .enqueue()
        readCharacteristic(incomingcallCharacteristic)
            .with(incomingcallCallback)
            .enqueue()
        readCharacteristic(incomingtextCharacteristic)
            .with(incomingtextCallback)
            .enqueue()

/**
        // Read the initial value of the button characteristic.
        readCharacteristic(buttonCharacteristic)
            .with(buttonCallback)
            .enqueue()

        // Read the initial value of the LED characteristic.
        readCharacteristic(ledCharacteristic)
            .with(ledCallback)
            .enqueue()

        **/
    }

    override fun onServicesInvalidated() {
        ledCharacteristic = null
        buttonCharacteristic = null
        timeCharacteristic = null
        timemodeCharacteristic = null
        timezoneCharacteristic = null
        dstCharacteristic = null
    }
}