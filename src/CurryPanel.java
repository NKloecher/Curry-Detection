import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
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
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class CurryPanel extends Application {

    private final GridPane pane = new GridPane();
    private final ImageView originalImage = new ImageView();
    private final ImageView greyscaleImage = new ImageView();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        launch();
    }


    private ImageView i;
    private ImageView i2;
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(pane);
        i = new ImageView(new Image(new File("res/flower.jpg").toURI().toString()));
        i2 = new ImageView(new Image(new File("res/flower.jpg").toURI().toString()));
        HBox hbox = new HBox();
        hbox.getChildren().addAll(i,i2);
        hbox.getChildren().add(originalImage);
        pane.getChildren().add(hbox);
        //pane.getChildren().add(new TextField("Olalalalala"));
        //pane.getChildren().add(new ImageView(new Image(new File("res/flower.jpg").toURI().toString())));
        stage.setTitle("TestFX");
        stage.setScene(scene);
        stage.show();
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(this::startCamera, 0 , 20, TimeUnit.MILLISECONDS);
        //startCamera();
    }

    private void startCamera() {
        VideoCapture camera = new VideoCapture(0);
        Mat frame = new Mat();
        camera.read(frame);

        if(!camera.isOpened()){
            System.out.println("Error");
            return;
        }

        while (camera.read(frame)) {
            System.out.println("working");
            BufferedImage imageB = matToBufferedImage(frame);
            BufferedImage greyscaleB = grayscale(imageB);

            Image orig = SwingFXUtils.toFXImage(imageB, null);
            //Image grey = SwingFXUtils.toFXImage(greyscaleB, null);
            i2.setImage(orig);
            //greyscaleImage.setImage(grey);
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
        frame.get(0, 0, data);

        return image;
    }


    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
