import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final String AD_CATEGORY = "Kids";
    private static final double SPEND_THRESHOLD = 0.29;

    private static final double CATEGORY_MATCH_MULTIPLIER    = 2.0;
    private static final double CATEGORY_PARTIAL_MULTIPLIER  = 0.8;
    private static final double CATEGORY_MISMATCH_MULTIPLIER = 0.5;
    private static final double SUBSCRIBED_MULTIPLIER        = 1.5;
    private static final double INTEREST_MATCH_MULTIPLIER    = 1.5;
    private static final double YOUNG_AGE_MULTIPLIER         = 1.3;
    private static final double HIGH_ENGAGEMENT_MULTIPLIER   = 1.3;
    private static final double MID_ENGAGEMENT_MULTIPLIER    = 1.1;

    private static final long VIEW_BRACKET_TINY   = 99;
    private static final long VIEW_BRACKET_SMALL  = 999;
    private static final long VIEW_BRACKET_MEDIUM = 4_999;
    private static final long VIEW_BRACKET_LARGE  = 24_999;
    private static final long VIEW_BRACKET_XLARGE = 99_999;
    private static final long VIEW_BRACKET_HUGE   = 499_999;

    private static final double VIEW_SCORE_TINY   = 3.0;
    private static final double VIEW_SCORE_SMALL  = 2.5;
    private static final double VIEW_SCORE_MEDIUM = 2.0;
    private static final double VIEW_SCORE_LARGE  = 1.5;
    private static final double VIEW_SCORE_XLARGE = 1.0;
    private static final double VIEW_SCORE_HUGE   = 0.6;
    private static final double VIEW_SCORE_VIRAL  = 0.3;

    private static final double HIGH_ENGAGEMENT_RATIO = 0.03;
    private static final double MID_ENGAGEMENT_RATIO  = 0.01;

    private static final int BID_TIER1_START = 200;
    private static final int BID_TIER1_MAX   = 1000;
    private static final int BID_TIER2_START = 100;
    private static final int BID_TIER2_MAX   = 500;
    private static final int BID_TIER3_START = 50;
    private static final int BID_TIER3_MAX   = 250;
    private static final int BID_TIER4_START = 20;
    private static final int BID_TIER4_MAX   = 100;
    private static final int BID_MIN_START   = 3;
    private static final int BID_MIN_MAX     = 15;

    private static final double SCORE_TIER1 = 5.0;
    private static final double SCORE_TIER2 = 3.5;
    private static final double SCORE_TIER3 = 2.5;
    private static final double SCORE_TIER4 = 1.5;

    static double calculateScore(String videoCategory, long viewCount, long commentCount,
                                 boolean subscribed, String interests, String age) {
        double score = 1.0;

        if      (viewCount <= VIEW_BRACKET_TINY)   score *= VIEW_SCORE_TINY;
        else if (viewCount <= VIEW_BRACKET_SMALL)  score *= VIEW_SCORE_SMALL;
        else if (viewCount <= VIEW_BRACKET_MEDIUM) score *= VIEW_SCORE_MEDIUM;
        else if (viewCount <= VIEW_BRACKET_LARGE)  score *= VIEW_SCORE_LARGE;
        else if (viewCount <= VIEW_BRACKET_XLARGE) score *= VIEW_SCORE_XLARGE;
        else if (viewCount <= VIEW_BRACKET_HUGE)   score *= VIEW_SCORE_HUGE;
        else                                        score *= VIEW_SCORE_VIRAL;

        if (videoCategory.equalsIgnoreCase(AD_CATEGORY)) {
            score *= CATEGORY_MATCH_MULTIPLIER;
        } else if (videoCategory.equalsIgnoreCase("Music")   ||
                videoCategory.equalsIgnoreCase("DIY")     ||
                videoCategory.equalsIgnoreCase("Cooking")) {
            score *= CATEGORY_PARTIAL_MULTIPLIER;
        } else {
            score *= CATEGORY_MISMATCH_MULTIPLIER;
        }

        if (subscribed) score *= SUBSCRIBED_MULTIPLIER;

        if (interests.contains(AD_CATEGORY)) score *= INTEREST_MATCH_MULTIPLIER;

        if (age.equals("13-17") || age.equals("18-24") || age.equals("25-34")) {
            score *= YOUNG_AGE_MULTIPLIER;
        }

        if (viewCount > 0 && commentCount > 0) {
            double ratio = (double) commentCount / viewCount;
            if      (ratio > HIGH_ENGAGEMENT_RATIO) score *= HIGH_ENGAGEMENT_MULTIPLIER;
            else if (ratio > MID_ENGAGEMENT_RATIO)  score *= MID_ENGAGEMENT_MULTIPLIER;
        }

        return score;
    }

    public static void main(String[] args) throws Exception {
        long budget = Long.parseLong(args[0]);
        long spendLimit = (long)(budget * SPEND_THRESHOLD);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;
        PrintStream log = System.err;

        out.println(AD_CATEGORY);
        out.flush();

        long ebucks = budget;
        int round = 0;

        while (true) {
            String line = in.readLine();
            if (line == null) break;

            if (line.startsWith("S ")) {
                log.println("Summary: " + line);
                continue;
            }

            if (!line.contains("video.category")) {
                log.println("Skip: " + line);
                continue;
            }

            Map<String, String> fields = new HashMap<>();
            for (String part : line.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) fields.put(kv[0].trim(), kv[1].trim());
            }

            String videoCategory = fields.getOrDefault("video.category", "");
            long viewCount       = Long.parseLong(fields.getOrDefault("video.viewCount", "0"));
            long commentCount    = Long.parseLong(fields.getOrDefault("video.commentCount", "0"));
            boolean subscribed   = "Y".equals(fields.getOrDefault("viewer.subscribed", "N"));
            String interests     = fields.getOrDefault("viewer.interests", "");
            String age           = fields.getOrDefault("viewer.age", "25-34");

            double score = calculateScore(videoCategory, viewCount, commentCount,
                    subscribed, interests, age);

            long spent = budget - ebucks;

            if (spent >= spendLimit || ebucks <= 0) {
                out.println("1 1");
                out.flush();
                String skip = in.readLine();
                if (skip == null) break;
                if (skip.startsWith("S ")) {
                    log.println("Summary: " + skip);
                    skip = in.readLine();
                    if (skip == null) break;
                }
                round++;
                continue;
            }

            int startBid;
            int maxBid;

            if      (score >= SCORE_TIER1) { startBid = BID_TIER1_START; maxBid = BID_TIER1_MAX; }
            else if (score >= SCORE_TIER2) { startBid = BID_TIER2_START; maxBid = BID_TIER2_MAX; }
            else if (score >= SCORE_TIER3) { startBid = BID_TIER3_START; maxBid = BID_TIER3_MAX; }
            else if (score >= SCORE_TIER4) { startBid = BID_TIER4_START; maxBid = BID_TIER4_MAX; }
            else                           { startBid = BID_MIN_START;   maxBid = BID_MIN_MAX;   }

            if (maxBid > ebucks)   maxBid   = (int) ebucks;
            if (startBid > maxBid) startBid = maxBid;

            out.println(startBid + " " + maxBid);
            out.flush();

            String result = in.readLine();
            if (result == null) break;

            if (result.startsWith("S ")) {
                log.println("Summary: " + result);
                result = in.readLine();
                if (result == null) break;
            }

            if (result.startsWith("W")) {
                int spent2 = Integer.parseInt(result.substring(2).trim());
                ebucks -= spent2;
                log.println("R" + round + " Won! score=" + String.format("%.2f", score)
                        + " spent=" + spent2 + " rem=" + ebucks);
            }

            round++;
        }
    }
}