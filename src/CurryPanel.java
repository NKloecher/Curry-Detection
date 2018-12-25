import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class CurryPanel extends Application {
    /*
    TODO VBox with slider options (fx thresholding) - init
        DNN/Object-Detection of container-ids → New Class probably, work on still images first
        -- symbol marks of todoos
     */

    private VideoCapture camera;
    private Mat frame = new Mat();
    private Mat original = new Mat();
    private Mat binaryFrame = new Mat();
    private Mat testingMat = new Mat();

    private ScheduledExecutorService frameService;
    private int blocksize = 21;
    private int C = 5;

    private final TabPane tabPane = new TabPane();
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

        Tab gridTab = new Tab();
        gridTab.setContent(pane);
        gridTab.setText("Contouring");
        Tab otherTab = new Tab();
        otherTab.setText("Other Tab");
        tabPane.getTabs().addAll(gridTab,otherTab);

        Scene scene = new Scene(tabPane);
        pane.setMinSize(1400, 980);
        pane.setHgap(10);
        pane.setVgap(10);

        ImageView testStillImage = new ImageView(new Image(new File("res/flower.jpg").toURI().toString()));

        //because imageviews are funky... (for video)
        originalImage.setPreserveRatio(true);
        originalImage.setFitWidth(640);
        originalImage.setFitHeight(480);
        greyscaleImage.setPreserveRatio(true);
        greyscaleImage.setFitWidth(640);
        greyscaleImage.setFitHeight(480);

        thresholdImage.setPreserveRatio(true);
        thresholdImage.setFitWidth(640);
        thresholdImage.setFitHeight(480);
        detectionImage.setPreserveRatio(true);
        detectionImage.setFitWidth(640);
        detectionImage.setFitHeight(480);


        pane.add(originalImage, 0, 0);
        pane.add(greyscaleImage, 1, 0);

        pane.add(thresholdImage,0,1);
        //pane.add(testStillImage,0,1);
        pane.add(detectionImage,1,1);

        VBox settingsBox = new VBox();
        settingsBox.setPadding(new Insets(7,0,0,0));
        settingsBox.setSpacing(5);
        Label labelBlocksize = new Label(String.format("Blocksize: %d", blocksize));
        Slider sliderBlocksize = new Slider(7,131,21);

        sliderBlocksize.setBlockIncrement(2);
        sliderBlocksize.valueProperty().addListener(((observableValue, oldVal, newVal) -> {
            int value = newVal.intValue();
            blocksize = value % 2 == 0 ? value + 1 : value; //blocksize non-even number > 5
            labelBlocksize.setText(String.format("Blocksize: %d", blocksize));
        } ));

        Label labelCval = new Label("C: 5");
        Slider sliderCval = new Slider(-10,10,5);
        sliderCval.setBlockIncrement(1);
        sliderCval.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            C = newVal.intValue();
            labelCval.setText(String.format("C: %d", C));
        });


        settingsBox.getChildren().addAll(labelBlocksize, sliderBlocksize, labelCval, sliderCval);
        pane.add(settingsBox, 2,0);

        stage.setTitle("CurryDetectionFX");
        stage.setScene(scene);

        stage.show();
        //camera = new VideoCapture(0);
        camera = new VideoCapture("res/virb.mp4");
        startCamera();


        //thresholdImage.setOnMouseClicked(event -> detectionImage.setImage(SwingFXUtils.toFXImage(testThreshStill(), null)));
    }

    private BufferedImage testThreshStill(){
        List<MatOfPoint> list = new ArrayList<>();
        Imgproc.findContours(binaryFrame, list, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        final List<Rect> rects = list.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        for (Rect rect : rects) {
            Imgproc.rectangle(original, rect.tl(), rect.br(), new Scalar(255, 70, 70), 1);
        }
        return matToBufferedImage(original);
    }

    private void startCamera() {
        Runnable grabFrame = () -> {
            if (camera.read(frame)) {
                processFrame(frame);
            }
            else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                camera.release();
                camera.open("res/virb.mp4");
            }
        };

        frameService = Executors.newSingleThreadScheduledExecutor();
        frameService.scheduleAtFixedRate(grabFrame, 0, 20, TimeUnit.MILLISECONDS);

    }

    private void processFrame(Mat frame) {
        //todo...   use the mat to buffered image method
        frame.copyTo(original);
        //Core.flip(original,original,1);
        Mat gray = new Mat();
        Mat threshold = new Mat();


        //colour
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();

        //Core.flip(frame,frame, 1);//todo.... don't process on flipped image
        frame.get(0, 0, data);

        //greyscale
        Imgproc.cvtColor(frame,gray,Imgproc.COLOR_BGR2GRAY);
        BufferedImage grayImage = new BufferedImage(gray.width(), frame.height(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2 = grayImage.getRaster();
        DataBufferByte dataBufferByte2 = (DataBufferByte) raster2.getDataBuffer();
        byte[] data2 = dataBufferByte2.getData();

        gray.get(0,0,data2);

        //thresholding
        Imgproc.GaussianBlur(gray,threshold, new Size(9,9),0);
        Imgproc.adaptiveThreshold(threshold,binaryFrame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, blocksize,C);
        BufferedImage thresImage = new BufferedImage(binaryFrame.width(), binaryFrame.height(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster3 = thresImage.getRaster();
        DataBufferByte dataBufferByte3 = (DataBufferByte) raster3.getDataBuffer();
        byte[] data3 = dataBufferByte3.getData();


        binaryFrame.get(0,0,data3);

        List<MatOfPoint> list = new ArrayList<>();
        Imgproc.findContours(binaryFrame, list, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        final List<Rect> rects = list.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        for (Rect rect : rects) {
            Imgproc.rectangle(original, rect.tl(), rect.br(), new Scalar(70, 255, 70), 1);
        }

        originalImage.setImage(SwingFXUtils.toFXImage(image,null));
        greyscaleImage.setImage(SwingFXUtils.toFXImage(grayImage,null));
        thresholdImage.setImage(SwingFXUtils.toFXImage(thresImage, null));
        detectionImage.setImage(SwingFXUtils.toFXImage(matToBufferedImage(original),null));
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

        //Core.flip(frame,frame, 1);
        frame.get(0, 0, data);

        return image;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
