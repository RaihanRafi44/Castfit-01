package com.raihan.castfit.presentation.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import com.raihan.castfit.presentation.login.LoginActivity
import com.raihan.castfit.utils.highLightWord
import com.raihan.castfit.utils.proceedWhen
import com.raihan.castfit.R
import com.raihan.castfit.databinding.ActivityRegisterBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {
    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private val registerViewModel: RegisterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupForm()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnRegister.setOnClickListener {
            doRegister()
        }
        binding.tvNavToLogin.highLightWord(getString(R.string.text_highlight_login_here)) {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
    }

    private fun doRegister() {
        val email = binding.layoutRegister.etEmail.text.toString().trim()
        val password = binding.layoutRegister.etPassword.text.toString().trim()
        val confirmPassword = binding.layoutRegister.etConfirmPassword.text.toString().trim()
        val fullName = binding.layoutRegister.etName.text.toString().trim()

        if (isFormValid(fullName, email, password, confirmPassword)) {
            proceedRegister(email, password, fullName)
        }
    }

    private fun proceedRegister(
        email: String,
        password: String,
        fullName: String,
    ) {
        registerViewModel.doRegister(email, fullName, password).observe(this) {
            it.proceedWhen(
                doOnSuccess = {
                    binding.pbLoading.isVisible = false
                    binding.btnRegister.isVisible = true
                    navigateToLogin()
                },
                doOnError = {
                    binding.pbLoading.isVisible = false
                    binding.btnRegister.isVisible = true
                    Toast.makeText(
                        this,
                        "Login Failed : ${it.exception?.message.orEmpty()}",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                doOnLoading = {
                    binding.pbLoading.isVisible = true
                    binding.btnRegister.isVisible = false
                },
            )
        }
    }

    private fun setupForm() {
        with(binding.layoutRegister) {
            tilEmail.isVisible = true
            tilPassword.isVisible = true
            tilName.isVisible = true
            tilConfirmPassword.isVisible = true
        }
    }

    private fun isFormValid(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return checkNameValidation(fullName) &&
                checkEmailValidation(email) &&
                checkPasswordValidation(password, binding.layoutRegister.tilPassword) &&
                checkPasswordValidation(confirmPassword, binding.layoutRegister.tilConfirmPassword) &&
                checkPwdAndConfirmPwd(password, confirmPassword)
    }


    private fun checkNameValidation(fullName: String): Boolean {
        return if (fullName.isEmpty()) {
            binding.layoutRegister.tilName.isErrorEnabled = true
            binding.layoutRegister.tilName.error = getString(R.string.text_error_name_cannot_empty)
            false
        } else {
            binding.layoutRegister.tilName.isErrorEnabled = false
            true
        }
    }

    private fun checkEmailValidation(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.layoutRegister.tilEmail.isErrorEnabled = true
            binding.layoutRegister.tilEmail.error = getString(R.string.text_error_email_empty)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutRegister.tilEmail.isErrorEnabled = true
            binding.layoutRegister.tilEmail.error = getString(R.string.text_error_email_invalid)
            false
        } else {
            binding.layoutRegister.tilEmail.isErrorEnabled = false
            true
        }
    }

    private fun checkPasswordValidation(
        confirmPassword: String,
        textInputLayout: TextInputLayout,
    ): Boolean {
        return if (confirmPassword.isEmpty()) {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error =
                getString(R.string.text_error_password_empty)
            false
        } else if (confirmPassword.length < 4) {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error =
                getString(R.string.text_error_password_less_than_4_char)
            false
        } else {
            textInputLayout.isErrorEnabled = false
            true
        }
    }

    private fun checkPwdAndConfirmPwd(
        password: String,
        confirmPassword: String,
    ): Boolean {
        return if (password != confirmPassword) {
            binding.layoutRegister.tilPassword.isErrorEnabled = true
            binding.layoutRegister.tilPassword.error =
                getString(R.string.text_password_does_not_match)
            binding.layoutRegister.tilConfirmPassword.isErrorEnabled = true
            binding.layoutRegister.tilConfirmPassword.error =
                getString(R.string.text_password_does_not_match)
            false
        } else {
            binding.layoutRegister.tilPassword.isErrorEnabled = false
            binding.layoutRegister.tilConfirmPassword.isErrorEnabled = false
            true
        }
    }
}