/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.utils.krypto;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.key.PrivateKey;
import com.atos.worldline.jss.commondev.lang.UnexpectedException;
import com.atos.worldline.jss.commondev.lang.XAssert;
import org.bouncycastle.operator.ContentSigner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by H216758 on 10/10/2018.
 */
public abstract class AbstractContentSigner implements ContentSigner {

    private final PrivateKey privateKey;
    private final ByteArrayOutputStream outputStream;
    private final DigestAlgorithmMapping digestAlgorithmMapping;


    public AbstractContentSigner(PrivateKey privateKey, String digestAlgorithm) {
        this.privateKey = XAssert.notNull(privateKey, "privateKey");
        this.outputStream = new ByteArrayOutputStream();
        this.digestAlgorithmMapping = DigestAlgorithmMapping.byName(digestAlgorithm);
    }

    public AbstractContentSigner(PrivateKey privateKey) {
        this(privateKey, "SHA256");
    }

    @Override
    public byte[] getSignature() {
        try {
            getOutputStream().flush();
            return doGetSignature(getOutputStream().toByteArray());
        } catch (final FunctionFailedException | IOException e) {
            throw UnexpectedException.wrapIfNeeded(e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public DigestAlgorithmMapping getDigestAlgorithmMapping() {
        return digestAlgorithmMapping;
    }

    protected abstract byte[] doGetSignature(byte[] csrInfo) throws FunctionFailedException;

}
