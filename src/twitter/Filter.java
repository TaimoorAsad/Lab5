/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filter consists of methods that filter a list of tweets for those matching a
 * condition.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Filter {

    /**
     * Find tweets written by a particular user.
     * 
     * @param tweets
     *            a list of tweets with distinct ids, not modified by this method.
     * @param username
     *            Twitter username, required to be a valid Twitter username as
     *            defined by Tweet.getAuthor()'s spec.
     * @return all and only the tweets in the list whose author is username,
     *         in the same order as in the input list.
     */
    public static List<Tweet> writtenBy(List<Tweet> tweets, String username) {
        final String normalized = username.toLowerCase(Locale.ROOT);
        final List<Tweet> result = new ArrayList<>();
        for (Tweet t : tweets) {
            if (t.getAuthor().toLowerCase(Locale.ROOT).equals(normalized)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Find tweets that were sent during a particular timespan.
     * 
     * @param tweets
     *            a list of tweets with distinct ids, not modified by this method.
     * @param timespan
     *            timespan
     * @return all and only the tweets in the list that were sent during the timespan,
     *         in the same order as in the input list.
     */
    public static List<Tweet> inTimespan(List<Tweet> tweets, Timespan timespan) {
        final Instant start = timespan.getStart();
        final Instant end = timespan.getEnd();
        final List<Tweet> result = new ArrayList<>();
        for (Tweet t : tweets) {
            Instant ts = t.getTimestamp();
            if (!ts.isBefore(start) && !ts.isAfter(end)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Find tweets that contain certain words.
     * 
     * @param tweets
     *            a list of tweets with distinct ids, not modified by this method.
     * @param words
     *            a list of words to search for in the tweets. 
     *            A word is a nonempty sequence of nonspace characters.
     * @return all and only the tweets in the list such that the tweet text (when 
     *         represented as a sequence of nonempty words bounded by space characters 
     *         and the ends of the string) includes *at least one* of the words 
     *         found in the words list. Word comparison is not case-sensitive,
     *         so "Obama" is the same as "obama".  The returned tweets are in the
     *         same order as in the input list.
     */
    public static List<Tweet> containing(List<Tweet> tweets, List<String> words) {
        final List<Tweet> result = new ArrayList<>();
        if (words.isEmpty()) return result;

        // Pre-normalize search words to lowercase
        final java.util.Set<String> search = new java.util.HashSet<>();
        for (String w : words) {
            if (w != null && !w.isEmpty()) {
                search.add(w.toLowerCase(Locale.ROOT));
            }
        }
        if (search.isEmpty()) return result;

        for (Tweet t : tweets) {
            String text = t.getText();
            // Split by spaces per spec; treat sequence of nonempty words bounded by spaces/ends
            String[] tokens = text.split(" ");
            boolean matched = false;
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                String tokenLower = token.toLowerCase(Locale.ROOT);
                if (search.contains(tokenLower)) {
                    matched = true;
                    break;
                }
            }
            if (matched) result.add(t);
        }
        return result;
    }

}
