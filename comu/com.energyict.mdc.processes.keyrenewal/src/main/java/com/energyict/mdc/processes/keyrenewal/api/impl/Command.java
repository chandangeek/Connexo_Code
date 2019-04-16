/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

public enum Command {
    RENEW_KEY,
    GENERATE_KEYPAIR,
    REQUEST_CSR,
    REQUEST_CERTIFICATE,
    UPLOAD_CERTIFICATE,
    TEST_COMMUNICATION;
}
