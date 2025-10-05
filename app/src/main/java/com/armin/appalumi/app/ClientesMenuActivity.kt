package com.armin.appalumi.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.databinding.ActivityClientesMenuBinding

class ClientesMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientesMenuBinding
    private lateinit var layoutRegistrarCliente: LinearLayout
    private lateinit var layoutListarClientes: LinearLayout
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClientesMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutRegistrarCliente = binding.btnRegistrarCliente
        layoutListarClientes = binding.btnListarClientes
        btnVolver = binding.btnVolver

        // Click en Registrar Cliente
        layoutRegistrarCliente.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Click en Listar Clientes
        layoutListarClientes.setOnClickListener {
            val intent = Intent(this, ListarActivity::class.java)
            startActivity(intent)
        }

        // Botón Volver
        binding.btnVolver.setOnClickListener {
            finish()
        }
    }
}