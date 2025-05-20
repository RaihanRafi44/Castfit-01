package com.raihan.castfit.presentation.activityuser

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.raihan.castfit.R
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.databinding.FragmentActivityBinding
import com.raihan.castfit.databinding.FragmentProfileBinding
import com.raihan.castfit.utils.proceedWhen
import org.koin.androidx.viewmodel.ext.android.viewModel

class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    private val activityViewModel: ActivityViewModel by viewModel()
    private val adapter = ActivityAdapter { activity ->
        showCancelDialog(activity)
    }

    override fun onResume() {
        super.onResume()
        observeData()
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
        //observeData()

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



    private fun setupProgress(){
        binding.rvOnProgressList.adapter = this@ActivityFragment.adapter
        //binding.rvOnProgressList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showCancelDialog(activity: ProgressActivity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Aktivitas?")
            .setMessage("Apakah kamu yakin ingin membatalkan aktivitas ini?")
            .setPositiveButton("Ya") { _, _ ->
                activityViewModel.removeProgress(activity)
                Log.d("ActivityLog", "Aktivitas '${activity.physicalActivityName}' telah dihapus dari halaman Activity.")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

}