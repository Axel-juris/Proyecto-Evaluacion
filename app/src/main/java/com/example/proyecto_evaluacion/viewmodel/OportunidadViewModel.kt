package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.Contacto
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.validation.OportunidadValidator
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OportunidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)

    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    val organizaciones: StateFlow<List<Organizacion>> = repository.getOrganizaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contactos de la organización elegida; cambia solo cuando cambia la selección
    @OptIn(ExperimentalCoroutinesApi::class)
    val contactos: StateFlow<List<Contacto>> = _uiState
        .map { it.organizacionId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getContactos(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarTitulo(valor: String) {
        _uiState.update {
            it.copy(
                titulo = valor,
                errorTitulo = OportunidadValidator.validarNombre(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarDescripcion(valor: String) {
        _uiState.update {
            it.copy(
                descripcion = valor,
                errorDescripcion = OportunidadValidator.validarDescripcion(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarTipo(valor: String) {
        _uiState.update {
            it.copy(
                tipoEvento = valor,
                errorTipo = OportunidadValidator.validarTipo(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarPrioridad(valor: String) {
        _uiState.update {
            it.copy(
                prioridad = valor,
                errorPrioridad = OportunidadValidator.validarPrioridad(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun seleccionarOrganizacion(id: Int, nombre: String) {
        _uiState.update {
            it.copy(
                organizacionId = id,
                organizacionNombre = nombre,
                errorOrganizacion = OportunidadValidator.validarOrganizacion(nombre),
                // Al cambiar de organización se limpia el contacto elegido
                contactoId = null,
                contactoNombre = "",
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun seleccionarContacto(id: Int?, nombre: String) {
        _uiState.update {
            it.copy(
                contactoId = id,
                contactoNombre = nombre,
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarFechaInicio(valor: String) {
        _uiState.update {
            it.copy(
                fechaInicio = valor,
                errorFechaInicio = OportunidadValidator.validarFechaInicio(valor),
                // Si ya hay fecha de término, se revisa de nuevo la coherencia
                errorFechaTermino = if (it.fechaTermino.isNotEmpty()) {
                    OportunidadValidator.validarFechaTermino(valor, it.fechaTermino)
                } else {
                    it.errorFechaTermino
                },
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarFechaTermino(valor: String) {
        _uiState.update {
            it.copy(
                fechaTermino = valor,
                errorFechaTermino = OportunidadValidator.validarFechaTermino(it.fechaInicio, valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarHorario(valor: String) {
        _uiState.update {
            it.copy(
                horario = valor,
                errorHorario = OportunidadValidator.validarHorario(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarLugar(valor: String) {
        _uiState.update {
            it.copy(
                lugar = valor,
                errorLugar = OportunidadValidator.validarLugar(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarPublico(valor: String) {
        _uiState.update {
            it.copy(
                publico = valor,
                errorPublico = OportunidadValidator.validarPublico(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun cambiarCosto(valor: String) {
        _uiState.update {
            it.copy(
                costo = valor,
                errorCosto = OportunidadValidator.validarCosto(valor),
                errorOperacion = null,
                guardadoExitoso = false
            )
        }
    }

    fun guardar() {
        val estado = _uiState.value

        val errorTitulo = OportunidadValidator.validarNombre(estado.titulo)
        val errorDescripcion = OportunidadValidator.validarDescripcion(estado.descripcion)
        val errorTipo = OportunidadValidator.validarTipo(estado.tipoEvento)
        val errorPrioridad = OportunidadValidator.validarPrioridad(estado.prioridad)
        val errorOrganizacion = OportunidadValidator.validarOrganizacion(estado.organizacionNombre)
        val errorFechaInicio = OportunidadValidator.validarFechaInicio(estado.fechaInicio)
        val errorFechaTermino =
            OportunidadValidator.validarFechaTermino(estado.fechaInicio, estado.fechaTermino)
        val errorHorario = OportunidadValidator.validarHorario(estado.horario)
        val errorLugar = OportunidadValidator.validarLugar(estado.lugar)
        val errorPublico = OportunidadValidator.validarPublico(estado.publico)
        val errorCosto = OportunidadValidator.validarCosto(estado.costo)

        // Se muestran los errores pero los valores ingresados se mantienen
        _uiState.update {
            it.copy(
                errorTitulo = errorTitulo,
                errorDescripcion = errorDescripcion,
                errorTipo = errorTipo,
                errorPrioridad = errorPrioridad,
                errorOrganizacion = errorOrganizacion,
                errorFechaInicio = errorFechaInicio,
                errorFechaTermino = errorFechaTermino,
                errorHorario = errorHorario,
                errorLugar = errorLugar,
                errorPublico = errorPublico,
                errorCosto = errorCosto,
                errorOperacion = null,
                guardadoExitoso = false
            )
        }

        val formularioValido = listOf(
            errorTitulo, errorDescripcion, errorTipo, errorPrioridad, errorOrganizacion,
            errorFechaInicio, errorFechaTermino, errorHorario, errorLugar,
            errorPublico, errorCosto
        ).all { it == null }

        if (!formularioValido) return

        val organizacionId = estado.organizacionId ?: return

        val usuario = SessionManager.usuarioActual.value
        if (usuario == null) {
            _uiState.update {
                it.copy(errorOperacion = "Tu sesión no es válida, vuelve a iniciar sesión")
            }
            return
        }

        viewModelScope.launch {
            try {
                val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                repository.guardarOportunidad(
                    Oportunidad(
                        titulo = estado.titulo.trim(),
                        descripcion = estado.descripcion.trim(),
                        monto = estado.costo.trim().toDouble(),
                        estado = "NUEVA",
                        prioridad = estado.prioridad,
                        fechaCreacion = hoy,
                        fechaEvento = estado.fechaInicio.trim(),
                        fechaTermino = estado.fechaTermino.trim(),
                        horario = estado.horario.trim(),
                        lugarEvento = estado.lugar.trim(),
                        tipoEvento = estado.tipoEvento,
                        publicoEsperado = estado.publico.trim().toInt(),
                        organizacionId = organizacionId,
                        contactoId = estado.contactoId,
                        // Ejecutivo: queda asignada a él. Supervisor: queda sin asignar.
                        ejecutivoId = if (usuario.rol == "EJECUTIVO") usuario.id else null
                    )
                )
                _uiState.value = OportunidadUiState(guardadoExitoso = true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No se pudo guardar la oportunidad, intenta de nuevo")
                }
            }
        }
    }
}