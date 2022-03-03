package com.TeamHotel.assignment1;

import java.util.List;
import org.tartarus.snowball.ext.porterStemmer;

final class Heading {
    private final String fullText;
    private final String processedText;

    private Heading(final String fullText, final String processedText) {
        this.fullText = fullText;
        this.processedText = processedText;
    }

    public static Heading of(final String fullText) {
        final StringBuilder preprocessedText = new StringBuilder();
        final porterStemmer stemmer = new porterStemmer();
        List.of(fullText.replaceAll("\\p{Punct}", "").split("\\s+")).stream()
        .map((word) -> word.toLowerCase())
        .map((String word) -> {
            // don't stem words with numbers.  They might be important
            if (word.matches(".*\\d.*")) {
                return word;
            } else {
                // for all other headings we stem them
                stemmer.setCurrent(word);
                stemmer.stem();
                return stemmer.getCurrent();
            }})
        .filter((word) -> !Stopwords.contains(word))
        .forEach((String word) -> preprocessedText.append(word).append(" "));
        if (!preprocessedText.isEmpty()) {
            preprocessedText.deleteCharAt(preprocessedText.length() - 1);
        } else {
            System.err.println("Heading: \"" + fullText + "\" preprocessed to an empty string");
        }
        return new Heading(fullText, preprocessedText.toString());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Heading) {
            Heading heading = (Heading)obj;
            return processedText.equals(heading.processedText);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return processedText.hashCode();
    }

    public String getFullText() {
        return fullText;
    }

    public String getProcessedText() {
        return processedText;
    }

}
