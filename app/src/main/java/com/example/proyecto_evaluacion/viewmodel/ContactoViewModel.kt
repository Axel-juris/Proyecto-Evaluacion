package com.example.proyecto_evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.proyecto_evaluacion.validation.ContactoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ContactoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ContactoUiState())
    val uiState: StateFlow<ContactoUiState> = _uiState.asStateFlow()

    fun cambiarNombre(valor: String) {
        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = ContactoValidator.validarNombre(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarOrganizacion(valor: String) {
        _uiState.update {
            it.copy(
                organizacion = valor,
                errorOrganizacion = ContactoValidator.validarOrganizacion(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarCargo(valor: String) {
        _uiState.update {
            it.copy(
                cargo = valor,
                errorCargo = ContactoValidator.validarCargo(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarArea(valor: String) {
        _uiState.update {
            it.copy(
                area = valor,
                errorArea = ContactoValidator.validarArea(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                errorCorreo = ContactoValidator.validarCorreo(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarTelefono(valor: String) {
        _uiState.update {
            it.copy(
                telefono = valor,
                errorTelefono = ContactoValidator.validarTelefono(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarMedio(valor: String) {
        _uiState.update {
            it.copy(
                medio = valor,
                errorMedio = ContactoValidator.validarMedio(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarObservaciones(valor: String) {
        _uiState.update {
            it.copy(
                observaciones = valor,
                errorObservaciones = ContactoValidator.validarObservaciones(valor),
                guardadoExitoso = false
            )
        }
    }

    fun guardar() {
        val estado = _uiState.value

        val errorNombre = ContactoValidator.validarNombre(estado.nombre)
        val errorOrganizacion = ContactoValidator.validarOrganizacion(estado.organizacion)
        val errorCargo = ContactoValidator.validarCargo(estado.cargo)
        val errorArea = ContactoValidator.validarArea(estado.area)
        val errorCorreo = ContactoValidator.validarCorreo(estado.correo)
        val errorTelefono = ContactoValidator.validarTelefono(estado.telefono)
        val errorMedio = ContactoValidator.validarMedio(estado.medio)
        val errorObservaciones = ContactoValidator.validarObservaciones(estado.observaciones)

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorOrganizacion = errorOrganizacion,
                errorCargo = errorCargo,
                errorArea = errorArea,
                errorCorreo = errorCorreo,
                errorTelefono = errorTelefono,
                errorMedio = errorMedio,
                errorObservaciones = errorObservaciones,
                guardadoExitoso = false
            )
        }

        val formularioValido = listOf(
            errorNombre, errorOrganizacion, errorCargo, errorArea,
            errorCorreo, errorTelefono, errorMedio, errorObservaciones
        ).all { it == null }

        if (formularioValido) {
            println("***** CONTACTO *****")
            println("Nombre: ${estado.nombre}")
            println("Organización: ${estado.organizacion}")
            println("Cargo: ${estado.cargo}")
            println("Área: ${estado.area}")
            println("Correo: ${estado.correo}")
            println("Teléfono: ${estado.telefono}")
            println("Medio preferente: ${estado.medio}")
            println("Observaciones: ${estado.observaciones}")

            _uiState.value = ContactoUiState(guardadoExitoso = true)
        }
    }
}