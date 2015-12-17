package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.datavault.DataVaultService;

import java.util.Map;
import java.util.Optional;

public interface WhiteBoard {
    String COMPONENTNAME = "HTW";
    DataVaultService getDataVaultService();
    KeyStore createKeystore();
    Map<String, String> getKeyPairDecrypted();
    Optional<KeyStore> getKeyPair();
}
