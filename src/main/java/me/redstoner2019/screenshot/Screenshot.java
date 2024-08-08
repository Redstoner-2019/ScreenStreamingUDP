package me.redstoner2019.screenshot;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.ptr.PointerByReference;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class Screenshot {
    private int width;
    private int height;

    private User32 user32;
    private GDI32 gdi32;
    private WinDef.HWND hWnd;
    private WinDef.HDC hdcWindow;
    private WinDef.HDC hdcMemDC;
    private WinDef.HBITMAP hBitmap;
    private PointerByReference pBits;

    public Screenshot(int width, int height){
        this.height = height;
        this.width = width;

        user32 = User32.INSTANCE;
        gdi32 = GDI32.INSTANCE;

        hWnd = user32.GetDesktopWindow();
        hdcWindow = user32.GetDC(hWnd);
        hdcMemDC = gdi32.CreateCompatibleDC(hdcWindow);

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // Negative to ensure top-down drawing
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;


        pBits = new PointerByReference();
        hBitmap = gdi32.CreateDIBSection(hdcMemDC, bmi, WinGDI.DIB_RGB_COLORS, pBits, null, 0);
        gdi32.SelectObject(hdcMemDC, hBitmap);
    }

    public BufferedImage screenshot(int x, int y){
        gdi32.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, x, y, GDI32.SRCCOPY);

        // Extract the pixel data
        int[] pixels = new int[width * height];
        Pointer pixelPointer = pBits.getValue();
        pixelPointer.read(0, pixels, 0, pixels.length);

        BufferedImage image = new BufferedImage(width, height, 1);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    public byte[] screenshotBytes(int x, int y){
        gdi32.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, x, y, GDI32.SRCCOPY);

        // Extract the pixel data
        int[] pixels = new int[width * height];
        Pointer pixelPointer = pBits.getValue();
        pixelPointer.read(0, pixels, 0, pixels.length);

        ByteBuffer bb = ByteBuffer.allocate(pixels.length * 4);

        for(int i : pixels){
            bb.put(intToBytes(i));
        }

        return bb.array();
    }

    public int[] screenshotInts(int x, int y){
        gdi32.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, x, y, GDI32.SRCCOPY);

        // Extract the pixel data
        int[] pixels = new int[width * height];
        Pointer pixelPointer = pBits.getValue();
        pixelPointer.read(0, pixels, 0, pixels.length);

        return pixels;
    }

    public static byte[] intToBytes(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }
}
