package com.TeamHotel.assignment1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

//import com.beust.jcommander.Parameter;

/**
 * Hyperparameters
 * 
 * A class for storing variables whose ideal values can only be
 * determined by running the model.
 * 
 * @ThreadSafe
 */
public class HyperParameters {
    //@Parameter
    //private List<String> parameters = new ArrayList<>();

    //@Parameter(names = {"--num-search-results"}, description = "Number of search query results to derive headings from");
    public static int numSearchResults = 20;
    //@Parameter(names={"--fulltext-includes-headings"}, description = "Should our queries look at each article's headings?")
    public static boolean fulltextIncludesHeadings = true;
    public static long numHeadingsToPredict = 10;
    public static int minHeadingOccurrancesForInclusion = 1;
    public static boolean queryIncludesFirstParagraph = true;

    public static void loadHyperParameters(final String parameterFile) {
        try (Scanner sc = new Scanner(new FileInputStream(parameterFile))) {
            numSearchResults = sc.nextInt();
            fulltextIncludesHeadings = sc.nextBoolean();
            numHeadingsToPredict = sc.nextInt();
            minHeadingOccurrancesForInclusion = sc.nextInt();
            queryIncludesFirstParagraph = sc.nextBoolean();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static void print() {
        System.out.println("The following hyper-parameters are in use:");
        System.out.println("numRetrieved = " + numSearchResults);
        System.out.println("numGenerated = " + numHeadingsToPredict);
        System.out.println("numOccurrences = " + minHeadingOccurrancesForInclusion);
    }

}
