package com.librompaz.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Perfil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class AsistenciaRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: AsistenciaRepository

    private val docente = MockData.usuarios.first { it.perfil == Perfil.DOCENTE }

    @Before
    fun crearBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = AsistenciaRepository(db)
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun guardarYLeerDevuelveLosMismosEstados() = runBlocking {
        val estados = mapOf(
            "E3A-1" to EstadoAsistencia.PRESENTE,
            "E3A-2" to EstadoAsistencia.AUSENTE
        )

        repo.guardar("C3A", "2026-10-01", estados, docente, "Asistencia 3° Básico A, 2026-10-01")

        assertEquals(estados, repo.asistenciaGuardada("C3A", "2026-10-01"))
    }


    @Test
    fun guardarDosVecesNoDuplicaYConservaElId() = runBlocking {
        val fecha = "2026-10-01"
        val detalle = "Asistencia 3° Básico A, $fecha"
        repo.guardar(
            "C3A", fecha,
            mapOf("E3A-1" to EstadoAsistencia.PRESENTE, "E3A-2" to EstadoAsistencia.PRESENTE),
            docente, detalle
        )
        val primeros = db.asistenciaDao().delCursoEnFecha("C3A", fecha)

        val accion = repo.guardar(
            "C3A", fecha,
            mapOf("E3A-1" to EstadoAsistencia.PRESENTE, "E3A-2" to EstadoAsistencia.AUSENTE),
            docente, detalle
        )
        val segundos = db.asistenciaDao().delCursoEnFecha("C3A", fecha)

        assertEquals("MODIFICAR", accion)
        assertEquals(2, segundos.size)
        assertEquals(primeros.map { it.id }.toSet(), segundos.map { it.id }.toSet())
        assertEquals("AUSENTE", segundos.first { it.estudianteId == "E3A-2" }.estado)
    }

    @Test
    fun cadaGuardadoDejaUnaAuditoriaConLaFirmaDelUsuario() = runBlocking {
        val fecha = "2026-10-01"
        val detalle = "Asistencia 3° Básico A, $fecha"
        repo.guardar("C3A", fecha, mapOf("E3A-1" to EstadoAsistencia.PRESENTE), docente, detalle)
        repo.guardar("C3A", fecha, mapOf("E3A-1" to EstadoAsistencia.AUSENTE), docente, detalle)

        val auditoria = repo.auditoria.first()

        assertEquals(2, auditoria.size)
        assertEquals(setOf("CREAR", "MODIFICAR"), auditoria.map { it.accion }.toSet())
        assertTrue(auditoria.all { it.usuario == docente.nombre && it.perfil == Perfil.DOCENTE })
    }

    @Test
    fun losDatosSobrevivenAlCerrarYReabrirLaBase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val nombre = "prueba_persistencia.db"
        context.deleteDatabase(nombre)
        val estados = mapOf("E3A-1" to EstadoAsistencia.AUSENTE)

        val primera = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        AsistenciaRepository(primera)
            .guardar("C3A", "2026-10-01", estados, docente, "Asistencia 3° Básico A, 2026-10-01")
        primera.close()

        val segunda = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        val leidos = AsistenciaRepository(segunda).asistenciaGuardada("C3A", "2026-10-01")
        segunda.close()
        context.deleteDatabase(nombre)

        assertEquals(estados, leidos)
    }
}