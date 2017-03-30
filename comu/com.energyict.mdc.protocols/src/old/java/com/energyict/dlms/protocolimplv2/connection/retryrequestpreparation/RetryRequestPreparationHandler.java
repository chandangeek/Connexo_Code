/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.protocolimplv2.connection.retryrequestpreparation;
import java.io.IOException;

/**
 * Interface used to indicate the class is suitable to prepare a retry request for a given {@link RetryRequestPreparationConsumer}
 *
 * @author sva
 * @since 23/08/2016 - 17:22
 */
public interface RetryRequestPreparationHandler {

    /**
     * Prepare the given request for use as retry request<br/>
     * In most cases, no preparation is needed & the original request can be used 1-on-1 for the retry,
     * in other cases the request should be modified (e.g. increment of frame counter)
     *
     * @param originalRequest the original request
     * @return the new request to be used for the retry
     */
    byte[] prepareRetryRequest(byte[] originalRequest) throws IOException;
}
