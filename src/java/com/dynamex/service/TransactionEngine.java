package com.dynamex.service;

import com.dynamex.model.Denomination;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Change-making engine.
 *
 * The DP solves the BOUNDED coin-change problem: each denomination has a
 * finite quantity in the cash drawer, so we cannot reuse a coin more times
 * than what is physically available. This is the multi-constrained / 0-1
 * Knapsack variant of coin change (as opposed to the textbook unbounded
 * version where every coin has infinite supply).
 */
public class TransactionEngine {

    public static class TransactionResult {
        public final int changeCentavos;
        public final Map<Integer,Integer> dpBreakdown;
        public final int dpUnits;
        public final int greedyUnits;
        public final int unitsSaved;
        public final long dpRuntimeNanos;
        public final boolean feasible;
        public final String message;

        public TransactionResult(int change,
                                 Map<Integer,Integer> dpBreakdown,
                                 int dpUnits,
                                 int greedyUnits,
                                 int unitsSaved,
                                 long ns,
                                 boolean feasible,
                                 String message) {
            this.changeCentavos = change;
            this.dpBreakdown = dpBreakdown;
            this.dpUnits = dpUnits;
            this.greedyUnits = greedyUnits;
            this.unitsSaved = unitsSaved;
            this.dpRuntimeNanos = ns;
            this.feasible = feasible;
            this.message = message;
        }
    }

    /**
     * Bounded coin-change DP.
     *
     * dp[a] = minimum number of physical units required to assemble amount `a`
     * given the supply vector `quantities` for each denomination in `coins`.
     *
     * For every coin type (v, k) we relax all amounts in descending order
     * (the standard 0-1 knapsack sweep, generalised to bounded multiplicity
     * by trying c = 1..k copies). This guarantees each coin instance is
     * counted at most once.
     *
     * Complexity: O(amount * sum(quantities)). For typical drawer sizes
     * (a few hundred units) this stays well below the unbounded version's
     * O(amount * coins) only nominally — but it is the price we pay for
     * physically realisable change.
     */
    public static class BoundedResult {
        public final int[] perCoinUsage;   // aligned with input coins[]
        public final int totalUnits;
        public final long runtimeNanos;
        public final boolean feasible;

        public BoundedResult(int[] perCoinUsage, int totalUnits, long ns, boolean feasible) {
            this.perCoinUsage = perCoinUsage;
            this.totalUnits = totalUnits;
            this.runtimeNanos = ns;
            this.feasible = feasible;
        }
    }

    public BoundedResult minCoinsBounded(int[] coins, int[] quantities, int amount) {
        long start = System.nanoTime();

        int n = coins.length;
        if (amount < 0) {
            return new BoundedResult(new int[n], 0, System.nanoTime() - start, false);
        }
        if (amount == 0) {
            return new BoundedResult(new int[n], 0, System.nanoTime() - start, true);
        }

        final int INF = Integer.MAX_VALUE / 4;
        int[] dp = new int[amount + 1];
        // pickedCount[i][a] = how many of coin i were used to reach the optimum at amount a
        int[][] picked = new int[n][amount + 1];

        for (int a = 1; a <= amount; a++) dp[a] = INF;
        dp[0] = 0;

        // Bounded knapsack sweep: process coin types one at a time,
        // relaxing amounts from high to low so each coin instance is
        // committed at most once per pass.
        for (int i = 0; i < n; i++) {
            int v = coins[i];
            int k = quantities[i];
            if (v <= 0 || k <= 0) continue;

            for (int a = amount; a >= v; a--) {
                int bestUse = 0;
                int bestDp  = dp[a];
                for (int c = 1; c <= k && c * v <= a; c++) {
                    int prev = dp[a - c * v];
                    if (prev == INF) continue;
                    int cand = prev + c;
                    if (cand < bestDp) {
                        bestDp = cand;
                        bestUse = c;
                    }
                }
                if (bestUse > 0) {
                    dp[a] = bestDp;
                    picked[i][a] = bestUse;
                }
            }
        }

        if (dp[amount] >= INF) {
            return new BoundedResult(new int[n], -1, System.nanoTime() - start, false);
        }

        // Reconstruct usage by walking backwards through coin types.
        int[] usage = new int[n];
        int remaining = amount;
        for (int i = n - 1; i >= 0; i--) {
            int used = picked[i][remaining];
            if (used > 0) {
                usage[i] = used;
                remaining -= used * coins[i];
            }
        }

        return new BoundedResult(usage, dp[amount], System.nanoTime() - start, true);
    }

