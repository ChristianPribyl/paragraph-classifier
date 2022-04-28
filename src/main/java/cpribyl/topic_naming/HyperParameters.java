package cpribyl.topic_naming;

/**
 * HyperParameters
 * 
 * A class for storing variables whose ideal values can only be
 * determined by running the model.
 *
 */
public class HyperParameters {
    public static int numSearchResults = 20;
    public static boolean fulltextIncludesHeadings = true;
    public static long numHeadingsToPredict = 10;
    public static int minHeadingOccurrencesForInclusion = 1;
    public static boolean queryIncludesFirstParagraph = true;

    public static void print() {
        System.out.println("The following hyper-parameters are in use:");
        System.out.println("numRetrieved = " + numSearchResults);
        System.out.println("numGenerated = " + numHeadingsToPredict);
        System.out.println("numOccurrences = " + minHeadingOccurrencesForInclusion);
    }

}
