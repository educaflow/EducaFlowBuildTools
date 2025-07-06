/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.deletemeta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    // Configuración de la conexión a la base de datos
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/educaflow";
    private static final String USER = "educaflow";
    private static final String PASSWORD = "educaflow";
    private static final String SCHEMA_NAME = "public"; // Cambia esto si tu esquema no es 'public'

    private static final Set<String> tablasExcluidas = Set.of("meta_file", "meta_sequence");
    
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        try {
            // 1. Cargar el driver de PostgreSQL
            Class.forName("org.postgresql.Driver");

            // 2. Establecer la conexión
            System.out.println("Conectando a la base de datos...");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Iniciar la transacción

            stmt = conn.createStatement();

            // --- PASO 1: Desactivar todas las restricciones de clave foránea ---
            System.out.println("Desactivando restricciones...");
            List<String> allTableNames = new ArrayList<>();
            ResultSet rsTableNames = stmt.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname = '" + SCHEMA_NAME + "'");
            while (rsTableNames.next()) {
                allTableNames.add(rsTableNames.getString("tablename"));
            }
            rsTableNames.close();

            for (String tableName : allTableNames) {
                System.out.println("  Deshabilitando triggers para: " + tableName);
                stmt.executeUpdate("ALTER TABLE " + tableName + " DISABLE TRIGGER ALL;");               
            }

            // --- PASO 2: Truncar las tablas que empiezan por 'meta_' ---
            System.out.println("Truncando tablas que empiezan por 'meta_'...");
            List<String> metaTables = new ArrayList<>();
            ResultSet rsTables = stmt.executeQuery(
                "SELECT tablename FROM pg_tables WHERE schemaname = '" + SCHEMA_NAME + "' AND tablename LIKE 'meta\\_%'"
            );
            while (rsTables.next()) {
                metaTables.add(rsTables.getString("tablename"));
            }
            rsTables.close();

            for (String tableName : metaTables) {
                
                if (!tablasExcluidas.contains(tableName)) {
                    System.out.println("  Truncando tabla: " + tableName);
                    // Si quieres truncar en cascada las tablas dependientes, añade " CASCADE"
                    // Si solo quieres borrar los datos de la tabla, quita " CASCADE"
                    stmt.executeUpdate("TRUNCATE TABLE " + tableName + " CASCADE;");
                }
            }

            // --- PASO 3: Reactivar todas las restricciones de clave foránea ---
            System.out.println("Reactivando restricciones ...");
            for (String tableName : allTableNames) {
                System.out.println("  Habilitando triggers para: " + tableName);
                stmt.executeUpdate("ALTER TABLE " + tableName + " ENABLE TRIGGER ALL;");              
            }

            // Confirmar la transacción
            conn.commit();
            System.out.println("Operación completada exitosamente.");

        } catch (Exception ex) {
            try {
                if (conn != null) {
                    System.err.println("Realizando rollback...");
                    conn.rollback(); 
                }
            } catch (SQLException rbse) {
                System.err.println("Error durante el rollback: " + rbse.getMessage());
            }
            throw new RuntimeException("Fallo al borrar los meta datos",ex);
        } finally {
            // Cerrar recursos en un bloque finally
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
                // No hacer nada
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            

        }
    }
}