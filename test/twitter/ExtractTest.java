/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy (input/output-space partitioning)
     *
     * getTimespan(tweets)
     *  Partitions for tweets:
     *   - size: 1; >1
     *   - order vs. timestamps: in order; out of order
     *   - timestamps: all equal; distinct min/max
     *  For size=1, expect zero-length [t..t]. For >1, expect [min(ti)..max(ti)] regardless of order.
     *  We avoid asserting behavior for empty input, since the spec is underdetermined there.
     *
     * getMentionedUsers(tweets)
     *  Partitions for mentions within text:
     *   - position: at start; middle; end
     *   - boundaries: preceded/followed by username-valid chars (letters/digits/_/-) vs. not
     *   - case: mixed case usernames; ensure case-insensitive and unique across tweets
     *   - characters: usernames with letters, digits, underscore, hyphen
     *   - false positives: email addresses like name@domain.tld should not count
     *  The returned set is case-insensitive and may choose any case; tests normalize to lowercase.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanSingleTweetZeroLength() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));
        assertEquals("start==tweet time", tweet1.getTimestamp(), timespan.getStart());
        assertEquals("end==tweet time", tweet1.getTimestamp(), timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanOutOfOrderTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet1));
        assertEquals("expected start min", d1, timespan.getStart());
        assertEquals("expected end max", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanSameTimestamp() {
        Tweet t3 = new Tweet(3, "charlie", "same time", d1);
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, t3));
        assertEquals("start==min==d1", d1, timespan.getStart());
        assertEquals("end==max==d1", d1, timespan.getEnd());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }
    
    @Test
    public void testGetMentionedUsersAtStart() {
        Tweet t = new Tweet(3, "alyssa", "@Alice hi there", d1);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        assertTrue("expect alice mentioned", toLower(mentioned).contains("alice"));
    }
    
    @Test
    public void testGetMentionedUsersCaseInsensitiveUniqueAcrossTweets() {
        Tweet tA = new Tweet(3, "alyssa", "talk to @ALICE", d1);
        Tweet tB = new Tweet(4, "ben", "pinging @alice now", d2);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(tA, tB));
        Set<String> lower = toLower(mentioned);
        assertTrue("contains alice", lower.contains("alice"));
        assertEquals("unique usernames", 1, lower.size());
    }
    
    @Test
    public void testGetMentionedUsersIgnoreEmailLike() {
        Tweet t = new Tweet(3, "alyssa", "contact bitdiddle@mit.edu please", d1);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        assertFalse("should not contain mit", toLower(mentioned).contains("mit"));
        assertTrue("no mentions", mentioned.isEmpty());
    }
    
    @Test
    public void testGetMentionedUsersBoundaryPrecededByUsernameChar() {
        Tweet t = new Tweet(3, "alyssa", "X@bob is invalid, but hey @bob works", d1);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        Set<String> lower = toLower(mentioned);
        assertTrue("contains bob once", lower.contains("bob"));
        assertEquals("only one valid mention of bob", 1, lower.size());
    }
    
    @Test
    public void testGetMentionedUsersValidHyphenUnderscoreAndDigits() {
        Tweet t = new Tweet(3, "alyssa", "hi @a_b-1!", d1);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        assertTrue("contains a_b-1", toLower(mentioned).contains("a_b-1"));
    }
    
    @Test
    public void testGetMentionedUsersAtEndWithPunctuation() {
        Tweet t = new Tweet(3, "alyssa", "talk to @Charlie!", d1);
        Set<String> mentioned = Extract.getMentionedUsers(Arrays.asList(t));
        assertTrue("contains charlie", toLower(mentioned).contains("charlie"));
    }

    // helper: normalize set strings to lowercase for case-insensitive assertions
    private static Set<String> toLower(Set<String> in) {
        java.util.Set<String> out = new java.util.HashSet<>();
        for (String s : in) out.add(s.toLowerCase());
        return out;
    }

}
