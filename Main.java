// Tanapat Deesamoot 65050366 
// More Writen code in this Final Quest is since the line 1376 in ImageManager.java
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Read image to find edge part
        ImageManager original = new ImageManager();
        original.read("images/FinalDIP67.bmp");

        // Find Edge Denoise part
        original.contraharmonicFilter(5,-5.5); // Delete peper noise
        original.otsuThreshold(); // Make image to binary
        ArrayList<Point> harrisPoint = original.detectHarrisFeatures(650);
        Point[] corner = original.getCornerPoints(harrisPoint);

        // Get new image to process
        ImageManager im = new ImageManager();
        im.read("images/FinalDIP67.bmp");
        // Make paper straight part
        double[][] srcPoints = {
            { corner[0].x, corner[0].y }, // top-left
            { corner[1].x, corner[1].y }, // top-right
            { corner[2].x, corner[2].y }, // bottom-right
            { corner[3].x, corner[3].y } // bottom-left
        };
        double[][] dstPoints = {
                { 0, 0 }, // top-left
                { 800, 0 }, // top-right
                { 800, 600 }, // bottom-right
                { 0, 600 } // bottom-left
        };
        double[] tmp = im.calculateHomography(srcPoints, dstPoints);
        im.applyHomography(tmp);
        
        // Denoise part
        im.averagingFilter(5);
        im.contraharmonicFilter(7,-7.5); // Delete peper noise
        im.otsuThreshold(); // Make image to binary
        im.fillEdgeOfImage(10); // Fill edge with background color

        // Classify part
        ArrayList<int[]> region = im.detectRegions(100); // Get region and store 3 number MaxX, MinY and MaxY 
        System.out.println("It have " + region.size() + " Region in this image");
        ArrayList<Integer> password = im.classify(region); // Get password from the image
        printPassword(password); // Print the password
        im.write("images/FinalImage.bmp");
    }

    private static void printPassword(ArrayList<Integer> passwordList) {
        String password = "";
        for(int digit : passwordList) password += digit;
        System.out.println("Your password is : " + password);
    }
}