import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static double calculateScore(String videoCategory, long viewCount, long commentCount,
                                 boolean subscribed, String interests, String age) {
        double score = 1.0;

        if      (viewCount <= 99)     score *= 3.0;
        else if (viewCount <= 999)    score *= 2.5;
        else if (viewCount <= 4999)   score *= 2.0;
        else if (viewCount <= 24999)  score *= 1.5;
        else if (viewCount <= 99999)  score *= 1.0;
        else if (viewCount <= 499999) score *= 0.6;
        else                          score *= 0.3;

        if (videoCategory.equalsIgnoreCase("Kids")) score *= 2.0;
        else if (videoCategory.equalsIgnoreCase("Music")   ||
                videoCategory.equalsIgnoreCase("DIY")     ||
                videoCategory.equalsIgnoreCase("Cooking")) score *= 0.8;
        else score *= 0.5;

        if (subscribed) score *= 1.5;

        if (interests.contains("Kids")) score *= 1.5;

        if (age.equals("13-17") || age.equals("18-24") || age.equals("25-34")) {
            score *= 1.3;
        }

        if (viewCount > 0 && commentCount > 0) {
            double ratio = (double) commentCount / viewCount;
            if (ratio > 0.03) score *= 1.3;
            else if (ratio > 0.01) score *= 1.1;
        }

        return score;
    }

    public static void main(String[] args) throws Exception {
        long budget = Long.parseLong(args[0]);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;
        PrintStream log = System.err;

        out.println("Kids");
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

            if (spent >= 2_900_000 || ebucks <= 0) {
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

            if (score >= 5.0) {
                startBid = 200; maxBid = 1000;
            } else if (score >= 3.5) {
                startBid = 100; maxBid = 500;
            } else if (score >= 2.5) {
                startBid = 50;  maxBid = 250;
            } else if (score >= 1.5) {
                startBid = 20;  maxBid = 100;
            } else {
                startBid = 3;   maxBid = 15;
            }

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