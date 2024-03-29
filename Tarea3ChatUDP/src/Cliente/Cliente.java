/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import javax.swing.JTextArea;

/**
 *
 * @author Julio A Mayoral
 */
public class Cliente extends Thread {

    private DatagramSocket socket;
    //Buffer
    private byte[] datosEntrantes = new byte[256];
    //El cliente establece un socket para comunicarse.

    private JTextArea areaTexto;

    public Cliente(DatagramSocket socket, JTextArea areaTexto) {
        this.socket = socket;
        this.areaTexto = areaTexto;
    }

    @Override
    public void run() {
        System.out.println("Iniciando hilo");
        while (true) {
            DatagramPacket paquete = new DatagramPacket(datosEntrantes, datosEntrantes.length);
            try {
                socket.receive(paquete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String mensaje = new String(paquete.getData(), 0, paquete.getLength());
            if (mensaje.startsWith("init;")) {
                String nombre = mensaje.substring(5);
                areaTexto.setText(areaTexto.getText() + nombre + " se ha conectado\n");
            } else if (mensaje.startsWith("exit;")) {
                String nombre = mensaje.substring(5);
                areaTexto.setText(areaTexto.getText() + nombre + " ha dejado la conversación\n");
            } else {
                areaTexto.setText(areaTexto.getText() + mensaje + "\n");
            }
        }
    }
}
