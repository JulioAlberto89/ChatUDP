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
    private static final int PUERTO = 9090;
    //Aquí se establece un socket en el puerto para escuchar las comunicaciones.
    private static DatagramSocket socket;

    static
    {
        try
        {
            socket = new DatagramSocket(PUERTO);
        } catch (SocketException e)
        {
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
        while (true)
        {
            DatagramPacket paquete = new DatagramPacket(datosEntrantes, datosEntrantes.length);
            try
            {
                socket.receive(paquete);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            String mensaje = new String(paquete.getData(), 0, paquete.getLength());
            System.out.println("Servidor recibió: " + mensaje);

            int puertoUsuario = paquete.getPort();

            if (mensaje.startsWith("init;"))
            {
                String nombre = mensaje.substring(5);
                usuarios.add(puertoUsuario);
                reenviarMensaje(nombre + " se ha conectado", puertoUsuario);
            } else if (mensaje.startsWith("exit;"))
            {
                String nombre = mensaje.substring(5);
                usuarios.remove(Integer.valueOf(puertoUsuario));
                reenviarMensaje(nombre + " ha dejado la conversación", puertoUsuario);
            } else
            {
                reenviarMensaje(mensaje, puertoUsuario);
            }
        }
    }

    private static void reenviarMensaje(String mensaje, int puertoUsuario) {
        byte[] mensajeBytes = mensaje.getBytes();
        for (int puertoReenvio : usuarios)
        {
            if (puertoReenvio != puertoUsuario)
            {
                DatagramPacket reenvio = new DatagramPacket(mensajeBytes, mensajeBytes.length, direccion, puertoReenvio);
                try
                {
                    socket.send(reenvio);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
