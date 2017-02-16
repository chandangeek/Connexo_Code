/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.Encrypter;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 *
 * @since 9/6/12 3:39 PM
 */
@ProviderType
public interface DataVault extends Serializable, Encrypter {
    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);
}