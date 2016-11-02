package com.energyict.encryption.asymetric.signature;

import com.energyict.mdc.protocol.api.security.ECCCurve;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Digital signature using ECDSA.
 *
 * @author alex
 */
@SuppressWarnings("restriction")
public final class ECDSASignatureImpl implements DigitalSignature {

    /**
     * Logger instance.
     */
    private static final Logger logger = Logger.getLogger(ECDSASignatureImpl.class.getName());

    /**
     * The signature.
     */
    private final Signature signature;

    /**
     * The algorithm.
     */
    private final ECCCurve algorithm;

    /**
     * Create a new instance.
     *
     * @param algo The algorithm.
     */
    public ECDSASignatureImpl(final ECCCurve algo) {
        try {
            this.algorithm = algo;
            this.signature = Signature.getInstance(algo.getSignatureAlgoName());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error instantiating signature : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized final byte[] sign(final byte[] data, final PrivateKey key) {
        try {
            this.signature.initSign(Objects.requireNonNull(key));
            this.signature.update(Objects.requireNonNull(data));

            return toOctetString(this.signature.sign());
        } catch (GeneralSecurityException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Error generating signature : [" + e.getMessage() + "]", e);
            }

            throw new IllegalStateException("Error generating signature : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Converts the DER encoded signature to an octet string (R || S).
     *
     * @param derEncodedSignature The DER encoded signature.
     * @return The octet string format.
     */
    private final byte[] toOctetString(final byte[] derEncodedSignature) {
        try {
            final DerInputStream stream = new DerInputStream(derEncodedSignature);
            final DerValue[] values = stream.getSequence(2);

            if (values.length == 2) {
                final byte[] r = values[0].getPositiveBigInteger().toByteArray();
                final byte[] s = values[1].getPositiveBigInteger().toByteArray();

                final byte[] concatenatedSignature = new byte[this.algorithm.getSignatureComponentSize() * 2];

                int srcIndex = r.length - this.algorithm.getSignatureComponentSize();

                if (srcIndex < 0) {
                    srcIndex = 0;
                }

                int targetIndex = this.algorithm.getSignatureComponentSize() - r.length;

                if (targetIndex < 0) {
                    targetIndex = 0;
                }

                System.arraycopy(r, srcIndex, concatenatedSignature, targetIndex, this.algorithm.getSignatureComponentSize());

                srcIndex = s.length - this.algorithm.getSignatureComponentSize();

                if (srcIndex < 0) {
                    srcIndex = 0;
                }

                targetIndex = this.algorithm.getSignatureComponentSize() * 2 - s.length;

                if (targetIndex < this.algorithm.getSignatureComponentSize()) {
                    targetIndex = this.algorithm.getSignatureComponentSize();
                }

                System.arraycopy(s, s.length - this.algorithm.getSignatureComponentSize(), concatenatedSignature, this.algorithm.getSignatureComponentSize(), this.algorithm.getSignatureComponentSize());

                return concatenatedSignature;
            } else {
                throw new IllegalStateException("Was expecting sequence of (r, s) when decoding ECDSA signature, instead got [" + values.length + "] values.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("IO error caught when converting DER encoded signature to an octetstring : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Converts the given signature (octet-string, R || S) to a DER encoded signature.
     *
     * @param octetString The concatenated signature (R || S).
     * @return The signature encoded as DER.
     */
    private final byte[] toDEREncodedSignature(final byte[] octetString) {
        try {
            if (octetString.length == 2 * this.algorithm.getSignatureComponentSize()) {
                final byte[] r = Arrays.copyOfRange(octetString, 0, this.algorithm.getSignatureComponentSize());
                final byte[] s = Arrays.copyOfRange(octetString, this.algorithm.getSignatureComponentSize(), this.algorithm.getSignatureComponentSize() * 2);

                try (final DerOutputStream stream = new DerOutputStream()) {
                    final DerValue rDerValue = new DerValue(DerValue.tag_Integer, r);
                    final DerValue sDerValue = new DerValue(DerValue.tag_Integer, s);

                    stream.putSequence(new DerValue[]{rDerValue, sDerValue});

                    return stream.toByteArray();
                }
            } else {
                throw new IllegalStateException("Expected a signature of size [" + (this.algorithm.getSignatureComponentSize() * 2) + "], instead got one of size [" + octetString.length + "]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("IO error caught when converting DER encoded signature to an octetstring : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized final boolean verify(final byte[] data, final byte[] signature, final PublicKey key) {
        try {
            this.signature.initVerify(key);
            this.signature.update(data);

            return this.signature.verify(this.toDEREncodedSignature(signature));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error verifying signature : [" + e.getMessage() + "]", e);
        }
    }
}
