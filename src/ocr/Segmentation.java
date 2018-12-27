package ocr;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public class Segmentation {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /*
        adaptive treshholding notes → {
        blocksize 10-20 for small images; upto 50 for medium; 100+ for large (whole image is id)
        15-20 seems decent for ACD testing purposes → RECHECK → if C>0 (black on white) chance of higher blocksize
        C 2 to 15 for BLACK chars on WHITE bg
        C -2 to -15 for WHITE chars on BLACK bg
        }
     */

    private static final String fromPath = "C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\Billeder\\ACD_Data_ID";
    private static final String outputPath = "C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\Billeder\\ACD_Data_Full_Results\\";

    public static void main(String[] args) {
//        File image = new File("C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\Billeder\\ACD_Data_Full\\03.png");
//        segment(image, 13, false, true);
//        File image = new File("C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\Billeder\\ACD_Data_Full");
//        segment(image.listFiles());
    }

    public static void segment(File[] images){
        Arrays.stream(images).forEach(x -> segment(x, 13, false, true));
    }

    public static MatOfByte[] segment(MatOfByte imageByte, int blockSize, boolean blackChar){

        Mat original = Imgcodecs.imdecode(imageByte, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        boolean isVertical = original.height() > original.width();

        original = sharpen(original, new Size(7,7));

        Mat input = new Mat();
        Mat greyscale = new Mat();
        Imgproc.cvtColor(original,input,Imgproc.COLOR_BGR2GRAY,1);
        Imgproc.cvtColor(original,greyscale,Imgproc.COLOR_BGR2GRAY,1);

        Imgproc.GaussianBlur(input,input,new Size(9,9),0);

        //Good for White char on Black bg
        Mat whiteOnBlack = new Mat();
        Imgproc.adaptiveThreshold(input,whiteOnBlack, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, blockSize, -5);
        Mat inverse_bit = new Mat();

        //Black char on White bg
        Mat blackOnWhite = new Mat();
        Imgproc.adaptiveThreshold(input,blackOnWhite, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, blockSize, 5);

        List<MatOfPoint> contours = new ArrayList<>();
        if (blackChar) Imgproc.findContours(blackOnWhite,contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        else Imgproc.findContours(whiteOnBlack,contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        final List<Rect> boundingRects = contours.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        List<MatOfPoint> filteredList = new ArrayList<>();

        int[] toBeRemoved = filterRects(original, boundingRects, new int[boundingRects.size()], isVertical);

        for (int i = 0; i < contours.size();i++){
            if (toBeRemoved[i] != -1) filteredList.add(contours.get(i));
        }
        //Filtered list of rects for proper kmeans evaluation
        final List<Rect> filteredBoundingRects = filteredList.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        double[] kmeansArray = kMeansSort(filteredBoundingRects, isVertical);

        //System.out.println("kmeans arr: " + Arrays.toString(kmeansArray));

        if (kmeansArray != null) sortContours(isVertical, filteredList, kmeansArray);

        else { //failsafe sort, burde aldrig ske
            filteredList.sort((c1,c2) -> {
                if (isVertical) return Imgproc.boundingRect(c1).y - Imgproc.boundingRect(c2).y;
                else return Imgproc.boundingRect(c1).x - Imgproc.boundingRect(c2).x;
            });
        }

        MatOfByte[] bytes = new MatOfByte[filteredList.size()];
        for (int i = 0; i < filteredList.size(); i++) {
            Mat submat = new Mat();
            Core.bitwise_not(greyscale,inverse_bit); //greyscale for AI in correct b/w
            if (blackChar) submat = greyscale.submat(Imgproc.boundingRect(filteredList.get(i)));
            else submat = inverse_bit.submat(Imgproc.boundingRect(filteredList.get(i)));
            MatOfByte byteImage = new MatOfByte();
            Imgcodecs.imencode(".png", submat, byteImage);
            bytes[i] = byteImage;
        }
//        System.out.println("Size of byteImage array: " + bytes.length);
        return bytes;
    }

    /**
     * Segments the image into individual characters.
     * @param image Takes the full ISO 6343 container ID as an image (DCSU 385127-8 22G1)
     * @param blockSize Blocksize for gaussian thresholding
     * @param blackChar true if black on white bg, or false opposite -
     * @param writeToDisk for testing purposes (and creating test-cases for AI)
     * @return All contours found on image in greyscale after filtering
     */
    public static MatOfByte[] segment(File image, int blockSize, boolean blackChar, boolean writeToDisk){
        String name = image.getName().substring(0,image.getName().length()-4);
//        System.out.println(name);

        Mat original = Imgcodecs.imread(image.getAbsolutePath());
        boolean isVertical = original.height() > original.width();

        original = sharpen(original, new Size(7,7));

        Mat input = new Mat();
        Mat greyscale = new Mat();
        Imgproc.cvtColor(original,input,Imgproc.COLOR_BGR2GRAY,1);
        Imgproc.cvtColor(original,greyscale,Imgproc.COLOR_BGR2GRAY,1);

        Imgproc.GaussianBlur(input,input,new Size(9,9),0);

        //Good for White char on Black bg
        //blocksize fra fil opløsning → virker ikke uden helt statisk situation
        Mat whiteOnBlack = new Mat();
        Imgproc.adaptiveThreshold(input,whiteOnBlack, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, blockSize, -5);
        Mat inverse_bit = new Mat();
//        Core.bitwise_not(whiteOnBlack, inverse_bit); //not in use - could perform better?

        //Black char on White bg
        Mat blackOnWhite = new Mat();
        Imgproc.adaptiveThreshold(input,blackOnWhite, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, blockSize, 5);
        //idea -> if contours < x redo with blocksize-2 → Might not be needed

        List<MatOfPoint> contours = new ArrayList<>();
        if (blackChar) Imgproc.findContours(blackOnWhite,contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        else Imgproc.findContours(whiteOnBlack,contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        final List<Rect> boundingRects = contours.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        List<MatOfPoint> filteredList = new ArrayList<>();

        int[] toBeRemoved = filterRects(original, boundingRects, new int[boundingRects.size()], isVertical);

        for (int i = 0; i < contours.size();i++){
            if (toBeRemoved[i] != -1) filteredList.add(contours.get(i));
        }
        //Filtered list of rects for proper kmeans evaluation
        final List<Rect> filteredBoundingRects = filteredList.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
        double[] kmeansArray = kMeansSort(filteredBoundingRects, isVertical);

        //System.out.println("kmeans arr: " + Arrays.toString(kmeansArray));

        if (kmeansArray != null) sortContours(isVertical, filteredList, kmeansArray);

        else { //failsafe sort, burde aldrig ske
            filteredList.sort((c1,c2) -> {
                if (isVertical) return Imgproc.boundingRect(c1).y - Imgproc.boundingRect(c2).y;
                else return Imgproc.boundingRect(c1).x - Imgproc.boundingRect(c2).x;
            });
        }

        //writes the list to file
        if (writeToDisk){
            for (int i = 0; i < filteredList.size(); i++) {
                Mat submat = new Mat();
                if (blackChar) submat = blackOnWhite.submat(Imgproc.boundingRect(filteredList.get(i)));
                else submat = greyscale.submat(Imgproc.boundingRect(filteredList.get(i)));
                Imgcodecs.imwrite(outputPath + name + "-I" + i + ".png",submat);
            }
        }
//        System.out.println(Arrays.toString(toBeRemoved));
//        System.out.println(filteredList.size());

        MatOfByte[] bytes = new MatOfByte[filteredList.size()];
        for (int i = 0; i < filteredList.size(); i++) {
            Mat submat = new Mat();
            Core.bitwise_not(greyscale,inverse_bit); //greyscale for AI in correct b/w
            if (blackChar) submat = greyscale.submat(Imgproc.boundingRect(filteredList.get(i)));
            else submat = inverse_bit.submat(Imgproc.boundingRect(filteredList.get(i)));
            MatOfByte byteImage = new MatOfByte();
            Imgcodecs.imencode(".png", submat, byteImage);
            bytes[i] = byteImage;
        }
//        System.out.println("Size of byteImage array: " + bytes.length);
        return bytes;
    }

    public static void sortContours(boolean isVertical, List<MatOfPoint> filteredList, double[] kmeansArray) {
        //write a comparator?
        if (isVertical) {
            filteredList.sort((rec1,rec2) -> {
                Rect r1 = Imgproc.boundingRect(rec1);
                Rect r2 = Imgproc.boundingRect(rec2);
                int c1,c2;
                c1 = Math.abs(r1.x - kmeansArray[0]) < Math.abs(r1.x - kmeansArray[1]) ? (int)kmeansArray[0] : (int)kmeansArray[1];
                c2 = Math.abs(r2.x - kmeansArray[0]) < Math.abs(r2.x - kmeansArray[1]) ? (int)kmeansArray[0] : (int)kmeansArray[1];
                if (c1 == c2) return r1.y - r2.y;
                else if (c1 < c2) return -1;
                else return 1;


            });
        } else {
            if (kmeansArray.length == 2){
                filteredList.sort((rec1,rec2) -> {
                    Rect r1 = Imgproc.boundingRect(rec1);
                    Rect r2 = Imgproc.boundingRect(rec2);
                    int c1,c2;
                    c1 = Math.abs(r1.y - kmeansArray[0]) < Math.abs(r1.y - kmeansArray[1]) ? (int)kmeansArray[0] : (int)kmeansArray[1];
                    c2 = Math.abs(r2.y - kmeansArray[0]) < Math.abs(r2.y - kmeansArray[1]) ? (int)kmeansArray[0] : (int)kmeansArray[1];
                    if (c1 == c2) return r1.x - r2.x;
                    else if (c1 < c2) return -1;
                    else return 1;


                });
            }else {
                filteredList.sort((rec1,rec2) -> {
                    Rect r1 = Imgproc.boundingRect(rec1);
                    Rect r2 = Imgproc.boundingRect(rec2);
                    int c1 = -1,c2 = -1;
                    int[] difs1 = new int[]{Math.abs(r1.y - (int)kmeansArray[0]),Math.abs(r1.y - (int)kmeansArray[1]),Math.abs(r1.y - (int)kmeansArray[2])};
                    int[] difs2 = new int[]{Math.abs(r2.y - (int)kmeansArray[0]),Math.abs(r2.y - (int)kmeansArray[1]),Math.abs(r2.y - (int)kmeansArray[2])};
                    int min = difs1[0];
                    int index = 0;
                    for (int i = 1; i < 3; i++) {
                        if (min > difs1[i]){
                            min = difs1[i];
                            index = i;
                        }
                    }
                    c1 = (int) kmeansArray[index];
                    min = difs2[0];
                    index = 0;
                    for (int i = 1; i < 3; i++) {
                        if (min > difs2[i]){
                            min = difs2[i];
                            index = i;
                        }
                    }
                    c2 = (int) kmeansArray[index];
                    if (c1 == c2) return r1.x - r2.x;
                    else if (c1 < c2) return -1;
                    else return 1;
                });
            }
        }
    }

    /**
     * Uses the k means clustering algorithm implementation in openCV to figure out where columns of text are placed
     * in the image. <br>
     *     x for vertical images. <br>
 *         y for horizontal images.
     * @return The two/three x, or y, coordinates corresponding to the heatmap clustering of contours.
     */
    public static double[] kMeansSort(List<Rect> rects, boolean isVertical) {
        Mat mat = new Mat(rects.size(),1,CvType.CV_32F);
        if (isVertical) {
            for (int i = 0; i < rects.size(); i++) {
                mat.put(i,0, rects.get(i).x);
            }
        }else {
            for (int i = 0; i < rects.size(); i++) {
                mat.put(i,0, rects.get(i).y);
            }
        }


        Mat centers = new Mat();
        Mat bestLabels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 1.0);
        double t;
        double[] result;
        if (isVertical) {
            t = Core.kmeans(mat, 2, bestLabels, criteria,10, Core.KMEANS_RANDOM_CENTERS, centers);
            result = new double[]{centers.get(0,0)[0],centers.get(1,0)[0]};
        }
        else {
            t = Core.kmeans(mat, 3, bestLabels, criteria,10, Core.KMEANS_RANDOM_CENTERS, centers);
            //System.out.println(centers.get(0,0)[0] + " " + centers.get(1,0)[0] + " " + centers.get(2,0)[0]);
            double c1 = centers.get(0,0)[0];
            double c2 = centers.get(1,0)[0];
            double c3 = centers.get(2,0)[0];
            double avgHeight = rects.stream().mapToDouble(x -> x.height).average().getAsDouble();
            //System.out.println(avgHeight);
            result = t(c1,c2,c3, avgHeight);
        }

        if (centers.rows() < 2) return null; //failsafe
        return result;
    }

    private static double[] t(double c1, double c2, double c3, double avg) {

        if (c1 + avg > c2 && c1 - avg < c2)  return new double[]{c1,c3};
        if (c1 + avg > c3 && c1 - avg < c3) return new double[]{c1,c2};
        if (c2 + avg > c3 && c2 - avg < c3) return new double[]{c2,c1};
        else return new double[]{c1,c2,c3};
    }


    /**
     *  Filters the contours found with opencv's findContours
     * @param original original image for size comparison
     * @param boundingRects List of bounding rects over the contours
     * @param toBeRemoved Array of ints corresponding to boundingRects index to be filtered; -1 = filter
     * @param isVertical Orientation of original image
     * @return toBeRemoved int[] - index of all contours to be removed from the original contour list
     */
    public static int[] filterRects(Mat original, List<Rect> boundingRects, int[] toBeRemoved, boolean isVertical) {
        //Remove full image contour and wrongly dimensioned contours
        boundingRects.forEach(x -> {
            if (x.width > x.height * 1.1) {
                toBeRemoved[boundingRects.indexOf(x)] = -1;
            }
            if (isVertical) {
                if (x.height > original.height() * 0.45) toBeRemoved[boundingRects.indexOf(x)] = -1;
            }
            else {
                if (x.width > original.width() * 0.45) toBeRemoved[boundingRects.indexOf(x)] = -1;
            }
        });

        //removes inner contours (from 0 & 8 etc.)
        for (int i = 0; i < boundingRects.size(); i++){
            Rect r1 = boundingRects.get(i);
            for (int k = 0; k < boundingRects.size(); k++){
                if (toBeRemoved[k] == -1) continue; //skip already filtered parts (full image)
                Rect r2 = boundingRects.get(k);
                if (r1.x > r2.x
                        && r1.y > r2.y
                        && r1.x + r1.width < r2.x + r2.width
                        && r1.y + r1.height < r2.y + r2.height) {
                    toBeRemoved[i] = -1;
                }
            }
        }
        //Remove small debris based on average area of bounding rects left
        final double[] avgArea = {0};
        IntStream.range(0,boundingRects.size())
                .filter(x -> toBeRemoved[x] != -1)
                .mapToDouble(y -> boundingRects.get(y).area())
                .average()
                .ifPresent(value -> avgArea[0] = value);
        for (int i = 0; i < boundingRects.size(); i++) {
            if (toBeRemoved[i] == -1) continue;
            if (boundingRects.get(i).area() < avgArea[0] * 0.4) toBeRemoved[i] = -1;
        }

        return toBeRemoved;
    }


    /**
     * Sharpens the image for better processing
     */
    public static Mat sharpen(Mat orig, Size size) {
        Mat output = new Mat();
        Imgproc.GaussianBlur(orig,output,size,0);
        Core.addWeighted(orig,1.5,output,-0.5,0,output);
        return output;
    }
}
