package com.armin.appalumi.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.armin.appalumi.databinding.ActivityProductosMenuBinding

class ProductosMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductosMenuBinding
    private lateinit var layoutRegistrarProducto: LinearLayout
    private lateinit var layoutListarProductos: LinearLayout
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductosMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutRegistrarProducto = binding.btnRegistrarProducto
        layoutListarProductos = binding.btnListarProductos
        btnVolver = binding.btnVolver

        // Click en Registrar Producto
        layoutRegistrarProducto.setOnClickListener {
            val intent = Intent(this, RegistrarProductoActivity::class.java)
            startActivity(intent)
        }

        // Click en Listar Productos
        layoutListarProductos.setOnClickListener {
            val intent = Intent(this, ListarProductosActivity::class.java)
            startActivity(intent)
        }


        // Botón Volver
        binding.btnVolver.setOnClickListener {
            finish()
        }
    }
}