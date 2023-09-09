package net.niceEli;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main extends JFrame {
    private BufferedImage bufferedImage;

    public Main(Mat image, Mat mask, String type) {
        // Set up the JFrame
        super("Pixel Sorting");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(image.cols(), image.rows());
        setVisible(true);

        // Convert the OpenCV Mat to a BufferedImage for display
        this.bufferedImage = new BufferedImage(image.cols(), image.rows(), BufferedImage.TYPE_3BYTE_BGR);
        updateBufferedImage(image);

        // Create a JPanel to display the image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferedImage, 0, 0, null);
            }
        };
        setContentPane(panel);

        // Perform pixel sorting based on the specified type
        if (type.equalsIgnoreCase("v")) {
            verticalPixelSort(image, mask);
        } else if (type.equalsIgnoreCase("h")) {
            horizontalPixelSort(image, mask);
        } else {
            System.out.println("Invalid type. Use 'v' for vertical or 'h' for horizontal.");
            System.exit(1);
        }

        // Update the displayed image
        updateBufferedImage(image);
        panel.repaint();
    }

    private void updateBufferedImage(Mat mat) {
        mat.convertTo(mat, CvType.CV_8U);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        byte[] data = new byte[mat.rows() * mat.cols() * (int) (mat.elemSize())];
        mat.get(0, 0, data);
        bufferedImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
    }

    private void verticalPixelSort(Mat image, Mat mask) {
        // Vertical pixel sorting
        for (int col = 0; col < image.cols(); col++) {
            Mat colData = image.col(col);
            Mat maskCol = mask.col(col);

            Mat sortedColData = new Mat();
            Core.sortIdx(colData, sortedColData, Core.SORT_EVERY_COLUMN + Core.SORT_ASCENDING);

            for (int row = 0; row < image.rows(); row++) {
                int idx = (int) sortedColData.get(row, 0)[0];
                double[] pixel = colData.get(idx, 0);
                image.put(row, col, pixel);
            }
        }
    }

    private void horizontalPixelSort(Mat image, Mat mask) {
        // Horizontal pixel sorting
        for (int row = 0; row < image.rows(); row++) {
            Mat rowData = image.row(row);
            Mat maskRow = mask.row(row);

            Mat sortedRowData = new Mat();
            Core.sortIdx(rowData, sortedRowData, Core.SORT_EVERY_ROW + Core.SORT_ASCENDING);

            for (int col = 0; col < image.cols(); col++) {
                int idx = (int) sortedRowData.get(0, col)[0];
                double[] pixel = rowData.get(0, idx);
                image.put(row, col, pixel);
            }
        }
    }

    public static void main(String[] args) {
        // Load the OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Check if the correct number of arguments are provided
        if (args.length != 3) {
            System.out.println("Usage: java Main <inputImagePath> <maskImagePath> <type>");
            System.exit(1);
        }

        // Read the input image and mask from command-line arguments
        String inputImagePath = args[0];
        String maskImagePath = args[1];
        String type = args[2];

        // Read the input image and mask
        Mat inputImage = Imgcodecs.imread(inputImagePath);
        Mat mask = Imgcodecs.imread(maskImagePath, Imgcodecs.IMREAD_GRAYSCALE);

        // Create and display the window
        SwingUtilities.invokeLater(() -> new Main(inputImage, mask, type));
    }
}
