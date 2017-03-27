/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import java.time.Instant;

/**
 * Created by bvn on 3/24/17.
 */
public class CsrInfo {
    public String alias;
    public long keyTypeId;
    public String keyEncryptionMethod;
    public Instant notBefore;
    public Instant notAfter;
    public String CN;
    public String OU;
    public String O;
    public String L;
    public String ST;
    public String C;
}
