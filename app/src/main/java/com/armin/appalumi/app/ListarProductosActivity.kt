package com.armin.appalumi.app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.R
import com.armin.appalumi.databinding.ActivityListarProductosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ListarProductosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarProductosBinding
    private lateinit var listViewProductos: ListView
    private var listaProductos = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListarProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listViewProductos = binding.listViewProductos

        // Cargar productos al iniciar
        cargarProductos()
    }

    private fun cargarProductos() {
        CoroutineScope(Dispatchers.IO).launch {
            val respuesta = ApiHelper.realizarPeticionGET("/productos")

            withContext(Dispatchers.Main) {
                if (respuesta != null) {
                    try {
                        Log.d("ListarProductos", "Respuesta: $respuesta")

                        val jsonArray = JSONArray(respuesta)
                        listaProductos.clear()

                        for (i in 0 until jsonArray.length()) {
                            val producto = jsonArray.getJSONObject(i)
                            listaProductos.add(producto)
                        }

                        if (listaProductos.isEmpty()) {
                            Toast.makeText(
                                this@ListarProductosActivity,
                                "No hay productos registrados",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val adapter = ProductoAdapter(listaProductos)
                            listViewProductos.adapter = adapter

                            Toast.makeText(
                                this@ListarProductosActivity,
                                "Total productos: ${listaProductos.size}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Log.e("ListarProductos", "Error: ${e.message}", e)
                        Toast.makeText(
                            this@ListarProductosActivity,
                            "Error al cargar productos: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ListarProductosActivity,
                        "Error de conexión con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    inner class ProductoAdapter(private val productos: List<JSONObject>) : BaseAdapter() {

        override fun getCount(): Int = productos.size

        override fun getItem(position: Int): Any = productos[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@ListarProductosActivity)
                .inflate(R.layout.item_producto, parent, false)

            try {
                val producto = productos[position]

                val ivImagen = view.findViewById<ImageView>(R.id.ivProductoImagen)
                val tvNombre = view.findViewById<TextView>(R.id.tvProductoNombre)
                val tvDimensiones = view.findViewById<TextView>(R.id.tvProductoDimensiones)
                val tvPrecio = view.findViewById<TextView>(R.id.tvProductoPrecio)
                val tvStock = view.findViewById<TextView>(R.id.tvProductoStock)
                val tvDescripcion = view.findViewById<TextView>(R.id.tvProductoDescripcion)
                val btnEditar = view.findViewById<ImageButton>(R.id.btnEditarProducto)

                // Nombre del producto
                tvNombre.text = producto.getString("nombre")

                // Construir dimensiones solo con lados que tienen valores
                val dimensiones = construirDimensiones(producto)
                if (dimensiones.isNotEmpty()) {
                    tvDimensiones.text = dimensiones
                    tvDimensiones.visibility = View.VISIBLE
                } else {
                    tvDimensiones.visibility = View.GONE
                }

                tvPrecio.text = "Bs. ${producto.optString("precio", "0.00")}"
                tvStock.text = "Stock: ${producto.optInt("stock", 0)} unidades"

                val descripcion = producto.optString("descripcion", "").trim()
                if (descripcion.isNotEmpty()) {
                    tvDescripcion.text = descripcion
                    tvDescripcion.visibility = View.VISIBLE
                } else {
                    tvDescripcion.visibility = View.GONE
                }

                // Cargar imagen con escalado
                val imagenBase64 = producto.optString("imagen", "")
                if (imagenBase64.isNotEmpty()) {
                    try {
                        val imageBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                            imageBytes, 0, imageBytes.size
                        )

                        val maxWidth = 300
                        val maxHeight = 300
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

                        ivImagen.setImageBitmap(scaledBitmap)
                    } catch (e: Exception) {
                        Log.e("ProductoAdapter", "Error cargando imagen: ${e.message}")
                        ivImagen.setImageResource(R.drawable.producto)
                    }
                } else {
                    ivImagen.setImageResource(R.drawable.producto)
                }

                // Botón editar
                btnEditar.setOnClickListener {
                    val intent = Intent(this@ListarProductosActivity, EditarProductoActivity::class.java)
                    intent.putExtra("producto_json", producto.toString())
                    startActivity(intent)
                }

            } catch (e: Exception) {
                Log.e("ProductoAdapter", "Error en getView: ${e.message}", e)
            }

            return view
        }

        private fun construirDimensiones(producto: JSONObject): String {
            val dimensiones = mutableListOf<String>()

            val ladoA = producto.optString("lado_a", "").trim()
            val ladoB = producto.optString("lado_b", "").trim()
            val ladoC = producto.optString("lado_c", "").trim()
            val ladoD = producto.optString("lado_d", "").trim()

            if (ladoA.isNotEmpty() && ladoA != "0" && ladoA != "0.0") {
                dimensiones.add("A=$ladoA mm")
            }
            if (ladoB.isNotEmpty() && ladoB != "0" && ladoB != "0.0") {
                dimensiones.add("B=$ladoB mm")
            }
            if (ladoC.isNotEmpty() && ladoC != "0" && ladoC != "0.0") {
                dimensiones.add("C=$ladoC mm")
            }
            if (ladoD.isNotEmpty() && ladoD != "0" && ladoD != "0.0") {
                dimensiones.add("D=$ladoD mm")
            }

            return if (dimensiones.isNotEmpty()) {
                dimensiones.joinToString(", ")
            } else {
                ""
            }
        }
    }
}