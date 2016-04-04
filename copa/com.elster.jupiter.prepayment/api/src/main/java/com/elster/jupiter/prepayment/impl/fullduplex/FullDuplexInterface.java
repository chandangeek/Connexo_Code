package com.elster.jupiter.prepayment.impl.fullduplex;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;

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

    void armBreaker(Device device, ServiceCall serviceCall, Instant activationDate);

    void connectBreaker(Device device, ServiceCall serviceCall, Instant activationDate);

    void disconnectBreaker(Device device, ServiceCall serviceCall, Instant activationDate);

    void disableLoadLimiting(Device device, ServiceCall serviceCall);

    void configureLoadLimitThresholdAndDuration(Device device, ServiceCall serviceCall, BigDecimal limit, String unit, Integer[] tariffs, Integer loadTolerance);

    void configureLoadLimitThreshold(Device device, ServiceCall serviceCall, BigDecimal limit, String unit, Integer[] tariffs);

    void configureLoadLimitDuration(Device device, ServiceCall serviceCall, Integer loadTolerance);

    void configureLoadLimitMeasurementReadingType(Device device, ServiceCall serviceCall, String measurementReadingType);

}