/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.protocolimplv2.connection.retryrequestpreparation;

/**
 *  * Interface used to indicate the class can use a {@link RetryRequestV2PreparationHandler} to prepare retry requests
 *
 * @author sva
 * @since 23/08/2016 - 17:25
 */
public interface RetryRequestV2PreparationConsumer {

    /**
     * Sets the {@link RetryRequestV2PreparationHandler} which should be used to prepare the retry requests
     *
     * @param retryRequestPreparationHandler
     */
    void setRetryRequestPreparationHandler(RetryRequestV2PreparationHandler retryRequestPreparationHandler);

    /**
     * Getter for boolean indicating whether the frame counter should be increased for each retry request
     * or whether the same frame counter should be used for all retries
     */
    boolean incrementFrameCounterForRetries();

}
