/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm;


import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import com.atos.worldline.jss.configuration.RawConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;

@Path("/hsm")
public class HsmTestController {

    @POST
    @Path("/hsm/init")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OperationStatus init(RawConfiguration cfg) {
        HsmModule hsm = new HsmModule();
        hsm.init(cfg);
        return new OperationStatus("JSM initialized", 200, null);
    }

    @GET
    @Path("/hsm/encrypt")
    @Produces(MediaType.APPLICATION_JSON)
    public OperationStatus encrypt(@QueryParam("keylabel") String keylabel, @QueryParam("text") String text) {
        try {
            HsmSecurityImpl hsms = new HsmSecurityImpl();
            EncryptionResponse encrypted = hsms.encrypt(new KeyLabel(keylabel), KeyDerivation.FIXED_KEY_ARRAY, text.getBytes(), null, PaddingAlgorithm.LEFT_NULL, ChainingMode.CBC);
            return new OperationStatus(new String(encrypted.getData(), StandardCharsets.UTF_8), 200, null);
        } catch (FunctionFailedException e) {
            return new OperationStatus("Error", 501, e);
        }
    }
}
