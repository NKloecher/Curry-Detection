package ocr;

import java.util.ArrayList;


public class Levenshtein {

    /**
     * Method for finding the closest guess against an array of targets. <br>
     *     Closest here meaning the guess with the lowest Levenshtein's distance to a target.
     * @param guesses Array of guesses made by the AI
     * @param targets Array of targets (container IDs)
     * @return Array of [Index of guesses, index of targets] with
     */
    public static ArrayList<ArrayList<Integer>> levenshteinDistance(String[] guesses, String[] targets) {
        int min = Integer.MAX_VALUE;
        ArrayList<Integer> indexOfGuesses = new ArrayList<>();
        ArrayList<Integer> indexOfTargets = new ArrayList<>();
        for (int i = 0; i < guesses.length; i++) {
            String g = guesses[i];
            for (int i1 = 0; i1 < targets.length; i1++) {
                String t = targets[i1];
                int lDist = levenshteinDistance(g, t);
                if (lDist <= min) {
                    if (lDist == min) {
                        indexOfGuesses.add(1,i);
                        indexOfTargets.add(1,i1);
                    }else {
                        min = lDist;
                        indexOfGuesses.clear();
                        indexOfGuesses.add(0,i);
                        indexOfTargets.clear();
                        indexOfTargets.add(0,i1);
                    }
                }
            }
        }
        ArrayList<ArrayList<Integer>> lists = new ArrayList<>();
        lists.add(indexOfGuesses);
        lists.add(indexOfTargets);
        return lists;
    }

    /**
     * Levenshtein algorithm for calculating 'distance' between strings. <br>
     * Based on https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     */
    private static int levenshteinDistance (String one, String two) {
        int len1 = one.length() + 1;
        int len2 = two.length() + 1;
        int[] cost = new int[len1];
        int[] newcost = new int[len1];

        for (int i = 0; i < len1; i++) cost[i] = i;

        for (int j = 1; j < len2; j++) {
            newcost[0] = j;

            for(int i = 1; i < len1; i++) {
                int match = (one.charAt(i - 1) == two.charAt(j - 1)) ? 0 : 1;

                //Insert & Delete scaled to be more expensive than replacing
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1 + 1; //+1 new
                int cost_delete  = newcost[i - 1] + 1 + 1; //+1 new

                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }
            int[] swap = cost; cost = newcost; newcost = swap;
        }
        return cost[len1 - 1];
    }
}
