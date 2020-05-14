package com.elster.jupiter.bootstrap;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PasswordDecryptService {
    String getDecryptPassword(String password, String fileName);
}
