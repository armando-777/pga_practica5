package com.armin.appalumi.app

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.R
import com.armin.appalumi.databinding.ActivityListarBinding
import kotlinx.coroutines.*
import org.json.JSONArray
import android.widget.TextView
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.*


class ListarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarBinding
    private lateinit var listViewClientes: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listViewClientes = binding.listViewClientes

        // Cargar usuarios al iniciar la actividad
        listarUsuarios()
    }

    private fun listarUsuarios() {
        CoroutineScope(Dispatchers.IO).launch {
            val respuesta = ApiHelper.realizarPeticionGET("/register")

            withContext(Dispatchers.Main) {
                if (respuesta != null) {
                    try {
                        Log.d("ListarActivity", "Respuesta del servidor: $respuesta")

                        // Convertir respuesta JSON (lista de objetos)
                        val jsonArray = JSONArray(respuesta)
                        val listaUsuarios = mutableListOf<String>()

                        for (i in 0 until jsonArray.length()) {
                            val usuario = jsonArray.getJSONObject(i)
                            val nombre = usuario.getString("nombre")
                            val apellido = usuario.getString("apellido")
                            val email = usuario.getString("email")
                            val telefono = usuario.optString("telefono", "N/A")
                            val direccion = usuario.optString("direccion", "N/A")
                            val fechaRegistro = usuario.optString("fecha_registro", "")

                            // Formatear fecha si existe
                            val fechaFormateada = if (fechaRegistro.isNotEmpty()) {
                                try {
                                    // Parsear fecha del servidor (formato: yyyy-MM-dd HH:mm:ss)
                                    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    val fecha = formatoEntrada.parse(fechaRegistro)
                                    fecha?.let { formatoSalida.format(it) } ?: fechaRegistro
                                } catch (e: Exception) {
                                    fechaRegistro
                                }
                            } else {
                                "N/A"
                            }

                            // Formato de presentación
                            listaUsuarios.add(
                                "Nombre: $nombre $apellido\n" +
                                        "Email: $email\n" +
                                        "Teléfono: $telefono\n" +
                                        "Dirección: $direccion\n" +
                                        "Registrado: $fechaFormateada"
                            )
                        }

                        if (listaUsuarios.isEmpty()) {
                            listaUsuarios.add("No hay usuarios registrados")
                        }

                        // Mostrar en el ListView
                        val adapter = object : ArrayAdapter<String>(
                            this@ListarActivity,
                            android.R.layout.simple_list_item_1,
                            listaUsuarios
                        ) {
                            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                                val view = super.getView(position, convertView, parent)
                                val textView = view.findViewById<android.widget.TextView>(android.R.id.text1)
                                textView.setTextColor(resources.getColor(R.color.teal_200))
                                return view
                            }
                        }
                        listViewClientes.adapter = adapter

                        Toast.makeText(
                            this@ListarActivity,
                            "Total de usuarios: ${jsonArray.length()}",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {
                        Log.e("ListarActivity", "Error parseando JSON: ${e.message}", e)
                        Toast.makeText(
                            this@ListarActivity,
                            "Error parseando JSON: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ListarActivity,
                        "Error al conectar con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar la lista cuando se vuelve a la actividad
        listarUsuarios()
    }
}