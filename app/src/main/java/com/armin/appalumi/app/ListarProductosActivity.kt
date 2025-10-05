package com.armin.appalumi.app

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
                            // Usar adaptador personalizado
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

    // Adaptador personalizado para mostrar productos con imagen
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
                val tvPerfil = view.findViewById<TextView>(R.id.tvProductoPerfil)
                val tvDimensiones = view.findViewById<TextView>(R.id.tvProductoDimensiones)
                val tvPrecio = view.findViewById<TextView>(R.id.tvProductoPrecio)
                val tvStock = view.findViewById<TextView>(R.id.tvProductoStock)
                val tvDescripcion = view.findViewById<TextView>(R.id.tvProductoDescripcion)

                // Cargar datos
                tvNombre.text = producto.getString("nombre")
                tvPerfil.text = "Perfil: ${producto.optString("perfil_nombre", "N/A")}"

                val ladoA = producto.optString("lado_a", "0")
                val ladoB = producto.optString("lado_b", "0")
                val ladoC = producto.optString("lado_c", "0")
                val ladoD = producto.optString("lado_d", "0")
                tvDimensiones.text = "Dimensiones: A=$ladoA mm, B=$ladoB mm, C=$ladoC mm, D=$ladoD mm"

                tvPrecio.text = "Precio: Bs. ${producto.optString("precio", "0.00")}"
                tvStock.text = "Stock: ${producto.optInt("stock", 0)} unidades"
                tvDescripcion.text = producto.optString("descripcion", "Sin descripción")

                // Cargar imagen
                val imagenBase64 = producto.optString("imagen", "")
                if (imagenBase64.isNotEmpty()) {
                    try {
                        val imageBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                            imageBytes, 0, imageBytes.size
                        )
                        ivImagen.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("ProductoAdapter", "Error cargando imagen: ${e.message}")
                        ivImagen.setImageResource(R.drawable.producto)
                    }
                } else {
                    ivImagen.setImageResource(R.drawable.producto)
                }

            } catch (e: Exception) {
                Log.e("ProductoAdapter", "Error en getView: ${e.message}", e)
            }

            return view
        }
    }
}