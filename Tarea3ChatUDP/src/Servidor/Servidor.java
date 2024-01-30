/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author Julio A Mayoral
 */
public class Servidor {
    
    //El servidor está encargado de recibir mensajes de los clientes y 
    //reenviarlos a todos los demás clientes conectados. 
    //Además, maneja los mensajes relacionados con archivos.

    private static byte[] datosEntrantes = new byte[256];
    private static final int PUERTO = 8123;
    //Aquí se establece un socket en el puerto para escuchar las comunicaciones.
    private static DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(PUERTO);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<Integer> usuarios = new ArrayList<>();
    
    private static InetAddress direccion = getInetAddress();
    
    //Este método se encarga de obtener la dirección IP correspondiente a "localhost".
    private static InetAddress getInetAddress() {
        try
        {
            return InetAddress.getByName("localhost");
        } catch (UnknownHostException e)
        {
            e.printStackTrace(); // O manejar la excepción según tus necesidades
            return null; // O lanzar una excepción más controlada
        }
    }

    public static void main(String[] args) {

        System.out.println("Servidor iniciado en el puerto " + PUERTO);

        //El servidor está en un bucle infinito, esperando recibir mensajes de los clientes.
        while (true) {
            DatagramPacket paquete = new DatagramPacket(datosEntrantes, datosEntrantes.length);
            try {
                socket.receive(paquete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String mensaje = new String(paquete.getData(), 0, paquete.getLength());
            System.out.println("Servidor recibió: " + mensaje);

            if (mensaje.contains("init;")) {
                usuarios.add(paquete.getPort());
            } else if (mensaje.startsWith("Archivo:")) {
                //
                String nombreArchivo = mensaje.substring("Archivo:".length()).trim();
                System.out.println("Archivo recibido: " + nombreArchivo);
                //
            } else {
                int puertoUsuario = paquete.getPort();
                byte[] mensajeBytes = mensaje.getBytes();

                // Reenviar a todos los usuarios (excepto al que envió el mensaje)
                for (int puertoReenvio : usuarios) {
                    if (puertoReenvio != puertoUsuario) {
                        DatagramPacket reenvio = new DatagramPacket(mensajeBytes, mensajeBytes.length, direccion, puertoReenvio);
                        try {
                            socket.send(reenvio);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
