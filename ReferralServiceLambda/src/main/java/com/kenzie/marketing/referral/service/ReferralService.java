package com.kenzie.marketing.referral.service;

import com.kenzie.marketing.referral.model.CustomerReferrals;
import com.kenzie.marketing.referral.model.LeaderboardEntry;
import com.kenzie.marketing.referral.model.Referral;
import com.kenzie.marketing.referral.model.ReferralRequest;
import com.kenzie.marketing.referral.model.ReferralResponse;
import com.kenzie.marketing.referral.service.converter.ReferralConverter;
import com.kenzie.marketing.referral.service.dao.ReferralDao;
import com.kenzie.marketing.referral.service.exceptions.InvalidDataException;
import com.kenzie.marketing.referral.service.model.ReferralRecord;
import com.kenzie.marketing.referral.service.task.ReferralTask;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReferralService {

    private ReferralDao referralDao;
    private ExecutorService executor;

    @Inject
    public ReferralService(ReferralDao referralDao) {
        this.referralDao = referralDao;
        this.executor = Executors.newCachedThreadPool();
    }

    // Necessary for testing, do not delete
    public ReferralService(ReferralDao referralDao, ExecutorService executor) {
        this.referralDao = referralDao;
        this.executor = executor;
    }

    public List<LeaderboardEntry> getReferralLeaderboard() {
        List<ReferralRecord> nodes = referralDao.findUsersWithoutReferrerId();
        List<LeaderboardEntry> threadsList = new ArrayList<>();
        List<Future<List<LeaderboardEntry>>> threadFutures = new ArrayList<>();
        List<LeaderboardEntry> topFive = new ArrayList<>();
        Comparator<LeaderboardEntry> comparator = (o1, o2) -> o2.getNumReferrals() - o1.getNumReferrals();

        for (ReferralRecord node : nodes) {
            ReferralTask task = new ReferralTask(node, this);
            threadFutures.add(executor.submit(task));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Executor was interrupted " + e);
        }

        for (Future<List<LeaderboardEntry>> list : threadFutures) {
            try {
                List<LeaderboardEntry> leaderboardEntries = list.get();
                if (leaderboardEntries != null) {
                    threadsList.addAll(leaderboardEntries);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }

        threadsList.sort(comparator);

        for (int i = 0; i < threadsList.size(); i++) {
            if (i == 6) {
                return topFive;
            }
            topFive.add(threadsList.get(i));
        }

        return topFive;
    }

        public CustomerReferrals getCustomerReferralSummary(String customerId) {
        CustomerReferrals referrals = new CustomerReferrals();

        // Task 2 Code Here
        List<Referral> directReferrals = getDirectReferrals(customerId);
        referrals.setNumFirstLevelReferrals(directReferrals.size());

        int firstLevelReferrals = 0;
        int secondLevelReferrals = 0;

        for (Referral referral: directReferrals) {
            firstLevelReferrals += getDirectReferrals(referral.getCustomerId()).size();
            for (Referral thirdReferral : getDirectReferrals(referral.getCustomerId())) {
                secondLevelReferrals += getDirectReferrals(thirdReferral.getCustomerId()).size();
            }
        }

        referrals.setNumSecondLevelReferrals(firstLevelReferrals);
        referrals.setNumThirdLevelReferrals(secondLevelReferrals);

        return referrals;
    }


    public List<Referral> getDirectReferrals(String customerId) {
        List<ReferralRecord> records = referralDao.findByReferrerId(customerId);

        // Task 1 Code Here
        return records.stream()
                .map(r -> new Referral(r.getCustomerId(), r.getReferrerId(), r.getDateReferred().toString()))
                .collect(Collectors.toList());
    }


    public ReferralResponse addReferral(ReferralRequest referral) {
        if (referral == null || referral.getCustomerId() == null || referral.getCustomerId().length() == 0) {
            throw new InvalidDataException("Request must contain a valid Customer ID");
        }
        ReferralRecord record = ReferralConverter.fromRequestToRecord(referral);
        referralDao.addReferral(record);
        return ReferralConverter.fromRecordToResponse(record);
    }

    public boolean deleteReferrals(List<String> customerIds){
        boolean allDeleted = true;

        if(customerIds == null){
            throw new InvalidDataException("Request must contain a valid list of Customer ID");
        }

        for(String customerId : customerIds){
            if(customerId == null || customerId.length() == 0){
                throw new InvalidDataException("Customer ID cannot be null or empty to delete");
            }

            ReferralRecord record = new ReferralRecord();
            record.setCustomerId(customerId);

            boolean deleted = referralDao.deleteReferral(record);

            if(!deleted){
                allDeleted = false;
            }
        }
        return allDeleted;
    }
}
