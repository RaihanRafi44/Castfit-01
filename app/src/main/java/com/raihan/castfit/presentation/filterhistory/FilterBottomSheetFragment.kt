package com.raihan.castfit.presentation.filterhistory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.raihan.castfit.R
import com.raihan.castfit.databinding.FragmentFilterBottomSheetBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Contoh penggunaan tombol
        binding.btnRangeDate.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Pilih Rentang Tanggal")
                    .build()

            dateRangePicker.show(parentFragmentManager, "DateRangePicker")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second

                // Format tanggal
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val start = formatter.format(Date(startDate ?: 0))
                val end = formatter.format(Date(endDate ?: 0))

                // Tampilkan di tombol atau simpan
                binding.btnRangeDate.text = "$start - $end"
            }
        }

        binding.btnActivityType.setOnClickListener {
            // Tampilkan filter jenis aktivitas
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}