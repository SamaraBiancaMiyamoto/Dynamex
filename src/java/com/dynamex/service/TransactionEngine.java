package com.dynamex.service;

import com.dynamex.model.Denomination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionEngine {

    public static class Result {
        public final List<Integer> denominations;  
        public final int totalUnits;
        public final long runtimeNanos;
        public Result(List<Integer> d, int n, long ns) {
            this.denominations = d;
            this.totalUnits = n;
            this.runtimeNanos = ns;
        }
    }

    public static class TransactionResult {
        public final int changeCentavos;
        public final Map<Integer,Integer> dpBreakdown;    
        public final int dpUnits;
        public final int greedyUnits;    
        public final int unitsSaved;       
        public final long dpRuntimeNanos;

        public TransactionResult(int change,
                                 Map<Integer,Integer> dpBreakdown,
                                 int dpUnits,
                                 int greedyUnits,
                                 int unitsSaved,
                                 long ns) {
            this.changeCentavos = change;
            this.dpBreakdown = dpBreakdown;
            this.dpUnits = dpUnits;
            this.greedyUnits = greedyUnits;
            this.unitsSaved = unitsSaved;
            this.dpRuntimeNanos = ns;
        }
    }

    public Result minCoins(int[] coins, int amount) {
        long start = System.nanoTime();

        if (amount < 0) return null;
        if (amount == 0) {
            return new Result(new ArrayList<Integer>(), 0, System.nanoTime() - start);
        }

        int[] dp        = new int[amount + 1];
        int[] usedCoin  = new int[amount + 1];

        for (int i = 0; i <= amount; i++) {
            dp[i] = Integer.MAX_VALUE;
            usedCoin[i] = -1;
        }
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int j = 0; j < coins.length; j++) {
                int c = coins[j];
                if (c <= i && dp[i - c] != Integer.MAX_VALUE) {
                    if (dp[i - c] + 1 < dp[i]) {
                        dp[i] = dp[i - c] + 1;
                        usedCoin[i] = c;
                    }
                }
            }
        }

        if (dp[amount] == Integer.MAX_VALUE) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        int current = amount;
        while (current > 0) {
            int coin = usedCoin[current];
            result.add(coin);
            current -= coin;
        }

        return new Result(result, dp[amount], System.nanoTime() - start);
    }

    public int greedyUnits(int[] coinsDescending, int amount) {
        int remaining = amount;
        int units = 0;
        for (int c : coinsDescending) {
            if (c <= 0) continue;
            int take = remaining / c;
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

        List<Integer> availList = new ArrayList<>();
        for (Denomination d : denominations) {
            if (d.isAvailable()) {
                availList.add(d.getValue());
            }
        }
        availList.sort(Collections.reverseOrder());

        int[] coinsAsc = new int[availList.size()];
        int[] coinsDesc = new int[availList.size()];
        for (int i = 0; i < availList.size(); i++) {
            coinsDesc[i] = availList.get(i);
            coinsAsc[availList.size() - 1 - i] = availList.get(i);
        }

        Result dp = minCoins(coinsAsc, change);
        if (dp == null) {
            return new TransactionResult(change, new LinkedHashMap<>(), -1, -1, 0, 0);
        }

        Map<Integer,Integer> counts = new LinkedHashMap<>();
        for (int v : coinsDesc) counts.put(v, 0);
        for (int v : dp.denominations) {
            counts.put(v, counts.getOrDefault(v, 0) + 1);
        }
        Map<Integer,Integer> ordered = new LinkedHashMap<>();
        for (Map.Entry<Integer,Integer> e : counts.entrySet()) {
            if (e.getValue() > 0) ordered.put(e.getKey(), e.getValue());
        }

        int greedy = greedyUnits(coinsDesc, change);
        int saved  = (greedy < 0) ? 0 : Math.max(0, greedy - dp.totalUnits);

        return new TransactionResult(change, ordered, dp.totalUnits, greedy, saved, dp.runtimeNanos);
    }

    private static int toCent(double v) {
        return (int) Math.round(v * 100.0);
    }
}
