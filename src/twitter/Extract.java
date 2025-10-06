/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            // Under-determined by spec; choose zero-length interval
            return new Timespan(Instant.EPOCH, Instant.EPOCH);
        }

        Instant start = tweets.get(0).getTimestamp();
        Instant end = start;

        for (Tweet tweet : tweets) {
            Instant t = tweet.getTimestamp();
            if (t.isBefore(start)) {
                start = t;
            }
            if (t.isAfter(end)) {
                end = t;
            }
        }

        return new Timespan(start, end);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        // Username rules (from Tweet.getAuthor spec): letters, digits, underscore, hyphen
        // Mention definition (from this spec): "@" followed by a username, and cannot be
        // immediately preceded or followed by a username-valid character.
        // Case-insensitive; return each username at most once.

        final java.util.Set<String> mentioned = new java.util.HashSet<>();
        final String usernameCharClass = "[A-Za-z0-9_-]";
        final String notUsernameCharClass = "[^A-Za-z0-9_-]";
        // Use word boundary-like guards based on allowed username chars.
        // Preceding char: start of string or NOT a username char.
        // Following part: capture 1+ username chars; ensure next char is end or NOT a username char.
        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?i)(?:^|" + notUsernameCharClass + ")@(" + usernameCharClass + "+)(?=$|" + notUsernameCharClass + ")");

        for (Tweet tweet : tweets) {
            String text = tweet.getText();
            java.util.regex.Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String user = matcher.group(1).toLowerCase();
                mentioned.add(user);
            }
        }
        return mentioned;
    }

}
