/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import java.security.KeyStore;
import java.util.Optional;

public interface UserDirectorySecurityProvider {
    Optional<KeyStore> getKeyStore(LdapUserDirectory ldapUserDirectory);
}