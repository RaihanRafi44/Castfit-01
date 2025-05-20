package com.raihan.castfit.presentation.profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.raihan.castfit.presentation.login.LoginActivity
import com.raihan.castfit.utils.proceedWhen
import com.raihan.castfit.R
import com.raihan.castfit.databinding.FragmentProfileBinding
import com.raihan.castfit.utils.calculateAge
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by viewModel()
    private var isSaveProfileButtonEnabled: Boolean = false
    private var dateOfBirthClickListener: View.OnClickListener? = null
    private var hasSavedAge = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModel.loadCurrentUser()
        profileViewModel.resetEditMode()
        observeEditMode()
        setupInputListeners()
        setupClickListeners()
        binding.layoutProfile.etDateOfBirth.isFocusable = false
        binding.layoutProfile.etDateOfBirth.isClickable = true
    }

    private fun setupInputListeners() {
        binding.layoutProfile.etEmail.isFocusable = false
        binding.layoutProfile.etEmail.isClickable = true
        binding.layoutProfile.etEmail.setOnClickListener {
            Toast.makeText(requireContext(), "Email tidak dapat diubah", Toast.LENGTH_SHORT).show()
        }

        binding.layoutProfile.etName.addTextChangedListener {
            isSaveProfileButtonEnabled = true
            updateSaveButtonState()
        }

        profileViewModel.editedFullName.observe(viewLifecycleOwner) {
            binding.layoutProfile.etName.setText(it)
        }

        profileViewModel.getCurrentUser()?.let {
            binding.layoutProfile.etEmail.setText(it.email)
        }
    }

    private fun observeEditMode() {
        profileViewModel.isEditMode.observe(viewLifecycleOwner) { isEdit ->
            binding.layoutProfile.etName.isEnabled = isEdit
            binding.layoutProfile.etEmail.isEnabled = isEdit

            binding.layoutProfile.btnEditProfile.isVisible = !isEdit
            binding.layoutProfile.btnCancelEdit.isVisible = isEdit
            binding.layoutProfile.llChangePassSave.isVisible = isEdit
            binding.layoutProfile.flBtnLogout.isVisible = !isEdit

            checkDateOfBirthStatus()
        }
    }

    private fun setupClickListeners() {
        binding.layoutProfile.btnLogout.setOnClickListener { showLogoutConfirmationDialog() }
        binding.layoutProfile.btnEditProfile.setOnClickListener { enterEditMode() }
        binding.layoutProfile.btnCancelEdit.setOnClickListener { exitEditMode() }
        binding.layoutProfile.btnSave.setOnClickListener { doEditProfile() }
        binding.layoutProfile.btnChangePass.setOnClickListener { requestChangePassword() }
    }

    private fun enterEditMode() {
        profileViewModel.changeEditMode()
        binding.layoutProfile.btnEditProfile.visibility = View.GONE
        binding.layoutProfile.btnCancelEdit.visibility = View.VISIBLE
        binding.layoutProfile.llChangePassSave.visibility = View.VISIBLE
        binding.layoutProfile.flBtnLogout.visibility = View.GONE

        checkDateOfBirthStatus()
    }

    private fun exitEditMode() {
        profileViewModel.changeEditMode(isCancel = true)

        // Kembalikan teks ke nilai sebelumnya
        binding.layoutProfile.etName.setText(profileViewModel.editedFullName.value)

        binding.layoutProfile.btnEditProfile.visibility = View.VISIBLE
        binding.layoutProfile.btnCancelEdit.visibility = View.GONE
        binding.layoutProfile.llChangePassSave.visibility = View.GONE
        binding.layoutProfile.flBtnLogout.visibility = View.VISIBLE

        isSaveProfileButtonEnabled = false
        updateSaveButtonState()

        // Hapus listener agar tidak bisa dibuka saat bukan edit mode
        binding.layoutProfile.etDateOfBirth.setOnClickListener(null)
        dateOfBirthClickListener = null
    }

    private fun doEditProfile() {
        if (checkNameValidation()) {
            val fullName = binding.layoutProfile.etName.text.toString().trim()
            proceedEdit(fullName)
        }
    }

    private fun proceedEdit(fullName: String) {
        profileViewModel.updateProfileName(fullName = fullName)
            .observe(viewLifecycleOwner) {
                it.proceedWhen(
                    doOnSuccess = {
                        binding.layoutProfile.pbLoadingSave.isVisible = false
                        binding.layoutProfile.btnSave.isVisible = true
                        isSaveProfileButtonEnabled = false
                        updateSaveButtonState()
                        Toast.makeText(requireContext(), "Pengubahan data profil sukses", Toast.LENGTH_SHORT).show()
                        profileViewModel.loadCurrentUser() // refresh nama terbaru
                    },
                    doOnError = {
                        binding.layoutProfile.pbLoadingSave.isVisible = false
                        binding.layoutProfile.btnSave.isVisible = true
                        Toast.makeText(requireContext(), "Pengubahan data profil gagal", Toast.LENGTH_SHORT).show()
                    },
                    doOnLoading = {
                        binding.layoutProfile.pbLoadingSave.isVisible = true
                        binding.layoutProfile.btnSave.isVisible = false
                    }
                )
            }
    }

    private fun updateSaveButtonState() {
        binding.layoutProfile.btnSave.isEnabled = isSaveProfileButtonEnabled
    }

    private fun checkNameValidation(): Boolean {
        val fullName = binding.layoutProfile.etName.text.toString().trim()
        return if (fullName.isEmpty()) {
            binding.layoutProfile.tilName.isErrorEnabled = true
            binding.layoutProfile.tilName.error = getString(R.string.text_error_name_cannot_empty)
            false
        } else {
            binding.layoutProfile.tilName.isErrorEnabled = false
            true
        }
    }

    private fun requestChangePassword() {
        profileViewModel.createChangePwdRequest()
        val dialog = buildChangePasswordDialog(
            "Reset password akan dikirimkan ke email ${profileViewModel.getCurrentUser()?.email}. Harap periksa inbox atau folder spam anda."
        )
        dialog.show()
    }

    private fun buildChangePasswordDialog(message: String): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Oke", null)
            .create()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Apakah kamu ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                profileViewModel.doLogout()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showDatePickerDialog() {
        if (hasSavedAge) return

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val age = calculateAge(selectedYear, selectedMonth, selectedDay)
            val dobString = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
            binding.layoutProfile.etDateOfBirth.setText(dobString)
            binding.layoutProfile.tvUserCurrentAge.text = "$age Tahun"

            // Simpan tanggal lahir dan umur ke Firestore
            hasSavedAge = true
            saveDateOfBirthToFirestore(selectedDay, selectedMonth, selectedYear, age)

            // Disable input agar tidak bisa isi ulang
            binding.layoutProfile.etDateOfBirth.isEnabled = false
            binding.layoutProfile.etDateOfBirth.setOnClickListener(null)

        }, year, month, day)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun checkDateOfBirthStatus() {
        profileViewModel.fetchDateOfBirthAndAge().observe(viewLifecycleOwner) { result ->
            result.proceedWhen(
                doOnSuccess = {
                    val (dob, age) = it.payload ?: Pair(null, null)
                    val isEdit = profileViewModel.isEditMode.value == true

                    dob?.let { binding.layoutProfile.etDateOfBirth.setText(it) }

                    if (age != null && age > 0) {
                        hasSavedAge = true
                        binding.layoutProfile.tvUserCurrentAge.text = "$age Tahun"
                        binding.layoutProfile.etDateOfBirth.isEnabled = false
                        binding.layoutProfile.etDateOfBirth.setOnClickListener(null)
                    } else {
                        hasSavedAge = false
                        if (isEdit) {
                            binding.layoutProfile.etDateOfBirth.isEnabled = true
                            binding.layoutProfile.etDateOfBirth.setOnClickListener {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Input Tanggal Lahir")
                                    .setMessage("Tanggal lahir hanya bisa diisi satu kali dan tidak bisa diubah. Lanjutkan?")
                                    .setPositiveButton("Ya") { _, _ -> showDatePickerDialog() }
                                    .setNegativeButton("Batal", null)
                                    .show()
                            }
                        } else {
                            binding.layoutProfile.etDateOfBirth.isEnabled = false
                            binding.layoutProfile.etDateOfBirth.setOnClickListener(null)
                        }
                    }
                },
                doOnError = {
                    Log.e("ProfileFragment", "Gagal memuat data DOB/age: ${it.exception}")
                }
            )
        }
    }

    private fun saveDateOfBirthToFirestore(day: Int, month: Int, year: Int, age: Int) {
        val dobString = "$day/${month + 1}/$year"

        profileViewModel.saveDateOfBirthAndAge(dobString, age).observe(viewLifecycleOwner) {
            it.proceedWhen(
                doOnSuccess = {
                    Toast.makeText(requireContext(), "Tanggal lahir berhasil disimpan.", Toast.LENGTH_SHORT).show()
                },
                doOnError = {
                    Toast.makeText(requireContext(), "Gagal menyimpan tanggal lahir. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
