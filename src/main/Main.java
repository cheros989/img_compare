package main;

import javafx.scene.shape.Circle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    private BufferedImage image1 = null;
    private BufferedImage image2 = null;
    private String outputPath = null;
    private int img_width;
    private int img_height;

    public static void main(String[] args) {
        Main main = new Main();

        JFrame jFrame = new JFrame();
        jFrame.setSize(300, 200);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);

        JPanel panel = new JPanel(new FlowLayout());

        JButton btnFile1 = new JButton("Select first image");
        btnFile1.setPreferredSize(new Dimension(250, 30));
        btnFile1.setBackground(new Color(255, 255, 255));
        JButton btnFile2 = new JButton("Select second image");
        btnFile2.setPreferredSize(new Dimension(250, 30));
        btnFile2.setBackground(new Color(255, 255, 255));
        JButton selectOutPath = new JButton("Select out path");
        selectOutPath.setPreferredSize(new Dimension(250, 30));
        selectOutPath.setBackground(new Color(255, 255, 255));
        JButton compareButton = new JButton("Compare images");
        compareButton.setPreferredSize(new Dimension(250, 30));
        compareButton.setBackground(new Color(255, 255, 255));
        compareButton.setBackground(new Color(100, 90, 90));
        compareButton.setForeground(new Color(255, 255, 255));
        compareButton.setEnabled(false);

        panel.add(btnFile1);
        panel.add(btnFile2);
        panel.add(selectOutPath);
        panel.add(compareButton);

        jFrame.add(panel);

        selectOutPath.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            File file;
            int returnVal = fc.showOpenDialog(jFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                main.outputPath = file.getAbsolutePath();
                main.setReady(selectOutPath, "out: " + file.getName());
                if (main.image1 != null && main.image2 != null && main.outputPath != null) {
                    main.enableButton(compareButton);
                }
            }
        });
        btnFile1.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            File file;
            int returnVal = fc.showOpenDialog(jFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                try {
                    main.image1 = ImageIO.read(new File(file.getAbsolutePath()));
                } catch (IOException ex) { }
                main.setReady(btnFile1, file.getName());
                if (main.image1 != null && main.image2 != null && main.outputPath != null) {
                    main.enableButton(compareButton);
                }
            }
        });

        btnFile2.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            File file;
            int returnVal = fc.showOpenDialog(jFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                try {
                    main.image2 = ImageIO.read(new File(file.getAbsolutePath()));
                } catch (IOException ex) { }
                main.setReady(btnFile2, file.getName());
                if (main.image1 != null && main.image2 != null && main.outputPath != null) {
                    main.enableButton(compareButton);
                }
            }
        });

        compareButton.addActionListener(e -> {
            if (main.image1 != null && main.image2 != null) {
                main.compareImages(main.image1, main.image2, compareButton);
            }
        });
    }

    private void enableButton(JButton button) {
        button.setEnabled(true);
        button.setBackground(Color.RED);
    }

    private void setReady (JButton button, String name) {
        button.setBackground(new Color(0, 155, 0));
        button.setForeground(Color.WHITE);
        button.setText(name);
    }

    private void compareImages(BufferedImage image1, BufferedImage image2, JButton button) {

        img_height= compareHeight(image1, image2);
        img_width = compareWidth(image1, image2);
        BufferedImage result_image = image1.getWidth() > image2.getWidth() ? deepCopy(image1) : deepCopy(image2);
        Graphics g = result_image.getGraphics();
        g.setColor(new Color(255, 0, 0));

        ArrayList<MyPoint> all_points = new ArrayList<>();
        ArrayList<ArrayList<MyPoint>> elements = new ArrayList<>();

        for (int row = 0; row < img_height; row++) {
            for (int col = 0; col < img_width; col++) {
                if (!isSameColor(getPixelColor(image1, col, row), (getPixelColor(image2, col, row)))) {
                    all_points.add(new MyPoint(col, row));
                }
            }
        }

        if (all_points.size() < 1) {
            return;
        }

        double all_pixels = img_height * img_width;
        double diff_percent = (all_points.size() / all_pixels) * 100;

        if (diff_percent > 50) {
            button.setText("Too much difference (" + Math.round(diff_percent) + "%)");
            return;
        }

        while (all_points.size() > 0) {
            ArrayList<MyPoint> buffer = new ArrayList<>();
            MyPoint start_point = all_points.get(0);
            System.out.println(start_point);
            ArrayList<MyPoint> element = new ArrayList<>();
            buffer.add(start_point);
            int buffer_start_size = 0;
            while (buffer_start_size != buffer.size()) {
                buffer_start_size = buffer.size();
                buffer = findPointsNear(buffer, all_points);
            }
            element.addAll(buffer);
            elements.add(element);
            all_points.removeAll(buffer);
        }

        for (ArrayList<MyPoint> elem : elements) {
            Rectangle rect = findRect(elem);
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
        }

        File outputfile = new File(outputPath);
        System.out.println(outputPath);
        button.setText("Compare images");
        try {
            ImageIO.write(result_image, "png", outputfile);
        } catch (IOException e) {}
    }

    private Rectangle findRect(ArrayList<MyPoint> element) {
        int min_x = img_width;
        int min_y = img_height;
        int max_x = 0;
        int max_y = 0;
        for (MyPoint p : element) {
            if (p.getX() < min_x)
                min_x = (int) p.getX();
            if (p.getY() < min_y)
                min_y = (int) p.getY();
            if (p.getX() > max_x)
                max_x = (int) p.getX();
            if (p.getY() > max_y)
                max_y = (int) p.getY();
        }

        int width = max_x - min_x;
        int height = max_y - min_y;

        return new Rectangle(min_x, min_y, width, height);
    }

    private ArrayList<MyPoint> findPointsNear(ArrayList<MyPoint> element, ArrayList<MyPoint> all_points) {

        ArrayList<MyPoint> buff = new ArrayList<>();
        for (MyPoint p : element) {
            Circle circle = new Circle(p.getX(), p.getY(), 5);
            for (MyPoint point : all_points) {
                if (circle.contains(point) && !point.inElement) {
                    point.inElement = true;
                    buff.add(point);
                }
            }
        }
        element.addAll(buff);
        return element;
    }

    private Color getPixelColor(BufferedImage bi, int x, int y) {
        Object colorData = bi.getRaster().getDataElements(x, y, null);
        int argb = bi.getColorModel().getRGB(colorData);
        return new Color(argb, true);
    }

    private int compareHeight(BufferedImage image1, BufferedImage image2) {
        if (image1.getHeight() < image2.getHeight()) {
            return image1.getHeight();
        }
        return image2.getHeight();
    }

    private int compareWidth(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() < image2.getWidth()) {
            return image1.getWidth();
        }
        return image2.getWidth();
    }

    private boolean isSameColor(Color color1, Color color2) {

        int r1 = color1.getRed();
        int r2 = color2.getRed();

        int g1 = color1.getGreen();
        int g2 = color2.getGreen();

        int b1 = color1.getBlue();
        int b2 = color2.getBlue();

        double red_diff = r1 - r2;
        double green_diff = g1 - g2;
        double blue_diff = b1 - b2;

        double red_dif_percent = (Math.abs(red_diff)/255)* 100;
        double green_diff_percent = (Math.abs(green_diff)/255)* 100;
        double blue_diff_percent = (Math.abs(blue_diff)/255)* 100;

        double general_diff = red_dif_percent + green_diff_percent + blue_diff_percent;

        if (general_diff > 10) {
            return false;
        }

        return true;
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
