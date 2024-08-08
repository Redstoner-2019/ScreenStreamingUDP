package me.redstoner2019.udp;

import me.redstoner2019.compression.CompressionUtil;
import me.redstoner2019.compression.DecompressionUtil;
import me.redstoner2019.screenshot.Screenshot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Client {
    private static DatagramSocket socket;
    private static InetAddress address;
    private static int width = 1920;
    private static int height = 1080;

    public static void main(String[] args) throws SocketException, UnknownHostException {
        JFrame frame = new JFrame();
        frame.setSize(1920,1080);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Test");
        frame.setVisible(true);

        JLabel screen = new JLabel();
        frame.add(screen);

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");

        final ByteBuffer[] frameBuffer = {ByteBuffer.allocate(width * height * 4)};

        Thread recieveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Screenshot scr = new Screenshot(1920,1080);

                while (true) {
                    try{
                        byte[] BUFFER = new byte[65535];
                        DatagramPacket packet = new DatagramPacket(BUFFER,BUFFER.length);

                        socket.receive(packet);

                        ByteBuffer packetBuffer = ByteBuffer.wrap(packet.getData());

                        short w = packetBuffer.getShort();
                        short h = packetBuffer.getShort();
                        int index0 = packetBuffer.getInt();
                        int index1 = packetBuffer.getInt();
                        byte[] data = new byte[packetBuffer.capacity() - 12];
                        packetBuffer.get(data);

                        data = DecompressionUtil.decompress(data);

                        if(width != w || height != h){
                            width = w;
                            height = h;
                            frameBuffer[0] = ByteBuffer.allocate(width * height * 4);
                            System.out.println("Recreated Buffer");
                        }

                        replaceBytes(frameBuffer[0],ByteBuffer.wrap(data),index0,index1);
                    }catch (Exception e){
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        recieveThread.start();

        Thread renderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int[] pixelData = convertByteArrayToIntArray(frameBuffer[0].array());
                    BufferedImage img = new BufferedImage(width,height,1);
                    img.setRGB(0, 0, width, height, pixelData , 0, width);
                    screen.setIcon(new ImageIcon(img));
                }
            }
        });
        renderThread.start();

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

                short width = (short) screenRect.getWidth();
                short height = (short) screenRect.getHeight();

                Screenshot scr = new Screenshot((int) screenRect.getWidth(),(int) screenRect.getHeight());

                while (true) {
                    byte[] screenshot = convertIntArrayToByteArray(scr.screenshotInts(0,0));

                    int size = 650000;

                    for (int i = 0; i < screenshot.length; i+=size) {
                        int i1 = Math.min(i + size,screenshot.length);

                        byte[] data = CompressionUtil.compress(Arrays.copyOfRange(screenshot, i,i1));

                        ByteBuffer dataBuffer = ByteBuffer.allocate(data.length + 12);
                        dataBuffer.putShort(width);
                        dataBuffer.putShort(height);
                        dataBuffer.putInt(i);
                        dataBuffer.putInt(i1);
                        dataBuffer.put(data);

                        DatagramPacket packet = new DatagramPacket(new byte[dataBuffer.capacity()],dataBuffer.capacity(),address,8002);
                        packet.setLength(dataBuffer.capacity());
                        packet.setData(dataBuffer.array());

                        try {
                            socket.send(packet);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        sendThread.start();
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

    public static byte[] intToBytes(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }

    public static short bytesToShort(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getShort();
    }

    public static byte[] convertIntArrayToByteArray(int[] intArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length * 4);
        for (int value : intArray) {
            byteBuffer.putInt(value);
        }
        return byteBuffer.array();
    }

    /**
     * Replaces a section of the destination ByteBuffer with the contents of the source ByteBuffer.
     *
     * @param dest          The destination ByteBuffer where the data will be replaced.
     * @param src           The source ByteBuffer that provides the replacement data.
     * @param startPosition The starting position in the destination ByteBuffer where the replacement begins.
     * @param endPosition   The ending position in the destination ByteBuffer where the replacement ends.
     */
    public static void replaceBytes(ByteBuffer dest, ByteBuffer src, int startPosition, int endPosition) {
        if (startPosition < 0 || endPosition > dest.capacity() || startPosition > endPosition) {
            throw new IllegalArgumentException("Invalid start or end position");
        }

        int length = endPosition - startPosition;

        if (src.remaining() < length) {
            throw new IllegalArgumentException("Source ByteBuffer does not have enough data to replace the specified range");
        }

        // Set the position of 'dest' to start at the replacement point
        dest.position(startPosition);

        // Set the limit of 'src' to the number of bytes that should be read
        src.limit(src.position() + length);
        src = src.slice(); // Get a slice of the buffer to ensure we only read the specified range

        // Replace the section in 'dest' with 'src'
        dest.put(src);

        // Optionally reset dest position and limit
        dest.position(0);
        dest.limit(dest.capacity());
    }
}
