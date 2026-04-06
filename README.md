# Attention Bid Bot

A competitive ad-bidding bot for the Playtech Summer 2026 internship assignment.

## Strategy

The bot advertises in the **Kids** category and uses a score-based bidding system to maximize the ratio of points earned per ebuck spent.

### Key Insights

- The scoring formula is `points / max(spent, 30% of budget)` — spending less than 30% of the budget still counts as 30%, so the optimal strategy is to spend just under the 30% threshold (~2.9M out of 10M) while maximizing points earned.
- Small viewCount videos (under 25,000 views) have higher base values — bidding on these yields more points per ebuck.
- Subscribed viewers and matching viewer interests significantly boost ad value.
- Younger age groups (13-34) respond better to Kids category ads.
- Low-scoring impressions still receive a minimal bid (3-15 ebucks) to help reach the 30% spending threshold without wasting budget on poor impressions.
- The bot stops aggressive bidding once ~2.9M ebucks have been spent.

### Bidding Logic

| Score | Start Bid | Max Bid |
|-------|-----------|---------|
| >= 5.0 | 200 | 1000 |
| >= 3.5 | 100 | 500 |
| >= 2.5 | 50 | 250 |
| >= 1.5 | 20 | 100 |
| < 1.5 | 3 | 15 |

### Score Calculation

Each impression is scored based on:

- **viewCount bracket** — lower view counts yield higher base values (niche videos are worth more per impression than viral ones)
- **Video category match** — Kids videos get a 2x multiplier
- **Subscriber status** — subscribed viewers add 50% to score
- **Viewer interests** — Kids interest adds 50%
- **Age group** — ages 13-34 add 30%
- **Comment/view ratio** — ratio > 0.03 adds 30%, ratio > 0.01 adds 10%

## Building
```bash
cd ~/attention-bid
mkdir -p out/production/attention-bid
javac -d out/production/attention-bid src/Main.java
cd out/production/attention-bid
jar cfe attention-bid.jar Main .
```

## Running

The bot is run by the harness automatically. Place the JAR in its own subdirectory:

harness-dir/
├── harness.jar
└── attention-bid/
└── attention-bid.jar

Then run the harness:
```bash
java -jar harness.jar
```

The bot can also be run standalone with the starting budget as the first argument:
```bash
java -jar attention-bid.jar 10000000
```

## Requirements

- Java 8-25
- No third-party libraries
- Single-threaded
- Max 192MB heap
- Communication via stdin/stdout only
- Logging via stderr