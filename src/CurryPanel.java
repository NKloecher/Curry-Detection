import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class CurryPanel extends Application {

    private final GridPane pane = new GridPane();
    private final ImageView originalImage = new ImageView();
    private final ImageView greyscaleImage = new ImageView();
    private final ImageView thresholdImage = new ImageView();
    private final ImageView detectionImage = new ImageView();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene(pane);
        pane.setMinSize(1280, 980);
        pane.setHgap(10);
        pane.setVgap(10);

        ImageView testStillImage = new ImageView(new Image(new File("res/flower.jpg").toURI().toString()));

        pane.add(originalImage,0,0);
        pane.add(greyscaleImage,1,0);

        //pane.add(thresholdImage,0,1);
        //pane.add(detectionImage,1,1);

        stage.setTitle("TestFX");
        stage.setScene(scene);

        stage.show();
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
            s.scheduleAtFixedRate(() -> {
                try {
                    startCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 20, TimeUnit.MILLISECONDS);
    }

    private void startCamera() throws Exception {
        VideoCapture camera = new VideoCapture(0);
        Mat frame = new Mat();
        camera.read(frame);

        if(!camera.isOpened()){
            System.out.println("Error");
            throw new Exception();
        }

        while (camera.read(frame)) {
            System.out.println("working");
            //todo work on original -> display flipped
            BufferedImage imageB = matToBufferedImage(frame);
            Image orig = SwingFXUtils.toFXImage(imageB, null);
            originalImage.setImage(orig);

            BufferedImage greyscaleB = grayscale(imageB);
            Image grey = SwingFXUtils.toFXImage(greyscaleB, null);
            greyscaleImage.setImage(grey);
        }
        camera.release();
    }



    private BufferedImage grayscale(BufferedImage img) {
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Color c = new Color(img.getRGB(j, i));

                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);

                Color newColor =
                        new Color(
                                red + green + blue,
                                red + green + blue,
                                red + green + blue);

                img.setRGB(j, i, newColor.getRGB());
            }
        }

        return img;
    }

    private BufferedImage matToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();

        Core.flip(frame,frame, 1);//todo maybe not here
        frame.get(0, 0, data);

        return image;
    }


    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
