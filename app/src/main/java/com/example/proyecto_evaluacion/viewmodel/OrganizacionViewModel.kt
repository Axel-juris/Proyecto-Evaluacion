package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.validation.OrganizacionValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrganizacionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)

    private val _uiState = MutableStateFlow(OrganizacionUiState())
    val uiState: StateFlow<OrganizacionUiState> = _uiState.asStateFlow()

    // Lista que viene de Room y se actualiza sola cuando se guarda una organización
    val organizaciones: StateFlow<List<Organizacion>> = repository.getOrganizaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarNombre(valor: String) {
        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = OrganizacionValidator.validarNombre(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarTipo(valor: String) {
        _uiState.update {
            it.copy(
                tipo = valor,
                errorTipo = OrganizacionValidator.validarTipo(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarRubro(valor: String) {
        _uiState.update {
            it.copy(
                rubro = valor,
                errorRubro = OrganizacionValidator.validarRubro(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarComuna(valor: String) {
        _uiState.update {
            it.copy(
                comuna = valor,
                errorComuna = OrganizacionValidator.validarComuna(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarRegion(valor: String) {
        _uiState.update {
            it.copy(
                region = valor,
                errorRegion = OrganizacionValidator.validarRegion(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarSitioWeb(valor: String) {
        _uiState.update {
            it.copy(
                sitioWeb = valor,
                errorSitioWeb = OrganizacionValidator.validarSitioWeb(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarObservaciones(valor: String) {
        _uiState.update {
            it.copy(
                observaciones = valor,
                errorObservaciones = OrganizacionValidator.validarObservaciones(valor),
                errorOperacion = null,
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

        // Se muestran los errores pero los valores ingresados se mantienen
        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorTipo = errorTipo,
                errorRubro = errorRubro,
                errorComuna = errorComuna,
                errorRegion = errorRegion,
                errorSitioWeb = errorSitioWeb,
                errorObservaciones = errorObservaciones,
                errorOperacion = null,
                guardadoExitoso = false
            )
        }

        val formularioValido = listOf(
            errorNombre, errorTipo, errorRubro, errorComuna,
            errorRegion, errorSitioWeb, errorObservaciones
        ).all { it == null }

        if (!formularioValido) return

        viewModelScope.launch {
            try {
                // Guarda el tipo con la escritura oficial de la lista, sin importar mayúsculas
                val tipoOficial = OrganizacionValidator.tiposValidos
                    .first { it.equals(estado.tipo.trim(), ignoreCase = true) }

                repository.guardarOrganizacion(
                    Organizacion(
                        nombre = estado.nombre.trim(),
                        tipo = tipoOficial,
                        rubro = estado.rubro.trim(),
                        comuna = estado.comuna.trim(),
                        region = estado.region.trim(),
                        sitioWeb = estado.sitioWeb.trim(),
                        observaciones = estado.observaciones.trim()
                    )
                )
                _uiState.value = OrganizacionUiState(guardadoExitoso = true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No se pudo guardar la organización, intenta de nuevo")
                }
            }
        }
    }
}