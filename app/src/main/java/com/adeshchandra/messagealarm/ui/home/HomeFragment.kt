package com.adeshchandra.messagealarm.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.adeshchandra.messagealarm.MessageAlarmApp
import com.adeshchandra.messagealarm.data.db.AppDatabase
import com.adeshchandra.messagealarm.data.prefs.PreferencesManager
import com.adeshchandra.messagealarm.databinding.FragmentHomeBinding
import com.adeshchandra.messagealarm.service.NotificationAlarmService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ─── ViewModel ───────────────────────────────────────────────────────────────
class HomeViewModel(
    private val prefs: PreferencesManager,
    private val db: AppDatabase
) : ViewModel() {

    val settings = prefs.settingsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        PreferencesManager.Settings()
    )

    val todayCount = db.notificationDao().getAllFlow()
        .combine(settings) { events, _ ->
            val dayStart = System.currentTimeMillis() - 86_400_000L
            events.count { it.timestamp >= dayStart }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val enabledAppsCount = db.appConfigDao().getAllFlow()
        .combine(settings) { configs, _ -> configs.count { it.isEnabled } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setMasterEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setMasterEnabled(enabled) }
    }
}

class HomeViewModelFactory(
    private val prefs: PreferencesManager,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(prefs, db) as T
    }
}

// ─── Fragment ─────────────────────────────────────────────────────────────────
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as MessageAlarmApp
        HomeViewModelFactory(app.preferencesManager, app.database)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        binding.btnGrantPermission.setOnClickListener { openNotificationAccessSettings() }
        binding.switchMaster.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMasterEnabled(isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.settings.collect { settings ->
                        // Avoid triggering listener while updating programmatically
                        binding.switchMaster.setOnCheckedChangeListener(null)
                        binding.switchMaster.isChecked = settings.masterEnabled
                        binding.switchMaster.setOnCheckedChangeListener { _, isChecked ->
                            viewModel.setMasterEnabled(isChecked)
                        }
                        updateMasterStatusUI(settings.masterEnabled)
                    }
                }
                launch {
                    viewModel.todayCount.collect { count ->
                        binding.tvAlarmsToday.text = count.toString()
                    }
                }
                launch {
                    viewModel.enabledAppsCount.collect { count ->
                        binding.tvEnabledApps.text = count.toString()
                    }
                }
            }
        }
    }

    private fun updatePermissionStatus() {
        val granted = NotificationAlarmService.isNotificationAccessGranted(requireContext())
        if (granted) {
            binding.cardPermission.visibility = View.GONE
            binding.cardStatus.visibility = View.VISIBLE
        } else {
            binding.cardPermission.visibility = View.VISIBLE
            binding.cardStatus.visibility = View.GONE
        }
    }

    private fun updateMasterStatusUI(enabled: Boolean) {
        binding.tvStatusLabel.text = if (enabled) "Active — Monitoring Notifications" else "Paused — Alarms Disabled"
        binding.ivStatusIcon.setImageResource(
            if (enabled) com.adeshchandra.messagealarm.R.drawable.ic_bell_ring
            else com.adeshchandra.messagealarm.R.drawable.ic_bell_off
        )
    }

    private fun openNotificationAccessSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
