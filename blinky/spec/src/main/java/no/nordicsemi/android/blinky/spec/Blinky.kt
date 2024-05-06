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
    /**
     * The current state of the time Mode.
     */
    val timeModeState: StateFlow<Boolean>
    /**
     * The current state of the daylight savings.
     */
    val dstState:StateFlow<Boolean>

    /**
     * The current state of the Time.
     */
    val timeState: StateFlow<Int>

    /**
     * The current state of the button.
     */
    val buttonState: StateFlow<Boolean>

    /**
     * Controls the time mode state.
     *
     * @param state the new state of the time mode.
     */
    suspend fun timeModeUpdate(state: Boolean)
    /**
     * Controls the daylight savings state.
     *
     * @param state the new state of the daylight savings state.
     */
    suspend fun dstUpdate(state: Boolean)

    suspend fun timeUpdate()
}