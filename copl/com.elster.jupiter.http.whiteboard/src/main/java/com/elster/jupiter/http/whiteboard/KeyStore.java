package com.elster.jupiter.http.whiteboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface KeyStore {
    BigDecimal getId();
    Instant getCreateTime();
    Instant getModTime();
    String getPrivateKey();
    String getPublicKey();
    void persist();
    void update() ;
    void delete();
    void createKeys();
    void updateKeys();
    List<KeyStore> getKeys();
    public Map<String, String> getKeyPairDecrypted(WhiteBoard whiteBoard);
}
