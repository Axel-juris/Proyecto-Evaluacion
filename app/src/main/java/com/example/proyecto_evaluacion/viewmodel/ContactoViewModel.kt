package com.example.proyecto_evaluacion.viewmodel

import androidx.lifecycle.ViewModel
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

class ContactoViewModel(
    private val repository: CrmRepository = CrmRepository.instancia
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactoUiState())
    val uiState: StateFlow<ContactoUiState> = _uiState.asStateFlow()

    // Organizaciones registradas, para el menú desplegable
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

    // Aplica un cambio al estado y limpia los avisos anteriores
    private fun actualizar(cambio: (ContactoUiState) -> ContactoUiState) {
        _uiState.update {
            cambio(it).copy(
                errorOperacion = null,
                guardadoExitoso = false,
                mensajeExito = null
            )
        }
    }

    fun seleccionarOrganizacion(id: Int, nombre: String) {
        actualizar {
            it.copy(
                organizacionId = id,
                organizacionNombre = nombre,
                errorOrganizacion = ContactoValidator.validarOrganizacion(nombre),
                // Si se cambia de organización, se abandona la edición en curso
                contactoEditandoId = if (it.organizacionId == id) it.contactoEditandoId else null
            )
        }
    }

    fun cambiarNombre(valor: String) {
        actualizar { it.copy(nombre = valor, errorNombre = ContactoValidator.validarNombre(valor)) }
    }

    fun cambiarCargo(valor: String) {
        actualizar { it.copy(cargo = valor, errorCargo = ContactoValidator.validarCargo(valor)) }
    }

    fun cambiarArea(valor: String) {
        actualizar { it.copy(area = valor, errorArea = ContactoValidator.validarArea(valor)) }
    }

    fun cambiarCorreo(valor: String) {
        actualizar { it.copy(correo = valor, errorCorreo = ContactoValidator.validarCorreo(valor)) }
    }

    fun cambiarTelefono(valor: String) {
        actualizar {
            it.copy(telefono = valor, errorTelefono = ContactoValidator.validarTelefono(valor))
        }
    }

    fun cambiarMedio(valor: String) {
        actualizar { it.copy(medio = valor, errorMedio = ContactoValidator.validarMedio(valor)) }
    }

    fun cambiarObservaciones(valor: String) {
        actualizar {
            it.copy(
                observaciones = valor,
                errorObservaciones = ContactoValidator.validarObservaciones(valor)
            )
        }
    }

    /** Carga un contacto existente en el formulario para modificarlo. */
    fun editar(contacto: Contacto) {
        _uiState.update {
            ContactoUiState(
                organizacionId = contacto.organizacionId,
                organizacionNombre = it.organizacionNombre,
                nombre = contacto.nombre,
                cargo = contacto.cargo,
                area = contacto.area,
                correo = contacto.correo,
                telefono = contacto.telefono,
                medio = contacto.medioPreferente,
                observaciones = contacto.observaciones,
                contactoEditandoId = contacto.id
            )
        }
    }

    /** Sale del modo edición y deja el formulario vacío (conserva la organización). */
    fun cancelarEdicion() {
        _uiState.update {
            ContactoUiState(
                organizacionId = it.organizacionId,
                organizacionNombre = it.organizacionNombre
            )
        }
    }

    /** Crea el contacto, o lo actualiza si el formulario está en modo edición. */
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
                guardadoExitoso = false,
                mensajeExito = null
            )
        }

        val formularioValido = listOf(
            errorOrganizacion, errorNombre, errorCargo, errorArea,
            errorCorreo, errorTelefono, errorMedio, errorObservaciones
        ).all { it == null }

        if (!formularioValido) return

        val organizacionId = estado.organizacionId ?: return
        val editandoId = estado.contactoEditandoId

        viewModelScope.launch {
            try {
                val contacto = Contacto(
                    id = editandoId ?: 0,
                    organizacionId = organizacionId,
                    nombre = estado.nombre.trim(),
                    cargo = estado.cargo.trim(),
                    area = estado.area.trim(),
                    correo = estado.correo.trim(),
                    telefono = estado.telefono.trim(),
                    medioPreferente = estado.medio.trim(),
                    observaciones = estado.observaciones.trim()
                )
                if (editandoId == null) {
                    repository.guardarContacto(contacto)
                } else {
                    repository.actualizarContacto(contacto)
                }
                // Limpia el formulario pero conserva la organización, para seguir trabajando en ella
                _uiState.value = ContactoUiState(
                    organizacionId = estado.organizacionId,
                    organizacionNombre = estado.organizacionNombre,
                    guardadoExitoso = true,
                    mensajeExito = if (editandoId == null) {
                        "Contacto guardado correctamente"
                    } else {
                        "Contacto actualizado correctamente"
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = if (editandoId == null) {
                            "No se pudo guardar el contacto, intenta de nuevo"
                        } else {
                            "No se pudo actualizar el contacto, intenta de nuevo"
                        }
                    )
                }
            }
        }
    }

    /** Elimina un contacto. Si justo se estaba editando, el formulario se limpia. */
    fun eliminar(contacto: Contacto) {
        viewModelScope.launch {
            try {
                repository.eliminarContacto(contacto.id)
                _uiState.update {
                    val estabaEditandolo = it.contactoEditandoId == contacto.id
                    val base = if (estabaEditandolo) {
                        ContactoUiState(
                            organizacionId = it.organizacionId,
                            organizacionNombre = it.organizacionNombre
                        )
                    } else {
                        it
                    }
                    base.copy(
                        errorOperacion = null,
                        guardadoExitoso = false,
                        mensajeExito = "Contacto eliminado correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensajeExito = null,
                        errorOperacion = "No se pudo eliminar el contacto. " +
                                "Si está asociado a una oportunidad, no se puede borrar."
                    )
                }
            }
        }
    }
}