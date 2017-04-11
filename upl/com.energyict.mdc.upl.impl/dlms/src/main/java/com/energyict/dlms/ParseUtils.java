package com.energyict.dlms;

import com.energyict.obis.ObisCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * DLMS ParseUtils
 */
public final class ParseUtils {
	
	/** The X.509 certificate factory type. */
	private static final String CERTIFICATE_FACTORY_X509 = "X.509";

    /**
     * Hide this static util class
     */
    private ParseUtils() {
        // Hide this static util class
    }

    /**
     * @param obisCode
     * @return
     */
    public static boolean isObisCodeAbstract(ObisCode obisCode) {
        return (obisCode.getA() == 0) && (obisCode.getB() == 0);
    }

    /**
     * @param obisCode
     * @return
     */
    public static boolean isObisCodeCumulative(ObisCode obisCode) {
        // no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin)
        return (obisCode.getA() != 0) && (obisCode.getC() != 0) && ((obisCode.getD() == 8) || (obisCode.getD() == 17) || (obisCode.getD() == 18));
    }

    /**
     * @param obisCode
     * @return
     */
    public static boolean isObisCodeChannelIntervalStatus(ObisCode obisCode) {
        // no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin)
        return (obisCode.getA() == 0) && (obisCode.getB() == 0) && (obisCode.getC() == 96) && (obisCode.getD() == 60) && (obisCode.getE() > 0) && (obisCode.getF() == 0);
    }

    /**
     * @param obisCode
     * @return
     */
    public static boolean isElectricityObisCode(ObisCode obisCode) {
        return ((obisCode.getA() == 1) && (obisCode.getB() >= 0) && (obisCode.getB() <= 64));
    }

    /**
     * @param array1
     * @param array2
     * @return
     */
    public static byte[] concatArray(byte[] array1, byte[] array2) {
        if ((array1 == null) && (array2 == null)) {
            return null;
        } else if (array1 == null) {
            return array2;
        } else if (array2 == null) {
            return array1;
        }
        byte[] newArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);
        return newArray;
    }

    /**
     * @param code
     * @return
     */
    public static boolean isObisCode(String code) {
        try {
            ObisCode.fromString(code).toString();
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Build up a stringbuffer containing the hex values from the byteArray.
     * Adds zero to the left if necessary.
     * ex:
     * b = {7, 1, 67, 7};
     * strByff.toString() = "07014307";
     *
     * @param b - the byteArray containing the ascii chars
     * @return
     */
    public static String decimalByteToString(byte[] b) {
        StringBuffer strBuff = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String str = Integer.toHexString(b[i] & 0xFF);
            if (str.length() == 1) {
                strBuff.append("0");
            }
            strBuff.append(str);
        }
        return strBuff.toString();
    }

    public static byte[] hexStringToByteArray(String str) {
        byte[] data = new byte[str.length() / 2];
        int offset = 0;
        int endOffset = 2;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) Integer.parseInt(str.substring(offset, endOffset), 16);
            offset = endOffset;
            endOffset += 2;
        }
        return data;
    }
    
	/**
	 * Parse a certificate chain from PEM or DER, provided using the passed stream. The caller is responsible for closing the stream.
	 * 
	 * @param 	certificateStream	Stream pointing to the certificate chain.
	 * 
	 * @return	The parsed certificates.
	 * 
	 * @throws 	IOException						If an IO error occurs.
	 * @throws	GeneralSecurityException		If a security error occurs.
	 */
    public static final X509Certificate[] parseCertificates(final byte[] derEncodedCertificates) throws GeneralSecurityException, IOException {
		final CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_FACTORY_X509);
		final List<X509Certificate> certificates = new ArrayList<>();
		
		try (final InputStream certificateStream = new ByteArrayInputStream(derEncodedCertificates)) {
			while (certificateStream.available() > 0) {
				certificates.add((X509Certificate)certificateFactory.generateCertificate(certificateStream));
			}
		}
		
		return certificates.toArray(new X509Certificate[certificates.size()]);
    }
    
	/**
	 * Converts the given chain to DER format.
	 * 
	 * @param 	chain		The chain to convert.
	 * 
	 * @return	The PEM formatted chain.
	 * 
	 * @throws	IOException						If an IO error occurs.
	 * @throws	GeneralSecurityException		If a security error occurs.
	 */
	public static final byte[] toDER(final X509Certificate... chain) throws IOException, GeneralSecurityException {
		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			for (final X509Certificate cert : chain) {
				stream.write(cert.getEncoded());
			}
			
			return stream.toByteArray();
		}
	}
}
