package me.redstoner2019.udp;

import me.redstoner2019.compression.CompressionUtil;
import me.redstoner2019.compression.DecompressionUtil;
import me.redstoner2019.screenshot.Screenshot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientOld {
    private static DatagramSocket socket;
    private static InetAddress address;
    public static final int BUFFER_S = 65000;

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        frame.setSize(1920,1080);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Test");
        frame.setVisible(true);

        JLabel screen = new JLabel();
        frame.add(screen);

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");

        //socket.setSendBufferSize(Integer.MAX_VALUE);
        //socket.setReceiveBufferSize(Integer.MAX_VALUE);

        final AtomicInteger[] frames = {new AtomicInteger()};
        final long[] update = {System.currentTimeMillis()};

        Thread recieveThread = new Thread(() -> {
            //Screenshot.init(2560,1440);
            try {
                socket.setSoTimeout(100);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                //screen.setIcon(new ImageIcon(Screenshot.getInstance().screenshot(0,0)));
                try {
                    byte[] BUFFER_SIZE = new byte[BUFFER_S];
                    DatagramPacket packet = new DatagramPacket(BUFFER_SIZE,BUFFER_SIZE.length);
                    socket.receive(packet);
                    if(packet.getLength() != 9) throw new RuntimeException("Packet lost");

                    ByteBuffer header = ByteBuffer.wrap(packet.getData());

                    boolean compressed = header.get() == 1;
                    int packets = header.getInt();
                    int size = header.getInt();

                    System.out.println(compressed + " / " + packets + " / " + size);

                    ByteBuffer imageData = ByteBuffer.allocate(size);

                    for (int i = 0; i < packets; i++) {
                        //System.out.println("Waiting for packet " + i + " / " + packets);
                        socket.receive(packet);
                        ByteBuffer packageBuffer = ByteBuffer.wrap(packet.getData());
                        //System.out.println("Packet size: " + packet.getLength());
                        imageData.put(packageBuffer.get(new byte[packet.getLength()]));
                        if(packet.getLength() == 9) throw new RuntimeException("Packet lost");
                    }

                    byte[] decompressed = imageData.array();

                    if(compressed) decompressed = DecompressionUtil.decompress(decompressed);

                    System.out.println("All Packets Received");

                    IntBuffer image = IntBuffer.allocate(decompressed.length / 4);

                    for (int i = 0; i < decompressed.length; i+=4) {
                        try{
                            image.put(bytesToInt(new byte[]{decompressed[i],decompressed[i+1],decompressed[i+2],decompressed[i+3]}));
                        }catch (Exception e){

                        }
                    }

                    BufferedImage img = new BufferedImage(1920,1080,1);
                    img.setRGB(0, 0, 1920, 1080, image.array() , 0, 1920);
                    screen.setIcon(new ImageIcon(img));

                } catch (Exception e) {
                    //System.out.println("Frame lost");
                }




                /*Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ByteArrayInputStream allData = new ByteArrayInputStream(packet.getData());

                            short x = bytesToShort(allData.readNBytes(2));
                            short y = bytesToShort(allData.readNBytes(2));
                            short w = bytesToShort(allData.readNBytes(2));
                            short h = bytesToShort(allData.readNBytes(2));

                            byte[] bytes;

                            if(DataRunnable.compress) bytes = DecompressionUtil.decompress(allData.readAllBytes());
                            else bytes = allData.readAllBytes();

                            ByteArrayInputStream imageData = new ByteArrayInputStream(bytes);

                            //System.out.println(x + " / " + y + " / " + w + " / " + h);

                            BufferedImage imageRead = ImageIO.read(imageData);

                            Graphics2D g = image.createGraphics();

                            g.drawImage(imageRead,x,y,w,h,null);

                            g.dispose();

                            frames[0].getAndIncrement();

                            if(System.currentTimeMillis() - update[0] > 1000){
                                update[0] = System.currentTimeMillis();
                                System.out.println();
                                System.out.println(frames[0].get() + " updates/s");
                                frames[0].set(0);
                            }

                            screen.setIcon(new ImageIcon(image));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                t.start();*/

            }
        });
        recieveThread.start();

        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        System.out.println(screenRect);

        Screenshot scr = new Screenshot((int) screenRect.getWidth(), (int) screenRect.getHeight());
        //Screenshot scr = new Screenshot(1920,1080);

        int updates = 0;
        long lastUpdate = System.currentTimeMillis();

        while (true) {
            //BufferedImage screenshot = scr.screenshot(0,0);
            int[] ints = scr.screenshotInts(0,0);
            byte[] byteData = convertIntArrayToByteArray(ints);

            boolean compress = false;

            if(compress) byteData = CompressionUtil.compress(byteData);

            List<byte[]> data = new ArrayList<>();

            int i = 0;
            while (i < byteData.length) {
                byte[] b = new byte[Math.min(BUFFER_S, byteData.length - i)];
                for (int j = 0; j < b.length; j++) {
                    b[j] = byteData[i];
                    i++;
                }
                data.add(b);
            }

            ByteBuffer buffer = ByteBuffer.allocate(9);
            buffer.put(compress ? (byte) 1 : (byte) 0);
            buffer.put(intToBytes(data.size()));
            buffer.put(intToBytes(byteData.length));

            socket.send(new DatagramPacket(buffer.array(),buffer.capacity(),address,8002));

            for(byte[] b : data){
                DatagramPacket packet = new DatagramPacket(b,b.length,address,8002);
                packet.setData(b);
                packet.setLength(b.length);
                socket.send(packet);
            }

            //if(true) break;

            //screenshot = Util.resize(screenshot,1280,720);

            /*Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedImage finalScreenshot = Util.resize(screenshot,1280,720);

                    //screenshot = Util.resize(screenshot,2560,1440);

                    int size = 512;

                    for (int x = 0; x < screenRect.getWidth(); x+=size) {
                        for (int y = 0; y < screenRect.getHeight(); y+=size) {
                            try{
                                ImageSender runnable = new ImageSender(finalScreenshot.getSubimage(x,y, (int) Math.min(size, finalScreenshot.getWidth()-x), (int) Math.min(size, finalScreenshot.getHeight()-y)),x,y,socket,address);
                                new Thread(runnable).start();
                            }catch (Exception e){
                                //System.out.println(Math.min(size,screenshot.getWidth()-x) + " " + x + " " + screenshot.getWidth());
                            }
                        }
                    }
                }
            });
            thread.start();*/
            //ImageSender.convertToByteArray(screenshot,"PNG");

            //bytes = CompressionUtil.compress(bytes);

            //List<byte[]> data = new ArrayList<>();

            /*int i = 0;

            while (i<bytes.length) {
                byte[] b = new byte[65000];
                for (int j = 2; j < 65000; j++) {
                    if(i >= bytes.length){
                        break;
                    }
                    b[j] = bytes[i];
                    i++;
                }
                data.add(b);
            }*/

            //System.out.println(data.size());

            updates++;

            if(System.currentTimeMillis() - lastUpdate > 1000){
                lastUpdate = System.currentTimeMillis();
                System.out.println(updates + " FPS sending");
                updates = 0;
            }
        }

        /*int size = 512;
        int threads = 0;

        for (int x = 0; x < screenRect.getWidth(); x+=size) {
            for (int y = 0; y < screenRect.getHeight(); y+=size) {
                DataRunnable runnable = new DataRunnable(x,y,size,size,socket,address);
                new Thread(runnable).start();
                threads++;
            }
        }*/



        //DataRunnable runnable = new DataRunnable(0,0,size,size,socket,address);
        //new Thread(runnable).start();
        //threads++;

        //System.out.println(threads + " Threads");
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
}