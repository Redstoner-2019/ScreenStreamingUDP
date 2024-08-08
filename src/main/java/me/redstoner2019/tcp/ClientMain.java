package me.redstoner2019.tcp;

import me.redstoner2019.compression.CompressionUtil;
import me.redstoner2019.compression.DecompressionUtil;
import me.redstoner2019.screenshot.Screenshot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        frame.setSize(1920,1080);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Test");
        frame.setVisible(true);

        JLabel screen = new JLabel();
        frame.add(screen);

        Socket socket = new Socket("localhost",8002);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        Thread recievingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int frames = 0;
                long lastUpdate = System.currentTimeMillis();
                while (true) {
                    try {
                        Object o = in.readObject();
                        if(o instanceof TransferPacket t){
                            BufferedImage img = new BufferedImage(t.getWidth(),t.getHeight(),1);
                            img.setRGB(0, 0, t.getWidth(), t.getHeight(), convertByteArrayToIntArray(DecompressionUtil.decompress(t.getBytes())), 0, t.getWidth());
                            screen.setIcon(new ImageIcon(img));
                            frames++;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Ending");
                        e.printStackTrace();
                        break;
                    }
                    if(System.currentTimeMillis()-lastUpdate > 1000){
                        frame.setTitle(frames + " FPS");
                        lastUpdate = System.currentTimeMillis();
                        frames = 0;
                    }
                }

            }
        });
        recievingThread.start();

        Thread sendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                Screenshot scr = new Screenshot((int) screenRect.getWidth(), (int) screenRect.getHeight());
                while (true) {
                    TransferPacket t = new TransferPacket((int) screenRect.getWidth(), (int) screenRect.getHeight(),CompressionUtil.compress(scr.screenshotBytes(0,0)));
                    try {
                        out.writeObject(t);
                        out.flush();
                    } catch (IOException e) {

                    }
                }
            }
        });
        sendingThread.start();
    }

    public static int[] convertByteArrayToIntArray(byte[] byteArray) {
        // Calculate the length of the resulting int array
        int[] intArray = new int[(int) Math.ceil(byteArray.length / 4.0)];

        // Convert byte array to int array
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = ((byteArray[i * 4] & 0xFF) << 24) |
                    ((byteArray[i * 4 + 1] & 0xFF) << 16) |
                    ((byteArray[i * 4 + 2] & 0xFF) << 8) |
                    (byteArray[i * 4 + 3] & 0xFF);
        }

        return intArray;
    }
}
