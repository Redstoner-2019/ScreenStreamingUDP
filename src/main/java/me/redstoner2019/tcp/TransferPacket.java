package me.redstoner2019.tcp;

import java.io.Serializable;

public class TransferPacket implements Serializable {
    private byte[] bytes;
    private int width;
    private int height;

    public TransferPacket(int width, int height, byte[] bytes) {
        this.height = height;
        this.width = width;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