    /**
     * Bounded greedy: take as many of each denomination as fits AND as many
     * as the drawer holds, in descending value order. Returns -1 if it
     * cannot exactly match the amount with the available supply.
     */
    public int greedyUnitsBounded(int[] coinsDescending, int[] quantitiesDescending, int amount) {
        int remaining = amount;
        int units = 0;
        for (int i = 0; i < coinsDescending.length; i++) {
            int c = coinsDescending[i];
            int q = quantitiesDescending[i];
            if (c <= 0 || q <= 0) continue;
            int take = Math.min(remaining / c, q);
            units += take;
            remaining -= take * c;
            if (remaining == 0) return units;
        }
        return remaining == 0 ? units : -1;
    }

    public TransactionResult computeChange(double totalDue, double cashTendered,
                                           List<Denomination> denominations) {
        int totalCent = toCent(totalDue);
        int cashCent  = toCent(cashTendered);
        int change    = cashCent - totalCent;
        if (change < 0) {
            return null;
        }

        // Snapshot available denominations + their bounded quantities, sorted
        // by descending value (so reconstruction yields a natural "bills first"
        // breakdown).
        List<Denomination> avail = new ArrayList<>();
        for (Denomination d : denominations) {
            if (d.isAvailable() && d.getQuantity() > 0) {
                avail.add(d);
            }
        }
        avail.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int n = avail.size();
        int[] coinsDesc = new int[n];
        int[] qtyDesc   = new int[n];
        for (int i = 0; i < n; i++) {
            coinsDesc[i] = avail.get(i).getValue();
            qtyDesc[i]   = avail.get(i).getQuantity();
        }

        BoundedResult dp = minCoinsBounded(coinsDesc, qtyDesc, change);

        if (!dp.feasible) {
            return new TransactionResult(
                    change, new LinkedHashMap<Integer,Integer>(),
                    -1, -1, 0, dp.runtimeNanos, false,
                    "Insufficient denominations in the drawer to assemble exact change.");
        }

        Map<Integer,Integer> ordered = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            if (dp.perCoinUsage[i] > 0) {
                ordered.put(coinsDesc[i], dp.perCoinUsage[i]);
            }
        }

        int greedy = greedyUnitsBounded(coinsDesc, qtyDesc, change);
        int saved  = (greedy < 0) ? 0 : Math.max(0, greedy - dp.totalUnits);

        return new TransactionResult(
                change, ordered, dp.totalUnits, greedy, saved,
                dp.runtimeNanos, true, "OK");
    }

    /** Atomically deduct the breakdown from the live denomination list. */
    public synchronized boolean applyBreakdown(Map<Integer,Integer> breakdown,
                                               List<Denomination> denominations) {
        // Validate first so we never partially deduct.
        for (Map.Entry<Integer,Integer> e : breakdown.entrySet()) {
            Denomination d = findIn(denominations, e.getKey());
            if (d == null || d.getQuantity() < e.getValue()) {
                return false;
            }
        }
        for (Map.Entry<Integer,Integer> e : breakdown.entrySet()) {
            Denomination d = findIn(denominations, e.getKey());
            d.deduct(e.getValue());
        }
        return true;
    }

    /** Add cash tendered (a single bill/coin) into the drawer when finalising. */
    public synchronized void depositTendered(int valueCent, List<Denomination> denominations) {
        Denomination d = findIn(denominations, valueCent);
        if (d != null) d.add(1);
    }

    private static Denomination findIn(List<Denomination> denominations, int value) {
        for (Denomination d : denominations) {
            if (d.getValue() == value) return d;
        }
        return null;
    }

    private static int toCent(double v) {
        return (int) Math.round(v * 100.0);
    }
}
