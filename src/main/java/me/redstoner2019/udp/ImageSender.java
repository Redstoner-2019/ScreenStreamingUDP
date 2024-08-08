package me.redstoner2019.udp;

import me.redstoner2019.compression.CompressionUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import static me.redstoner2019.udp.DataRunnable.compress;

public class ImageSender implements Runnable{
    private BufferedImage image;
    private int x;
    private int y;
    private DatagramSocket socket;
    private InetAddress address;

    public ImageSender(BufferedImage image, int x, int y, DatagramSocket socket, InetAddress address) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.socket = socket;
        this.address = address;
    }

    @Override
    public void run() {
        try {
            byte[] BUFFER = capture(x,y,image.getWidth(),image.getHeight());
            DatagramPacket packet = new DatagramPacket(BUFFER,BUFFER.length, address, 8002);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] capture(int x, int y, int w, int h) throws Exception {
        byte[] data = convertToByteArray(image,"bmp");
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 8);
        buffer.put(ByteBuffer.allocate(2).putShort((short) x).array());
        buffer.put(ByteBuffer.allocate(2).putShort((short) y).array());
        buffer.put(ByteBuffer.allocate(2).putShort((short) w).array());
        buffer.put(ByteBuffer.allocate(2).putShort((short) h).array());
        buffer.put(data);

        return buffer.array();
    }

    public static byte[] convertToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        baos.flush();
        byte[] imageInBytes = baos.toByteArray();
        baos.close();
        if(compress) return CompressionUtil.compress(imageInBytes);
        else return imageInBytes;
    }
}
