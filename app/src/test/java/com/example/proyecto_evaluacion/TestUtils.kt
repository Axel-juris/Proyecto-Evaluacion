package com.example.proyecto_evaluacion

import com.example.proyecto_evaluacion.data.Contacto
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.data.Seguimiento
import com.example.proyecto_evaluacion.data.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Reemplaza Dispatchers.Main (que no existe en pruebas JVM) para poder usar viewModelScope. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/** Mantiene un colector activo para que los StateFlow con WhileSubscribed publiquen valores. */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.mantenerActivo(flujo: Flow<T>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flujo.collect { } }
}

/** Espera (tiempo real) a que se cumpla una condición; sirve para código que salta a Dispatchers.IO. */
fun esperarHasta(timeoutMs: Long = 3000, condicion: () -> Boolean) {
    val limite = System.currentTimeMillis() + timeoutMs
    while (!condicion() && System.currentTimeMillis() < limite) Thread.sleep(10)
}

/** Ejecuta [bloque] y devuelve la excepción esperada; falla si no se lanza o es de otro tipo. */
suspend inline fun <reified E : Throwable> esperarError(bloque: suspend () -> Unit): E {
    try {
        bloque()
    } catch (e: Throwable) {
        if (e is E) return e
        throw e
    }
    throw AssertionError("Se esperaba una excepción ${E::class.simpleName}")
}

/** Fecha dd/MM/yyyy desplazada [dias] días desde hoy (negativo = pasado). */
fun fechaRelativa(dias: Int): String {
    val calendario = Calendar.getInstance()
    calendario.add(Calendar.DAY_OF_YEAR, dias)
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendario.time)
}

fun httpError(codigo: Int) = HttpException(Response.error<Any>(codigo, "{}".toResponseBody(null)))

fun respuestaError(codigo: Int): Response<Void> = Response.error(codigo, "{}".toResponseBody(null))

fun respuestaOk(): Response<Void> = Response.success(null)

// ---- Datos de prueba ----

val SUPERVISOR = Usuario(id = 1, nombre = "Sofía Supervisora", correo = "sofia@duoc.cl", rol = "SUPERVISOR")
val EJECUTIVO = Usuario(id = 2, nombre = "Eva Ejecutiva", correo = "eva@duoc.cl", rol = "EJECUTIVO")
val OTRO_EJECUTIVO = Usuario(id = 3, nombre = "Omar Ejecutivo", correo = "omar@duoc.cl", rol = "EJECUTIVO")

fun organizacion(id: Int = 1, nombre: String = "Acme SpA") = Organizacion(
    id = id, nombre = nombre, tipo = "Empresa", rubro = "Retail",
    comuna = "Santiago", region = "Metropolitana"
)

fun contacto(id: Int = 7, organizacionId: Int = 1) = Contacto(
    id = id, organizacionId = organizacionId, nombre = "Ana Pérez", cargo = "Jefa de Eventos",
    area = "Eventos", correo = "ana@acme.cl", telefono = "+56912345678", medioPreferente = "Correo"
)

fun oportunidad(
    id: Int = 5,
    titulo: String = "Feria de beneficios",
    estado: String = "NUEVA",
    prioridad: String = "MEDIA",
    ejecutivoId: Int? = 2,
    organizacionId: Int = 1,
    contactoId: Int? = null,
    fechaEvento: String = "15/03/2030",
    fotoUri: String? = null
) = Oportunidad(
    id = id, titulo = titulo, descripcion = "Descripción de prueba", monto = 1000.0,
    estado = estado, prioridad = prioridad, fechaCreacion = "01/01/2030",
    fechaEvento = fechaEvento, fechaTermino = fechaEvento, horario = "09:30",
    lugarEvento = "Centro", tipoEvento = "Feria empresarial", publicoEsperado = 100,
    organizacionId = organizacionId, contactoId = contactoId, ejecutivoId = ejecutivoId,
    fotoUri = fotoUri
)

fun seguimiento(
    id: Int = 1,
    oportunidadId: Int = 5,
    ejecutivoId: Int = 2,
    proxima: String? = null
) = Seguimiento(
    id = id, oportunidadId = oportunidadId, ejecutivoId = ejecutivoId, fecha = "01/01/2030",
    tipoGestion = "LLAMADA", observacion = "Se llamó al contacto", proximaGestionFecha = proxima
)