/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.hsm.gogo;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.api.ServerStatus;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.impl.HsmConfigurationService;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.response.ServiceKeyInjectionResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * This class is just for test purpose for the time being
 */
@Component(name = "com.elster.jupiter.hsm.gogo.HsmConfigGogo",
        service = {HsmConfigGogo.class},
        property = {"name=HSM.console",
                "osgi.command.scope=jupiter",
                "osgi.command.function=hsmConfigPrint",
                "osgi.command.function=hsmPrepareServiceKey",
                "osgi.command.function=hsmServiceKeyInjection"},
        immediate = true)
public class HsmConfigGogo {

    private static final Logger logger = LoggerFactory.getLogger(HsmConfigGogo.class);

    private volatile HsmEnergyService hsmEnergyService;
    private volatile HsmConfigurationService hsmConfigurationService;

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) {
        this.hsmConfigurationService = hsmConfigurationService;
    }

    public void hsmConfigPrint() throws HsmBaseException {
        logger.debug("hsmConfigPrint");
        HsmConfiguration hsmConfiguration = hsmConfigurationService.getHsmConfiguration();
        System.out.println("JSS file:" + hsmConfiguration.getJssInitFile());
        Collection<HsmLabelConfiguration> labels = hsmConfiguration.getLabels();
        for (HsmLabelConfiguration label : labels) {
            System.out.println("Configured label:" + label.toString());
        }
        ServerStatus serverStatus = JSSRuntimeControl.getServerStatus();
        System.out.println("HSM server status = " + serverStatus);
    }

    public void hsmPrepareServiceKey() {
        System.out.println("usage               :    hsmPrepareServiceKey <service_key> <service_key_label> <key_value>");
        System.out.println("service_key         :    Service key value (hex)");
        System.out.println("service_key_label   :    Service key label");
        System.out.println("key_value           :    Key value (hex)");
    }

    public void hsmPrepareServiceKey(String hexServiceKey, String kek, String hexKeyValue) throws HsmBaseException {
        logger.debug("PrepareServiceKey");
        System.out.println("HSM service key (hex) = " + hexServiceKey);
        System.out.println("HSM KEK (hex) = " + (new KeyLabel(kek)).getValue());
        System.out.println("HSM key value (hex) = " + hexKeyValue);

        Message message = hsmEnergyService.prepareServiceKey(hexServiceKey, kek, hexKeyValue);
        System.out.println("HSM prepared service key (hex) = " + message.toHex());
    }

    public void hsmServiceKeyInjection() {
        System.out.println("usage                    :    hsmServiceKeyInjection <preapared_service_key> <signature> <verify_key>");
        System.out.println("preapared_service_key    :    HSM wrapped service key value (hex)");
        System.out.println("signature                :    HSM signature for service key (hex)");
        System.out.println("verify_key               :    Verify key label");
    }

    public void hsmServiceKeyInjection(String hexPreaparedServiceKey, String hexSignature, String hexVerifyKey) throws HsmBaseException {
        logger.debug("ServiceKeyInjection");

        ServiceKeyInjectionResponse skiResponse = hsmEnergyService.serviceKeyInjection(hexPreaparedServiceKey,
                hexSignature, hexVerifyKey);

        System.out.println("HSM prepare service key (hex) = " + hexPreaparedServiceKey);
        System.out.println("HSM signature (hex) = " + hexSignature);
        System.out.println("HSM service key injection service key (hex) = " + ((Message)skiResponse).toHex());
        Optional.ofNullable(skiResponse.getWarning())
                .ifPresent(warning -> System.out.println("HSM service key injection warning = " + warning));
    }
}