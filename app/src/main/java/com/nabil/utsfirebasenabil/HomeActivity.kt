package com.nabil.utsfirebasenabil

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var welcomeTextView: TextView
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNim: EditText
    private lateinit var etProdi: EditText
    private lateinit var btnSimpan: Button
    private lateinit var rvMhs: RecyclerView
    private lateinit var ref: DatabaseReference
    private lateinit var mhsList: MutableList<Mahasiswa>
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference("mahasiswa")

        welcomeTextView = findViewById(R.id.welcomeTextView)
        etNama = findViewById(R.id.etNama)
        etAlamat = findViewById(R.id.etAlamat)
        etNim = findViewById(R.id.etNim)
        etProdi = findViewById(R.id.etProdi)
        btnSimpan = findViewById(R.id.btnSimpan)
        rvMhs = findViewById(R.id.rv_mhs)

        btnSimpan.setOnClickListener(this)

        rvMhs.layoutManager = LinearLayoutManager(this)
        rvMhs.setHasFixedSize(true)
        mhsList = mutableListOf()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    mhsList.clear()
                    for (h in snapshot.children) {
                        val mahasiswa = h.getValue(Mahasiswa::class.java)
                        mahasiswa?.let {
                            mhsList.add(it)
                        }
                    }
                    val adapter = AdapterMahasiswa(this@HomeActivity, mhsList)
                    rvMhs.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Gagal membaca data dari Firebase", Toast.LENGTH_SHORT).show()
            }
        })

        val user = mAuth.currentUser
        user?.let {
            val email = it.email
            val name = extractNameFromEmail(email)
            welcomeTextView.text = "Hallo $name"
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSimpan -> saveData()
        }
    }

    private fun saveData() {
        val nama: String = etNama.text.toString().trim()
        val alamat: String = etAlamat.text.toString().trim()
        val nim: String = etNim.text.toString().trim()
        val prodi: String = etProdi.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Isi Nama!"
            return
        }

        if (alamat.isEmpty()) {
            etAlamat.error = "Isi Alamat!"
            return
        }

        if (nim.isEmpty()) {
            etNim.error = "Isi Nim!"
            return
        }

        if (prodi.isEmpty()) {
            etProdi.error = "Isi Prodi!"
            return
        }

        val mhsId = ref.push().key
        val mhs = Mahasiswa(mhsId, nama, alamat, nim, prodi)
        mhsId?.let {
            ref.child(it).setValue(mhs).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Gagal menyimpan data ke Firebase", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun extractNameFromEmail(email: String?): String {
        return email?.substringBefore("@") ?: "User"
    }
}
