package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.Contacto
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.data.Seguimiento
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.data.Usuario
import com.example.proyecto_evaluacion.fechas.Evidencias
import com.example.proyecto_evaluacion.validation.ReglasEstado
import com.example.proyecto_evaluacion.validation.SeguimientoValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleOportunidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)
    private val usuario = SessionManager.usuarioActual.value

    private val idOportunidad = MutableStateFlow<Int?>(null)
    private val _form = MutableStateFlow(EstadoFormulario())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val oportunidadFlow: Flow<Oportunidad?> = idOportunidad.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observarOportunidad(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val seguimientosFlow: Flow<List<Seguimiento>> = idOportunidad.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getSeguimientos(id)
    }

    private data class Datos(
        val oportunidad: Oportunidad?,
        val organizaciones: List<Organizacion>,
        val usuarios: List<Usuario>,
        val contactos: List<Contacto>,
        val seguimientos: List<Seguimiento>
    )

    private val datos: Flow<Datos> = combine(
        oportunidadFlow,
        repository.getOrganizaciones(),
        repository.getUsuarios(),
        repository.getTodosContactos(),
        seguimientosFlow
    ) { oportunidad, organizaciones, usuarios, contactos, seguimientos ->
        Datos(oportunidad, organizaciones, usuarios, contactos, seguimientos)
    }

    val uiState: StateFlow<DetalleOportunidadUiState> = combine(datos, _form) { d, form ->
        val oportunidad = d.oportunidad
        val rol = usuario?.rol
        val idUsuario = usuario?.id
        val esSupervisor = rol == "SUPERVISOR"

        if (oportunidad == null) {
            DetalleOportunidadUiState(
                cargando = idOportunidad.value == null,
                esSupervisor = esSupervisor,
                form = form
            )
        } else {
            // Restricción de acceso: el ejecutivo solo ve las oportunidades asignadas a él
            val autorizado = esSupervisor ||
                    (idUsuario != null && oportunidad.ejecutivoId == idUsuario)

            DetalleOportunidadUiState(
                cargando = false,
                oportunidad = oportunidad,
                organizacionNombre = d.organizaciones
                    .firstOrNull { it.id == oportunidad.organizacionId }?.nombre
                    ?: "Sin organización",
                contactoNombre = d.contactos
                    .firstOrNull { it.id == oportunidad.contactoId }
                    ?.let { "${it.nombre} (${it.cargo})" }
                    ?: "Sin contacto",
                responsable = d.usuarios
                    .firstOrNull { it.id == oportunidad.ejecutivoId }?.nombre
                    ?: "Sin asignar",
                ejecutivos = d.usuarios.filter { it.rol == "EJECUTIVO" },
                seguimientos = d.seguimientos.map { seguimiento ->
                    SeguimientoItem(
                        seguimiento = seguimiento,
                        autor = d.usuarios
                            .firstOrNull { it.id == seguimiento.ejecutivoId }?.nombre
                            ?: "Desconocido"
                    )
                },
                esSupervisor = esSupervisor,
                autorizado = autorizado,
                transicionesPermitidas = if (autorizado && rol != null) {
                    ReglasEstado.transicionesPermitidas(oportunidad.estado, rol)
                } else {
                    emptyList()
                },
                form = form
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DetalleOportunidadUiState()
    )

    fun cargar(id: Int) {
        idOportunidad.value = id
    }

    // Permite que la pantalla informe resultados de acciones nativas (cámara, calendario)
    fun informar(exito: String?, error: String?) {
        _form.update { it.copy(mensajeExito = exito, mensajeError = error) }
    }

    fun limpiarMensajes() {
        _form.update { it.copy(mensajeExito = null, mensajeError = null) }
    }

    fun guardarFoto(uriNueva: String) {
        val contexto = getApplication<Application>()
        val estado = uiState.value
        val oportunidad = estado.oportunidad

        if (oportunidad == null || !estado.autorizado) {
            Evidencias.eliminar(contexto, uriNueva)
            informar(null, "No tienes permiso para registrar evidencias en esta oportunidad")
            return
        }

        val uriAnterior = oportunidad.fotoUri

        viewModelScope.launch {
            try {
                repository.actualizarFotoOportunidad(oportunidad.id, uriNueva)
                // Se borra la foto anterior para no dejar archivos huérfanos
                withContext(Dispatchers.IO) { Evidencias.eliminar(contexto, uriAnterior) }
                informar("Evidencia fotográfica guardada correctamente", null)
            } catch (e: Exception) {
                withContext(Dispatchers.IO) { Evidencias.eliminar(contexto, uriNueva) }
                informar(null, "No se pudo guardar la foto, intenta de nuevo")
            }
        }
    }

    fun cambiarTipoGestion(valor: String) {
        _form.update {
            it.copy(
                tipoGestion = valor,
                errorTipo = SeguimientoValidator.validarTipo(valor),
                mensajeExito = null,
                mensajeError = null,
                seguimientoGuardado = false
            )
        }
    }

    fun cambiarObservacion(valor: String) {
        _form.update {
            it.copy(
                observacion = valor,
                errorObservacion = SeguimientoValidator.validarObservacion(valor),
                mensajeExito = null,
                mensajeError = null,
                seguimientoGuardado = false
            )
        }
    }

    fun cambiarProximaFecha(valor: String) {
        _form.update {
            it.copy(
                proximaFecha = valor,
                errorProximaFecha = SeguimientoValidator.validarProximaFecha(valor),
                mensajeExito = null,
                mensajeError = null,
                seguimientoGuardado = false
            )
        }
    }

    fun cambiarEstado(nuevoEstado: String) {
        val estado = uiState.value
        val oportunidad = estado.oportunidad ?: return

        // La regla se comprueba aquí y no solo en la pantalla
        if (!estado.autorizado || nuevoEstado !in estado.transicionesPermitidas) {
            informar(null, "No tienes permiso para este cambio de estado")
            return
        }

        viewModelScope.launch {
            try {
                repository.cambiarEstadoOportunidad(oportunidad.id, nuevoEstado)
                informar("Estado actualizado correctamente", null)
            } catch (e: Exception) {
                informar(null, "No se pudo actualizar el estado, intenta de nuevo")
            }
        }
    }

    fun asignar(ejecutivoId: Int) {
        val estado = uiState.value
        val oportunidad = estado.oportunidad ?: return

        // Solo el supervisor puede asignar o reasignar
        if (!estado.esSupervisor) {
            informar(null, "Solo el supervisor puede asignar responsables")
            return
        }

        viewModelScope.launch {
            try {
                repository.reasignarEjecutivo(oportunidad.id, ejecutivoId)
                informar("Responsable asignado correctamente", null)
            } catch (e: Exception) {
                informar(null, "No se pudo asignar el responsable, intenta de nuevo")
            }
        }
    }

    fun guardarSeguimiento() {
        val estado = uiState.value
        val oportunidad = estado.oportunidad ?: return
        val usuarioActual = usuario ?: return

        if (!estado.autorizado) {
            informar(null, "No tienes permiso para registrar gestiones aquí")
            return
        }

        val form = _form.value

        val errorTipo = SeguimientoValidator.validarTipo(form.tipoGestion)
        val errorObservacion = SeguimientoValidator.validarObservacion(form.observacion)
        val errorProximaFecha = SeguimientoValidator.validarProximaFecha(form.proximaFecha)

        // Se muestran los errores pero los valores ingresados se mantienen
        _form.update {
            it.copy(
                errorTipo = errorTipo,
                errorObservacion = errorObservacion,
                errorProximaFecha = errorProximaFecha,
                mensajeExito = null,
                mensajeError = null,
                seguimientoGuardado = false
            )
        }

        if (errorTipo != null || errorObservacion != null || errorProximaFecha != null) return

        viewModelScope.launch {
            try {
                val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                repository.guardarSeguimiento(
                    Seguimiento(
                        oportunidadId = oportunidad.id,
                        ejecutivoId = usuarioActual.id,
                        fecha = hoy,
                        tipoGestion = form.tipoGestion,
                        observacion = form.observacion.trim(),
                        proximaGestionFecha = form.proximaFecha.trim().ifEmpty { null }
                    )
                )
                _form.value = EstadoFormulario(
                    mensajeExito = "Gestión registrada correctamente",
                    seguimientoGuardado = true
                )
            } catch (e: Exception) {
                informar(null, "No se pudo guardar la gestión, intenta de nuevo")
            }
        }
    }
}