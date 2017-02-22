/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

/**
 * Interface to all cryptographic elements that allow renewal. This would be private keys, symmetric keys and certificates.
 * It's up to the implementing class to make sure all renewal information is available.
 * Note that not all key encryption methods will permit automatic renewal.
 */
public interface Renewable {
    public void renewValue(); // TODO add KeyAccessorType as parameter?
}
