package com.kenzie.marketing.referral.service.dao;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.kenzie.marketing.referral.service.exceptions.InvalidDataException;
import com.kenzie.marketing.referral.service.model.ReferralRecord;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDeleteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ReferralDao {
    private DynamoDBMapper mapper;
    static final Logger log = LogManager.getLogger();

    /**
     * Allows access to and manipulation of Match objects from the data store.
     * @param mapper Access to DynamoDB
     */
    public ReferralDao(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    public ReferralRecord addReferral(ReferralRecord referral) {
        try {
            mapper.save(referral, new DynamoDBSaveExpression()
                    .withExpected(ImmutableMap.of(
                            "CustomerId",
                            new ExpectedAttributeValue().withExists(false)
                    )));
        } catch (ConditionalCheckFailedException e) {
            throw new InvalidDataException("Customer has already been referred");
        }

        return referral;
    }

    public boolean deleteReferral(ReferralRecord referral) {
        try {
            mapper.delete(referral, new DynamoDBDeleteExpression()
                    .withExpected(ImmutableMap.of(
                            "CustomerId",
                            new ExpectedAttributeValue().withValue(new AttributeValue(referral.getCustomerId())).withExists(true)
                    )));
        } catch (AmazonDynamoDBException e) {
            log.info(e.getMessage());
            log.info(e.getStackTrace());
            return false;
        }

        return true;
    }

    public List<ReferralRecord> findByReferrerId(String referrerId) {
        ReferralRecord referralRecord = new ReferralRecord();
        referralRecord.setReferrerId(referrerId);

        DynamoDBQueryExpression<ReferralRecord> queryExpression = new DynamoDBQueryExpression<ReferralRecord>()
                .withHashKeyValues(referralRecord)
                .withIndexName("ReferrerIdIndex")
                .withConsistentRead(false);

        return mapper.query(ReferralRecord.class, queryExpression);
    }

    public List<ReferralRecord> findUsersWithoutReferrerId() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("attribute_not_exists(ReferrerId)");

        return mapper.scan(ReferralRecord.class, scanExpression);
    }
}
