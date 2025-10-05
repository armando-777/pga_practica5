package com.armin.appalumi.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import java.io.IOException
import java.net.SocketTimeoutException

object ApiHelper {
    // Cambia esta IP por la IP de tu computadora donde corre Flask
    private const val API_BASE_URL = "http://192.168.0.13:5000"
    // private const val API_BASE_URL = "http://10.0.2.2:5000" // Para emulador Android Studio
    private const val TAG = "ApiHelper"

    fun realizarPeticionPOST(endpoint: String, datos: Map<String, String>): JSONObject? {
        var conexion: HttpURLConnection? = null

        return try {
            val url = URL("$API_BASE_URL$endpoint")
            conexion = url.openConnection() as HttpURLConnection

            Log.d(TAG, "=== INICIANDO PETICIÓN POST ===")
            Log.d(TAG, "URL completa: $API_BASE_URL$endpoint")
            Log.d(TAG, "Datos a enviar: $datos")

            // Configuración de la conexión
            conexion.apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                connectTimeout = 30000 // 30 segundos
                readTimeout = 30000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "AluminovaApp/1.0")
            }

            // Convertir datos a JSON
            val jsonObject = JSONObject(datos)
            val jsonString = jsonObject.toString()
            Log.d(TAG, "JSON generado: $jsonString")

            // Enviar datos
            conexion.outputStream.use { outputStream ->
                val bytes = jsonString.toByteArray(Charsets.UTF_8)
                outputStream.write(bytes)
                outputStream.flush()
                Log.d(TAG, "Datos enviados correctamente")
            }

            // Obtener respuesta
            val responseCode = conexion.responseCode
            val responseMessage = conexion.responseMessage
            Log.d(TAG, "Código de respuesta: $responseCode")
            Log.d(TAG, "Mensaje de respuesta: $responseMessage")

            // Leer respuesta
            val inputStream = if (responseCode in 200..299) {
                conexion.inputStream
            } else {
                conexion.errorStream ?: conexion.inputStream
            }

            val response = inputStream?.bufferedReader()?.use { reader ->
                reader.readText()
            } ?: ""

            Log.d(TAG, "Respuesta del servidor: $response")

            if (response.isBlank()) {
                Log.e(TAG, "Respuesta vacía del servidor")
                return null
            }

            // Intentar parsear como JSON
            try {
                JSONObject(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear JSON: ${e.message}")
                Log.e(TAG, "Respuesta no válida: $response")
                null
            }

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout en la conexión: ${e.message}")
            null
        } catch (e: IOException) {
            Log.e(TAG, "Error de E/O: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}", e)
            null
        } finally {
            conexion?.disconnect()
            Log.d(TAG, "=== FIN PETICIÓN POST ===")
        }
    }

    fun realizarPeticionGET(endpoint: String): String? {
        var conexion: HttpURLConnection? = null

        return try {
            val url = URL("$API_BASE_URL$endpoint")
            conexion = url.openConnection() as HttpURLConnection

            Log.d(TAG, "=== INICIANDO PETICIÓN GET ===")
            Log.d(TAG, "URL completa: $API_BASE_URL$endpoint")

            conexion.apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "AluminovaApp/1.0")
            }

            val responseCode = conexion.responseCode
            val responseMessage = conexion.responseMessage
            Log.d(TAG, "Código de respuesta GET: $responseCode")
            Log.d(TAG, "Mensaje de respuesta GET: $responseMessage")

            if (responseCode !in 200..299) {
                Log.e(TAG, "Error HTTP: $responseCode - $responseMessage")
                val errorStream = conexion.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e(TAG, "Respuesta de error: $errorResponse")
                return null
            }

            val inputStream = conexion.inputStream
            val response = inputStream?.bufferedReader()?.use { reader ->
                reader.readText()
            } ?: ""

            Log.d(TAG, "Respuesta GET exitosa (primeros 500 chars): ${response.take(500)}")

            if (response.isBlank()) {
                Log.e(TAG, "Respuesta vacía del servidor")
                return null
            }

            response

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout en petición GET: ${e.message}")
            null
        } catch (e: IOException) {
            Log.e(TAG, "Error de E/O en GET: ${e.message}")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en GET: ${e.message}", e)
            e.printStackTrace()
            null
        } finally {
            conexion?.disconnect()
            Log.d(TAG, "=== FIN PETICIÓN GET ===")
        }
    }

    // Función para verificar conectividad con Flask
    fun verificarConectividad(): Boolean {
        return try {
            val url = URL("$API_BASE_URL/test")
            val conexion = url.openConnection() as HttpURLConnection

            Log.d(TAG, "=== VERIFICANDO CONECTIVIDAD ===")
            Log.d(TAG, "URL de prueba: $API_BASE_URL/test")

            conexion.apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("User-Agent", "AluminovaApp/1.0")
            }

            val responseCode = conexion.responseCode
            Log.d(TAG, "Test de conectividad - Código: $responseCode")

            val respuesta = conexion.inputStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.d(TAG, "Test de conectividad - Respuesta: $respuesta")

            conexion.disconnect()

            val conectado = responseCode in 200..299
            Log.d(TAG, "Estado de conectividad: ${if (conectado) "CONECTADO" else "NO CONECTADO"}")
            conectado

        } catch (e: Exception) {
            Log.e(TAG, "Error de conectividad: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    // Función para obtener la IP actual configurada
    fun obtenerIPConfigurada(): String {
        return API_BASE_URL
    }
}