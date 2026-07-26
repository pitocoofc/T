package com.ndjlib.archiver

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileAdapter(
    private val itemList: List<FileModel>,
    private val onItemClick: (FileModel) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtIcon: TextView = view.findViewById(R.id.txtIcon)
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtDetails: TextView = view.findViewById(R.id.txtDetails)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        val context = holder.itemView.context

        if (item.isBackOption) {
            holder.txtIcon.text = "⬆️"
            holder.txtIcon.setBackgroundColor(0xFF4CAF50.toInt()) // Verde ZArchiver
            holder.txtName.text = ".."
            holder.txtDetails.text = ""
            holder.txtDate.text = ""
        } else {
            val file = item.file
            holder.txtName.text = file.name

            // Formatar data
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            holder.txtDate.text = dateFormat.format(Date(file.lastModified()))

            if (file.isDirectory) {
                holder.txtIcon.setBackgroundColor(0xFFFF9800.toInt()) // Laranja ZArchiver
                
                // Ícones dinâmicos ao estilo ZArchiver
                holder.txtIcon.text = when (file.name.lowercase()) {
                    "dcim", "pictures" -> "📷"
                    "download", "downloads" -> "📥"
                    "documents" -> "💼"
                    "music", "audiobooks", "ringtones" -> "🎵"
                    "android" -> "🤖"
                    else -> "📁"
                }

                val qtd = file.listFiles()?.size ?: 0
                holder.txtDetails.text = if (qtd == 0) "Vazio" else "$qtd itens"
            } else {
                holder.txtIcon.setBackgroundColor(0xFF607D8B.toInt()) // Cinza pra arquivos
                holder.txtIcon.text = "📄"
                holder.txtDetails.text = Formatter.formatFileSize(context, file.length())
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = itemList.size
}
