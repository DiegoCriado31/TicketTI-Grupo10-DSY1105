package com.example.ticketti.repository

import com.example.ticketti.model.Credential

class AuthRepository(
    private val usuarios: List<Credential> = Credential.Usuarios
) {

    fun login(username: String, password: String): Boolean {
        return buscarUsuario(username)?.password == password
    }

    // Devuelve el usuario (con su rol) o null si no existe
    fun buscarUsuario(username: String): Credential? {
        return usuarios.firstOrNull { it.username.equals(username, ignoreCase = true) }
    }
}