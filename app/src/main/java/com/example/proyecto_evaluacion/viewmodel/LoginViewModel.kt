package com.example.proyecto_evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.validation.LoginValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: CrmRepository = CrmRepository.instancia
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun cambiarCorreo(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = LoginValidator.validarCorreo(valor),
                errorLogin = null
            )
        }
    }

    fun cambiarClave(valor: String) {
        _uiState.update {
            it.copy(
                clave = valor,
                errorClave = LoginValidator.validarClave(valor),
                errorLogin = null
            )
        }
    }

    fun ingresar(onExitoso: () -> Unit) {
        val estado = _uiState.value

        val errorCorreo = LoginValidator.validarCorreo(estado.correo)
        val errorClave = LoginValidator.validarClave(estado.clave)

        _uiState.update {
            it.copy(errorCorreo = errorCorreo, errorClave = errorClave, errorLogin = null)
        }

        if (errorCorreo != null || errorClave != null) return

        _uiState.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                val usuario = repository.login(estado.correo.trim(), estado.clave)
                if (usuario != null) {
                    SessionManager.iniciarSesion(usuario)
                    _uiState.update { it.copy(cargando = false, clave = "") }
                    onExitoso()
                } else {
                    _uiState.update {
                        it.copy(cargando = false, errorLogin = "Correo o clave incorrectos")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin conexión, servidor apagado o error interno
                _uiState.update {
                    it.copy(cargando = false, errorLogin = e.message ?: "No se pudo iniciar sesión")
                }
            }
        }
    }
}