package com.armin.appalumi.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.armin.appalumi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var username : EditText
    lateinit var password: EditText
    lateinit var loginButton: Button
    lateinit var olvidadoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tu código original del login
        binding.loginButton.setOnClickListener(View.OnClickListener {
            if (binding.username.text.toString() == "user" && binding.password.text.toString() == "1234"){
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Navegar al Panel de Gestión
                val intent = Intent(this, PanelActivity::class.java)
                intent.putExtra("username", binding.username.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        })

        // Funcionalidad del botón olvidado
        binding.olvidadoButton.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Recuperar Contraseña")
            builder.setMessage("Credenciales predeterminadas:\n\nUsuario: user\nContraseña: 1234\n\nPara otras cuentas, contacte al administrador de Aluminnova.")
            builder.setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNegativeButton("Usar Predeterminadas") { dialog, _ ->
                binding.username.setText("user")
                binding.password.setText("1234")
                dialog.dismiss()
            }
            builder.create().show()
        }
    }
}