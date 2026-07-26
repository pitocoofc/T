package com.ndjlib.archiver

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class TextEditorActivity : AppCompatActivity() {

    private lateinit var etContent: EditText
    private lateinit var txtFileName: TextView
    private lateinit var btnSave: ImageView
    private lateinit var btnBack: ImageView
    private var currentFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_editor)

        etContent = findViewById(R.id.etContent)
        txtFileName = findViewById(R.id.txtFileName)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        val filePath = intent.getStringExtra("FILE_PATH")
        if (filePath != null) {
            currentFile = File(filePath)
            txtFileName.text = currentFile?.name
            lerArquivo()
        }

        btnSave.setOnClickListener { salvarArquivo() }
        btnBack.setOnClickListener { finish() }
    }

    private fun lerArquivo() {
        try {
            val conteudo = currentFile?.readText() ?: ""
            etContent.setText(conteudo)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao ler arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvarArquivo() {
        try {
            currentFile?.writeText(etContent.text.toString())
            Toast.makeText(this, "Arquivo salvo com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
