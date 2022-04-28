package cpribyl.topic_naming;

import java.io.InputStream;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

/**
 * Stopwords
 * Singleton for loading and using stopwords.
 */
class Stopwords {
    static Set<String> stopWords;
    final static String stopWordFile = "stop.txt";
    static boolean stopwordsLoaded = false;

    public static boolean contains(final String word) {
        assert(stopwordsLoaded);
        return stopWords.contains(word);
    }

    public static void loadStopwords() {
        if (!stopwordsLoaded) {
            stopWords = new HashSet<>();
            try {
                InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream(stopWordFile);
                assert file != null;
                final Scanner sc = new Scanner(file);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.indexOf('|') != -1) {
                        line = line.substring(0, line.indexOf('|'));
                    }
                    final String word = line.trim();
                    if (!word.isEmpty()) {
                        stopWords.add(word);
                    }
                }
                sc.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            stopwordsLoaded = true;
        }
    }
}