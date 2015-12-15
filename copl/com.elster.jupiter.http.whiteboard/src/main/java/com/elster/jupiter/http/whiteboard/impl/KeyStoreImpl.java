package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.KeyStore;
import com.elster.jupiter.http.whiteboard.WhiteBoard;
import com.elster.jupiter.orm.DataModel;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public final class KeyStoreImpl implements KeyStore {

    private BigDecimal id;
    private Instant createTime;
    private Instant modTime;
    private String privateKey;
    private String publicKey;
    private final DataModel dataModel;
    private final WhiteBoard whiteBoard;
    private List<KeyStore> keys;

    @Inject
    private KeyStoreImpl(DataModel dataModel, WhiteBoard whiteBoard) {
        this.dataModel = dataModel;
        this.whiteBoard = whiteBoard;
    }

    static KeyStoreImpl from(DataModel dataModel, WhiteBoard whiteBoard) {
        return new KeyStoreImpl(dataModel, whiteBoard).init();
    }

    KeyStoreImpl init() {
        Map<String, String> keyMap = generateKeys();
        if (keyMap != null) {
            this.id = new BigDecimal(1);
            this.privateKey = keyMap.get("PRV");
            this.publicKey = keyMap.get("PUB");
            manageKeys();
        }
        return this;
    }

    private void manageKeys() {
        List<KeyStore> keyList = getKeys();
        if (keyList.isEmpty()) {
            persist();
        } else if (!keyList.isEmpty() && keyList.size() == 1) {
            update();
        } else {
            deleteAll();
            persist();
        }
    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void delete() {
        dataModel.mapper(KeyStore.class).remove(this);
    }

    public void deleteAll(){
        dataModel.mapper(KeyStore.class).remove(keys);
    }

    @Override
    public void persist() {
        dataModel.mapper(KeyStore.class).persist(this);
    }

    @Override
    public void update() {dataModel.mapper(KeyStore.class).update(this);}

    @Override
    public void createKeys() {
        if (getKeys().isEmpty()) {
            KeyStoreImpl result = KeyStoreImpl.from(dataModel, whiteBoard);
            doGetKeys().add(result);
        }
    }

    @Override
    public void updateKeys() {
        if (getKeys().size() ==1) {
            doGetKeys().removeAll(getKeys());
            KeyStoreImpl result = KeyStoreImpl.from(dataModel, whiteBoard);
            doGetKeys().add(result);
        }
    }

    @Override
    public List<KeyStore> getKeys() {
        return Collections.unmodifiableList(doGetKeys());

    }

    private List<KeyStore> doGetKeys() {
        keys = new CopyOnWriteArrayList<>(dataModel.mapper(KeyStore.class).find());
        return keys;
    }

    private Map<String, String> generateKeys(){
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            keyGenerator.initialize(1024,random);
            KeyPair keyPair = keyGenerator.genKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            String prvKey = DatatypeConverter.printBase64Binary(privateKey.getEncoded());
            String pubKey = DatatypeConverter.printBase64Binary(publicKey.getEncoded());
            String encryptedPubKey = whiteBoard.getDataVaultService().encrypt(pubKey.getBytes());
            String encryptedPrvKey = whiteBoard.getDataVaultService().encrypt(prvKey.getBytes());
            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("PRV",encryptedPrvKey);
            keyMap.put("PUB", encryptedPubKey);
            return keyMap;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Map<String, String> getKeyPairDecrypted(WhiteBoard whiteBoard){
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("PRV",new String(whiteBoard.getDataVaultService().decrypt(this.getPrivateKey())));
        keyMap.put("PUB", new String(whiteBoard.getDataVaultService().decrypt(this.getPublicKey())));
        return keyMap;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyStore)) {
            return false;
        }
        KeyStore resource = (KeyStore) o;
        return publicKey.equals(resource.getPublicKey());

    }

    @Override
    public int hashCode() {
        return publicKey.hashCode();
    }

    @Override
    public String toString() {
        return "KeyStoreImpl{" +
                "privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }


}

