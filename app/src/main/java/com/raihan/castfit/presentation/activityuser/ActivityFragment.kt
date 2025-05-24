package com.raihan.castfit.presentation.activityuser

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.raihan.castfit.R
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.databinding.FragmentActivityBinding
import com.raihan.castfit.utils.proceedWhen
import org.koin.androidx.viewmodel.ext.android.viewModel

class ActivityFragment : Fragment() {

    /*private lateinit var binding: FragmentActivityBinding
    private val activityViewModel: ActivityViewModel by viewModel()
    private val adapter = ActivityAdapter { activity, position ->
        showCancelDialog(activity, position)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivityBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProgress()
        observeData()
        observeDeleteResult()
    }

    private fun observeData() {
        activityViewModel.getAllProgress().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    binding.rvOnProgressList.isVisible = true
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data on progress size: ${list.size}")
                        adapter.setData(list)
                    } ?: Log.d("ActivityFragment", "Payload null")
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error: ${it.exception}")
                    binding.rvOnProgressList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading on progress data...")
                }
            )
        }
    }

    private fun setupProgress() {
        binding.rvOnProgressList.adapter = this@ActivityFragment.adapter
        binding.rvOnProgressList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showCancelDialog(activity: ProgressActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin membatalkan aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                activityViewModel.removeProgress(activity)
                adapter.removeItem(position)
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' telah dihapus dari halaman Activity.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun observeDeleteResult() {
        activityViewModel.deleteOperationResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Delete operation successful")
                    // Data akan refresh otomatis karena LiveData observe
                    Toast.makeText(requireContext(), "Aktivitas berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Delete operation failed")
                    Toast.makeText(requireContext(), "Gagal menghapus aktivitas", Toast.LENGTH_SHORT).show()
                }
                // Reset result setelah ditangani
                activityViewModel.resetDeleteResult()
            }
        }
    }*/

    private lateinit var binding: FragmentActivityBinding
    private val activityViewModel: ActivityViewModel by viewModel()

    private val progressAdapter = ActivityAdapter(
        onCancelClick = { activity, position ->
            showCancelDialog(activity, position)
        },
        onFinishClick = { activity, position ->
            showFinishDialog(activity, position)
        }
    )

    private val historyAdapter = HistoryActivityAdapter { history, position ->
        showDeleteHistoryDialog(history, position)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivityBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        observeData()
        observeDeleteResult()
        observeFinishResult()
        observeDeleteHistoryResult()
    }

    private fun observeData() {
        // Observe progress data
        activityViewModel.getAllProgress().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    binding.rvOnProgressList.isVisible = true
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data on progress size: ${list.size}")
                        progressAdapter.setData(list)
                    } ?: Log.d("ActivityFragment", "Progress payload null")
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading progress: ${it.exception}")
                    binding.rvOnProgressList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading on progress data...")
                }
            )
        }

        // Observe history data
        activityViewModel.getAllHistory().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    binding.rvHistoryList.isVisible = true
                    it.payload?.let { list ->
                        Log.d("ActivityFragment", "Data history size: ${list.size}")
                        historyAdapter.setData(list)
                    } ?: Log.d("ActivityFragment", "History payload null")
                },
                doOnError = {
                    Log.e("ActivityFragment", "Error loading history: ${it.exception}")
                    binding.rvHistoryList.isVisible = false
                },
                doOnLoading = {
                    Log.d("ActivityFragment", "Loading history data...")
                }
            )
        }
    }

    private fun setupRecyclerViews() {
        // Setup progress RecyclerView
        binding.rvOnProgressList.adapter = progressAdapter
        binding.rvOnProgressList.layoutManager = LinearLayoutManager(requireContext())

        // Setup history RecyclerView
        binding.rvHistoryList.adapter = historyAdapter
        binding.rvHistoryList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showCancelDialog(activity: ProgressActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin membatalkan aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                activityViewModel.removeProgress(activity)
                progressAdapter.removeItem(position)
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dihapus dari halaman Activity.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showFinishDialog(activity: ProgressActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin menyelesaikan aktivitas '${activity.physicalActivityName}'? Aktivitas akan dipindahkan ke riwayat.")
            .setPositiveButton("Ya") { _, _ ->
                activityViewModel.finishActivity(activity)
                progressAdapter.removeItem(position) //Hapus UI list dari progress
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' akan dipindahkan ke history.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showDeleteHistoryDialog(history: HistoryActivity, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Riwayat?")
            .setMessage("Apakah kamu yakin ingin menghapus riwayat aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                activityViewModel.removeHistory(history)
                historyAdapter.removeItem(position)
                Log.d("ActivityLog", "Riwayat aktivitas dengan ID '${history.id}' akan dihapus.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun observeDeleteResult() {
        activityViewModel.deleteOperationResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Delete operation successful")
                    Toast.makeText(requireContext(), "Aktivitas berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Delete operation failed")
                    Toast.makeText(requireContext(), "Gagal menghapus aktivitas", Toast.LENGTH_SHORT).show()
                }
                activityViewModel.resetDeleteResult()
            }
        }
    }

    private fun observeFinishResult() {
        activityViewModel.finishOperationResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Finish operation successful")
                    Toast.makeText(requireContext(), "Aktivitas berhasil diselesaikan dan dipindahkan ke riwayat", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Finish operation failed")
                    Toast.makeText(requireContext(), "Gagal menyelesaikan aktivitas", Toast.LENGTH_SHORT).show()
                }
                activityViewModel.resetFinishResult()
            }
        }
    }

    private fun observeDeleteHistoryResult() {
        activityViewModel.deleteHistoryResult.observe(viewLifecycleOwner) { result ->
            result?.let { success ->
                if (success) {
                    Log.d("ActivityFragment", "Delete history operation successful")
                    Toast.makeText(requireContext(), "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ActivityFragment", "Delete history operation failed")
                    Toast.makeText(requireContext(), "Gagal menghapus riwayat", Toast.LENGTH_SHORT).show()
                }
                activityViewModel.resetDeleteHistoryResult()
            }
        }
    }
}