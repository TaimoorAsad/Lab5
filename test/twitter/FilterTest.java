/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy (input/output-space partitioning)
     *
     * writtenBy(tweets, username)
     *  - author match count: 0; 1; >1
     *  - case: username case-insensitive
     *  - order: preserve input order
     *
     * inTimespan(tweets, timespan)
     *  - inclusion: before; inside-start; inside-middle; inside-end; after
     *  - boundaries: inclusive start and end
     *  - results: 0; 1; >1, preserving input order
     *
     * containing(tweets, words)
     *  - words list: empty; one word; multiple words; duplicates
     *  - match: exact token equality (space-bounded), not substring; case-insensitive
     *  - punctuation: token with trailing punctuation doesn't match bare word
     *  - results: 0; 1; >1, preserving input order
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d0 = Instant.parse("2016-02-17T09:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }
    
    @Test
    public void testWrittenByCaseInsensitiveAndOrder() {
        Tweet t3 = new Tweet(3, "Alyssa", "another note", d2);
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet2, tweet1, t3), "ALYSSA");
        assertEquals("two tweets by alyssa", 2, writtenBy.size());
        assertEquals("preserve order 1st is tweet1", tweet1, writtenBy.get(0));
        assertEquals("preserve order 2nd is t3", t3, writtenBy.get(1));
    }
    
    @Test
    public void testWrittenByNoResults() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "nobody");
        assertTrue("expected empty list", writtenBy.isEmpty());
    }
    
    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }
    
    @Test
    public void testInTimespanInclusiveBounds() {
        List<Tweet> inTimespan = Filter.inTimespan(
            Arrays.asList(tweet1, tweet2), new Timespan(d1, d2));
        assertEquals("both included on inclusive bounds", 2, inTimespan.size());
    }
    
    @Test
    public void testInTimespanOnlyStartMatch() {
        List<Tweet> inTimespan = Filter.inTimespan(
            Arrays.asList(tweet1, tweet2), new Timespan(d1, d1));
        assertEquals("only tweet1 at start time", 1, inTimespan.size());
        assertEquals(tweet1, inTimespan.get(0));
    }
    
    @Test
    public void testInTimespanNoMatches() {
        List<Tweet> inTimespan = Filter.inTimespan(
            Arrays.asList(tweet1, tweet2), new Timespan(d0.minusSeconds(7200), d0.minusSeconds(3600)));
        assertTrue("expected empty list", inTimespan.isEmpty());
    }
    
    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));
        
        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }
    
    @Test
    public void testContainingEmptyWords() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList());
        assertTrue("expected empty result for empty words", containing.isEmpty());
    }
    
    @Test
    public void testContainingCaseInsensitiveWord() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("RIVEST"));
        assertEquals("both tweets contain rivest", 2, containing.size());
    }
    
    @Test
    public void testContainingExactTokenNotSubstringOrPunctuation() {
        Tweet t3 = new Tweet(3, "sam", "I enjoy talk, shows", d2);
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, t3), Arrays.asList("talk"));
        assertTrue("tweet1 contains exact token talk", containing.contains(tweet1));
        assertTrue("tweet2 contains exact token talk", containing.contains(tweet2));
        assertFalse("t3 has 'talk,' which should not match 'talk'", containing.contains(t3));
    }
    
    @Test
    public void testContainingAnyOfMultipleWords() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("minutes", "foo"));
        assertEquals("only tweet2 has 'minutes' token", 1, containing.size());
        assertEquals(tweet2, containing.get(0));
    }

}
