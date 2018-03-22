package pt.isel.pdm.yawa.activities.settings

import android.content.Context
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.TimePicker
import pt.isel.pdm.yawa.R

class TimePickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    companion object {
        const val MINUTES = 60
        const val SECONDS = 60
        const val MILLIS: Long = 1000
    }

    val picker: TimePicker by lazy { TimePicker(context) }

    init {
        dialogLayoutResource = R.layout.time_picker
        positiveButtonText = "OK"
        negativeButtonText = "CANCEL"
        dialogIcon = null
    }

    override fun onCreateDialogView(): View {
        return picker
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        if (positiveResult) {
            val time = hoursToMs(picker.currentHour) + minutesToMs(picker.currentMinute)
            persistLong(time)
        }
    }


    private fun hoursToMs(hours: Int): Long {
        return minutesToMs(hours * MINUTES)
    }

    private fun minutesToMs(minutes: Int): Long {
        return minutes * SECONDS * MILLIS
    }

}