package com.adeshchandra.messagealarm.ui.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.adeshchandra.messagealarm.MessageAlarmApp
import com.adeshchandra.messagealarm.R
import com.adeshchandra.messagealarm.data.prefs.PreferencesManager
import com.adeshchandra.messagealarm.databinding.FragmentSettingsBinding
import com.google.android.material.slider.Slider
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: PreferencesManager) : ViewModel() {
    val settings = prefs.settingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.Settings())

    fun setVolume(v: Int)                         = viewModelScope.launch { prefs.setAlarmVolume(v) }
    fun setVibration(enabled: Boolean)            = viewModelScope.launch { prefs.setVibrationEnabled(enabled) }
    fun setScreenWake(enabled: Boolean)           = viewModelScope.launch { prefs.setScreenWakeEnabled(enabled) }
    fun setSoundUri(uri: String)                  = viewModelScope.launch { prefs.setAlarmSoundUri(uri) }
    fun setSnooze(min: Int)                       = viewModelScope.launch { prefs.setSnoozeMinutes(min) }
    fun setDndEnabled(enabled: Boolean)           = viewModelScope.launch { prefs.setDndEnabled(enabled) }
    fun setDndHours(start: Int, end: Int)         = viewModelScope.launch { prefs.setDndHours(start, end) }
    fun setRepeatAlarm(enabled: Boolean)          = viewModelScope.launch { prefs.setRepeatAlarm(enabled) }
    fun setRepeatSettings(interval: Int, max: Int)= viewModelScope.launch { prefs.setRepeatSettings(interval, max) }
    fun setFlash(enabled: Boolean)                = viewModelScope.launch { prefs.setFlashEnabled(enabled) }
}

class SettingsViewModelFactory(private val prefs: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return SettingsViewModel(prefs) as T
    }
}

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var isBinding = false // Prevent feedback loops

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory((requireActivity().application as MessageAlarmApp).preferencesManager)
    }

    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setSoundUri(uri?.toString() ?: "")
            binding.tvSoundName.text = getRingtoneName(uri?.toString() ?: "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeSettings()
    }

    private fun setupClickListeners() {
        // Ringtone picker
        binding.rowAlarmSound.setOnClickListener { openRingtonePicker() }

        // DND time pickers
        binding.tvDndStart.setOnClickListener { showTimePicker("DND Start", true) }
        binding.tvDndEnd.setOnClickListener   { showTimePicker("DND End",   false) }

        // Fiverr link
        binding.btnFiverrLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://fiverr.com/adesh_chandra")))
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { s ->
                    isBinding = true

                    binding.sliderVolume.value         = s.alarmVolume.toFloat()
                    binding.switchVibration.isChecked  = s.vibrationEnabled
                    binding.switchScreenWake.isChecked = s.screenWakeEnabled
                    binding.switchRepeat.isChecked     = s.repeatAlarm
                    binding.switchDnd.isChecked        = s.dndEnabled
                    binding.switchFlash.isChecked      = s.flashEnabled
                    binding.tvSoundName.text           = getRingtoneName(s.alarmSoundUri)
                    binding.tvDndStart.text            = formatHour(s.dndStartHour)
                    binding.tvDndEnd.text              = formatHour(s.dndEndHour)
                    binding.groupDndTimes.visibility   = if (s.dndEnabled) View.VISIBLE else View.GONE

                    // Snooze chip selection
                    when (s.snoozeMinutes) {
                        5  -> binding.chipSnooze5.isChecked  = true
                        10 -> binding.chipSnooze10.isChecked = true
                        15 -> binding.chipSnooze15.isChecked = true
                        30 -> binding.chipSnooze30.isChecked = true
                    }

                    isBinding = false
                }
            }
        }

        // Listeners (only fire when user changes, not during bind)
        binding.sliderVolume.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                if (!isBinding) viewModel.setVolume(slider.value.toInt())
            }
        })
        binding.switchVibration.setOnCheckedChangeListener  { _, c -> if (!isBinding) viewModel.setVibration(c) }
        binding.switchScreenWake.setOnCheckedChangeListener { _, c -> if (!isBinding) viewModel.setScreenWake(c) }
        binding.switchRepeat.setOnCheckedChangeListener     { _, c -> if (!isBinding) viewModel.setRepeatAlarm(c) }
        binding.switchDnd.setOnCheckedChangeListener        { _, c ->
            if (!isBinding) {
                viewModel.setDndEnabled(c)
                binding.groupDndTimes.visibility = if (c) View.VISIBLE else View.GONE
            }
        }
        binding.switchFlash.setOnCheckedChangeListener      { _, c -> if (!isBinding) viewModel.setFlash(c) }

        binding.chipGroupSnooze.setOnCheckedStateChangeListener { group, checkedIds ->
            if (!isBinding && checkedIds.isNotEmpty()) {
                val min = when (checkedIds.first()) {
                    R.id.chipSnooze5  -> 5
                    R.id.chipSnooze10 -> 10
                    R.id.chipSnooze15 -> 15
                    R.id.chipSnooze30 -> 30
                    else -> 5
                }
                viewModel.setSnooze(min)
            }
        }
    }

    private fun openRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
        ringtonePickerLauncher.launch(intent)
    }

    private fun showTimePicker(title: String, isStart: Boolean) {
        val current = viewModel.settings.value
        val hour = if (isStart) current.dndStartHour else current.dndEndHour
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(0)
            .setTitleText(title)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    val h = this.hour
                    val s = viewModel.settings.value
                    if (isStart) viewModel.setDndHours(h, s.dndEndHour)
                    else viewModel.setDndHours(s.dndStartHour, h)
                }
            }
            .show(childFragmentManager, "time_picker")
    }

    private fun getRingtoneName(uri: String): String {
        if (uri.isBlank()) return "Default Alarm"
        return try {
            RingtoneManager.getRingtone(requireContext(), Uri.parse(uri))?.getTitle(requireContext()) ?: "Custom Sound"
        } catch (_: Exception) { "Custom Sound" }
    }

    private fun formatHour(hour: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$h:00 $amPm"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
