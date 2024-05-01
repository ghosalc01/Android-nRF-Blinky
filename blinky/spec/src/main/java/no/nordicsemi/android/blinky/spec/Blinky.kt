package no.nordicsemi.android.blinky.spec

import kotlinx.coroutines.flow.StateFlow

interface Blinky {

    enum class State {
        LOADING,
        READY,
        NOT_AVAILABLE
    }

    /**
     * Connects to the device.
     */
    suspend fun connect()

    /**
     * Disconnects from the device.
     */
    fun release()

    /**
     * The current state of the blinky.
     */
    val state: StateFlow<State>

    val timeState: StateFlow<Boolean>

    val timezoneState: StateFlow<Boolean>

    val timemodeState: StateFlow<Boolean>

    val dstState: StateFlow<Boolean>

    val notificationState: StateFlow<Boolean>

    val incomingcallState: StateFlow<Boolean>
    val incomingtextState: StateFlow<Boolean>

    /**
     * The current state of the LED.
     */
    val ledState: StateFlow<Boolean>

    /**
     * The current state of the button.
     */
    val buttonState: StateFlow<Boolean>

    /**
     * Controls the LED state.
     *
     * @param state the new state of the LED.
     */
    suspend fun turnLed(state: Boolean)
}