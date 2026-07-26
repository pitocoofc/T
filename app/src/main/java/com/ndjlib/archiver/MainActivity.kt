package com.ndjlib.archiver

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var diretorioAtual: File = Environment.getExternalStorageDirectory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.fileListView)

        // Ao clicar num item da lista
        listView.setOnItemClickListener { _, _, position, _ ->
            val arquivoSelecionado = listView.adapter.getItem(position) as String
            val novoCaminho = File(diretorioAtual, arquivoSelecionado)

            if (novoCaminho.isDirectory) {
                diretorioAtual = novoCaminho
                carregarArquivos()
            } else {
                Toast.makeText(this, "Arquivo: ${novoCaminho.name}", Toast.LENGTH_SHORT).show()
            }
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

        val arquivos = diretorioAtual.listFiles()
        val listaNomes = mutableListOf<String>()

        // Opção de voltar pasta se não estiver na raiz
        if (diretorioAtual.parentFile != null && diretorioAtual != Environment.getExternalStorageDirectory()) {
            listaNomes.add(".. (Voltar)")
        }

        arquivos?.sortedBy { !it.isDirectory }?.forEach { file ->
            val prefixo = if (file.isDirectory) "📁 " else "📄 "
            listaNomes.add(prefixo + file.name)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaNomes)
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            carregarArquivos()
        }
    }

    // Botão Voltar do celular para navegar nas pastas
    override fun onBackPressed() {
        if (diretorioAtual != Environment.getExternalStorageDirectory() && diretorioAtual.parentFile != null) {
            diretorioAtual = diretorioAtual.parentFile!!
            carregarArquivos()
        } else {
            super.onBackPressed()
        }
    }
}
