/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.datasource;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.InvalidPasswordException;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import com.elster.jupiter.bootstrap.oracle.impl.ConnectionProperties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.bootstrap.BootstrapService.KEY_FILE;

public interface DataSourceProvider {
    DataSource createDataSource(ConnectionProperties properties) throws SQLException;
}
