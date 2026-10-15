package com.example.proyecto_evaluacion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.proyecto_evaluacion.validation.OrganizacionValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrganizacionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OrganizacionUiState())
    val uiState: StateFlow<OrganizacionUiState> = _uiState.asStateFlow()

    fun cambiarNombre(valor: String) {
        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = OrganizacionValidator.validarNombre(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarTipo(valor: String) {
        _uiState.update {
            it.copy(
                tipo = valor,
                errorTipo = OrganizacionValidator.validarTipo(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarRubro(valor: String) {
        _uiState.update {
            it.copy(
                rubro = valor,
                errorRubro = OrganizacionValidator.validarRubro(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarComuna(valor: String) {
        _uiState.update {
            it.copy(
                comuna = valor,
                errorComuna = OrganizacionValidator.validarComuna(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarRegion(valor: String) {
        _uiState.update {
            it.copy(
                region = valor,
                errorRegion = OrganizacionValidator.validarRegion(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarSitioWeb(valor: String) {
        _uiState.update {
            it.copy(
                sitioWeb = valor,
                errorSitioWeb = OrganizacionValidator.validarSitioWeb(valor),
                guardadoExitoso = false
            )
        }
    }

    fun cambiarObservaciones(valor: String) {
        _uiState.update {
            it.copy(
                observaciones = valor,
                errorObservaciones = OrganizacionValidator.validarObservaciones(valor),
                guardadoExitoso = false
            )
        }
    }

    fun guardar() {
        val estado = _uiState.value

        val errorNombre = OrganizacionValidator.validarNombre(estado.nombre)
        val errorTipo = OrganizacionValidator.validarTipo(estado.tipo)
        val errorRubro = OrganizacionValidator.validarRubro(estado.rubro)
        val errorComuna = OrganizacionValidator.validarComuna(estado.comuna)
        val errorRegion = OrganizacionValidator.validarRegion(estado.region)
        val errorSitioWeb = OrganizacionValidator.validarSitioWeb(estado.sitioWeb)
        val errorObservaciones = OrganizacionValidator.validarObservaciones(estado.observaciones)

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorTipo = errorTipo,
                errorRubro = errorRubro,
                errorComuna = errorComuna,
                errorRegion = errorRegion,
                errorSitioWeb = errorSitioWeb,
                errorObservaciones = errorObservaciones,
                guardadoExitoso = false
            )
        }

        val formularioValido = listOf(
            errorNombre, errorTipo, errorRubro, errorComuna,
            errorRegion, errorSitioWeb, errorObservaciones
        ).all { it == null }

        if (formularioValido) {
            println("***** ORGANIZACIÓN *****")
            println("Nombre: ${estado.nombre}")
            println("Tipo: ${estado.tipo}")
            println("Rubro: ${estado.rubro}")
            println("Comuna: ${estado.comuna}")
            println("Región: ${estado.region}")
            println("Sitio web: ${estado.sitioWeb}")
            println("Observaciones: ${estado.observaciones}")

            // Limpia el formulario y activa el mensaje de confirmación
            _uiState.value = OrganizacionUiState(guardadoExitoso = true)
        }
    }
}