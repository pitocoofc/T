package com.ndjlib.archiver

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtCurrentPath: TextView
    private lateinit var fabAdd: FloatingActionButton
    private var diretorioAtual: File = Environment.getExternalStorageDirectory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewFiles)
        txtCurrentPath = findViewById(R.id.txtCurrentPath)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAdd.setOnClickListener {
            Toast.makeText(this, "Criar nova pasta / arquivo", Toast.LENGTH_SHORT).show()
        }

        verificarEPedirPermissao()
    }

    private fun verificarEPedirPermissao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                carregarArquivos()
            }
        } else {
            carregarArquivos()
        }
    }

    private fun carregarArquivos() {
        if (!diretorioAtual.canRead()) {
            Toast.makeText(this, "Sem permissão de leitura nesta pasta", Toast.LENGTH_SHORT).show()
            return
        }

        txtCurrentPath.text = diretorioAtual.absolutePath.replace("/storage/emulated/0", "0")

        val arquivos = diretorioAtual.listFiles()
        val listaItens = mutableListOf<FileModel>()

        // Opção '..' para voltar
        if (diretorioAtual != Environment.getExternalStorageDirectory() && diretorioAtual.parentFile != null) {
            listaItens.add(FileModel(diretorioAtual.parentFile!!, isBackOption = true))
        }

        // Ordena: Pastas primeiro, depois Arquivos
        arquivos?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))?.forEach { file ->
            listaItens.add(FileModel(file))
        }

        recyclerView.adapter = FileAdapter(listaItens) { item ->
            if (item.isBackOption) {
                diretorioAtual = diretorioAtual.parentFile!!
                carregarArquivos()
            } else if (item.file.isDirectory) {
                diretorioAtual = item.file
                carregarArquivos()
            } else {
                Toast.makeText(this, "Arquivo: ${item.file.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            carregarArquivos()
        }
    }

    override fun onBackPressed() {
        if (diretorioAtual != Environment.getExternalStorageDirectory() && diretorioAtual.parentFile != null) {
            diretorioAtual = diretorioAtual.parentFile!!
            carregarArquivos()
        } else {
            super.onBackPressed()
        }
    }
}
