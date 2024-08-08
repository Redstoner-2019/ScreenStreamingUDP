package me.redstoner2019.udp;

import me.redstoner2019.compression.CompressionUtil;
import me.redstoner2019.screenshot.Screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class DataRunnable implements Runnable {
    public static boolean compress = true;
    private int x;
    private int y;
    private int w;
    private int h;
    private DatagramSocket socket;
    private InetAddress address;
    private Screenshot screenshot;

    public DataRunnable(int x, int y, int w, int h, DatagramSocket socket, InetAddress address) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.socket = socket;
        this.address = address;
        screenshot = new Screenshot(w,h);
    }

    @Override
    public void run() {
        int updates = 0;
        long lastUpdate = 0;
        long updateTime = 0;
        while (true) {
            try {
                long updateStart = System.currentTimeMillis();

                byte[] BUFFER = capture(x,y,w,h);

                updateTime+=(System.currentTimeMillis() - updateStart);

                DatagramPacket packet = new DatagramPacket(BUFFER,BUFFER.length, address, 8002);
                updates++;
                socket.send(packet);
                if(System.currentTimeMillis() - lastUpdate > 1000){
                    lastUpdate = System.currentTimeMillis();
                    System.out.println(updates + " / s, update avg " + (updateTime / updates));
                    updates = 0;
                    updateTime = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] capture(int x, int y, int w, int h) throws Exception {
        BufferedImage screen = screenshot(x,y,w,h);
        byte[] data = convertToByteArray(screen,"jpg");
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 8);
        buffer.put(ByteBuffer.allocate(2).putShort((short) x).array());
        buffer.put(ByteBuffer.allocate(2).putShort((short) y).array());
        buffer.put(ByteBuffer.allocate(2).putShort((short) w).array());
        buffer.put(ByteBuffer.allocate(2).putShort((short) h).array());
        buffer.put(data);

        return buffer.array();
    }

    public byte[] convertToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        baos.flush();
        byte[] imageInBytes = baos.toByteArray();
        baos.close();
        if(compress) return CompressionUtil.compress(imageInBytes);
        else return imageInBytes;
    }

    public BufferedImage screenshot(int x, int y, int w, int h) {
        return screenshot.screenshot(x,y);
    }
}
