package com.raihan.castfit.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import com.raihan.castfit.presentation.forgotpass.ForgotPassActivity
import com.raihan.castfit.presentation.register.RegisterActivity
import com.raihan.castfit.utils.highLightWord
import com.raihan.castfit.utils.proceedWhen
import com.raihan.castfit.R
import com.raihan.castfit.databinding.ActivityLoginBinding
import com.raihan.castfit.presentation.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupForm()
        setClickListeners()
        observeResult()
    }

    private fun setupForm() {
        with(binding.layoutLogin) {
            tilEmail.isVisible = true
            tilPassword.isVisible = true
        }
    }

    private fun observeResult() {
        loginViewModel.loginResult.observe(this) {
            it.proceedWhen(
                doOnSuccess = {
                    binding.pbLoading.isVisible = false
                    binding.btnLogin.isVisible = true
                    navigateToMain()
                },
                doOnError = {
                    binding.pbLoading.isVisible = false
                    binding.btnLogin.isVisible = true
                    Toast.makeText(
                        this,
                        "Login Failed : ${it.exception?.message.orEmpty()}",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                doOnLoading = {
                    binding.pbLoading.isVisible = true
                    binding.btnLogin.isVisible = false
                },
            )
        }
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    private fun setClickListeners() {
        binding.btnLogin.setOnClickListener {
            doLogin()
        }
        binding.tvNavToRegister.highLightWord(getString(R.string.text_highlight_register)) {
            navigateToRegister()
        }

        binding.layoutLogin.tvForgotPassword.setOnClickListener{
            navigateToForgotPass()
        }
    }

    private fun navigateToRegister() {
        startActivity(
            Intent(this, RegisterActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
    }

    private fun doLogin() {
        if (isFormValid()) {
            val email = binding.layoutLogin.etEmail.text.toString().trim()
            val password = binding.layoutLogin.etPassword.text.toString().trim()
            loginViewModel.doLogin(email, password)
        }
    }

    private fun isFormValid(): Boolean {
        val email = binding.layoutLogin.etEmail.text.toString().trim()
        val password = binding.layoutLogin.etPassword.text.toString().trim()

        return checkEmailValidation(email) &&
                checkPasswordValidation(password, binding.layoutLogin.tilPassword)
    }

    private fun checkEmailValidation(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.layoutLogin.tilEmail.isErrorEnabled = true
            binding.layoutLogin.tilEmail.error = getString(R.string.text_error_email_empty)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutLogin.tilEmail.isErrorEnabled = true
            binding.layoutLogin.tilEmail.error = getString(R.string.text_error_email_invalid)
            false
        } else {
            binding.layoutLogin.tilEmail.isErrorEnabled = false
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

    private fun navigateToForgotPass() {
        startActivity(
            Intent(this, ForgotPassActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
    }
}