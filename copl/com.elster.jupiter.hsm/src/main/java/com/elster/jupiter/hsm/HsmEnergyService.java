/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.keys.HsmKey;
import com.elster.jupiter.hsm.model.keys.HsmRenewKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.hsm.model.response.ServiceKeyInjectionResponse;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEnergyService {

    HsmKey importKey(ImportKeyRequest importKeyRequest) throws HsmBaseException;

    HsmRenewKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException;

    Message prepareServiceKey(String hexServiceKey, String kek, String hexKeyValue) throws HsmBaseException;

    ServiceKeyInjectionResponse serviceKeyInjection(String hexData, String hexSignature, String verifyKey) throws HsmBaseException;
}
