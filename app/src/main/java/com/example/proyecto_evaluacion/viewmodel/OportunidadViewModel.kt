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
import com.example.proyecto_evaluacion.validation.ReglasEstado
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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

    // Carga una oportunidad existente en el formulario, si el usuario tiene permiso para editarla
    fun cargarParaEdicion(id: Int) {
        // Si ya se cargó (por ejemplo, tras girar la pantalla) no se vuelve a pisar lo escrito
        if (_uiState.value.editandoId == id) return

        _uiState.update { it.copy(editandoId = id, cargandoEdicion = true, mensajeBloqueo = null) }

        viewModelScope.launch {
            try {
                val oportunidad = repository.getOportunidadPorId(id)
                if (oportunidad == null) {
                    bloquear("No se encontró la oportunidad")
                    return@launch
                }

                val usuario = SessionManager.usuarioActual.value
                val permitido = ReglasEstado.puedeEditar(
                    usuario?.rol, usuario?.id, oportunidad.ejecutivoId, oportunidad.estado
                )
                if (!permitido) {
                    bloquear("No tienes permiso para editar esta oportunidad")
                    return@launch
                }

                val organizacion = repository.getOrganizaciones().first()
                    .firstOrNull { it.id == oportunidad.organizacionId }

                val contacto = oportunidad.contactoId?.let { contactoId ->
                    repository.getContactos(oportunidad.organizacionId).first()
                        .firstOrNull { it.id == contactoId }
                }

                _uiState.value = OportunidadUiState(
                    titulo = oportunidad.titulo,
                    descripcion = oportunidad.descripcion,
                    tipoEvento = oportunidad.tipoEvento,
                    prioridad = oportunidad.prioridad,
                    organizacionId = oportunidad.organizacionId,
                    organizacionNombre = organizacion?.nombre ?: "",
                    contactoId = contacto?.id,
                    contactoNombre = contacto?.let { "${it.nombre} (${it.cargo})" } ?: "Sin contacto",
                    fechaInicio = oportunidad.fechaEvento,
                    fechaTermino = oportunidad.fechaTermino,
                    horario = oportunidad.horario,
                    lugar = oportunidad.lugarEvento,
                    publico = oportunidad.publicoEsperado.toString(),
                    costo = oportunidad.monto.toLong().toString(),
                    editandoId = id
                )
            } catch (e: Exception) {
                bloquear("No se pudo cargar la oportunidad, intenta de nuevo")
            }
        }
    }

    private fun bloquear(mensaje: String) {
        _uiState.update { it.copy(cargandoEdicion = false, mensajeBloqueo = mensaje) }
    }

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

        val editandoId = estado.editandoId

        viewModelScope.launch {
            try {
                if (editandoId != null) {
                    // Edición: se parte de la oportunidad guardada para conservar estado,
                    // responsable, fecha de detección y foto, que no se editan aquí
                    val original = repository.getOportunidadPorId(editandoId)
                    val permitido = original != null && ReglasEstado.puedeEditar(
                        usuario.rol, usuario.id, original.ejecutivoId, original.estado
                    )

                    if (original == null || !permitido) {
                        _uiState.update {
                            it.copy(errorOperacion = "No tienes permiso para editar esta oportunidad")
                        }
                        return@launch
                    }

                    repository.actualizarOportunidad(
                        original.copy(
                            titulo = estado.titulo.trim(),
                            descripcion = estado.descripcion.trim(),
                            monto = estado.costo.trim().toDouble(),
                            prioridad = estado.prioridad,
                            fechaEvento = estado.fechaInicio.trim(),
                            fechaTermino = estado.fechaTermino.trim(),
                            horario = estado.horario.trim(),
                            lugarEvento = estado.lugar.trim(),
                            tipoEvento = estado.tipoEvento,
                            publicoEsperado = estado.publico.trim().toInt(),
                            organizacionId = organizacionId,
                            contactoId = estado.contactoId
                        )
                    )
                    _uiState.update { it.copy(guardadoExitoso = true, errorOperacion = null) }
                } else {
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
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No se pudo guardar la oportunidad, intenta de nuevo")
                }
            }
        }
    }
}