package com.elster.jupiter.prepayment.impl.fullduplex;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.servicecall.ServiceCall;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Interface containing the FullDuplex methods needed by Prepayment<br/>
 * Note: this is a draft/dummy version, which should be replaced by the actual FullDuplexInterface (once fully developed by Platform/Kore team)
 *
 * @author sva
 * @since 1/04/2016 - 15:01
 */
public interface FullDuplexInterface {

    void armBreaker(EndDevice endDevice, ServiceCall serviceCall, Instant activationDate);

    void connectBreaker(EndDevice endDevice, ServiceCall serviceCall, Instant activationDate);

    void disconnectBreaker(EndDevice endDevice, ServiceCall serviceCall, Instant activationDate);

    void disableLoadLimiting(EndDevice endDevice, ServiceCall serviceCall);

    void configureLoadLimitThresholdAndDuration(EndDevice endDevice, ServiceCall serviceCall, BigDecimal limit, String unit, Integer loadTolerance);

    void configureLoadLimitThreshold(EndDevice endDevice, ServiceCall serviceCall, BigDecimal limit, String unit);

    void scheduleStatusInformationTask(EndDevice endDevice, Instant scheduleTime);

}