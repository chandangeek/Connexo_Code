/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import java.security.KeyStore;

public interface UserDirectorySecurityProvider {
    KeyStore getKeyStore(LdapUserDirectory ldapUserDirectory);
}