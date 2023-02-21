package com.kenzie.marketing.referral.service.task;

import com.kenzie.marketing.referral.model.LeaderboardEntry;
import com.kenzie.marketing.referral.service.ReferralService;
import com.kenzie.marketing.referral.service.model.ReferralRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class ReferralTask implements Callable<List<LeaderboardEntry>> {
    ReferralRecord record;
    ReferralService referralService;

    public ReferralTask (ReferralRecord record, ReferralService referralService) {
        this.record = record;
        this.referralService = referralService;
    }

    @Override
    public List<LeaderboardEntry> call() {
        Comparator<LeaderboardEntry> comparator = (o1, o2) -> o2.getNumReferrals() - o1.getNumReferrals();

        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setNumReferrals(referralService.getDirectReferrals(record.getCustomerId()).size());
        entry.setCustomerId(record.getCustomerId());
        leaderboardEntries.add(entry);

        leaderboardEntries.sort(comparator);

        List<LeaderboardEntry> topFive = new ArrayList<>();

        for (int i = 0; i < leaderboardEntries.size(); i++) {
            if (i == 6) {
                return topFive;
            }
            topFive.add(leaderboardEntries.get(i));
        }

        return topFive;
    }
}