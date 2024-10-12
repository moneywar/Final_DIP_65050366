import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.awt.Color;

class ImageManager {
    public int width;
    public int height;
    public int bitDepth;

    private BufferedImage img;
    private BufferedImage original;

    public ImageManager() {
    }

    public boolean read(String fileName) {
        try {
            img = ImageIO.read(new File(fileName));

            width = img.getWidth();
            height = img.getHeight();
            bitDepth = img.getColorModel().getPixelSize();
            System.out.println("Image " + fileName + " with " + width + " x " + height + " pixels (" + bitDepth
                    + " bitsper pixel) has been read!");

            original = new BufferedImage(width, height, img.getType());
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    original.setRGB(x, y, img.getRGB(x, y));
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean write(String fileName) {
        try {
            ImageIO.write(img, "bmp", new File(fileName));
            System.out.println("Image " + fileName + " has been written!");

            return true;
        } catch (IOException e) {
            System.out.println(e);

            return false;
        } catch (NullPointerException e) {
            System.out.println(e);
            return false;
        }
    }

    public void restoreToOriginal() {
        width = original.getWidth();
        height = original.getHeight();
        img = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, original.getRGB(x, y));
            }
        }
    }

    public void convertToGrayscale() {
        if (img == null)
            return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                int avg = (r + g + b) / 3;
                int newColor = (avg << 16) | (avg << 8) | avg;

                img.setRGB(x, y, newColor);
            }
        }
    }

    public void invert() {
        if (img == null)
            return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                color = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, color);
            }
        }
    }

    public void addSaltNoise(double percent) {
        if (img == null)
            return;
        double noOfPX = height * width;
        int noiseAdded = (int) (percent * noOfPX);
        Random rnd = new Random();
        int whiteColor = 255 << 16 | 255 << 8 | 255;
        for (int i = 1; i <= noiseAdded; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            img.setRGB(x, y, whiteColor);
        }
    }

    public void addPepperNoise(double percent) {
        if (img == null)
            return;
        double noOfPX = height * width;
        int noiseAdded = (int) (percent * noOfPX);
        Random rnd = new Random();
        int blackColor = 0;
        for (int i = 1; i <= noiseAdded; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            img.setRGB(x, y, blackColor);
        }
    }

    public void addPepperAndSalt(double percent) {
        addPepperNoise(percent);
        addSaltNoise(percent);
    }

    public void addUniformNoise(double percent, int distribution) {
        if (img == null)
            return;
        double noOfPX = height * width;
        int noiseAdded = (int) (percent * noOfPX);
        Random rnd = new Random();
        for (int i = 1; i <= noiseAdded; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            int color = img.getRGB(x, y);
            int gray = color & 0xff;
            gray += (rnd.nextInt(distribution * 2) - distribution);
            gray = gray > 255 ? 255 : gray;
            gray = gray < 0 ? 0 : gray;
            int newColor = gray << 16 | gray << 8 | gray;
            img.setRGB(x, y, newColor);
        }
    }

    public void contraharmonicFilter(int size, double Q) {
        if (img == null)
            return;
        if (size % 2 == 0) {
            System.out.println("Size Invalid: must be odd number!");
            return;
        }
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sumRedAbove = 0, sumGreenAbove = 0, sumBlueAbove = 0;
                double sumRedBelow = 0, sumGreenBelow = 0, sumBlueBelow = 0;
                for (int i = y - size / 2; i <= y + size / 2; i++) {
                    for (int j = x - size / 2; j <= x + size / 2; j++) {
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            int color = img.getRGB(j, i);
                            int r = (color >> 16) & 0xff;
                            int g = (color >> 8) & 0xff;
                            int b = color & 0xff;
                            sumRedAbove += Math.pow(r, Q + 1);
                            sumGreenAbove += Math.pow(g, Q + 1);
                            sumBlueAbove += Math.pow(b, Q + 1);
                            sumRedBelow += Math.pow(r, Q);
                            sumGreenBelow += Math.pow(g, Q);
                            sumBlueBelow += Math.pow(b, Q);
                        }
                    }
                }
                sumRedAbove /= sumRedBelow;
                sumRedAbove = sumRedAbove > 255 ? 255 : sumRedAbove;
                sumRedAbove = sumRedAbove < 0 ? 0 : sumRedAbove;
                sumGreenAbove /= sumGreenBelow;
                sumGreenAbove = sumGreenAbove > 255 ? 255 : sumGreenAbove;
                sumGreenAbove = sumGreenAbove < 0 ? 0 : sumGreenAbove;
                sumBlueAbove /= sumBlueBelow;
                sumBlueAbove = sumBlueAbove > 255 ? 255 : sumBlueAbove;
                sumBlueAbove = sumBlueAbove < 0 ? 0 : sumBlueAbove;

                int newColor = ((int) sumRedAbove << 16) | ((int) sumGreenAbove << 8) | (int) sumBlueAbove;

                tempBuf.setRGB(x, y, newColor);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void alphaTrimmedFilter(int size, int d) {
        if (img == null)
            return;
        if (size % 2 == 0) {
            System.out.println("Size Invalid: must be odd number!");
            return;
        }
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] kernelRed = new int[size * size];
                int[] kernelGreen = new int[size * size];
                int[] kernelBlue = new int[size * size];
                for (int i = y - size / 2; i <= y + size / 2; i++) {
                    for (int j = x - size / 2; j <= x + size / 2; j++) {
                        int r, g, b;
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            int color = img.getRGB(j, i);
                            r = (color >> 16) & 0xff;
                            g = (color >> 8) & 0xff;
                            b = color & 0xff;
                            kernelRed[(i - (y - size / 2)) * size + (j - (x - size / 2))] = r;
                            kernelGreen[(i - (y - size / 2)) * size + (j - (x - size / 2))] = g;
                            kernelBlue[(i - (y - size / 2)) * size + (j - (x - size / 2))] = b;
                        }
                    }
                }
                for (int i = 0; i < size * size - 1; i++) {
                    for (int j = 0; j < size * size - i - 1; j++) {
                        int temp;
                        if (kernelRed[j] > kernelRed[j + 1]) {
                            temp = kernelRed[j];
                            kernelRed[j] = kernelRed[j + 1];
                            kernelRed[j + 1] = temp;
                        }
                        if (kernelGreen[j] > kernelGreen[j + 1]) {
                            temp = kernelGreen[j];
                            kernelGreen[j] = kernelGreen[j + 1];
                            kernelGreen[j + 1] = temp;
                        }
                        if (kernelBlue[j] > kernelBlue[j + 1]) {
                            temp = kernelBlue[j];
                            kernelBlue[j] = kernelBlue[j + 1];
                            kernelBlue[j + 1] = temp;
                        }
                    }
                }
                int remainingPixel = size * size - d;
                int red = 0, green = 0, blue = 0;
                for (int i = 0; i < remainingPixel; i++) {
                    red += kernelRed[(d / 2) + i];
                    green += kernelGreen[(d / 2) + i];
                    blue += kernelBlue[(d / 2) + i];
                }

                red /= remainingPixel;
                red = red > 255 ? 255 : red;
                red = red < 0 ? 0 : red;
                green /= remainingPixel;
                green = green > 255 ? 255 : green;
                green = green < 0 ? 0 : green;
                blue /= remainingPixel;
                blue = blue > 255 ? 255 : blue;
                blue = blue < 0 ? 0 : blue;
                int newColor = (red << 16) | (green << 8) | blue;
                tempBuf.setRGB(x, y, newColor);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public int[] getGrayscaleHistogram() {
        if (img == null)
            return null;
        convertToGrayscale();
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int gray = color & 0xff;
                histogram[gray]++;
            }
        }
        restoreToOriginal();
        return histogram;
    }

    public float getContrast() {
        if (img == null)
            return 0;
        float contrast = 0;
        int[] histogram = getGrayscaleHistogram();
        float avgIntensity = 0;
        float pixelNum = width * height;
        for (int i = 0; i < histogram.length; i++) {
            avgIntensity += histogram[i] * i;
        }
        avgIntensity /= pixelNum;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int value = color & 0xff;
                contrast += Math.pow((value) - avgIntensity, 2);
            }
        }
        contrast = (float) Math.sqrt(contrast / pixelNum);
        return contrast;
    }

    // Quest 4
    public void adjustContrast(int contrast) {
        if (img == null)
            return;
        float currentContrast = getContrast();
        int[] histogram = getGrayscaleHistogram();
        float avgIntensity = 0;
        float pixelNum = width * height;
        for (int i = 0; i < histogram.length; i++) {
            avgIntensity += histogram[i] * i;
        }
        avgIntensity /= pixelNum;
        float min = avgIntensity - currentContrast;
        float max = avgIntensity + currentContrast;
        float newMin = avgIntensity - currentContrast - contrast / 2;
        float newMax = avgIntensity + currentContrast + contrast / 2;
        newMin = newMin < 0 ? 0 : newMin;
        newMax = newMax < 0 ? 0 : newMax;
        newMin = newMin > 255 ? 255 : newMin;
        newMax = newMax > 255 ? 255 : newMax;
        if (newMin > newMax) {
            float temp = newMax;
            newMax = newMin;
            newMin = temp;
        }
        float contrastFactor = (newMax - newMin) / (max - min);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                r = (int) ((r - min) * contrastFactor + newMin);
                r = r > 255 ? 255 : r;
                r = r < 0 ? 0 : r;
                g = (int) ((g - min) * contrastFactor + newMin);
                g = g > 255 ? 255 : g;
                g = g < 0 ? 0 : g;
                b = (int) ((b - min) * contrastFactor + newMin);
                b = b > 255 ? 255 : b;
                b = b < 0 ? 0 : b;
                color = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, color);
            }
        }
    }

    public void resizeNearestNeighbour(double scaleX, double scaleY) {
        if (img == null)
            return;
        int newWidth = (int) Math.round(width * scaleX);
        int newHeight = (int) Math.round(height * scaleY);
        BufferedImage tempBuf = new BufferedImage(newWidth, newHeight, img.getType());
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int xNearest = (int) Math.round(x / scaleX);
                int yNearest = (int) Math.round(y / scaleY);
                xNearest = xNearest >= width ? width - 1 : xNearest;
                xNearest = xNearest < 0 ? 0 : xNearest;
                yNearest = yNearest >= height ? height - 1 : yNearest;
                yNearest = yNearest < 0 ? 0 : yNearest;
                tempBuf.setRGB(x, y, img.getRGB(xNearest, yNearest));
            }
        }
        img = new BufferedImage(newWidth, newHeight, img.getType());
        width = newWidth;
        height = newHeight;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void resizeBilinear(double scaleX, double scaleY) {
        if (img == null)
            return;
        int newWidth = (int) Math.round(width * scaleX);
        int newHeight = (int) Math.round(height * scaleY);
        BufferedImage tempBuf = new BufferedImage(newWidth, newHeight, img.getType());
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                double oldX = x / scaleX;
                double oldY = y / scaleY;
                // get 4 coordinates
                int x1 = Math.min((int) Math.floor(oldX), width - 1);
                int y1 = Math.min((int) Math.floor(oldY), height - 1);
                int x2 = Math.min((int) Math.ceil(oldX), width - 1);
                int y2 = Math.min((int) Math.ceil(oldY), height - 1);
                // get colours
                int color11 = img.getRGB(x1, y1);
                int r11 = (color11 >> 16) & 0xff;
                int g11 = (color11 >> 8) & 0xff;
                int b11 = color11 & 0xff;

                int color12 = img.getRGB(x1, y2);
                int r12 = (color12 >> 16) & 0xff;
                int g12 = (color12 >> 8) & 0xff;
                int b12 = color12 & 0xff;

                int color21 = img.getRGB(x2, y1);
                int r21 = (color21 >> 16) & 0xff;
                int g21 = (color21 >> 8) & 0xff;
                int b21 = color21 & 0xff;

                int color22 = img.getRGB(x2, y2);
                int r22 = (color22 >> 16) & 0xff;
                int g22 = (color22 >> 8) & 0xff;
                int b22 = color22 & 0xff;
                // interpolate x
                double P1r = (x2 - oldX) * r11 + (oldX - x1) * r21;
                double P1g = (x2 - oldX) * g11 + (oldX - x1) * g21;
                double P1b = (x2 - oldX) * b11 + (oldX - x1) * b21;
                double P2r = (x2 - oldX) * r12 + (oldX - x1) * r22;
                double P2g = (x2 - oldX) * g12 + (oldX - x1) * g22;
                double P2b = (x2 - oldX) * b12 + (oldX - x1) * b22;
                if (x1 == x2) {
                    P1r = r11;
                    P1g = g11;
                    P1b = b11;
                    P2r = r22;
                    P2g = g22;
                    P2b = b22;
                }
                // interpolate y
                double Pr = (y2 - oldY) * P1r + (oldY - y1) * P2r;
                double Pg = (y2 - oldY) * P1g + (oldY - y1) * P2g;
                double Pb = (y2 - oldY) * P1b + (oldY - y1) * P2b;
                if (y1 == y2) {
                    Pr = P1r;
                    Pg = P1g;
                    Pb = P1b;
                }
                int r = (int) Math.round(Pr);
                int g = (int) Math.round(Pg);
                int b = (int) Math.round(Pb);
                r = r > 255 ? 255 : r;
                r = r < 0 ? 0 : r;
                g = g > 255 ? 255 : g;
                g = g < 0 ? 0 : g;
                b = b > 255 ? 255 : b;
                b = b < 0 ? 0 : b;
                int newColor = (r << 16) | (g << 8) | b;
                tempBuf.setRGB(x, y, newColor);
            }
        }
        img = new BufferedImage(newWidth, newHeight, img.getType());
        width = newWidth;
        height = newHeight;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void erosion(StructuringElement se) {
        if (img == null)
            return;
        convertToGrayscale();
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isEroded = true;
                int min = Integer.MAX_VALUE;
                se_check: for (int i = y - se.origin.y; i < y + se.height - se.origin.y; i++) {
                    for (int j = x - se.origin.x; j < x + se.width - se.origin.x; j++) {
                        int seCurrentX = j - (x - se.origin.x);
                        int seCurrentY = i - (y - se.origin.y);
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            if (!se.ignoreElements.contains(new Point(seCurrentX, seCurrentY))) {
                                int color = img.getRGB(j, i);
                                int gray = color & 0xff;
                                if (se.elements[seCurrentX][seCurrentY] != gray) {
                                    isEroded = false;
                                    break se_check;
                                } else if (min > gray)
                                    min = gray;
                            }
                        } else {
                            isEroded = false;
                            break se_check;
                        }
                    }
                }
                int newGray = 0;
                if (isEroded) {
                    newGray = min;
                }
                int newColor = (newGray << 16) | (newGray << 8) | newGray;
                tempBuf.setRGB(x, y, newColor);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void dilation(StructuringElement se) {
        if (img == null)
            return;
        convertToGrayscale();
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isDilated = false;
                se_check: for (int i = y - (se.height - se.origin.y - 1); i < y + se.height -
                        (se.height - se.origin.y - 1); i++) {
                    for (int j = x - (se.width - se.origin.x - 1); j < x + se.width -
                            (se.width - se.origin.x - 1); j++) {
                        int seCurrentX = se.width - (j - x + se.origin.x) - 1;
                        int seCurrentY = se.height - (i - y + se.origin.y) - 1;
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            if (!se.ignoreElements.contains(new Point(seCurrentX, seCurrentY))) {
                                int color = img.getRGB(j, i);
                                int gray = color & 0xff;
                                if (se.elements[seCurrentX][seCurrentY] == gray) {
                                    isDilated = true;
                                    break se_check;
                                }
                            }
                        } else {
                            isDilated = false;
                            break se_check;
                        }
                    }
                }
                if (isDilated) {
                    int max = Integer.MIN_VALUE;
                    for (int i = y - (se.height - se.origin.y - 1); i < y + se.height -
                            (se.height - se.origin.y - 1); i++) {
                        for (int j = x - (se.width - se.origin.x - 1); j < x + se.width
                                - (se.width - se.origin.x - 1); j++) {
                            if (i >= 0 && i < height && j >= 0 && j < width) {
                                int color = img.getRGB(j, i);
                                int gray = color & 0xff;
                                if (max < gray)
                                    max = gray;
                            }
                        }
                    }
                    int newGray = max;
                    int newColor = (newGray << 16) | (newGray << 8) | newGray;
                    tempBuf.setRGB(x, y, newColor);
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void BoundaryExtraction(StructuringElement se) {
        BufferedImage newImg = new BufferedImage(width, height, img.getType());

        erosion(se);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int color = img.getRGB(j, i) & 0xff;
                int colorOriginal = original.getRGB(j, i) & 0xff;
                int gray = colorOriginal - color >= 0 ? colorOriginal - color : 0;
                int newColor = (gray << 16) | (gray << 8) | gray;
                newImg.setRGB(j, i, newColor);
            }
        }
        img = newImg;
    }

    public void thresholding(int threshold) {
        if (img == null)
            return;
        convertToGrayscale();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int gray = color & 0xff;
                gray = gray < threshold ? 0 : 255;
                color = (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, color);
            }
        }
    }

    public void otsuThreshold() {
        if (img == null)
            return;
        convertToGrayscale();
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                int gray = color & 0xff;
                histogram[gray]++;
            }
        }
        float[] histogramNorm = new float[histogram.length];
        float pixelNum = width * height;
        for (int i = 0; i < histogramNorm.length; i++) {
            histogramNorm[i] = histogram[i] / pixelNum;
        }
        float[] histogramCS = new float[256];
        float[] histogramMean = new float[256];
        for (int i = 0; i < 256; i++) {
            if (i == 0) {
                histogramCS[i] = histogramNorm[i];
                histogramMean[i] = 0;
            } else {
                histogramCS[i] = histogramCS[i - 1] + histogramNorm[i];
                histogramMean[i] = histogramMean[i - 1] + histogramNorm[i] * i;
            }
        }
        float globalMean = histogramMean[255];
        float max = Float.MIN_VALUE;
        float maxVariance = Float.MIN_VALUE;
        int countMax = 0;
        for (int i = 0; i < 256; i++) {
            float variance = (float) Math.pow(globalMean * histogramCS[i] - histogramMean[i],

                    2) / (histogramCS[i] * (1 - histogramCS[i]));
            if (variance > maxVariance) {
                maxVariance = variance;
                max = i;
                countMax = 1;
            } else if (variance == maxVariance) {
                countMax++;
                max = ((max * (countMax - 1)) + i) / countMax;
            }
        }
        thresholding((int) Math.round(max));
    }

    public void linearSpatialFilter(double[] kernel, int size) {
        if (img == null)
            return;
        if (size % 2 == 0) {
            System.out.println("Size Invalid: must be odd number!");
            return;
        }
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sumRed = 0, sumGreen = 0, sumBlue = 0;
                for (int i = y - size / 2; i <= y + size / 2; i++) {
                    for (int j = x - size / 2; j <= x + size / 2; j++) {
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            int color = img.getRGB(j, i);
                            int r = (color >> 16) & 0xff;
                            int g = (color >> 8) & 0xff;
                            int b = color & 0xff;

                            sumRed += r * kernel[(i - (y - size / 2)) * size + (j - (x - size / 2))];

                            sumGreen += g * kernel[(i - (y - size / 2)) * size + (j - (x - size / 2))];

                            sumBlue += b * kernel[(i - (y - size / 2)) * size + (j - (x - size / 2))];

                        }
                    }
                }
                sumRed = sumRed > 255 ? 255 : sumRed;
                sumRed = sumRed < 0 ? 0 : sumRed;
                sumGreen = sumGreen > 255 ? 255 : sumGreen;
                sumGreen = sumGreen < 0 ? 0 : sumGreen;
                sumBlue = sumBlue > 255 ? 255 : sumBlue;
                sumBlue = sumBlue < 0 ? 0 : sumBlue;
                int newColor = ((int) sumRed << 16) | ((int) sumGreen << 8) | (int) sumBlue;
                tempBuf.setRGB(x, y, newColor);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void cannyEdgeDetector(int lower, int upper) {
        // Step 1 - Apply 5 x 5 Gaussian filter
        double[] gaussian = { 2.0 / 159.0, 4.0 / 159.0, 5.0 / 159.0, 4.0 / 159.0, 2.0 / 159.0,
                4.0 / 159.0, 9.0 / 159.0, 12.0 / 159.0, 9.0 / 159.0, 4.0 / 159.0,
                5.0 / 159.0, 12.0 / 159.0, 15.0 / 159.0, 12.0 / 159.0, 5.0 / 159.0,
                4.0 / 159.0, 9.0 / 159.0, 12.0 / 159.0, 9.0 / 159.0, 4.0 / 159.0,
                2.0 / 159.0, 4.0 / 159.0, 5.0 / 159.0, 4.0 / 159.0, 2.0 / 159.0 };
        linearSpatialFilter(gaussian, 5);
        convertToGrayscale();

        // Step 2 - Find intensity gradient
        double[] sobelX = { 1, 0, -1, 2, 0, -2, 1, 0, -1 };
        double[] sobelY = { 1, 2, 1, 0, 0, 0, -1, -2, -1 };
        double[][] magnitude = new double[height][width];
        double[][] direction = new double[height][width];
        for (int y = 3; y < height - 3; y++) {
            for (int x = 3; x < width - 3; x++) {
                double gx = 0, gy = 0;
                for (int i = y - 1; i <= y + 1; i++) {
                    for (int j = x - 1; j <= x + 1; j++) {
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            int color = img.getRGB(j, i);
                            int gray = color & 0xff;
                            gx += gray * sobelX[(i - (y - 1)) * 3 + (j - (x - 1))];
                            gy += gray * sobelY[(i - (y - 1)) * 3 + (j - (x - 1))];
                        }
                    }
                }
                magnitude[y][x] = Math.sqrt(gx * gx + gy * gy);
                direction[y][x] = Math.atan2(gy, gx) * 180 / Math.PI;
            }
        }

        // Step 3 - Nonmaxima Suppression
        double[][] gn = new double[height][width];
        for (int y = 3; y < height - 3; y++) {
            for (int x = 3; x < width - 3; x++) {
                int targetX = 0, targetY = 0;
                // find closest direction
                if (direction[y][x] <= -157.5) {
                    targetX = 1;
                    targetY = 0;
                } else if (direction[y][x] <= -112.5) {
                    targetX = 1;
                    targetY = -1;
                } else if (direction[y][x] <= -67.5) {
                    targetX = 0;
                    targetY = 1;
                } else if (direction[y][x] <= -22.5) {
                    targetX = 1;
                    targetY = 1;
                } else if (direction[y][x] <= 22.5) {
                    targetX = 1;
                    targetY = 0;
                } else if (direction[y][x] <= 67.5) {
                    targetX = 1;
                    targetY = -1;
                } else if (direction[y][x] <= 112.5) {
                    targetX = 0;
                    targetY = 1;
                } else if (direction[y][x] <= 157.5) {
                    targetX = 1;
                    targetY = 1;
                } else {
                    targetX = 1;
                    targetY = 0;
                }
                if (y + targetY >= 0 && y + targetY < height &&
                        x + targetX >= 0 && x + targetX < width &&
                        magnitude[y][x] < magnitude[y + targetY][x + targetX]) {
                    gn[y][x] = 0;
                } else if (y - targetY >= 0 && y - targetY < height &&
                        x - targetX >= 0 && x - targetX < width &&
                        magnitude[y][x] < magnitude[y - targetY][x - targetX]) {
                    gn[y][x] = 0;
                } else {
                    gn[y][x] = magnitude[y][x];
                }
            }
        }

        // Step 4 - Hysteresis Thresholding
        // set back first
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newGray = (int) gn[y][x];
                newGray = newGray > 255 ? 255 : newGray;
                newGray = newGray < 0 ? 0 : newGray;
                int newColor = (newGray << 16) | (newGray << 8) | newGray;
                img.setRGB(x, y, newColor);
            }
        }
        // upper threshold checking with recursive
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int checking = img.getRGB(x, y) & 0xff;
                if (checking >= upper) {
                    checking = 255;
                    int newColor = (checking << 16) | (checking << 8) | checking;
                    img.setRGB(x, y, newColor);
                    hystConnect(x, y, lower);
                }
            }
        }
        // clear unwanted values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int checking = img.getRGB(x, y) & 0xff;
                if (checking != 255) {
                    int newColor = (0 << 16) | (0 << 8) | 0;
                    img.setRGB(x, y, newColor);
                }
            }
        }
    }

    private void hystConnect(int x, int y, int threshold) {
        int value = 0;
        for (int i = y - 1; i <= y + 1; i++) {
            for (int j = x - 1; j <= x + 1; j++) {
                if ((j < width) && (i < height) &&
                        (j >= 0) && (i >= 0) &&
                        (j != x) && (i != y)) {
                    value = img.getRGB(j, i) & 0xff;
                    if (value != 255) {
                        if (value >= threshold) {
                            int newColor = (255 << 16) | (255 << 8) | 255;
                            img.setRGB(j, i, newColor);
                            hystConnect(j, i, threshold);
                        } else {

                            int newColor = (0 << 16) | (0 << 8) | 0;
                            img.setRGB(j, i, newColor);
                        }
                    }
                }
            }
        }
    }

    public void houghTransform(double percent) {
        // The image should be converted to edge map first
        // Work out how the hough space is quantized
        int numOfTheta = 720;
        double thetaStep = Math.PI / numOfTheta;
        int highestR = (int) (Math.max(width, height) * Math.sqrt(2));
        int centreX = width / 2;
        int centreY = height / 2;
        System.out.println("Hough array w: " + numOfTheta + " height: " + 2 * highestR);
        // Create the hough array and initialize to zero
        int[][] houghArray = new int[numOfTheta][2 * highestR];
        for (int i = 0; i < numOfTheta; i++) {
            for (int j = 0; j < 2 * highestR; j++) {
                houghArray[i][j] = 0;
            }
        }
        // Step 1 - find each edge pixel
        // Find edge points and vote in array
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pointColor = img.getRGB(x, y) & 0xff;
                if (pointColor != 0) {
                    // Edge pixel found
                    for (int i = 0; i < numOfTheta; i++) {
                        // Step 2 - Apply the line equation and update hough array
                        // Work out the r values for each theta step
                        int r = (int) ((x - centreX) * Math.cos(i * thetaStep)
                                + (y - centreY) * Math.sin(i * thetaStep));
                        // Move all values into positive range for display purposes
                        r = r + highestR;
                        if (r < 0 || r >= 2 * highestR)
                            continue;
                        // Increment hough array
                        houghArray[i][r]++;
                    }
                }
            }
        }

        // Step 3 - Apply threshold to hough array to find line
        int maxHough = 0;
        for (int i = 0; i < numOfTheta; i++) {
            for (int j = 0; j < 2 * highestR; j++) {
                // Find the max hough value for the thresholding operation
                if (houghArray[i][j] > maxHough) {
                    maxHough = houghArray[i][j];
                }
            }
        }
        // Set the threshold limit
        int threshold = (int) (percent * maxHough);
        // Step 4 - Draw lines
        // Search for local peaks above threshold to draw
        for (int i = 0; i < numOfTheta; i++) {
            for (int j = 0; j < 2 * highestR; j++) {
                // only consider points above threshold
                if (houghArray[i][j] >= threshold) {
                    // see if local maxima
                    boolean draw = true;
                    int peak = houghArray[i][j];
                    for (int k = -1; k <= 1; k++) {
                        for (int l = -1; l <= 1; l++) {
                            // not seeing itself
                            if (k == 0 && l == 0)
                                continue;
                            int testTheta = i + k;
                            int testOffset = j + l;
                            if (testOffset < 0 || testOffset >= 2 * highestR)
                                continue;
                            if (testTheta < 0)
                                testTheta = testTheta + numOfTheta;
                            if (testTheta >= numOfTheta)
                                testTheta = testTheta - numOfTheta;
                            if (houghArray[testTheta][testOffset] > peak) {
                                // found bigger point
                                draw = false;
                                break;
                            }
                        }
                    }
                    // point found is not local maxima
                    if (!draw)
                        continue;
                    // if local maxima, draw red back
                    double tsin = Math.sin(i * thetaStep);
                    double tcos = Math.cos(i * thetaStep);
                    if (i <= numOfTheta / 4 || i >= (3 * numOfTheta) / 4) {
                        for (int y = 0; y < height; y++) {
                            // vertical line

                            int x = (int) (((j - highestR) - ((y - centreY) * tsin)) / tcos) + centreX;

                            if (x < width && x >= 0) {
                                int redColor = (255 << 16) | (0 << 8) | 0;
                                img.setRGB(x, y, redColor);
                            }
                        }
                    } else {
                        for (int x = 0; x < width; x++) {
                            // horizontal line

                            int y = (int) (((j - highestR) - ((x - centreX) * tcos)) / tsin) + centreY;

                            if (y < height && y >= 0) {
                                int redColor = (255 << 16) | (0 << 8) | 0;
                                img.setRGB(x, y, redColor);
                            }
                        }
                    }
                }
            }
        }
    }

    public void ADIAbsolute(String[] sequences, int threshold, int step) {
        if (img == null)
            return;
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int n = 0; n < sequences.length; n++) {
            BufferedImage otherImage = null;
            try {
                otherImage = ImageIO.read(new File(sequences[n]));
            } catch (IOException e) {
                System.out.println(e);
                return;
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color1 = img.getRGB(x, y);
                    int r1 = (color1 >> 16) & 0xff;
                    int g1 = (color1 >> 8) & 0xff;
                    int b1 = color1 & 0xff;
                    int color2 = otherImage.getRGB(x, y);
                    int r2 = (color2 >> 16) & 0xff;
                    int g2 = (color2 >> 8) & 0xff;
                    int b2 = color2 & 0xff;
                    int dr = r1 - r2;
                    int dg = g1 - g2;
                    int db = b1 - b2;
                    int dGray = (int) Math.round(0.2126 * dr + 0.7152 * dg + 0.0722 * db);
                    if (Math.abs(dGray) > threshold) {
                        int currentColor = tempBuf.getRGB(x, y) & 0xff;
                        currentColor += step;
                        currentColor = currentColor > 255 ? 255 : currentColor;
                        currentColor = currentColor < 0 ? 0 : currentColor;

                        int newColor = (currentColor << 16) | (currentColor << 8) | currentColor;

                        tempBuf.setRGB(x, y, newColor);
                    }
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public void ADINegative(String[] sequences, int threshold, int step) {
        if (img == null)
            return;
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int n = 0; n < sequences.length; n++) {
            BufferedImage otherImage = null;
            try {
                otherImage = ImageIO.read(new File(sequences[n]));
            } catch (IOException e) {
                System.out.println(e);
                return;
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color1 = img.getRGB(x, y);
                    int r1 = (color1 >> 16) & 0xff;
                    int g1 = (color1 >> 8) & 0xff;
                    int b1 = color1 & 0xff;
                    int color2 = otherImage.getRGB(x, y);
                    int r2 = (color2 >> 16) & 0xff;
                    int g2 = (color2 >> 8) & 0xff;
                    int b2 = color2 & 0xff;
                    int dr = r1 - r2;
                    int dg = g1 - g2;
                    int db = b1 - b2;
                    int dGray = (int) Math.round(0.2126 * dr + 0.7152 * dg + 0.0722 * db);
                    if (dGray < -threshold) {
                        int currentColor = tempBuf.getRGB(x, y) & 0xff;
                        currentColor += step;
                        currentColor = currentColor > 255 ? 255 : currentColor;
                        currentColor = currentColor < 0 ? 0 : currentColor;

                        int newColor = (currentColor << 16) | (currentColor << 8) | currentColor;

                        tempBuf.setRGB(x, y, newColor);
                    }
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    public ArrayList<Point> detectHarrisFeatures(int strongest) {
        // convert to gray scale first
        double[][] Ix = new double[height][width];
        double[][] Iy = new double[height][width];
        // Initialize matrices to store products of gradients
        double[][] Ix2 = new double[height][width];
        double[][] Iy2 = new double[height][width];
        double[][] Ixy = new double[height][width];
        // Compute gradients Ix and Iy, drop the border
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int color = img.getRGB(x, y);
                int gray = color & 0xff;
                Ix[y][x] = ((img.getRGB(x + 1, y) & 0xff) - (img.getRGB(x - 1, y) & 0xff)) / 2.0;

                Iy[y][x] = ((img.getRGB(x, y + 1) & 0xff) - (img.getRGB(x, y - 1) & 0xff)) / 2.0;

                Ix2[y][x] = Ix[y][x] * Ix[y][x];
                Iy2[y][x] = Iy[y][x] * Iy[y][x];
                Ixy[y][x] = Ix[y][x] * Iy[y][x];
            }
        }

        // apply 3 x 3 gaussian smoothing for each matrices
        double[][] Sx2 = new double[height][width];
        double[][] Sy2 = new double[height][width];
        double[][] Sxy = new double[height][width];
        double[] gaussian = {
                1.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0,
                2.0 / 16.0, 4.0 / 16.0, 1.0 / 16.0,
                1.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0
        };
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                Sx2[y][x] = 0;
                Sy2[y][x] = 0;
                Sxy[y][x] = 0;
                for (int i = y - 1; i <= y + 1; i++) {
                    for (int j = x - 1; j <= x + 1; j++) {
                        Sx2[y][x] += Ix2[i][j] * gaussian[(i - (y - 1)) * 3 + (j - (x - 1))];
                        Sy2[y][x] += Iy2[i][j] * gaussian[(i - (y - 1)) * 3 + (j - (x - 1))];
                        Sxy[y][x] += Ixy[i][j] * gaussian[(i - (y - 1)) * 3 + (j - (x - 1))];
                    }
                }
            }
        }

        double[][] corners = new double[height][width];
        // Compute the corner response function R
        // High R = Corner, Low R = Flat, Negative R = Edge
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double det = Sx2[y][x] * Sy2[y][x] - Sxy[y][x] * Sxy[y][x];
                double trace = Sx2[y][x] + Sy2[y][x];
                corners[y][x] = det - 0.04 * trace * trace;
            }
        }

        ArrayList<Point> cornerPoints = new ArrayList<>();
        ArrayList<Double> cornerValues = new ArrayList<>();
        // Maxima Suspression (see if it is the maximum value to the neighbours)
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // if negative, not a corner
                if (corners[y][x] < 0)
                    continue;
                // see if local maxima
                double peak = corners[y][x];
                boolean isMaxima = true;
                // Check 3x3 neighborhood
                for (int k = -1; k <= 1 && isMaxima; k++) {
                    for (int l = -1; l <= 1; l++) {
                        if (k == 0 && l == 0)
                            continue; // Skip the center pixel
                        int testX = x + k;
                        int testY = y + l;
                        if (corners[testY][testX] > peak) {
                            isMaxima = false;
                            break; // Early exit if a larger neighbor is found
                        }
                    }
                }
                if (isMaxima) {
                    // Point is a local maxima, find the correct position to insert
                    int insertPos = 0;
                    while (insertPos < cornerValues.size() && cornerValues.get(insertPos) > peak)
                    {
                        insertPos++;
                    }
                    // Insert corner in the correct position
                    cornerPoints.add(insertPos, new Point(x, y));
                    cornerValues.add(insertPos, peak);
                    // If we have more points than needed, remove the weakest ones
                    if (cornerPoints.size() > strongest) {
                        cornerPoints.remove(strongest);
                        cornerValues.remove(strongest);
                    }
                }
            }
        }
        // restoreToOriginal();
        // Draw red X
        for (Point p : cornerPoints) {
            int redColor = (255 << 16) | (0 << 8) | 0;
            img.setRGB(p.x, p.y, redColor);
            img.setRGB(p.x + 1, p.y + 1, redColor);
            img.setRGB(p.x + 1, p.y - 1, redColor);
            img.setRGB(p.x - 1, p.y + 1, redColor);
            img.setRGB(p.x - 1, p.y - 1, redColor);
        }
        return cornerPoints;
    }

    public double[] calculateHomography(double[][] srcPoints, double[][] dstPoints) {
        double[][] A = new double[8][8];
        double[] b = new double[8];
        for (int i = 0; i < 4; i++) {
            double xSrc = srcPoints[i][0];
            double ySrc = srcPoints[i][1];
            double xDst = dstPoints[i][0];
            double yDst = dstPoints[i][1];
            A[2 * i][0] = xSrc;
            A[2 * i][1] = ySrc;
            A[2 * i][2] = 1;
            A[2 * i][3] = 0;
            A[2 * i][4] = 0;
            A[2 * i][5] = 0;
            A[2 * i][6] = -xSrc * xDst;
            A[2 * i][7] = -ySrc * xDst;
            A[2 * i + 1][0] = 0;
            A[2 * i + 1][1] = 0;
            A[2 * i + 1][2] = 0;
            A[2 * i + 1][3] = xSrc;
            A[2 * i + 1][4] = ySrc;
            A[2 * i + 1][5] = 1;
            A[2 * i + 1][6] = -xSrc * yDst;
            A[2 * i + 1][7] = -ySrc * yDst;
            b[2 * i] = xDst;
            b[2 * i + 1] = yDst;
        }
        // Solve using Gaussian elimination
        // This function will solve the system A * x = b
        // You can use Gaussian elimination, LU decomposition, or any other method
        return gaussianElimination(A, b);
    }

    public double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;
        for (int i = 0; i < n; i++) {
            // Pivoting
            int max = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(A[j][i]) > Math.abs(A[max][i])) {
                    max = j;
                }
            }
            // Swap rows in A
            double[] temp = A[i];
            A[i] = A[max];
            A[max] = temp;
            // Swap entries in b
            double t = b[i];
            b[i] = b[max];
            b[max] = t;
            // Normalize the row
            for (int k = i + 1; k < n; k++) {

                double factor = A[k][i] / A[i][i];

                b[k] -= factor * b[i];
                for (int j = i; j < n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
            }
        }
        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }
        // The last element of the homography matrix (h33) is 1
        double[] homography = new double[9];
        System.arraycopy(x, 0, homography, 0, 8);
        homography[8] = 1;
        return homography;
    }

    public static double[] invertHomography(double[] H) {
        double[] invH = new double[9];
        double det = H[0] * (H[4] * H[8] - H[5] * H[7])
                - H[1] * (H[3] * H[8] - H[5] * H[6])
                + H[2] * (H[3] * H[7] - H[4] * H[6]);

        if (det == 0)
            throw new IllegalArgumentException("Matrix is not invertible");
        double invDet = 1.0 / det;
        invH[0] = invDet * (H[4] * H[8] - H[5] * H[7]);
        invH[1] = invDet * (H[2] * H[7] - H[1] * H[8]);
        invH[2] = invDet * (H[1] * H[5] - H[2] * H[4]);
        invH[3] = invDet * (H[5] * H[6] - H[3] * H[8]);
        invH[4] = invDet * (H[0] * H[8] - H[2] * H[6]);
        invH[5] = invDet * (H[2] * H[3] - H[0] * H[5]);
        invH[6] = invDet * (H[3] * H[7] - H[4] * H[6]);
        invH[7] = invDet * (H[1] * H[6] - H[0] * H[7]);
        invH[8] = invDet * (H[0] * H[4] - H[1] * H[3]);
        return invH;
    }

    public double[] applyHomographyToPoint(double[] H, double x, double y) {
        // Homogeneous coordinates calculation after transformation
        double xh = H[0] * x + H[1] * y + H[2];
        double yh = H[3] * x + H[4] * y + H[5];
        double w = H[6] * x + H[7] * y + H[8];
        // Normalize by w to get the Cartesian coordinates in the destination image
        double xPrime = xh / w;
        double yPrime = yh / w;
        return new double[] { xPrime, yPrime };
    }

    public void applyHomography(double[] H) {
        BufferedImage output = new BufferedImage(width, height, img.getType());
        double[] invH = invertHomography(H);
        // Iterate over every pixel in the destination image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Apply the inverse of the homography to find the corresponding source pixel
                double[] sourcePoint = applyHomographyToPoint(invH, x, y);
                int srcX = (int) Math.round(sourcePoint[0]);
                int srcY = (int) Math.round(sourcePoint[1]);
                // Check if the calculated source coordinates are within the source image bounds

                if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                    // Copy the pixel from the source image to the destination image
                    Color color = new Color(img.getRGB(srcX, srcY));
                    output.setRGB(x, y, color.getRGB());
                } else {
                    // If out of bounds, set the destination pixel to a default color
                    output.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, output.getRGB(x, y));
            }
        }
    }

    public void median(int size) {
        int filterSize = size; // Size of the median filter, can be 3x3, 5x5, etc.
        int offset = filterSize / 2;

        for (int y = offset; y < img.getHeight() - offset; y++) {
            for (int x = offset; x < img.getWidth() - offset; x++) {
                int[] neighborhood = new int[filterSize * filterSize];
                int index = 0;

                // Collect the pixels in the neighborhood
                for (int i = -offset; i <= offset; i++) {
                    for (int j = -offset; j <= offset; j++) {
                        neighborhood[index++] = img.getRGB(x + j, y + i);
                    }
                }

                // Sort the neighborhood pixels to find the median
                java.util.Arrays.sort(neighborhood);

                // Set the median value to the output image
                img.setRGB(x, y, neighborhood[neighborhood.length / 2]);
            }
        }
    }

    public void averagingFilter(int size) {
        if (img == null)
            return;
        if (size % 2 == 0) {
            System.out.println("Size Invalid: must be odd number!");
            return;
        }
        BufferedImage tempBuf = new BufferedImage(width, height, img.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sumRed = 0, sumGreen = 0, sumBlue = 0;
                for (int i = y - size / 2; i <= y + size / 2; i++) {
                    for (int j = x - size / 2; j <= x + size / 2; j++) {
                        if (i >= 0 && i < height && j >= 0 && j < width) {
                            int color = img.getRGB(j, i);
                            int r = (color >> 16) & 0xff;
                            int g = (color >> 8) & 0xff;
                            int b = color & 0xff;
                            sumRed += r;
                            sumGreen += g;
                            sumBlue += b;
                        }
                    }
                }
                sumRed /= (size * size);
                sumRed = sumRed > 255 ? 255 : sumRed;
                sumRed = sumRed < 0 ? 0 : sumRed;
                sumGreen /= (size * size);
                sumGreen = sumGreen > 255 ? 255 : sumGreen;
                sumGreen = sumGreen < 0 ? 0 : sumGreen;
                sumBlue /= (size * size);
                sumBlue = sumBlue > 255 ? 255 : sumBlue;
                sumBlue = sumBlue < 0 ? 0 : sumBlue;
                int newColor = (sumRed << 16) | (sumGreen << 8) | sumBlue;
                tempBuf.setRGB(x, y, newColor);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, tempBuf.getRGB(x, y));
            }
        }
    }

    // After this line is my code that add in this Final Quest
    // Detect regions in the binary image using connected component labeling
    public ArrayList<int[]> detectRegions(int threshold) {
        boolean[][] visited = new boolean[height][width];
        ArrayList<ArrayList<Point>> regions = new ArrayList<>();
        ArrayList<int[]> point3 = new ArrayList<>();

        // Traverse each pixel in the binary image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isBlack(img.getRGB(x, y)) && !visited[y][x]) {
                    // New region found, perform flood-fill to collect all connected pixels
                    ArrayList<Point> region = new ArrayList<>();
                    int[] point = new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE};
                    point = floodFill(visited, x, y, region, point);
                    if (region.size() < threshold)
                        continue;
                    point3.add(point);
                    regions.add(region);
                }
            }
        }
        // Sort ArrayList by the first element of each array to classify from left to right
        Collections.sort(point3, new Comparator<int[]>() {
            @Override
            public int compare(int[] arr1, int[] arr2) {
                return Integer.compare(arr1[0], arr2[0]);
            }
        });
        return point3;
    }

    // Find all black pixel in region
    public int[] floodFill(boolean[][] visited, int startX, int startY, ArrayList<Point> region, int[] point) {
        // Stack to store points for processing
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(startX, startY));
        point = new int[] {startX, startY, startY};

        // Process all points in the stack
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.x;
            int y = p.y;

            // If the point is out of bounds or already visited, continue
            if (x < 0 || x >= width || y < 0 || y >= height || visited[y][x] || !isBlack(img.getRGB(x, y))) {
                continue;
            }

            point[0] = x > point[0] ? x : point[0];
            point[1] = y < point[1] ? y : point[1];
            point[2] = y > point[2] ? y : point[2];

            // Mark the point as visited and add it to the region
            visited[y][x] = true;
            region.add(p);

            // Push neighbors (4-connectivity)
            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }

        return point;
    }
    
    // Check that pixel is black
    private boolean isBlack(int color) {
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        if(r == 0 && g == 0 && b == 0) return true;
        return false;
    }

    public void fillEdgeOfImage(int size) {
        int newColor = (255 << 16) | (255 << 8) | 255;
        //Fill the top edge
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, newColor);
            }
        }
        //Fill the left edge
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                img.setRGB(x, y, newColor);
            }
        }
        //Fill the right edge
        for (int x = img.getWidth() - size; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                img.setRGB(x, y, newColor);
            }
        }
        //Fill the bottom edge
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = img.getHeight() - size; y < img.getHeight(); y++) {
                img.setRGB(x, y, newColor);
            }
        }
    }
    
    // Classify each region
    public ArrayList<Integer> classify(ArrayList<int[]> region){
        ArrayList<Integer> password = new ArrayList<>();
        int digitCount = 1;
        for(int[] digitInfo : region) {
            System.out.println("Digit : " + digitCount++);
            int predictClass = 0;
            int topScore = 0;
            for(int i=0;i<10;i++) {
                try {
                    BufferedImage pattern = ImageIO.read(new File("images/Pattern/" + i + ".png")); // Get pattern Image
                    BufferedImage downScalePattern = downscaleImageNearestNeighbor(pattern, digitInfo); // Make pattern image fit in size of the region
                    int currentScore = scoring(downScalePattern, digitInfo); // Process the score of this class
                    System.out.println("Class " + i + " have score " + currentScore);
                    if(currentScore > topScore) {
                        predictClass = i;
                        topScore = currentScore;
                    }
                } catch (Exception e) {
                    System.out.println("Can't load pattern Image" + e);
                }
            }
            System.out.println("This digit is " + predictClass + " with score " + topScore);
            System.out.println();
            password.add(predictClass);
        }

        return password;
    }

    // Scoreing each class by compare each pixel
    private int scoring(BufferedImage pattern, int[] digitInfo) {
        int match = 0;
        int minXOriginal = digitInfo[0] - pattern.getWidth();
        int minYOriginal = digitInfo[1];
        for(int y = 0; y < pattern.getHeight(); y++) {
            for(int x = 0; x < pattern.getWidth(); x++) {
                if(pattern.getRGB(x, y) == img.getRGB(minXOriginal + x, minYOriginal + y)) {
                    match++;
                }
            }
        }

        return match;
    }

    // Make pattern image fit in region size
    private BufferedImage downscaleImageNearestNeighbor(BufferedImage patternImage, int[] point) {
        double aspectRatio = (double) patternImage.getWidth() / patternImage.getHeight();
        int newHeight = point[2] - point[1];
        int newWidth = (int) (newHeight * aspectRatio);

        // Create a new buffered image for the downscaled image
        BufferedImage downscaledImage = new BufferedImage(newWidth, newHeight, patternImage.getType());

        // Calculate the scaling factors
        double xScale = (double) patternImage.getWidth() / newWidth;
        double yScale = (double) patternImage.getHeight() / newHeight;

        // Nearest-neighbor interpolation
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int nearestX = (int) (x * xScale);
                int nearestY = (int) (y * yScale);

                int pixelColor = patternImage.getRGB(nearestX, nearestY);

                downscaledImage.setRGB(x, y, pixelColor);
            }
        }

        return downscaledImage;
    }

    public Point[] getCornerPoints(ArrayList<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("The list of points cannot be null or empty.");
        }

        // Initialize with the first point
        Point lowestX = points.get(0);
        Point lowestY = points.get(0);
        Point highestX = points.get(0);
        Point highestY = points.get(0);

        // Iterate through the points to find the extremes
        for (Point p : points) {
            if (p.x < lowestX.x) {
                lowestX = p;
            }
            if (p.y < lowestY.y) {
                lowestY = p;
            }
            if (p.x > highestX.x) {
                highestX = p;
            }
            if (p.y > highestY.y) {
                highestY = p;
            }
        }

        // Return the four extreme points
        return new Point[] { lowestX, lowestY, highestX, highestY };
    }

    public FrequencyDomainManager getFrequencyDomain() {
        convertToGrayscale();
        FrequencyDomainManager fft = new FrequencyDomainManager(this);
        restoreToOriginal();
        return fft;
    }

    public BufferedImage getImage() {
        return img;
    }
}

class StructuringElement {
    public int[][] elements;
    public int width, height;
    public Point origin;
    public ArrayList<Point> ignoreElements;

    public StructuringElement(int width, int height, Point origin) {
        this.width = width;
        this.height = height;
        if (origin.x < 0 || origin.x >= width || origin.y < 0 || origin.y >= height) {
            this.origin = new Point();
        } else {
            this.origin = new Point(origin);
        }
        ignoreElements = new ArrayList<>();
        elements = new int[width][height];
    }
}

class Point {
    public int x;
    public int y;

    public Point(Point a) {
        this.x = a.x;
        this.y = a.y;
    }

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
