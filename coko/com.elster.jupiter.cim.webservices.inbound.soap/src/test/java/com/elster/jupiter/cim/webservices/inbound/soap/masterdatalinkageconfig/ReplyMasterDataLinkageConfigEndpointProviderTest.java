/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import ch.iec.tc57._2011.replymasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.ReplyMasterDataLinkageConfig;

import javax.xml.ws.Service;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ReplyMasterDataLinkageConfigEndpointProviderTest extends AbstractMasterDataLinkageTest {

    @Test
    public void testGet() throws Exception {
        //Act and verify
        Assertions.assertThat(getInstance(ReplyMasterDataLinkageConfigEndpointProvider.class).get())
                .isNotNull()
                .isInstanceOf(Service.class)
                .isInstanceOf(ReplyMasterDataLinkageConfig.class);
    }

    @Test
    public void testGetService() throws Exception {
        //Act and verify
        Assertions.assertThat(getInstance(ReplyMasterDataLinkageConfigEndpointProvider.class).getService())
                .isNotNull()
                .isEqualTo(MasterDataLinkageConfigPort.class);
    }

}