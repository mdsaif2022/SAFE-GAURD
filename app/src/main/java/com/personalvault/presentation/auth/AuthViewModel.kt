package com.personalvault.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalvault.domain.model.UserSession
import com.personalvault.domain.repository.AuthRepository
import com.personalvault.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AuthUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val session: UserSession? = null,
    val isSuccess: Boolean = false,
    val showForgotPasswordDialog: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

    init {
        authRepository.getCurrentSession()
            .onEach { session ->
                _uiState.value = _uiState.value.copy(session = session)
            }
            .launchIn(viewModelScope)
    }

    fun onFullNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            fullName = name,
            fullNameError = null,
            generalError = null
        )
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun onForgotPasswordClicked() {
        _uiState.value = _uiState.value.copy(showForgotPasswordDialog = true)
    }

    fun dismissForgotPasswordDialog() {
        _uiState.value = _uiState.value.copy(showForgotPasswordDialog = false)
    }

    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        var emailErr: String? = null
        var passErr: String? = null

        if (email.isBlank()) {
            emailErr = "Email address is required"
        } else if (!emailRegex.matches(email)) {
            emailErr = "Please enter a valid email address"
        }

        if (password.isBlank()) {
            passErr = "Password is required"
        } else if (password.length < 8) {
            passErr = "Password must be at least 8 characters long"
        }

        if (emailErr != null || passErr != null) {
            _uiState.value = currentState.copy(
                emailError = emailErr,
                passwordError = passErr
            )
            return
        }

        authRepository.login(email, password)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            generalError = null
                        )
                    }
                    is Resource.Success -> {
                        // Securely clear sensitive transient password data from memory state
                        _uiState.value = _uiState.value.copy(
                            password = "",
                            confirmPassword = "",
                            isLoading = false,
                            session = result.data,
                            isSuccess = true
                        )
                        onSuccess()
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun createAccount(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val fullName = currentState.fullName.trim()
        val email = currentState.email.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword

        var nameErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null
        var confirmPassErr: String? = null

        if (fullName.isBlank()) {
            nameErr = "Full name is required"
        } else if (fullName.length < 2) {
            nameErr = "Full name must be at least 2 characters"
        }

        if (email.isBlank()) {
            emailErr = "Email address is required"
        } else if (!emailRegex.matches(email)) {
            emailErr = "Please enter a valid email address"
        }

        if (password.isBlank()) {
            passErr = "Password is required"
        } else if (password.length < 8) {
            passErr = "Password must be at least 8 characters long"
        }

        if (confirmPassword.isBlank()) {
            confirmPassErr = "Please confirm your password"
        } else if (confirmPassword != password) {
            confirmPassErr = "Passwords do not match"
        }

        if (nameErr != null || emailErr != null || passErr != null || confirmPassErr != null) {
            _uiState.value = currentState.copy(
                fullNameError = nameErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmPassErr
            )
            return
        }

        authRepository.createAccount(email, password, fullName)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            generalError = null
                        )
                    }
                    is Resource.Success -> {
                        // Securely clear sensitive transient password data from memory state
                        _uiState.value = _uiState.value.copy(
                            password = "",
                            confirmPassword = "",
                            isLoading = false,
                            session = result.data,
                            isSuccess = true
                        )
                        onSuccess()
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            fullNameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        )
    }
}
