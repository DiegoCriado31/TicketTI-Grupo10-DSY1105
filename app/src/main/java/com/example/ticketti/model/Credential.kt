package com.example.ticketti.model

// Perfiles del caso: cliente, administrador/coordinador y tecnico
enum class Rol { ADMIN, CLIENTE, TECNICO }

data class Credential(
    val username: String,
    val password: String,
    val rol: Rol
) {
    companion object {
        // Usuarios ficticios para la demostracion
        val Admin = Credential(username = "admin", password = "123", rol = Rol.ADMIN)
        val Cliente = Credential(username = "cliente", password = "123", rol = Rol.CLIENTE)
        val Cliente2 = Credential(username = "cliente2", password = "123", rol = Rol.CLIENTE)
        val Tecnico = Credential(username = "tecnico", password = "123", rol = Rol.TECNICO)

        val Usuarios = listOf(Admin, Cliente, Cliente2, Tecnico)
    }
}