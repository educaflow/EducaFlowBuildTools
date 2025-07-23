/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.deleteaxelortables;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Main {

    private static final Set<String> tablasExcluidas = Set.of("meta_file", "meta_sequence","auth_user","auth_group");
    private static final Set<String> tablasIncluidas = new HashSet<>(); 
    
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        String dataBaseDriver;
        String dataBaseURL;
        String dataBaseUser;
        String dataBasePassword ;
        String schemaName = "public";

        try {
            Properties prop = new Properties();

            try (FileReader reader = new FileReader(args[0])) {

                prop.load(reader);

                dataBaseDriver = prop.getProperty("db.default.driver");
                dataBaseURL = prop.getProperty("db.default.url");
                dataBaseUser = prop.getProperty("db.default.user");
                dataBasePassword = prop.getProperty("db.default.password");
            }
            
            
            if (args.length>0) {
                for(int i=1;i<args.length;i++) {
                    tablasIncluidas.add(args[i]);
                }
            }
            
            
            Class.forName(dataBaseDriver);

            System.out.println("Conectando a la base de datos...");
            conn = DriverManager.getConnection(dataBaseURL, dataBaseUser, dataBasePassword);
            conn.setAutoCommit(false); // Iniciar la transacción

            stmt = conn.createStatement();

            System.out.println("Desactivando restricciones...");
            List<String> allTableNames = new ArrayList<>();
            ResultSet rsTableNames = stmt.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname = '" + schemaName + "'");
            while (rsTableNames.next()) {
                allTableNames.add(rsTableNames.getString("tablename"));
            }
            rsTableNames.close();

            for (String tableName : allTableNames) {
                stmt.executeUpdate("ALTER TABLE " + tableName + " DISABLE TRIGGER ALL;");               
            }

            // --- PASO 2: Truncar las tablas que empiezan por 'meta_' ---
            truncateTablesByLike(stmt,schemaName,"meta\\_%",tablasExcluidas);
            truncateTablesByLike(stmt,schemaName,"auth\\_%",tablasExcluidas);

            for(String tableName:tablasIncluidas) {
                stmt.executeUpdate("TRUNCATE TABLE " + tableName + " CASCADE;");
            }
            

            // --- PASO 3: Reactivar todas las restricciones de clave foránea ---
            System.out.println("Reactivando restricciones ...");
            for (String tableName : allTableNames) {
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
    
    
    private static void truncateTablesByLike(Statement stmt,String schemaName, String like,Set<String> tablasExcluidas) {
        try {
            System.out.println("Borrando contenido de las tablas que empiezan por '" + like+ "' ....." );
            List<String> metaTables = new ArrayList<>();
            ResultSet rsTables = stmt.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname = '" + schemaName + "' AND tablename LIKE '" + like +"'"
            );
            while (rsTables.next()) {
                metaTables.add(rsTables.getString("tablename"));
            }
            rsTables.close();

            for (String tableName : metaTables) {

                if (!tablasExcluidas.contains(tableName)) {
                    System.out.println("Borrando contenido de la tabla:"+tableName);
                    stmt.executeUpdate("TRUNCATE TABLE " + tableName + " CASCADE;");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}