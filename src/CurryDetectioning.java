import dk.picit.ai.test.AIMainOCR;
import ocr.OCRReaderID;
import org.opencv.core.MatOfByte;

public class CurryDetectioning {

    private static AIMainOCR ai;

    static {
        ai = new AIMainOCR("aidataFinal.dat", 24, 24);
    }

    public String[] processImage(MatOfByte byteImage,int blocksize){
        return OCRReaderID.OCRBridge(ai, byteImage, blocksize);
    }

}
