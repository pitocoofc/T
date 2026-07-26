package com.ndjlib.archiver

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
            exibirMenuCriar()
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
            Toast.makeText(this, "Sem permissão de leitura", Toast.LENGTH_SHORT).show()
            return
        }

        txtCurrentPath.text = diretorioAtual.absolutePath.replace("/storage/emulated/0", "0")

        val arquivos = diretorioAtual.listFiles()
        val listaItens = mutableListOf<FileModel>()

        if (diretorioAtual != Environment.getExternalStorageDirectory() && diretorioAtual.parentFile != null) {
            listaItens.add(FileModel(diretorioAtual.parentFile!!, isBackOption = true))
        }

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
                exibirOpcoesArquivo(item.file)
            }
        }
    }

    // Menu do Botão (+) estilo ZArchiver
    private fun exibirMenuCriar() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_create_menu, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.optCreateFolder).setOnClickListener {
            dialog.dismiss()
            dialogCriarPasta()
        }

        view.findViewById<TextView>(R.id.optCreateFile).setOnClickListener {
            dialog.dismiss()
            dialogCriarArquivo()
        }

        dialog.show()
    }

    // Modal de Opções do Arquivo
    private fun exibirOpcoesArquivo(file: File) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_file_options, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.txtOptionTitle).text = file.name

        // Abrir (Verifica se é texto pra usar o editor próprio ou sistema externo)
        view.findViewById<TextView>(R.id.optOpen).setOnClickListener {
            dialog.dismiss()
            if (isTextFile(file)) {
                abrirEditorTextoProprio(file)
            } else {
                abrirArquivoExterno(file)
            }
        }

        // Renomear
        view.findViewById<TextView>(R.id.optRename).setOnClickListener {
            dialog.dismiss()
            dialogRenomear(file)
        }

        // Excluir
        view.findViewById<TextView>(R.id.optDelete).setOnClickListener {
            dialog.dismiss()
            if (file.delete()) {
                Toast.makeText(this, "Excluído com sucesso", Toast.LENGTH_SHORT).show()
                carregarArquivos()
            } else {
                Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun isTextFile(file: File): Boolean {
        val exts = listOf("txt", "html", "htm", "json", "xml", "log", "js", "css", "py", "kt", "java", "c", "cpp")
        return exts.contains(file.extension.lowercase())
    }

    private fun abrirEditorTextoProprio(file: File) {
        val intent = Intent(this, TextEditorActivity::class.java).apply {
            putExtra("FILE_PATH", file.absolutePath)
        }
        startActivity(intent)
    }

    private fun abrirArquivoExterno(file: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir arquivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dialogCriarPasta() {
        val input = EditText(this)
        input.hint = "Nome da Pasta"

        AlertDialog.Builder(this)
            .setTitle("Nova Pasta")
            .setView(input)
            .setPositiveButton("Criar") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) {
                    val novaPasta = File(diretorioAtual, nome)
                    if (novaPasta.mkdir()) {
                        carregarArquivos()
                    } else {
                        Toast.makeText(this, "Erro ao criar pasta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun dialogCriarArquivo() {
        val input = EditText(this)
        input.hint = "nome_do_arquivo.txt"

        AlertDialog.Builder(this)
            .setTitle("Novo Arquivo")
            .setView(input)
            .setPositiveButton("Criar") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) {
                    val novoArquivo = File(diretorioAtual, nome)
                    try {
                        if (novoArquivo.createNewFile()) {
                            carregarArquivos()
                            abrirEditorTextoProprio(novoArquivo)
                        } else {
                            Toast.makeText(this, "Arquivo já existe ou erro ao criar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Erro ao criar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun dialogRenomear(file: File) {
        val input = EditText(this)
        input.setText(file.name)

        AlertDialog.Builder(this)
            .setTitle("Renomear")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = input.text.toString().trim()
                if (novoNome.isNotEmpty()) {
                    val destino = File(file.parent, novoNome)
                    if (file.renameTo(destino)) {
                        carregarArquivos()
                    } else {
                        Toast.makeText(this, "Erro ao renomear", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
