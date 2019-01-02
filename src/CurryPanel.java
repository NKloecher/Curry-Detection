import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
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
    TODO DNN/Object-Detection of container-ids → New Class probably, work on still images first
        Custom segmentation and ocr handling
        more stuff
     */

    /*  Guide to Frames
        frame → from camera/video
        original → copy of frame
        (grey is local variable)
        binaryFrame → adaptive threshold
        contourMatHalf → copy of original + rectangle of contours
        (other contour local, half-sized)
     */

    private VideoCapture camera;
    private Mat frame = new Mat();
    private Mat original = new Mat();
    private Mat binaryFrame = new Mat();
    private Mat contourMatHalf = new Mat();
    private Mat contourMat = new Mat();
    //private Mat testingMat = new Mat();

    private boolean paused = false;
    private int blocksize = 21;
    private int C = 5;

    //root
    private final GridPane root = new GridPane();
    private final TabPane tabPane = new TabPane();

    //first tab
    private final GridPane firstTabGridPane = new GridPane();
    private final ImageView originalImage = new ImageView();
    private final ImageView greyscaleImage = new ImageView();
    private final ImageView thresholdImage = new ImageView();
    private final ImageView detectionImage = new ImageView();

    //second tab
    private final ImageView otherView = new ImageView();

    //settings box
    private final TextArea ocrGuess = new TextArea();
    private final Button pauseButton = new Button("Pause");

    private CurryDetectioning cd = new CurryDetectioning();


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

        Tab gridTab = new Tab();
        gridTab.setContent(firstTabGridPane);
        gridTab.setText("Contouring");
        Tab otherTab = new Tab();
        otherTab.setText("Other Tab");
        tabPane.getTabs().addAll(gridTab,otherTab);

//        root.setPrefSize(640+640+120, 980);
        Scene scene = new Scene(root);
        firstTabGridPane.setHgap(10);
        firstTabGridPane.setVgap(10);

        ImageView testStillImage = new ImageView(new Image(new File("res/flower.jpg").toURI().toString()));
        testStillImage.setFitHeight(0);

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


        firstTabGridPane.add(originalImage, 0, 0);
        firstTabGridPane.add(greyscaleImage, 1, 0);

        firstTabGridPane.add(thresholdImage,0,1);
        //firstTabGridPane.add(testStillImage,0,1);
        firstTabGridPane.add(detectionImage,1,1);

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

        pauseButton.setOnAction(event -> paused = !paused);

        settingsBox.getChildren().addAll(labelBlocksize, sliderBlocksize, labelCval, sliderCval, pauseButton, ocrGuess);

        GridPane otherPane = new GridPane();
        otherPane.add(otherView,0,0);
        otherTab.setContent(otherPane);

        root.add(tabPane,0,0);
        root.add(settingsBox, 1,0);

        stage.setTitle("CurryDetectionFX");
        stage.setScene(scene);

        stage.show();
        //camera = new VideoCapture(0); //webcam
//        camera = new VideoCapture("res/virb.mp4"); //video
        camera = new VideoCapture("res/cutOut.mp4");
        startCamera();
        startGuessing();

    }
    private void startGuessing() {
        Runnable makeGuess = () -> {
            //if (frame.empty() || paused) return;
            MatOfByte imagebytes = new MatOfByte();
            Mat pp = new Mat(original, new Rect(original.width()/3,original.height()/4,original.width()/4,original.height()/4*3));
            Imgcodecs.imencode(".png", pp, imagebytes);
            ocrGuess.setText(cd.processImage(imagebytes, blocksize));
        };

        ScheduledExecutorService guessService = Executors.newSingleThreadScheduledExecutor();
        guessService.scheduleAtFixedRate(makeGuess, 200, 100, TimeUnit.MILLISECONDS);
    }

    private void startCamera() {
        Runnable grabFrame = () -> {
            if (paused) {
                return;
            }
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
//                camera.open("res/virb.mp4");
                camera.open("res/cutOut.mp4");
            }
        };

        ScheduledExecutorService frameService = Executors.newSingleThreadScheduledExecutor();
        frameService.scheduleAtFixedRate(grabFrame, 0, 20, TimeUnit.MILLISECONDS);

    }

    private void processFrame(Mat frame) {
        frame.copyTo(original);
        frame.copyTo(contourMat);
        //Core.flip(original,original,1);
        Mat gray = new Mat();

        //colour
        BufferedImage image = matToBufferedImage(frame);

        //greyscale
        Imgproc.cvtColor(frame,gray,Imgproc.COLOR_BGR2GRAY);
        BufferedImage grayImage = matToBufferedImage(gray);


        //thresholding
        Imgproc.GaussianBlur(gray,binaryFrame, new Size(9,9),0);
        Imgproc.adaptiveThreshold(binaryFrame,binaryFrame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, blocksize,C);
        BufferedImage threshImage = matToBufferedImage(binaryFrame);


        //Contours
        List<MatOfPoint> list = new ArrayList<>();
        Imgproc.findContours(binaryFrame, list, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        final List<Rect> rects = list.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        for (Rect rect : rects) {
            Imgproc.rectangle(contourMat, rect.tl(), rect.br(), new Scalar(70, 255, 70), 1);
        }


        //2nd Contours
//        Mat pp = new Mat(frame, new Rect(original.width()/2,0,original.width()/2,original.height()));
//        Mat ppBin = new Mat(binaryFrame, new Rect(original.width()/2,0,original.width()/2,original.height()));
        Mat pp = new Mat(frame, new Rect(original.width()/3,original.height()/4,original.width()/3,original.height()/4*3));
        Mat ppBin = new Mat(binaryFrame, new Rect(original.width()/3,original.height()/4,original.width()/4,original.height()/4*3));
        pp.copyTo(contourMatHalf);
        List<MatOfPoint> list2 = new ArrayList<>();
        Imgproc.findContours(ppBin, list2, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        final List<Rect> rects2 = list2.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        for (Rect rect : rects2) {
            Imgproc.rectangle(contourMatHalf, rect.tl(), rect.br(), new Scalar(70, 255, 70), 1);
        }

        otherView.setImage(SwingFXUtils.toFXImage(matToBufferedImage(contourMatHalf), null));

        originalImage.setImage(SwingFXUtils.toFXImage(image,null));
        greyscaleImage.setImage(SwingFXUtils.toFXImage(grayImage,null));
        thresholdImage.setImage(SwingFXUtils.toFXImage(threshImage, null));
        detectionImage.setImage(SwingFXUtils.toFXImage(matToBufferedImage(contourMat),null));
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
