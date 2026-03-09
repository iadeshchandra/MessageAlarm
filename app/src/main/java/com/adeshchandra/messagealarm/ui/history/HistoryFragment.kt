package com.adeshchandra.messagealarm.ui.history

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adeshchandra.messagealarm.MessageAlarmApp
import com.adeshchandra.messagealarm.data.db.AppDatabase
import com.adeshchandra.messagealarm.data.model.NotificationEvent
import com.adeshchandra.messagealarm.databinding.FragmentHistoryBinding
import com.adeshchandra.messagealarm.databinding.ItemNotificationHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── ViewModel ────────────────────────────────────────────────────────────────
class HistoryViewModel(private val db: AppDatabase) : ViewModel() {

    val history = db.notificationDao().getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) { db.notificationDao().deleteAll() }
    }
}

class HistoryViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HistoryViewModel(db) as T
    }
}

// ─── Adapter ──────────────────────────────────────────────────────────────────
class HistoryAdapter : ListAdapter<NotificationEvent, HistoryAdapter.VH>(DIFF) {

    private val fmt = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())

    inner class VH(private val b: ItemNotificationHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(event: NotificationEvent) {
            b.tvAppName.text   = event.appName
            b.tvTitle.text     = event.title.ifBlank { "(no title)" }
            b.tvContent.text   = event.content.ifBlank { "(no content)" }
            b.tvTime.text      = fmt.format(Date(event.timestamp))
            b.chipBestMatch.isVisible = event.isUpworkBestMatch
            b.chipFiverr.isVisible    = event.isFiverr
            b.chipUpwork.isVisible    = event.isUpwork && !event.isUpworkBestMatch
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNotificationHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<NotificationEvent>() {
            override fun areItemsTheSame(a: NotificationEvent, b: NotificationEvent) = a.id == b.id
            override fun areContentsTheSame(a: NotificationEvent, b: NotificationEvent) = a == b
        }
    }
}

// ─── Fragment ─────────────────────────────────────────────────────────────────
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((requireActivity().application as MessageAlarmApp).database)
    }

    private val adapter = HistoryAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.adapter = adapter

        binding.btnClearHistory.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear History")
                .setMessage("Delete all notification alarm history?")
                .setPositiveButton("Clear") { _, _ -> viewModel.clearAll() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collect { events ->
                    adapter.submitList(events)
                    binding.tvEmptyHistory.isVisible = events.isEmpty()
                    binding.btnClearHistory.isVisible = events.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
