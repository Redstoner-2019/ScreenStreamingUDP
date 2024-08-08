import me.redstoner2019.screenshot.Screenshot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            // Define the area to capture
            int x = 100; // x-coordinate of the top-left corner
            int y = 100; // y-coordinate of the top-left corner
            int width = 512; // width of the area
            int height = 512; // height of the area

            /*Screenshot.init(512,512);

            // Capture the screen using JNA
            long start = System.currentTimeMillis();
            BufferedImage screenImage = Screenshot.getInstance().screenshot(x, y);
            System.out.println(System.currentTimeMillis() - start);
            if (screenImage != null) {
                // Save the captured image to a file
                ImageIO.write(screenImage, "png", new File("jna_screenshot.png"));
                System.out.println("A screenshot saved!");
            } else {
                System.out.println("Failed to capture the screen.");
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}