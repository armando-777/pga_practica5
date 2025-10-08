package com.armin.appalumi.app

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.R
import com.armin.appalumi.databinding.ActivityEditarProductoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarProductoBinding
    private var productoActual: JSONObject? = null
    private var productoId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del producto
        val productoJson = intent.getStringExtra("producto_json")
        if (productoJson != null) {
            productoActual = JSONObject(productoJson)
            cargarDatosProducto(productoActual!!)
        } else {
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Botón actualizar
        binding.btnActualizar.setOnClickListener {
            actualizarProducto()
        }

        // Botón eliminar
        binding.btnEliminar.setOnClickListener {
            mostrarDialogoEliminar()
        }
    }

    private fun cargarDatosProducto(producto: JSONObject) {
        try {
            productoId = producto.getInt("id")

            // Cargar nombre del perfil
            val nombrePerfil = producto.optString("perfil_nombre", "Sin perfil")
            binding.tvCategoria.text = "Perfil: $nombrePerfil"

            // Cargar campos
            binding.etNombre.setText(producto.getString("nombre"))
            binding.ladoA.setText(producto.optString("lado_a", ""))
            binding.ladoB.setText(producto.optString("lado_b", ""))
            binding.ladoC.setText(producto.optString("lado_c", ""))
            binding.ladoD.setText(producto.optString("lado_d", ""))
            binding.etPrecio.setText(producto.optString("precio", ""))
            binding.etStock.setText(producto.optInt("stock", 0).toString())
            binding.etDescripcion.setText(producto.optString("descripcion", ""))

            // Cargar imagen
            val imagenBase64 = producto.optString("imagen", "")
            if (imagenBase64.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        imageBytes, 0, imageBytes.size
                    )

                    val maxWidth = 800
                    val maxHeight = 600
                    val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                        val ratio = Math.min(
                            maxWidth.toFloat() / bitmap.width,
                            maxHeight.toFloat() / bitmap.height
                        )
                        val width = (bitmap.width * ratio).toInt()
                        val height = (bitmap.height * ratio).toInt()
                        android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
                    } else {
                        bitmap
                    }

                    binding.ivProducto.setImageBitmap(scaledBitmap)
                } catch (e: Exception) {
                    Log.e("EditarProducto", "Error cargando imagen: ${e.message}")
                    binding.ivProducto.setImageResource(R.drawable.producto)
                }
            } else {
                binding.ivProducto.setImageResource(R.drawable.producto)
            }

        } catch (e: Exception) {
            Log.e("EditarProducto", "Error cargando producto: ${e.message}", e)
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarProducto() {
        if (binding.etNombre.text.toString().isEmpty() ||
            binding.etPrecio.text.toString().isEmpty() ||
            binding.etStock.text.toString().isEmpty()) {
            Toast.makeText(this, "Complete los campos requeridos", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = mapOf(
            "id" to productoId.toString(),
            "nombre" to binding.etNombre.text.toString().trim(),
            "lado_a" to binding.ladoA.text.toString().trim(),
            "lado_b" to binding.ladoB.text.toString().trim(),
            "lado_c" to binding.ladoC.text.toString().trim(),
            "lado_d" to binding.ladoD.text.toString().trim(),
            "precio" to binding.etPrecio.text.toString().trim(),
            "stock" to binding.etStock.text.toString().trim(),
            "descripcion" to binding.etDescripcion.text.toString().trim()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val respuesta = ApiHelper.realizarPeticionPOST("/update_producto", datos)

            withContext(Dispatchers.Main) {
                if (respuesta != null) {
                    val status = respuesta.optString("status")
                    val message = respuesta.optString("message")

                    if (status == "success") {
                        Toast.makeText(
                            this@EditarProductoActivity,
                            "Producto actualizado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditarProductoActivity,
                            "Error: $message",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@EditarProductoActivity,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Está seguro que desea eliminar este producto?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto() {
        val datos = mapOf("id" to productoId.toString())

        CoroutineScope(Dispatchers.IO).launch {
            val respuesta = ApiHelper.realizarPeticionPOST("/delete_producto", datos)

            withContext(Dispatchers.Main) {
                if (respuesta != null) {
                    val status = respuesta.optString("status")
                    val message = respuesta.optString("message")

                    if (status == "success") {
                        Toast.makeText(
                            this@EditarProductoActivity,
                            "Producto eliminado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditarProductoActivity,
                            "Error: $message",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@EditarProductoActivity,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}