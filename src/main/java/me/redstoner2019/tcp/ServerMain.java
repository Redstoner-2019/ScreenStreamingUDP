package me.redstoner2019.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerMain {
    public static List<ClientHandler> clients = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8002);
        Thread clientKeepAlive = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2000);
                        Iterator<ClientHandler> iterator = clients.iterator();
                        while (iterator.hasNext()){
                            ClientHandler client = iterator.next();
                            /*try{
                                client.ping();
                            }catch (Exception e){
                                iterator.remove();
                            }*/
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        clientKeepAlive.start();

        while (true) {
            Socket s = ss.accept();
            ClientHandler client = new ClientHandler(s);
            clients.add(client);
            new Thread(client).start();
        }
    }

    public static void broadcast(Serializable s){
        Iterator<ClientHandler> iterator = clients.iterator();
        while (iterator.hasNext()){
            ClientHandler client = iterator.next();
            try{
                client.sendObject(s);
            }catch (Exception e){
                iterator.remove();
            }
        }
    }
}

class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        while (true) {
            try{
                Serializable s = (Serializable) ois.readObject();
                ServerMain.broadcast(s);
            }catch (Exception e){
                ServerMain.clients.remove(this);
                break;
            }
        }
    }

    public void ping() throws IOException {
        oos.writeObject(new String("ping"));
    }
    public void sendObject(Serializable s) throws IOException{
        oos.writeObject(s);
        oos.flush();
    }
}
