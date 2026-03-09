package com.adeshchandra.messagealarm.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adeshchandra.messagealarm.MessageAlarmApp
import com.adeshchandra.messagealarm.data.db.AppDatabase
import com.adeshchandra.messagealarm.data.model.AppConfig
import com.adeshchandra.messagealarm.data.model.KnownApps
import com.adeshchandra.messagealarm.databinding.FragmentAppsBinding
import com.adeshchandra.messagealarm.databinding.ItemAppConfigBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─── Data class for UI ────────────────────────────────────────────────────────
data class AppItem(
    val config: AppConfig,
    val icon: Drawable?,
    val isSpecial: Boolean
)

// ─── ViewModel ────────────────────────────────────────────────────────────────
class AppsViewModel(private val db: AppDatabase, private val context: Context) : ViewModel() {

    val appConfigs = db.appConfigDao().getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun syncInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // User apps only
                .map { it.packageName }

            val existing = db.appConfigDao().getAll().map { it.packageName }.toSet()

            // Add new apps
            installed.forEach { pkg ->
                if (pkg !in existing) {
                    val appName = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }
                                  catch (_: Exception) { pkg }
                    db.appConfigDao().insert(
                        AppConfig(
                            packageName = pkg,
                            appName     = appName,
                            isEnabled   = pkg in listOf(KnownApps.FIVERR, KnownApps.UPWORK) // Pre-enable Fiverr/Upwork
                        )
                    )
                }
            }

            // Remove uninstalled apps
            existing.forEach { pkg ->
                if (pkg !in installed) db.appConfigDao().delete(pkg)
            }
        }
    }

    fun setEnabled(pkg: String, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { db.appConfigDao().setEnabled(pkg, enabled) }
    }

    fun setUpworkBestMatchOnly(pkg: String, bestMatchOnly: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { db.appConfigDao().setUpworkBestMatchOnly(pkg, bestMatchOnly) }
    }
}

class AppsViewModelFactory(private val db: AppDatabase, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AppsViewModel(db, context) as T
    }
}

// ─── Adapter ──────────────────────────────────────────────────────────────────
class AppsAdapter(
    private val onToggle: (String, Boolean) -> Unit,
    private val onBestMatchToggle: (String, Boolean) -> Unit
) : ListAdapter<AppItem, AppsAdapter.VH>(DIFF) {

    private var fullList: List<AppItem> = emptyList()
    private var query: String = ""

    fun submitFullList(list: List<AppItem>) {
        fullList = list
        filter(query)
    }

    fun filter(q: String) {
        query = q
        val filtered = if (q.isBlank()) fullList
        else fullList.filter { it.config.appName.contains(q, ignoreCase = true) }
        submitList(filtered)
    }

    inner class VH(private val b: ItemAppConfigBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: AppItem) {
            b.tvAppName.text = item.config.appName
            b.tvPkgName.text = item.config.packageName
            b.ivAppIcon.setImageDrawable(item.icon)
            b.switchEnabled.setOnCheckedChangeListener(null)
            b.switchEnabled.isChecked = item.config.isEnabled

            // Special Upwork Best Match row
            val isUpwork = item.config.packageName == KnownApps.UPWORK
            b.rowBestMatch.isVisible = isUpwork && item.config.isEnabled
            if (isUpwork) {
                b.switchBestMatch.setOnCheckedChangeListener(null)
                b.switchBestMatch.isChecked = item.config.upworkBestMatchOnly
                b.switchBestMatch.setOnCheckedChangeListener { _, checked ->
                    onBestMatchToggle(item.config.packageName, checked)
                }
            }

            // Highlight special apps
            b.chipSpecial.isVisible = item.isSpecial
            b.chipSpecial.text = when (item.config.packageName) {
                KnownApps.FIVERR -> "Fiverr"
                KnownApps.UPWORK -> "Upwork"
                else -> ""
            }

            b.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(item.config.packageName, checked)
                // Show/hide best match row dynamically
                if (isUpwork) b.rowBestMatch.isVisible = checked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAppConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppItem>() {
            override fun areItemsTheSame(a: AppItem, b: AppItem) = a.config.packageName == b.config.packageName
            override fun areContentsTheSame(a: AppItem, b: AppItem) = a.config == b.config
        }
    }
}

// ─── Fragment ─────────────────────────────────────────────────────────────────
class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by viewModels {
        val app = requireActivity().application as MessageAlarmApp
        AppsViewModelFactory(app.database, requireContext())
    }

    private val adapter = AppsAdapter(
        onToggle         = { pkg, enabled -> viewModel.setEnabled(pkg, enabled) },
        onBestMatchToggle= { pkg, bm      -> viewModel.setUpworkBestMatchOnly(pkg, bm) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvApps.adapter = adapter

        // Search
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean { adapter.filter(q ?: ""); return true }
        })

        // Sync apps on first load
        viewModel.syncInstalledApps()

        // Observe configs
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appConfigs.collect { configs ->
                    val pm = requireContext().packageManager
                    val items = withContext(Dispatchers.IO) {
                        configs.map { config ->
                            val icon = try { pm.getApplicationIcon(config.packageName) } catch (_: Exception) { null }
                            val isSpecial = config.packageName in listOf(KnownApps.FIVERR, KnownApps.UPWORK)
                            AppItem(config, icon, isSpecial)
                        }.sortedWith(compareByDescending<AppItem> { it.isSpecial }.thenBy { it.config.appName })
                    }
                    adapter.submitFullList(items)
                    binding.tvEmptyState.isVisible = items.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
