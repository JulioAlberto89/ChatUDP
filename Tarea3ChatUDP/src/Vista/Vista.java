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
            socket = new DatagramSocket(); // init to any available port
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static InetAddress address;

    static {
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String identifier;

    private static final int SERVER_PORT = 8000; // send to server

    private static final JTextArea messageArea = new JTextArea();
    private static final JTextField inputBox = new JTextField();
    private static final JButton fileButton = new JButton("Seleccionar Archivo");

    public static void main(String[] args) throws IOException {
        // Pedir al usuario que ingrese su nombre
        identifier = JOptionPane.showInputDialog("Ingrese su nombre:");

        if (identifier == null || identifier.trim().isEmpty()) {
            // Si el usuario cancela o no ingresa un nombre, salimos de la aplicación
            System.exit(0);
        }

        // thread for receiving messages
        Cliente clientThread = new Cliente(socket, messageArea);
        clientThread.start();

        // send initialization message to the server
        byte[] uuid = ("init;" + identifier).getBytes();
        DatagramPacket initialize = new DatagramPacket(uuid, uuid.length, address, SERVER_PORT);
        socket.send(initialize);

        SwingUtilities.invokeLater(() -> {
            new Vista().setVisible(true); // launch GUI
        });
    }

    public Vista() {
        super("Chat Client - " + identifier);

        messageArea.setEditable(false);

        inputBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(identifier + ";" + inputBox.getText());
                String current = messageArea.getText();
                messageArea.setText(current + identifier + ";" + inputBox.getText() + "\n");
                inputBox.setText("");
            }
        });

        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(Vista.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    // Obtener la ruta del archivo seleccionado
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();

                    // Enviar la URL o la ruta del archivo a través del chat
                    sendMessage(identifier + ";Archivo:" + filePath);
                }
            }
        });

        // put everything on screen
        setLayout(new BorderLayout());
        add(new JScrollPane(messageArea), BorderLayout.CENTER);
        add(inputBox, BorderLayout.SOUTH);
        add(fileButton, BorderLayout.NORTH); // Agregar el botón para seleccionar archivos
        setSize(550, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Método para enviar mensajes a través del chat
    private void sendMessage(String message) {
        byte[] msg = message.getBytes();
        DatagramPacket send = new DatagramPacket(msg, msg.length, address, SERVER_PORT);
        try {
            socket.send(send);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}