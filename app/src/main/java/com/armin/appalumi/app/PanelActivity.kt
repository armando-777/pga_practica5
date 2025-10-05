package com.armin.appalumi.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.databinding.ActivityPanelBinding

class PanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding
    private lateinit var welcomeText: TextView
    private lateinit var layoutVentas: LinearLayout
    private lateinit var layoutProductos: LinearLayout
    private lateinit var layoutClientes: LinearLayout
    private lateinit var layoutUsuarios: LinearLayout
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el nombre de usuario del intent
        val username = intent.getStringExtra("username") ?: "Usuario"

        welcomeText = binding.welcomeText
        btnCerrarSesion = binding.btnCerrarSesion

        // Mostrar bienvenida personalizada
        welcomeText.text = "Bienvenido, $username"

        // Obtener referencias a los LinearLayout del GridLayout
        val gridPanel = binding.gridPanel
        layoutVentas = gridPanel.getChildAt(0) as LinearLayout
        layoutProductos = gridPanel.getChildAt(1) as LinearLayout
        layoutClientes = gridPanel.getChildAt(2) as LinearLayout
        layoutUsuarios = gridPanel.getChildAt(3) as LinearLayout

        // Click en Ventas (para después)
        layoutVentas.setOnClickListener {
            Toast.makeText(this, "Módulo de Ventas - Próximamente", Toast.LENGTH_SHORT).show()
        }

        // Click en Productos - Navega a ProductosMenuActivity
        layoutProductos.setOnClickListener {
            val intent = Intent(this, ProductosMenuActivity::class.java)
            startActivity(intent)
        }

        // Click en Clientes - Navega a ClientesMenuActivity
        layoutClientes.setOnClickListener {
            val intent = Intent(this, ClientesMenuActivity::class.java)
            startActivity(intent)
        }

        // Click en Usuarios (para después)
        layoutUsuarios.setOnClickListener {
            Toast.makeText(this, "Módulo de Usuarios - Próximamente", Toast.LENGTH_SHORT).show()
        }

        // Botón Cerrar Sesión
        binding.btnCerrarSesion.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            finish() // Cierra PanelActivity y vuelve al login
        }
    }

    // Evitar que el botón atrás cierre la sesión sin confirmación
    override fun onBackPressed() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesión")
        builder.setMessage("¿Desea cerrar sesión?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}