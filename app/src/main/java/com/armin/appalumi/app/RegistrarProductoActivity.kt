package com.armin.appalumi.app

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.R
import com.armin.appalumi.databinding.ActivityRegistrarProductoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class RegistrarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarProductoBinding
    private var perfilSeleccionado: JSONObject? = null
    private var listaPerfiles = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar conectividad y cargar perfiles
        verificarYCargarPerfiles()

        // Botón para seleccionar imagen/perfil
        binding.btnSeleccionarImagen.setOnClickListener {
            if (listaPerfiles.isNotEmpty()) {
                mostrarDialogoPerfiles()
            } else {
                Toast.makeText(this, "Cargando perfiles, espere...", Toast.LENGTH_SHORT).show()
                cargarPerfiles()
            }
        }

        // Botón registrar producto
        binding.btnRegistrar.setOnClickListener {
            registrarProducto()
        }
    }

    private fun verificarYCargarPerfiles() {
        CoroutineScope(Dispatchers.IO).launch {
            val conectado = ApiHelper.verificarConectividad()
            withContext(Dispatchers.Main) {
                if (conectado) {
                    Log.d("RegistrarProducto", "Servidor accesible")
                    cargarPerfiles()
                } else {
                    Log.e("RegistrarProducto", "Servidor no accesible")
                    Toast.makeText(
                        this@RegistrarProductoActivity,
                        "No se puede conectar al servidor en ${ApiHelper.obtenerIPConfigurada()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun cargarPerfiles() {
        Log.d("RegistrarProducto", "Iniciando carga de perfiles...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ApiHelper.realizarPeticionGET("/perfiles")
                Log.d("RegistrarProducto", "Respuesta recibida: $respuesta")

                withContext(Dispatchers.Main) {
                    if (respuesta.isNullOrBlank()) {
                        Log.e("RegistrarProducto", "Respuesta nula o vacía del servidor")
                        Toast.makeText(
                            this@RegistrarProductoActivity,
                            "Error: No se pudo conectar al servidor",
                            Toast.LENGTH_LONG
                        ).show()
                        return@withContext
                    }

                    try {
                        Log.d("RegistrarProducto", "Intentando parsear JSON...")
                        val jsonArray = JSONArray(respuesta)
                        listaPerfiles.clear()

                        for (i in 0 until jsonArray.length()) {
                            val perfil = jsonArray.getJSONObject(i)
                            val nombrePerfil = perfil.optString("nombre", "Sin nombre")
                            Log.d("RegistrarProducto", "Perfil $i: $nombrePerfil")
                            listaPerfiles.add(perfil)
                        }

                        Log.d("RegistrarProducto", "Total perfiles cargados: ${listaPerfiles.size}")

                        if (listaPerfiles.isEmpty()) {
                            Toast.makeText(
                                this@RegistrarProductoActivity,
                                "No hay perfiles disponibles en el sistema",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@RegistrarProductoActivity,
                                "${listaPerfiles.size} perfiles cargados",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: org.json.JSONException) {
                        Log.e("RegistrarProducto", "Error parseando JSON: ${e.message}", e)
                        Log.e("RegistrarProducto", "Respuesta recibida: $respuesta")
                        Toast.makeText(
                            this@RegistrarProductoActivity,
                            "Error: Respuesta inválida del servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Log.e("RegistrarProducto", "Error inesperado: ${e.message}", e)
                        Toast.makeText(
                            this@RegistrarProductoActivity,
                            "Error al procesar perfiles: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("RegistrarProducto", "Error en coroutine: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegistrarProductoActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun mostrarDialogoPerfiles() {
        val items = listaPerfiles.map { it.getString("nombre") }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Perfil")
            .setItems(items) { dialog, which ->
                perfilSeleccionado = listaPerfiles[which]
                cargarDatosPerfil(perfilSeleccionado!!)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarDatosPerfil(perfil: JSONObject) {
        try {
            // Actualizar nombre del perfil
            val nombrePerfil = perfil.getString("nombre")
            binding.tvCategoria.text = "Perfil: $nombrePerfil"
            binding.etNombre.setText(nombrePerfil)

            // Los lados A, B, C, D NO se autocompletan - el usuario los ingresa manualmente

            // Cargar imagen desde base64
            val imagenBase64 = perfil.optString("imagen", "")
            if (imagenBase64.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        imageBytes, 0, imageBytes.size
                    )
                    binding.ivProducto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("RegistrarProducto", "Error cargando imagen: ${e.message}")
                    binding.ivProducto.setImageResource(R.drawable.producto)
                }
            } else {
                binding.ivProducto.setImageResource(R.drawable.producto)
            }

            Toast.makeText(this, "Perfil seleccionado: $nombrePerfil", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("RegistrarProducto", "Error cargando datos del perfil: ${e.message}", e)
            Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
        }

    }

    private fun registrarProducto() {
        // Validar campos
        if (perfilSeleccionado == null) {
            Toast.makeText(this, "Debe seleccionar un perfil", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.etPrecio.text.toString().isEmpty() ||
            binding.etStock.text.toString().isEmpty()) {
            Toast.makeText(this, "Complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
            return
        }

        // Preparar datos
        val datos = mapOf(
            "perfil_id" to perfilSeleccionado!!.getInt("id").toString(),
            "nombre" to binding.etNombre.text.toString().trim(),
            "lado_a" to binding.ladoA.text.toString().trim(),
            "lado_b" to binding.ladoB.text.toString().trim(),
            "lado_c" to binding.ladoC.text.toString().trim(),
            "lado_d" to binding.ladoD.text.toString().trim(),
            "precio" to binding.etPrecio.text.toString().trim(),
            "stock" to binding.etStock.text.toString().trim(),
            "descripcion" to binding.etDescripcion.text.toString().trim()
        )

        // Enviar a la API
        CoroutineScope(Dispatchers.IO).launch {
            val respuesta = ApiHelper.realizarPeticionPOST("/add_producto", datos)

            withContext(Dispatchers.Main) {
                if (respuesta != null) {
                    val status = respuesta.optString("status")
                    val message = respuesta.optString("message")

                    if (status == "success") {
                        Toast.makeText(
                            this@RegistrarProductoActivity,
                            "Producto registrado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        limpiarFormulario()
                    } else {
                        Toast.makeText(
                            this@RegistrarProductoActivity,
                            "Error: $message",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RegistrarProductoActivity,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun limpiarFormulario() {
        perfilSeleccionado = null
        binding.tvCategoria.text = "Perfil: "
        binding.etNombre.text?.clear()
        binding.ladoA.text?.clear()
        binding.ladoB.text?.clear()
        binding.ladoC.text?.clear()
        binding.ladoD.text?.clear()
        binding.etPrecio.text?.clear()
        binding.etStock.text?.clear()
        binding.etDescripcion.text?.clear()
        binding.ivProducto.setImageResource(R.drawable.producto)
    }

    override fun onResume() {
        super.onResume()
        // Recargar perfiles si la lista está vacía
        if (listaPerfiles.isEmpty()) {
            cargarPerfiles()
        }
    }
}