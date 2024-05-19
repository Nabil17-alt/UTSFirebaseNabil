package com.nabil.utsfirebasenabil

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdapterMahasiswa(
    private val context: Context,
    private val mhsList: MutableList<Mahasiswa>
) : RecyclerView.Adapter<AdapterMahasiswa.ViewHolder>() {

    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("mahasiswa")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mhs, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mahasiswa = mhsList[position]
        holder.tvNama.text = mahasiswa.nama
        holder.tvAlamat.text = mahasiswa.alamat
        holder.tvNim.text = mahasiswa.nim
        holder.tvProdi.text = mahasiswa.prodi

        holder.itemView.setOnClickListener {
            showOptionsDialog(mahasiswa, position)
        }
    }

    override fun getItemCount(): Int {
        return mhsList.size
    }

    private fun showOptionsDialog(mahasiswa: Mahasiswa, position: Int) {
        val options = arrayOf("Edit", "Hapus", "Cancel")
        val builder = AlertDialog.Builder(context)
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showEditDialog(mahasiswa)
                1 -> deleteData(mahasiswa, position)
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun showEditDialog(mahasiswa: Mahasiswa) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_edit, null)
        builder.setView(view)

        val etNama = view.findViewById<EditText>(R.id.etNama)
        val etAlamat = view.findViewById<EditText>(R.id.etAlamat)
        val etNim = view.findViewById<EditText>(R.id.etNim)
        val etProdi = view.findViewById<EditText>(R.id.etProdi)

        etNama.setText(mahasiswa.nama)
        etAlamat.setText(mahasiswa.alamat)
        etNim.setText(mahasiswa.nim)
        etProdi.setText(mahasiswa.prodi)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newNama = etNama.text.toString().trim()
            val newAlamat = etAlamat.text.toString().trim()
            val newNim = etNim.text.toString().trim()
            val newProdi = etProdi.text.toString().trim()

            if (newNama.isEmpty() || newAlamat.isEmpty() || newNim.isEmpty() || newProdi.isEmpty()) {
                Toast.makeText(context, "Nama atau alamat atau nim atau proditidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val updatedMahasiswa = Mahasiswa(mahasiswa.id, newNama, newAlamat, newNim, newProdi)
            mahasiswa.id?.let {
                ref.child(it).setValue(updatedMahasiswa).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun deleteData(mahasiswa: Mahasiswa, position: Int) {
        mahasiswa.id?.let {
            ref.child(it).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mhsList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, mhsList.size)
                    Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamat)
        val tvNim: TextView = itemView.findViewById(R.id.tvNim)
        val tvProdi: TextView = itemView.findViewById(R.id.tvProdi)
    }
}
