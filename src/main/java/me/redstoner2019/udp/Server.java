package me.redstoner2019.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static me.redstoner2019.udp.Client.BUFFER_S;

public class Server {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(8002);
        socket.setSendBufferSize(Integer.MAX_VALUE);
        socket.setReceiveBufferSize(Integer.MAX_VALUE);
        byte[] BUFFER_SIZE = new byte[BUFFER_S];
        while (true) {
            DatagramPacket packet = new DatagramPacket(BUFFER_SIZE,BUFFER_SIZE.length);
            socket.receive(packet);
            socket.send(packet);
        }
    }
}
