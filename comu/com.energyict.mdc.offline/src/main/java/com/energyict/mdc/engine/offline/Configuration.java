package com.energyict.mdc.engine.offline;

import com.energyict.mdc.common.ApplicationException;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

class Configuration {

    private static final byte[] DES_KEY_CONTENT = {50, 51, 49, 55, 69, 73, 67, 84};
    private static final byte[] AES_KEY_CONTENT = {112, 31, 54, 24, 7, -28, 125, 121, 28, 127, -25, 115, -83, 114, -54, -49};


    private String urlSpec;
    private File file;

    Configuration(String urlSpec) {
        this.urlSpec = urlSpec.trim();
    }

    Configuration(File file) {
        this.file = file;
    }

    Properties getProperties() {
        try {
            InputStream baseStream;
            if (urlSpec == null) {
                baseStream = new FileInputStream(file);
            } else {
                baseStream = new URL(urlSpec).openStream();
            }
            try {
                ObjectInputStream stream = new ObjectInputStream(baseStream);
                SealedObject seal = (SealedObject) stream.readObject();
                return getProperties(seal);
            } finally {
                baseStream.close();
            }
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        } catch (ClassNotFoundException ex) {
            throw new ApplicationException(ex);
        }
    }

    private static SecretKey getKey(String algorithm) {
        if ("AES".equals(algorithm)) {
            SecretKeySpec skeySpec = new SecretKeySpec(AES_KEY_CONTENT, "AES");
            return skeySpec;
        } else {
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
                return factory.generateSecret(new DESKeySpec(DES_KEY_CONTENT));
            } catch (NoSuchAlgorithmException ex) {
                throw new ApplicationException(ex);
            } catch (InvalidKeyException ex) {
                throw new ApplicationException(ex);
            } catch (InvalidKeySpecException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    private static SealedObject getSeal(Properties properties) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey("AES"));
            return new SealedObject(properties, cipher);
        } catch (NoSuchAlgorithmException ex) {
            throw new ApplicationException(ex);
        } catch (InvalidKeyException ex) {
            throw new ApplicationException(ex);
        } catch (NoSuchPaddingException ex) {
            throw new ApplicationException(ex);
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        } catch (IllegalBlockSizeException ex) {
            throw new ApplicationException(ex);
        }
    }

    static void writeSeal(Properties properties, File file) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
        try {
            stream.writeObject(getSeal(properties));
        } finally {
            stream.close();
        }
    }

    private static Properties getProperties(SealedObject seal) {
        try {
            return (Properties) seal.getObject(getKey(seal.getAlgorithm()));
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        } catch (ClassNotFoundException ex) {
            throw new ApplicationException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new ApplicationException(ex);
        } catch (InvalidKeyException ex) {
            throw new ApplicationException(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put("dbUser", "eitest");
        props.put("dbPassword", "eitest");
        writeSeal(props, new File("eiserver.enc"));
    }

}
