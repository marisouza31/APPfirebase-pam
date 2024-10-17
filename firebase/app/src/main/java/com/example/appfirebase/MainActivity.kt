package com.example.appfirebase

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfirebase.ui.theme.AppFirebaseTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(db)
        }
    }
}

@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var turma by remember { mutableStateOf("") }
    var clientes by remember { mutableStateOf<List<Map<String, String>>>(listOf()) }

    fun loadClientes() {
        db.collection("Clientes")
            .get()
            .addOnSuccessListener { result ->
                val fetchedClientes = result.documents.map { document ->
                    mapOf(
                        "nome" to (document.getString("nome") ?: ""),
                        "turma" to (document.getString("turma") ?: "")
                    )
                }
                clientes = fetchedClientes

                // Logar clientes no Logcat
                for (cliente in fetchedClientes) {
                    Log.d(TAG, "Cliente: Nome=${cliente["nome"]}, Turma=${cliente["turma"]}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    LaunchedEffect(Unit) {
        loadClientes()
    }

    AppFirebaseTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFD893E4) // Cor de fundo
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Header()
                InputFields(nome, turma) { newNome, newTurma ->
                    nome = newNome
                    turma = newTurma
                }
                // Centraliza o botão
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ActionButton(db, nome, turma) {
                        loadClientes() // Atualiza a lista após adicionar um cliente
                    }
                }

                // Lista de Clientes
                Text(
                    text = "Lista de Clientes:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f) // Ocupa o espaço restante
                ) {
                    items(clientes) { cliente ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = cliente["nome"] ?: "", fontFamily = FontFamily.Serif)
                            Text(text = cliente["turma"] ?: "", fontFamily = FontFamily.Serif)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.etec), // Substitua pelo seu recurso de imagem
            contentDescription = "Imagem Redonda",
            modifier = Modifier
                .size(40.dp) // Tamanho da imagem
                .clip(CircleShape), // Torna a imagem redonda
            contentScale = ContentScale.Crop // Para garantir que a imagem se ajuste ao formato
        )

        Spacer(modifier = Modifier.width(8.dp)) // Espaço entre a imagem e o texto

        Text(
            text = "App Firebase - Cadastrar Clientes",
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InputFields(nome: String, turma: String, onValueChange: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(24.dp))
        InputField(label = "Nome", value = nome, onValueChange = { newNome -> onValueChange(newNome, turma) })
        Spacer(modifier = Modifier.height(16.dp))
        InputField(label = "Turma", value = turma, onValueChange = { newTurma -> onValueChange(nome, newTurma) })
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionButton(db: FirebaseFirestore, nome: String, turma: String, onSuccess: () -> Unit) {
    Button(
        onClick = {
            val client = hashMapOf(
                "nome" to nome,
                "turma" to turma
            )
            db.collection("Clientes").add(client)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot written with ID ${documentReference.id}")
                    onSuccess() // Atualiza a lista após adicionar um novo cliente
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAD51BD)),
        modifier = Modifier.height(48.dp) // Tamanho do botão
    ) {
        Text(
            text = "Cadastrar",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold // Título em negrito
        )
    }
}
