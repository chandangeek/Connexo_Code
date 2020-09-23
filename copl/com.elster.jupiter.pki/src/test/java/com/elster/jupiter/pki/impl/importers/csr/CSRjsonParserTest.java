package com.elster.jupiter.pki.impl.importers.csr;

import org.junit.Test;

import com.elster.jupiter.pki.CertificateRequestData;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CSRjsonParserTest {
    @Test
    public void testJsonParsing() {
        String jsonParam = "{\"dlms-signature\":{\"ca\":\"SM-GW-BEACON-SUBCA\",\"cp\":\"SM-GW-BEACON-DLMSSERVER-DIGSIGN\",\"ee\":\"EE-SM-GW-BEACON\"},\"dlms-agreement\":{\"ca\":\"SM-GW-BEACON-SUBCA\",\"cp\":\"SM-GW-BEACON-DLMSSERVER-KA\",\"ee\":\"EE-SM-GW-BEACON\"},\"dlms-tls-\":{\"ca\":\"SM-GW-BEACON-SUBCA\",\"cp\":\"SM-GW-BEACON-DLMSSERVER-TLS\",\"ee\":\"EE-SM-GW-BEACON\"},\"tls\":{\"ca\":\"SM-GW-BEACON-SUBCA\",\"cp\":\"SM-GW-BEACON-WEBSERVER-TLS\",\"ee\":\"EE-SM-GW-BEACON\"}}";
        CertificateRequestData requestData = new CertificateRequestData(jsonParam, "defaultCA", "defaultEE", "defaultCP","");

        assertThat(requestData.getCaName()).isEqualTo("defaultCA");

        requestData.setPrefix("dlms-signature");
        assertThat(requestData.getCaName()).isEqualTo("SM-GW-BEACON-SUBCA");
    }

    @Test
    public void testInvalidJson() {
        String jsonParam = "xxxinvalid";

        try{
            new CertificateRequestData(jsonParam, "defaultCA", "defaultEE", "defaultCP","");
            fail("JSON is ivalid, the parser should throw an exception");
        } catch (Exception ex){
            assertThat(ex.getClass()).isEqualTo(RuntimeException.class);
        }
    }


}