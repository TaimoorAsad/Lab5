/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }
    
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertTrue("expected empty list", influencers.isEmpty());
    }

    // 2. Tweets Without Mentions: no entries should be added to the graph
    @Test
    public void testGuessFollowsGraphNoMentions() {
        final Instant t = Instant.parse("2023-01-01T00:00:00Z");
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, "Alice", "hello world", t),
            new Tweet(2, "Bob", "nice day", t)
        );
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue("expected empty graph when no mentions", followsGraph.isEmpty());
    }

    // 3. Single Mention: author who mentions someone follows that user
    @Test
    public void testGuessFollowsGraphSingleMention() {
        final Instant t = Instant.parse("2023-01-01T00:00:00Z");
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, "Ernie", "hi @Bert!", t)
        );
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue("expected author present", followsGraph.containsKey("ernie"));
        assertTrue("expected mention as followee", followsGraph.get("ernie").contains("bert"));
    }

    // 4. Multiple Mentions: all mentioned users should be followed by the author
    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        final Instant t = Instant.parse("2023-01-01T00:00:00Z");
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, "Charlie", "hey @Alice and @Bob, check this out", t)
        );
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);
        Set<String> expected = new HashSet<>(Arrays.asList("alice", "bob"));
        assertEquals("expected both mentions followed", expected, followsGraph.get("charlie"));
    }

    // 5. Multiple Tweets from One User: repeated mentions across tweets are captured and deduplicated
    @Test
    public void testGuessFollowsGraphMultipleTweetsOneUser() {
        final Instant t1 = Instant.parse("2023-01-01T00:00:00Z");
        final Instant t2 = Instant.parse("2023-01-02T00:00:00Z");
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, "Dana", "@Evan news?", t1),
            new Tweet(2, "Dana", "ping @Evan and @Fran", t2)
        );
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);
        Set<String> expected = new HashSet<>(Arrays.asList("evan", "fran"));
        assertEquals("expected union of mentions", expected, followsGraph.get("dana"));
    }

    // 7. Single User Without Followers: a user without followers still appears with 0 influence
    @Test
    public void testInfluencersSingleUserNoFollowers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>());
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        assertEquals("expected single user returned", Arrays.asList("alice"), influencers);
    }

    // 8. Single Influencer: one user has followers
    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("a", new HashSet<>(Arrays.asList("b")));
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        assertEquals("b should have most followers", "b", influencers.get(0));
        assertTrue("a should also be listed", influencers.contains("a"));
    }

    // 9. Multiple Influencers: verify correct ordering for different follower counts
    @Test
    public void testInfluencersMultiple() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("a", new HashSet<>(Arrays.asList("b")));
        followsGraph.put("c", new HashSet<>(Arrays.asList("b")));
        followsGraph.put("d", new HashSet<>(Arrays.asList("c")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);
        assertFalse(influencers.isEmpty());
        // b should be first (2 followers), c should be second (1 follower)
        assertEquals("b", influencers.get(0));
        assertEquals("c", influencers.get(1));
        // The remaining users have 0 followers; their relative order is unspecified
        assertTrue(influencers.containsAll(Arrays.asList("a", "d")));
    }

    // 10. Tied Influence: equal influencers are handled; both appear before users with fewer followers
    @Test
    public void testInfluencersTied() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("x", new HashSet<>(Arrays.asList("b")));
        followsGraph.put("y", new HashSet<>(Arrays.asList("c")));
        // b and c each have 1 follower; a and d have 0
        followsGraph.put("a", new HashSet<>());
        followsGraph.put("d", new HashSet<>());

        List<String> influencers = SocialNetwork.influencers(followsGraph);
        // First two must be some ordering of b and c
        List<String> topTwo = influencers.subList(0, 2);
        assertTrue(topTwo.contains("b") && topTwo.contains("c"));
        // All users should be present
        assertTrue(influencers.containsAll(Arrays.asList("a", "b", "c", "d", "x", "y")));
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

}
