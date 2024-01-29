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

    private static byte[] datosEntrantes = new byte[256];
    private static final int PUERTO = 8000;

    private static DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(PUERTO);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<Integer> usuarios = new ArrayList<>();

    private static final InetAddress direccion;

    static {
        try {
            // Esto se tendría que modificar con la IP si queremos que otro equipo sea el host.
            // SÓLO en el caso en el que otra persona quiera ser el servidor. Si solo se quiere chatear, no haría falta.
            direccion = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        System.out.println("Servidor iniciado en el puerto " + PUERTO);

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
                // Aquí puedes manejar la información del archivo
                String nombreArchivo = mensaje.substring("Archivo:".length()).trim();
                System.out.println("Archivo recibido: " + nombreArchivo);
                // Puedes realizar acciones adicionales, como guardar el archivo o procesar la información según tus necesidades.
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
