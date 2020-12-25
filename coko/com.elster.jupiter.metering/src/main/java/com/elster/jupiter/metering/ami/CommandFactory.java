/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@ConsumerType
public interface CommandFactory {

    EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType) throws UnsupportedCommandException;

    EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity quantity) throws UnsupportedCommandException;

    EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice) throws UnsupportedCommandException;

    EndDeviceCommand createKeyRenewalCommand(EndDevice endDevice, List<SecurityAccessorType> securityAccessortypes, boolean isServiceKey) throws UnsupportedCommandException;

    EndDeviceCommand createGenerateKeyPairCommand(EndDevice endDevice, CertificateType certificateType) throws UnsupportedCommandException;

    EndDeviceCommand createGenerateCSRCommand(EndDevice endDevice, CertificateType certificateType) throws UnsupportedCommandException;

    EndDeviceCommand createImportCertificateCommand(EndDevice endDevice, SecurityAccessorType securityAccessorType) throws UnsupportedCommandException;

    EndDeviceCommand createUpdateCreditAmountCommand(EndDevice endDevice, String creditType, BigDecimal creditAmount) throws UnsupportedCommandException;

    EndDeviceCommand createUpdateCreditDaysLimitCommand(EndDevice endDevice, BigDecimal creditDaysLimitFirst, BigDecimal creditDaysLimitScnd) throws UnsupportedCommandException;

    EndDeviceCommand createChangeStepTariffCommand(EndDevice endDevice, StepTariffInfo stepTariffInfo) throws UnsupportedCommandException;

    EndDeviceCommand createChangeTaxRatesCommand(EndDevice endDevice, ChangeTaxRatesInfo taxRatesInfo) throws UnsupportedCommandException;

    EndDeviceCommand createSwitchTaxAndStepTariffCommand(EndDevice endDevice, String tariffType, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createSwitchChargeModeCommand(EndDevice endDevice, String chargeMode, Instant activationDate);

}