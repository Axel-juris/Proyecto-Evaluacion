package com.example.proyecto_evaluacion.viewmodel

import androidx.lifecycle.ViewModel
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

class OrganizacionViewModel(
    private val repository: CrmRepository = CrmRepository.instancia
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrganizacionUiState())
    val uiState: StateFlow<OrganizacionUiState> = _uiState.asStateFlow()

    // Lista que viene del microservicio y se actualiza sola al guardar, editar o eliminar
    val organizaciones: StateFlow<List<Organizacion>> = repository.getOrganizaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aplica un cambio al estado y limpia los avisos anteriores
    private fun actualizar(cambio: (OrganizacionUiState) -> OrganizacionUiState) {
        _uiState.update {
            cambio(it).copy(
                errorOperacion = null,
                guardadoExitoso = false,
                mensajeExito = null
            )
        }
    }

    fun cambiarNombre(valor: String) {
        actualizar { it.copy(nombre = valor, errorNombre = OrganizacionValidator.validarNombre(valor)) }
    }

    fun cambiarTipo(valor: String) {
        actualizar { it.copy(tipo = valor, errorTipo = OrganizacionValidator.validarTipo(valor)) }
    }

    fun cambiarRubro(valor: String) {
        actualizar { it.copy(rubro = valor, errorRubro = OrganizacionValidator.validarRubro(valor)) }
    }

    fun cambiarComuna(valor: String) {
        actualizar {
            it.copy(comuna = valor, errorComuna = OrganizacionValidator.validarComuna(valor))
        }
    }

    fun cambiarRegion(valor: String) {
        actualizar {
            it.copy(region = valor, errorRegion = OrganizacionValidator.validarRegion(valor))
        }
    }

    fun cambiarSitioWeb(valor: String) {
        actualizar {
            it.copy(sitioWeb = valor, errorSitioWeb = OrganizacionValidator.validarSitioWeb(valor))
        }
    }

    fun cambiarObservaciones(valor: String) {
        actualizar {
            it.copy(
                observaciones = valor,
                errorObservaciones = OrganizacionValidator.validarObservaciones(valor)
            )
        }
    }

    /** Carga una organización existente en el formulario para modificarla. */
    fun editar(organizacion: Organizacion) {
        _uiState.value = OrganizacionUiState(
            nombre = organizacion.nombre,
            tipo = organizacion.tipo,
            rubro = organizacion.rubro,
            comuna = organizacion.comuna,
            region = organizacion.region,
            sitioWeb = organizacion.sitioWeb,
            observaciones = organizacion.observaciones,
            organizacionEditandoId = organizacion.id
        )
    }

    /** Sale del modo edición y deja el formulario vacío. */
    fun cancelarEdicion() {
        _uiState.value = OrganizacionUiState()
    }

    /** Crea la organización, o la actualiza si el formulario está en modo edición. */
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
                guardadoExitoso = false,
                mensajeExito = null
            )
        }

        val formularioValido = listOf(
            errorNombre, errorTipo, errorRubro, errorComuna,
            errorRegion, errorSitioWeb, errorObservaciones
        ).all { it == null }

        if (!formularioValido) return

        val editandoId = estado.organizacionEditandoId

        viewModelScope.launch {
            try {
                // Guarda el tipo con la escritura oficial de la lista, sin importar mayúsculas
                val tipoOficial = OrganizacionValidator.tiposValidos
                    .first { it.equals(estado.tipo.trim(), ignoreCase = true) }

                val organizacion = Organizacion(
                    id = editandoId ?: 0,
                    nombre = estado.nombre.trim(),
                    tipo = tipoOficial,
                    rubro = estado.rubro.trim(),
                    comuna = estado.comuna.trim(),
                    region = estado.region.trim(),
                    sitioWeb = estado.sitioWeb.trim(),
                    observaciones = estado.observaciones.trim()
                )
                if (editandoId == null) {
                    repository.guardarOrganizacion(organizacion)
                } else {
                    repository.actualizarOrganizacion(organizacion)
                }
                _uiState.value = OrganizacionUiState(
                    guardadoExitoso = true,
                    mensajeExito = if (editandoId == null) {
                        "Organización guardada correctamente"
                    } else {
                        "Organización actualizada correctamente"
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = if (editandoId == null) {
                            "No se pudo guardar la organización, intenta de nuevo"
                        } else {
                            "No se pudo actualizar la organización, intenta de nuevo"
                        }
                    )
                }
            }
        }
    }

    /** Elimina una organización. Si justo se estaba editando, el formulario se limpia. */
    fun eliminar(organizacion: Organizacion) {
        viewModelScope.launch {
            try {
                repository.eliminarOrganizacion(organizacion.id)
                _uiState.update {
                    val base = if (it.organizacionEditandoId == organizacion.id) {
                        OrganizacionUiState()
                    } else {
                        it
                    }
                    base.copy(
                        errorOperacion = null,
                        guardadoExitoso = false,
                        mensajeExito = "Organización eliminada correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensajeExito = null,
                        errorOperacion = "No se pudo eliminar la organización. " +
                                "Si tiene contactos u oportunidades asociados, no se puede borrar."
                    )
                }
            }
        }
    }
}