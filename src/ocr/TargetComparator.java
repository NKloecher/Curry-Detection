package ocr;

import dk.picit.ai.Target;

import java.util.Comparator;

/**
 * Compares two Targets based on their guesses, either based on letter our numbers.
 */
public class TargetComparator implements Comparator<Target> {
    private boolean letterComparator;

    /**
     * Creates a comparator for sorting a list of Targets. <br>
 *     true means descending letters→numbers | false means descending numbers→letters. <br>
 *     It does not the change the original ordering of two letters/numbers (sorted by quality)
     * @param letterComparator True if compare by letters, false if by numbers
     */
    public TargetComparator(boolean letterComparator){this.letterComparator=letterComparator;}

    @Override
    public int compare(Target o1, Target o2) {
        if (letterComparator){
            if (Character.isLetter((char)o1.value) && !Character.isLetter((char)o2.value)) return -1;
            if (!Character.isLetter((char)o1.value) && Character.isLetter((char)o2.value)) return 1;
            else return (int) (o1.quality - o2.quality);
        }
        else {
            if (Character.isDigit((char)o1.value) && !Character.isDigit((char)o2.value)) return -1;
            if (!Character.isDigit((char)o1.value) && Character.isDigit((char)o2.value)) return 1;
            else return (int) (o1.quality - o2.quality);
        }
    }
}
