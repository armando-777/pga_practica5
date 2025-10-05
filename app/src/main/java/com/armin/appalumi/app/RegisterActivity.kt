package com.armin.appalumi.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.util.Patterns
import com.armin.appalumi.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    lateinit var registerUsername : EditText
    lateinit var registerLastName: EditText
    lateinit var registerEmail: EditText
    lateinit var registerPhone: EditText
    lateinit var registerAddress: EditText
    lateinit var registButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerUsername = binding.registerUsername
        registerLastName = binding.registerLastName
        registerEmail = binding.registerEmail
        registerPhone = binding.registerPhone
        registerAddress = binding.registerAddress
        registButton = binding.registButton

        binding.registButton.setOnClickListener(View.OnClickListener {
            val nombre = binding.registerUsername.text.toString().trim()
            val apellido = binding.registerLastName.text.toString().trim()
            val email = binding.registerEmail.text.toString().trim()
            val telefono = binding.registerPhone.text.toString().trim()
            val direccion = binding.registerAddress.text.toString().trim()

            // Validar campos vacíos
            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() ||
                telefono.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            // Validar formato de email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingrese un email válido", Toast.LENGTH_SHORT).show()
                binding.registerEmail.error = "Email inválido"
                return@OnClickListener
            }

            // Si todo es válido, registrar usuario
            registrarUsuario()
        })
    }

    private fun registrarUsuario() {
        val datos = mapOf(
            "nombre" to binding.registerUsername.text.toString().trim(),
            "apellido" to binding.registerLastName.text.toString().trim(),
            "email" to binding.registerEmail.text.toString().trim(),
            "telefono" to binding.registerPhone.text.toString().trim(),
            "direccion" to binding.registerAddress.text.toString().trim()
        )

        // Corrutina para no bloquear UI
        CoroutineScope(Dispatchers.IO).launch {
            val respuesta: JSONObject? = ApiHelper.realizarPeticionPOST("/add_register", datos)

            withContext(Dispatchers.Main) {
                if (respuesta != null) {
                    val status = respuesta.optString("status")
                    val message = respuesta.optString("message")

                    if (status == "success") {
                        Toast.makeText(this@RegisterActivity, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                        limpiarFormulario()
                        finish() // Cerrar actividad y volver
                    } else {
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                    }

                } else {
                    Toast.makeText(this@RegisterActivity, "Error en la conexión con el servidor", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun limpiarFormulario() {
        binding.registerUsername.text?.clear()
        binding.registerLastName.text?.clear()
        binding.registerEmail.text?.clear()
        binding.registerPhone.text?.clear()
        binding.registerAddress.text?.clear()
        binding.registerUsername.requestFocus()
    }
}