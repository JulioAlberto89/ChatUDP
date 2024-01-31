/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import Cliente.Cliente;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Usuario mañana
 */
public class Vista extends JFrame {

    private static final DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(); // inicializar en cualquier puerto disponible
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static InetAddress direccion;

    static {
        try {
            // IP a cambiar por la IP de la otra persona con la que se quiere chatear.
            direccion = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String identificador;

    private static final int PUERTO_SERVIDOR = 9090; // enviar al servidor

    private static final JTextArea areaMensajes = new JTextArea();
    private static final JTextField cajaTexto = new JTextField();
    private static final JButton botonArchivo = new JButton("Seleccionar Archivo");

    public static void main(String[] args) throws IOException {
        // Pedir al usuario que ingrese su nombre
        identificador = JOptionPane.showInputDialog("Ingrese su nombre:");

        if (identificador == null || identificador.trim().isEmpty()) {
            // Si el usuario cancela o no ingresa un nombre, salir de la aplicación
            System.exit(0);
        }

        // Hilo para recibir mensajes
        Cliente hiloCliente = new Cliente(socket, areaMensajes);
        hiloCliente.start();

        // Enviar mensaje de inicialización al servidor
        byte[] uuid = ("init;" + identificador).getBytes();
        DatagramPacket inicializar = new DatagramPacket(uuid, uuid.length, direccion, PUERTO_SERVIDOR);
        socket.send(inicializar);

        SwingUtilities.invokeLater(()
                -> {
            new Vista().setVisible(true); // lanzar la interfaz gráfica
        });
    }

    public Vista() {
        super("Cliente de Chat - " + identificador);

        areaMensajes.setEditable(false);

        cajaTexto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mensajeTexto = cajaTexto.getText().trim(); // Obtener el texto y eliminar espacios en blanco al inicio y al final

                if (!mensajeTexto.isEmpty()) {
                    // Solo enviar el mensaje si la caja de texto no está vacía
                    enviarMensaje(identificador + " dice: " + mensajeTexto);
                    String actual = areaMensajes.getText();
                    areaMensajes.setText(actual + identificador + ": " + mensajeTexto + "\n");
                    cajaTexto.setText("");
                } else {
                    // Mostrar JOptionPane si la caja de texto está vacía
                    JOptionPane.showMessageDialog(Vista.this, "Por favor, ingresa un mensaje antes de enviar.");
                }
            }
        });

        botonArchivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser seleccionadorArchivo = new JFileChooser();
                int resultado = seleccionadorArchivo.showOpenDialog(Vista.this);

                if (resultado == JFileChooser.APPROVE_OPTION) {
                    // Obtener la ruta del archivo seleccionado
                    String rutaArchivo = seleccionadorArchivo.getSelectedFile().getAbsolutePath();

                    // Enviar la URL o la ruta del archivo a través del chat
                    String mensaje = identificador + " envió el archivo: " + rutaArchivo;
                    enviarMensaje(mensaje);

                    // Añadir el mensaje al JTextArea del cliente que envía el archivo
                    String actual = areaMensajes.getText();
                    areaMensajes.setText(actual + mensaje + "\n");
                }
            }
        });

        // colocar todo en la pantalla
        setLayout(new BorderLayout());
        add(new JScrollPane(areaMensajes), BorderLayout.CENTER);
        add(cajaTexto, BorderLayout.SOUTH);
        add(botonArchivo, BorderLayout.NORTH); // Agregar el botón para seleccionar archivos
        setSize(550, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Método para enviar mensajes a través del chat
    private void enviarMensaje(String mensaje) {
        byte[] msg = mensaje.getBytes();
        DatagramPacket enviar = new DatagramPacket(msg, msg.length, direccion, PUERTO_SERVIDOR);
        try {
            socket.send(enviar);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
