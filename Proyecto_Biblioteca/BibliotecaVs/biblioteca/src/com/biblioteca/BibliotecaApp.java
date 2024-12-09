package com.biblioteca;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class BibliotecaApp {
    private static final String controlador = "com.mysql.cj.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/bibliotecabase";
    private static final String usuario = "root";
    private static final String contraseña = "Chimaru2005";

    private Usuario usuarioActual;

    public Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName(controlador);
            conexion = DriverManager.getConnection(url, usuario, contraseña);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return conexion;
    }

    private Usuario buscarUsuarioEnBaseDeDatos(String documento) {
        String sql = "SELECT id, nombre FROM usuarios WHERE documento = ?";
        try (Connection conexion = conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, documento);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                return new Usuario(nombre, documento, 0, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar usuario en la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void registrarUsuario(String nombre, String documento) {
        String sql = "INSERT INTO usuarios (nombre, documento) VALUES (?, ?)";
        try (Connection conexion = conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, documento);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Usuario registrado exitosamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listarLibros(JPanel panel) {
        String sql = "SELECT id, nombre, autor, categoria, disponible FROM libros";
        try (Connection conexion = conectar();
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            panel.removeAll();
            panel.setLayout(new GridLayout(0, 3, 10, 10));

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String autor = rs.getString("autor");
                String categoria = rs.getString("categoria");
                boolean disponible = rs.getBoolean("disponible");

                JPanel libroPanel = new JPanel();
                libroPanel.setLayout(new BorderLayout());
                libroPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                libroPanel.setBackground(disponible ? new Color(232, 255, 232) : new Color(255, 232, 232));

                JLabel label = new JLabel("<html><b>Nombre:</b> " + nombre + "<br><b>Autor:</b> " + autor +
                        "<br><b>Categoría:</b> " + categoria + "<br><b>Disponible:</b> " + (disponible ? "Sí" : "No") + "</html>");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                libroPanel.add(label, BorderLayout.CENTER);

                panel.add(libroPanel);
            }
            panel.revalidate();
            panel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar los libros: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inicializarInterfaz() {
        // Configurar FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Ventana principal
        JFrame frame = new JFrame("Biblioteca");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Panel de inicio de sesión
        JPanel panelLogin = new JPanel(new GridLayout(4, 1, 10, 10));
        panelLogin.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel documentoLabel = new JLabel("Documento:");
        JTextField documentoField = new JTextField();
        JButton loginButton = new JButton("Iniciar sesión");
        JButton registerButton = new JButton("Registrarse");

        panelLogin.add(documentoLabel);
        panelLogin.add(documentoField);
        panelLogin.add(loginButton);
        panelLogin.add(registerButton);

        // Panel principal
        JPanel panelMain = new JPanel(new BorderLayout());
        JPanel panelLibros = new JPanel();
        JScrollPane scrollLibros = new JScrollPane(panelLibros);
        JButton listarLibrosButton = new JButton("Listar libros");

        panelMain.add(scrollLibros, BorderLayout.CENTER);
        panelMain.add(listarLibrosButton, BorderLayout.SOUTH);

        // Eventos de los botones
        loginButton.addActionListener(e -> {
            String documento = documentoField.getText();
            usuarioActual = buscarUsuarioEnBaseDeDatos(documento);

            if (usuarioActual != null) {
                JOptionPane.showMessageDialog(frame, "Bienvenido, " + usuarioActual.getNombre());
                frame.remove(panelLogin);
                frame.add(panelMain);
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Usuario no encontrado.");
            }
        });

        registerButton.addActionListener(e -> {
            String documento = documentoField.getText();
            String nombre = JOptionPane.showInputDialog(frame, "Ingrese su nombre:");
            if (nombre != null && !nombre.isEmpty()) {
                registrarUsuario(nombre, documento);
            } else {
                JOptionPane.showMessageDialog(frame, "Nombre inválido.");
            }
        });

        listarLibrosButton.addActionListener(e -> listarLibros(panelLibros));

        // Mostrar ventana
        frame.add(panelLogin);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new BibliotecaApp().inicializarInterfaz();
    }
}
