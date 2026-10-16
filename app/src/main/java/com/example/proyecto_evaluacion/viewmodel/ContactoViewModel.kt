package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.Contacto
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.validation.ContactoValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ContactoUiState())
    val uiState: StateFlow<ContactoUiState> = _uiState.asStateFlow()

    // Organizaciones guardadas en Room, para el menú desplegable
    val organizaciones: StateFlow<List<Organizacion>> = repository.getOrganizaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contactos de la organización seleccionada: cambia solo cuando cambia la selección
    @OptIn(ExperimentalCoroutinesApi::class)
    val contactos: StateFlow<List<Contacto>> = _uiState
        .map { it.organizacionId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getContactos(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarOrganizacion(id: Int, nombre: String) {
        _uiState.update {
            it.copy(
                organizacionId = id,
                organizacionNombre = nombre,
                errorOrganizacion = ContactoValidator.validarOrganizacion(nombre),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarNombre(valor: String) {
        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = ContactoValidator.validarNombre(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarCargo(valor: String) {
        _uiState.update {
            it.copy(
                cargo = valor,
                errorCargo = ContactoValidator.validarCargo(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarArea(valor: String) {
        _uiState.update {
            it.copy(
                area = valor,
                errorArea = ContactoValidator.validarArea(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = ContactoValidator.validarCorreo(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarTelefono(valor: String) {
        _uiState.update {
            it.copy(
                telefono = valor,
                errorTelefono = ContactoValidator.validarTelefono(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarMedio(valor: String) {
        _uiState.update {
            it.copy(
                medio = valor,
                errorMedio = ContactoValidator.validarMedio(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarObservaciones(valor: String) {
        _uiState.update {
            it.copy(
                observaciones = valor,
                errorObservaciones = ContactoValidator.validarObservaciones(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun guardar() {
        val estado = _uiState.value

        val errorOrganizacion = ContactoValidator.validarOrganizacion(estado.organizacionNombre)
        val errorNombre = ContactoValidator.validarNombre(estado.nombre)
        val errorCargo = ContactoValidator.validarCargo(estado.cargo)
        val errorArea = ContactoValidator.validarArea(estado.area)
        val errorCorreo = ContactoValidator.validarCorreo(estado.correo)
        val errorTelefono = ContactoValidator.validarTelefono(estado.telefono)
        val errorMedio = ContactoValidator.validarMedio(estado.medio)
        val errorObservaciones = ContactoValidator.validarObservaciones(estado.observaciones)

        // Se muestran los errores pero los valores ingresados se mantienen
        _uiState.update {
            it.copy(
                errorOrganizacion = errorOrganizacion,
                errorNombre = errorNombre,
                errorCargo = errorCargo,
                errorArea = errorArea,
                errorCorreo = errorCorreo,
                errorTelefono = errorTelefono,
                errorMedio = errorMedio,
                errorObservaciones = errorObservaciones,
                errorOperacion = null,
                guardadoExitoso = false
            )
        }

        val formularioValido = listOf(
            errorOrganizacion, errorNombre, errorCargo, errorArea,
            errorCorreo, errorTelefono, errorMedio, errorObservaciones
        ).all { it == null }

        if (!formularioValido) return

        val organizacionId = estado.organizacionId ?: return

        viewModelScope.launch {
            try {
                repository.guardarContacto(
                    Contacto(
                        organizacionId = organizacionId,
                        nombre = estado.nombre.trim(),
                        cargo = estado.cargo.trim(),
                        area = estado.area.trim(),
                        correo = estado.correo.trim(),
                        telefono = estado.telefono.trim(),
                        medioPreferente = estado.medio.trim(),
                        observaciones = estado.observaciones.trim()
                    )
                )
                // Limpia el formulario pero conserva la organización, para agregar otro contacto a la misma
                _uiState.value = ContactoUiState(
                    organizacionId = estado.organizacionId,
                    organizacionNombre = estado.organizacionNombre,
                    guardadoExitoso = true
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No se pudo guardar el contacto, intenta de nuevo")
                }
            }
        }
    }
}