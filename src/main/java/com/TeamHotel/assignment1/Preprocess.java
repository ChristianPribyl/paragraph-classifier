package com.TeamHotel.assignment1;

import java.util.List;

/**
 * Proprocess
 * A utility class providing functions for preprocessing strings for use in queries.
 * 
 * FIXME: move all preprocessing to this class.  Or remove this class entirely.
 * 
 * @NotThreadSafe
 */
public class Preprocess {

    public static String searchtext(String string) {
        return string.replaceAll("[^\\w\\s]", "").toLowerCase();
    }

    public static String indexSections(String sections) {
        StringBuilder sb = new StringBuilder();
        List.of(sections.split(",")).iterator().forEachRemaining(section -> {
            sb.append(section.trim().replaceAll("[^\\w\\s]", "")).append(",");
        });
        return sb.toString();
    }

}
