package com.energyict.mdc.engine.offline.core;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.common.ApplicationException;

import com.energyict.mdc.engine.impl.tools.TimeZoneManager;
import com.energyict.mdc.engine.offline.UserEnvironment;
import org.apache.commons.codec.binary.Base64;
import sun.security.x509.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * a collection of utility methods
 */
public class Utils {

    static DateFormat formatter =
            DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT);


    /**
     * Creates a new instance of Utils
     */
    private Utils() {
    }

    public static Date toDate(Instant instant) {
        BigInteger milis = BigInteger.valueOf(instant.getEpochSecond()).multiply(
                BigInteger.valueOf(1000));
        milis = milis.add(BigInteger.valueOf(instant.getNano()).divide(
                BigInteger.valueOf(1_000_000)));
        return new Date(milis.longValue());
    }

    /**
     * return the argument between singe quotes,
     * escaping quotes in the argument with a quote character
     *
     * @param in string to quote
     * @return the quoted string
     */
    static public String quoted(String in) {
        if (in == null) {
            return "''";
        }
        StringBuffer buffer = new StringBuffer(in.length() + 2);
        Utils.appendQuoted(buffer, in);
        return buffer.toString();
    }

    /**
     * return the first argument withuot quotes
     *
     * @param in    the string to unquote
     * @param quote the quote character
     * @return the unquoted String
     */
    static public String unquoted(String in, char quote) {
        if (in == null) {
            return "";
        }
        in = in.trim();
        if (in.length() == 0) {
            return in;
        }
        if (in.charAt(0) != quote) {
            return in;
        }
        if (in.charAt(in.length() - 1) == quote) {
            return in.substring(1, in.length() - 1);
        } else {
            return in;
        }
    }

    public static String getHexStringFromBytes(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(String.format("%02X", new Object[]{aByte & 255}));
        }
        return builder.toString();
    }

    public static byte[] getBytesFromHexString(String hexString) throws IllegalArgumentException {
        if (hexString == null || hexString.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert hex empty string to byte array!");
        } else {
            String cleanHexString = hexString.toUpperCase().replaceAll("[^0-9A-F]", "");
            if (cleanHexString.length() % 2 != 0) {
                throw new IllegalArgumentException("Hex string [" + cleanHexString + "] should have an even number of nibbles, but length was [" + cleanHexString.length() + "]");
            } else {
                ByteArrayOutputStream bb = new ByteArrayOutputStream();

                for (int i = 0; i < cleanHexString.length(); i += 2) {
                    bb.write(Integer.parseInt(cleanHexString.substring(i, i + 2), 16));
                }

                return bb.toByteArray();
            }
        }
    }

    /**
     * append the quoted version of the second argument to the first
     *
     * @param buffer StringBuffer to append to
     * @param in     string to quote and append to the first argument
     */
    static public void appendQuoted(StringBuffer buffer, String in) {
        buffer.append('\'');
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'') {
                buffer.append("''");
            } else {
                buffer.append(c);
            }
        }
        buffer.append('\'');
    }

    /**
     * Create an X.509 Certificate signed with the given private key
     *
     * @param subject   the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param issuer    the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param publicKey the public key for this certificate
     * @param days      how many days from now the Certificate is valid for
     * @param algorithm the signing algorithm, eg "SHA256withECDSA"
     */
    public static X509Certificate generateSignedCertificate(String subject, String issuer, PublicKey publicKey, int days, String algorithm, PrivateKey signingPrivateKey) throws GeneralSecurityException, IOException {
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000L);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name subjectName = new X500Name(subject);
        X500Name issuerName = new X500Name(issuer);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));

        if (getJavaVersion() >= 1.8) {
            info.set(X509CertInfo.SUBJECT, subjectName);
            info.set(X509CertInfo.ISSUER, issuerName);
        } else {
            info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(subjectName));
            info.set(X509CertInfo.ISSUER, new CertificateIssuerName(issuerName));
        }

        info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.EC_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        //Set the proper extensions on the CA certificate
        if (subject.contains("CA,")) {
            CertificateExtensions certificateExtensions = new CertificateExtensions();
            certificateExtensions.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(true, 5));
            KeyUsageExtension keyUsageExtension = new KeyUsageExtension();
            keyUsageExtension.set(KeyUsageExtension.KEY_CERTSIGN, true);
            keyUsageExtension.set(KeyUsageExtension.CRL_SIGN, true);
            certificateExtensions.set(KeyUsageExtension.NAME, keyUsageExtension);

            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(signingPrivateKey, algorithm);

        // Update the algorithm, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(signingPrivateKey, algorithm);
        return cert;
    }

    /**
     *  Returns the runtime Java version.
     *
     * @return - Double representing the Java version (1.7, 1.8, etc)
     */
    public static double getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version == null) {
            return 0;
        }
        try {
            int pos = version.indexOf('.');
            pos = version.indexOf('.', pos + 1);
            return Double.parseDouble(version.substring(0, pos));
        } catch (Exception ex){
            return 0;
        }
    }


    /**
     * Decodes the provided base64 string into the ASN.1 DER encoded certificate, which will then be converted to an actual X.509 v3 certificate instance
     */
    public static java.security.cert.Certificate decodeBase64Certificate(String base64Certificate) throws CertificateException {
        byte[] derEncodedCertificate = Base64.decodeBase64(base64Certificate);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(derEncodedCertificate);
        return certFactory.generateCertificate(in);
    }

    /**
     * return a TimeZone with the same offset as the argument,
     * but without DST corrections
     *
     * @param timeZone the reference TimeZone
     * @return the calculated TimeZone
     */
    static public TimeZone getStandardTimeZone(TimeZone timeZone) {
        int offset = timeZone.getRawOffset() / 3600000;
        return TimeZone.getTimeZone("GMT" + (offset < 0 ? "" : "+") + offset);
    }

    /**
     * return the GMT TimeZone corresponding to the argument timeZone
     * when DST is active
     *
     * @param timeZone the reference TimeZone
     * @return calculated TimeZone
     */
    static public TimeZone getStandardTimeZoneDST(TimeZone timeZone) {
        int offset = (timeZone.getRawOffset() / 3600000) + (timeZone.getDSTSavings() / 3600000);
        return TimeZone.getTimeZone("GMT" + (offset < 0 ? "" : "+") + offset);
    }

    /**
     * test if the argument is an Oracle SQLException caused by a duplicate key.
     * The current test compares the argument's SQLState to 23000.
     *
     * @param ex the oracle SQLException to test
     * @return true if the exception was caused by a duplicate key
     */
    static public boolean isDuplicateKey(SQLException ex) {
        return "23000".equals(ex.getSQLState());
    }

    /**
     * test if the argument is an Oracle SQLException caused by a resource busy condition on a lock no wait
     *
     * @param ex the oracle SQLException to test
     * @return true if the exception was caused by a resource busy
     */
    static public boolean isResourceBusy(SQLException ex) {
        return ex != null && ex.getErrorCode() == 54;
    }

    /**
     * test if the argument is an Oracle SQLException caused by trying to
     * create a table that already exists.
     * The current test compares the argument's SQLState to 23000.
     *
     * @param ex the oracle SQLException to test
     * @return true if the exception was caused by trying to create an existing table
     * <p/>
     * ORA-00955: name is already used by an existing object
     */
    static public boolean tableAlreadyExists(SQLException ex) {
        return "00955".equals(ex.getSQLState());
    }

    /**
     * return a string representation of the argument's stack trace
     *
     * @param e Throwable to examine
     * @return the argument's stack trace
     */
    static public String stack2string(Throwable e) {
        OutputStream out = new ByteArrayOutputStream();
        PrintStream prnout = new PrintStream(out);
        e.printStackTrace(prnout);
        return out.toString();
    }


    /**
     * returns a BigDecimal equal to the argument.
     *
     * @param in the number to convert to BigDecimal.
     * @return the BigDecimal conversion result.
     */
    static public BigDecimal toBigDecimal(Number in) {
        BigDecimal result;
        if (in instanceof BigDecimal) {
            result = (BigDecimal) in;
        } else {
            result = new BigDecimal(in.toString());
        }
        if (result.scale() < 0) {
            result = result.setScale(0);
        }
        return result;
    }

    /**
     * Returns a String representing the argument in the system's default format.
     *
     * @param date the date to format.
     * @return the formatted date String.
     */
    synchronized static public String format(Date date) {
        return formatter.format(date);
    }

    /**
     * Returns a Timestamp representing the argument
     *
     * @param date date to convert
     * @return a timestamp
     */
    public static Timestamp asTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }

    /**
     * Returns a Date representing the argument
     *
     * @param seconds date in seconds since 1/1/1970 to convert
     * @return a Date object
     */
    public static Date asDate(long seconds) {
        return seconds == 0 ? null : new Date(seconds * 1000L);
    }

    public static Long getTimestampFieldValue(Date timestamp) {
        if (timestamp == null)
            return null;
        return Utils.asSeconds(timestamp);
    }

    /**
     * Returns the number of seconds since 1/1/19O70
     * represented by the argument
     *
     * @param date date to convert
     * @return number of seconds since 1/1/1970
     */
    public static long asSeconds(Date date) {
        return date == null ? 0 : date.getTime() / 1000L;
    }

    /**
     * returns a Level representing the argument
     *
     * @param value the level id
     * @return the Level
     */
    public static Level asLevel(int value) {
        if (value == Level.OFF.intValue()) {
            return Level.OFF;
        }
        if (value == Level.SEVERE.intValue()) {
            return Level.SEVERE;
        }
        if (value == Level.WARNING.intValue()) {
            return Level.WARNING;
        }
        if (value == Level.INFO.intValue()) {
            return Level.INFO;
        }
        if (value == Level.CONFIG.intValue()) {
            return Level.CONFIG;
        }
        if (value == Level.FINE.intValue()) {
            return Level.FINE;
        }
        if (value == Level.FINER.intValue()) {
            return Level.FINER;
        }
        if (value == Level.FINEST.intValue()) {
            return Level.FINEST;
        }
        if (value == Level.ALL.intValue()) {
            return Level.ALL;
        }
        return Level.INFO;
    }

    /**
     * returns a String representing the argument
     *
     * @param timeZone timeZone to represent
     * @return the String representation
     */
    public static String asString(TimeZone timeZone) {
        if (timeZone == null) {
            return null;
        }
        String result = timeZone.getID();
        if (result.length() > 2 &&
                "GMT".equals(result.substring(0, 3))) {
            long rawOffset = timeZone.getRawOffset() / 3600000;
            if (rawOffset == 0) {
                return "GMT";
            } else {
                return "GMT" + (rawOffset > 0 ? "+" : "") + rawOffset;
            }
        } else {
            return result;
        }
    }

    /**
     * creates a deep copy of the argument.
     * The argument must be serializable
     *
     * @param obj obj to copy
     * @return the deep copy.
     * @throws ApplicationException if an error occurred.
     */
    public static Object deepCopy(Object obj) throws ApplicationException {
        Object result = null;
        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            ObjectOutputStream objOutStream = new ObjectOutputStream(byteOutStream);
            objOutStream.writeObject(obj);
            ByteArrayInputStream byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
            ObjectInputStream objInStream = new ObjectInputStream(byteInStream);
            result = objInStream.readObject();
        } catch (IOException e) {
            throw new ApplicationException(e);
        } catch (ClassNotFoundException e) {
            throw new ApplicationException(e);
        }
        return result;
    }

    /**
     * Tests if we are running on windows
     *
     * @return true if windows is OS , false otherwise
     */
    public static boolean isOSWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return ((os.indexOf("windows xp") != -1) ||
                (os.indexOf("windows 2003") != -1) ||
                (os.indexOf("windows 2000") != -1) ||
                (os.indexOf("windows me") != -1) ||
                (os.indexOf("windows 98") != -1));

    }

    /**
     * appends the argument in sql syntax
     *
     * @param buffer buffer to append on
     * @param date   date to append
     */
    public static void appendSqlTimestamp(StringBuffer buffer, Date date) {
        buffer.append("TIMESTAMP");
        appendQuoted(buffer, new Timestamp(date.getTime()).toString());
    }

    /* tests if two objects are equal in a logical sense
    * enhances Object.equals in two ways:
    *     supports null arguments
    *     works around a misfeauture in BigDecimal that two
    *     BigDecimals with the same value , but different scale are not equal
    *     also tests correctly if a java.sql.Date is equal to a java.util.Date
    */

    /**
     * Makes ther argument a valid html string
     *
     * @param s argument to escape
     * @return the escaped string
     */
    public static String escapeHtml(String s) {
        int n = s.length();
        StringBuffer sb = new StringBuffer(n);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\u003C':
                    sb.append("&lt;");
                    break;
                case '\u003E':
                    sb.append("&gt;");
                    break;
                case '\u0026':
                    sb.append("&amp;");
                    break;
                case '\u0022':
                    sb.append("&quot;");
                    break;
                case '\u00E0':
                    sb.append("&agrave;");
                    break;
                case '\u00C0':
                    sb.append("&Agrave;");
                    break;
                case '\u00E2':
                    sb.append("&acirc;");
                    break;
                case '\u00C2':
                    sb.append("&Acirc;");
                    break;
                case '\u00E4':
                    sb.append("&auml;");
                    break;
                case '\u00C4':
                    sb.append("&Auml;");
                    break;
                case '\u00E5':
                    sb.append("&aring;");
                    break;
                case '\u00C5':
                    sb.append("&Aring;");
                    break;
                case '\u00E6':
                    sb.append("&aelig;");
                    break;
                case '\u00C6':
                    sb.append("&AElig;");
                    break;
                case '\u00E7':
                    sb.append("&ccedil;");
                    break;
                case '\u00C7':
                    sb.append("&Ccedil;");
                    break;
                case '\u00E9':
                    sb.append("&eacute;");
                    break;
                case '\u00C9':
                    sb.append("&Eacute;");
                    break;
                case '\u00E8':
                    sb.append("&egrave;");
                    break;
                case '\u00C8':
                    sb.append("&Egrave;");
                    break;
                case '\u00EA':
                    sb.append("&ecirc;");
                    break;
                case '\u00CA':
                    sb.append("&Ecirc;");
                    break;
                case '\u00EB':
                    sb.append("&euml;");
                    break;
                case '\u00CB':
                    sb.append("&Euml;");
                    break;
                case '\u00EF':
                    sb.append("&iuml;");
                    break;
                case '\u00CF':
                    sb.append("&Iuml;");
                    break;
                case '\u00F4':
                    sb.append("&ocirc;");
                    break;
                case '\u00D4':
                    sb.append("&Ocirc;");
                    break;
                case '\u00F6':
                    sb.append("&ouml;");
                    break;
                case '\u00D6':
                    sb.append("&Ouml;");
                    break;
                case '\u00F8':
                    sb.append("&oslash;");
                    break;
                case '\u00D8':
                    sb.append("&Oslash;");
                    break;
                case '\u00DF':
                    sb.append("&szlig;");
                    break;
                case '\u00F9':
                    sb.append("&ugrave;");
                    break;
                case '\u00D9':
                    sb.append("&Ugrave;");
                    break;
                case '\u00FB':
                    sb.append("&ucirc;");
                    break;
                case '\u00DB':
                    sb.append("&Ucirc;");
                    break;
                case '\u00FC':
                    sb.append("&uuml;");
                    break;
                case '\u00DC':
                    sb.append("&Uuml;");
                    break;
                case '\u00AE':
                    sb.append("&reg;");
                    break;
                case '\u00A9':
                    sb.append("&copy;");
                    break;
                case '\u0080':
                    sb.append("&euro;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Merges two list, optionally removing duplicates
     *
     * @param removeDuplicates true to remove duplicates
     * @param list1            first list
     * @param list2            second list
     * @return the merged list
     */
    public static List mergeLists(boolean removeDuplicates, List list1, List list2) {
        List result = new ArrayList();
        result.addAll(list1);
        Iterator it = list2.iterator();
        Object element;
        while (it.hasNext()) {
            element = it.next();
            if (!result.contains(element)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Creates a "copy of " String
     *
     * @param name       orginal value
     * @param occurrence occurrence count
     * @return the copy of String
     */
    public static String nameForCopy(String name, int occurrence) {
        StringBuffer result = new StringBuffer();
        String copyOf = TranslatorProvider.instance.get().getTranslator().getTranslation("copyOf");
        String copyXOf = TranslatorProvider.instance.get().getTranslator().getTranslation("copyXOf");
        if (occurrence == 0) {
            result.append(copyOf);
        } else {
            Object[] arguments = {new Integer(occurrence)};
            result.append(MessageFormat.format(copyXOf, arguments));
        }
        if (result.length() > 0) {
            result.append(" ");
        }
        result.append(name);
        return result.toString();
    }

    /**
     * Checks if the given string is null or empty (length=0)
     *
     * @param strToTest the string to test
     * @return true if the given string is null or empty (length=0)
     */
    public static boolean isNull(String strToTest) {
        if (strToTest == null) {
            return true;
        }
        for (int index = 0; index < strToTest.length(); index++) {
            if (!Character.isWhitespace(strToTest.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the modulo
     *
     * @param value        the value to calculate the modulo of
     * @param modParameter the modulo parameter
     * @return the 1st parameter modulo the 2nd as a BigDecimal
     */
    public static BigDecimal modulo(BigDecimal value, int modParameter) {
        BigDecimal intValue = new BigDecimal(value.intValue());
        BigDecimal fraction = value.add(intValue.negate());
        BigDecimal intModValue = new BigDecimal(value.intValue() % modParameter);
        return intModValue.add(fraction);
    }

    /**
     * Creates a consumption (Quantity) out of the given parameters
     *
     * @param val               the value
     * @param unit              the unit
     * @param intervalInSeconds the interval time (in seconds)
     * @param minimumScale      the desired minimum scale of the result
     * @return a consumption (Quantity) created out of the given parameters
     */
    public static Quantity asConsumption(BigDecimal val, Unit unit, int intervalInSeconds, int minimumScale) {
        BigDecimal value = val;
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        if (unit.isFlowUnit()) {
            unit = unit.getVolumeUnit();
            BigDecimal denominator = new BigDecimal(3600d / intervalInSeconds);
            value = value.divide(
                    denominator,
                    Math.max(value.scale() + 2, minimumScale),
                    BigDecimal.ROUND_HALF_UP);
        }
        return new Quantity(value, unit);
    }

    public static Calendar getUtcCalendar() {
        return Calendar.getInstance(TimeZoneManager.getTimeZone("UTC"));
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            //silently ignore
        }
    }

    // Since MessageFormat.format() interprets a single quote (')
    // as the start of a quoted string, a pattern string like
    // "It's recommended to change ... within {0,number{ day(s)"
    // isn't processed as expected: the {0, number} part is seen as part
    // of a quoted (no to touch) string and is NOT replaced by the 1st argument
    // eg. The result of
    // 1) MessageFormat.format("It's recommended to change ... within {0,number} day(s)", 10)
    // 2) MessageFormat.format("It's recommended to change '{0} to {0,number}' within {0,number} day(s)", 10)
    // 3) MessageFormat.format("It''s recommended to change '{0} to {0,number}' within {0,number} day(s)", 10)
    // is
    // 1) Its recommended to change ... within {0,number} day(s)         -- No argument replacement done
    // 2) Its recommended to change 10 to 10 within {0,number} day(s)    -- Wrong argument replacement done
    // 3) It's recommended to change {0} to {0,number} within 10 day(s)  -- Correct/As expected
    // Therefor in the pattern, we first replace each single quote
    // by two single quotes (indicating that quote is NOT the start of a quoted string)
    public static String format(String pattern, Object[] arguments) {
        try {
            return MessageFormat.format(pattern.replaceAll("'", "''"), arguments);
        } catch (Exception ex) {
            // this is used also in Exception generation, so instead of throwing an un-wanted format exception, return the
            // pattern with the exception details attached.
            // see https://jira.eict.vpdc/browse/EISERVERSG-4512

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);
            stringWriter.toString();

            return pattern + "\n" + ex.getLocalizedMessage() + "\n" + stringWriter.toString();
        }
    }

    public static String subString(String in, int start) {
        if (in == null) {
            return "";
        }
        if (start > in.length()) {
            return "";
        }
        return in.substring(start);
    }

    public static String subString(String in, int start, int end) {
        if (in == null) {
            return "";
        }
        if (start > in.length()) {
            return "";
        }
        if (end <= start) {
            return "";
        }
        if (end > in.length()) {
            return in.substring(start);
        } else {
            return in.substring(start, end);
        }
    }

    public static Object instantiate(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        } catch (InstantiationException ex) {
            return null;
        }
    }

    public static String capitalize(String in) {
        switch (in.length()) {
            case 0:
                return in;
            case 1:
                return in.toUpperCase();
            default:
                return in.substring(0, 1).toUpperCase() + in.substring(1, in.length());
        }
    }

    public static int getJavaDayOfWeek(int eiserverDayOfWeek) {
        switch (eiserverDayOfWeek) {
            case 1:
                return Calendar.MONDAY;
            case 2:
                return Calendar.TUESDAY;
            case 3:
                return Calendar.WEDNESDAY;
            case 4:
                return Calendar.THURSDAY;
            case 5:
                return Calendar.FRIDAY;
            case 6:
                return Calendar.SATURDAY;
            case 7:
                return Calendar.SUNDAY;
            default:
                throw new ApplicationException(
                        "Invalid eiserverDayOfWeek: " + eiserverDayOfWeek);
        }
    }

    public static int getJavaMonth(int eiserverMonth) {
        switch (eiserverMonth) {
            case 1:
                return Calendar.JANUARY;
            case 2:
                return Calendar.FEBRUARY;
            case 3:
                return Calendar.MARCH;
            case 4:
                return Calendar.APRIL;
            case 5:
                return Calendar.MAY;
            case 6:
                return Calendar.JUNE;
            case 7:
                return Calendar.JULY;
            case 8:
                return Calendar.AUGUST;
            case 9:
                return Calendar.SEPTEMBER;
            case 10:
                return Calendar.OCTOBER;
            case 11:
                return Calendar.NOVEMBER;
            case 12:
                return Calendar.DECEMBER;
            default:
                throw new ApplicationException(
                        "Invalid eiserver month: " + eiserverMonth);
        }
    }

    public static int getEiserverDayOfWeek(int javaDayOfWeek) {
        switch (javaDayOfWeek) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            case Calendar.SUNDAY:
                return 7;
            default:
                throw new ApplicationException(
                        "Invalid javaDayOfWeek: " + javaDayOfWeek);
        }
    }

    public static String getDefaultServerName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    public static String getServerName() throws UnknownHostException {
        String localhost = getDefaultServerName();
        return System.getProperty("servername", localhost);
    }

    // Apparently writing key/value pairs in the registry by using Preferences.put() throws an exception when
    // + the key is longer than Preferences.MAX_KEY_LENGTH (80)
    // [and the value is longer then Preferences.MAX_VALUE_LENGTH (8*1024) but I assume that won't ever cause problems)
    // So in that case we have to trim the key before putting

    public static void putPreference(Preferences prefs, String key, String value) {
        if (key.length() > Preferences.MAX_KEY_LENGTH) {
            key = chopRegistryKey(key);
        }
        try {
            prefs.put(key, value);
        } catch (NullPointerException npEx) {
            throw npEx;
        } catch (IllegalArgumentException iaEx) {
            throw iaEx;
        } catch (IllegalStateException isEx) {
            throw isEx;
        }
    }

    public static String getPreference(Preferences prefs, String key, String defaultValue) {
        if (key.length() > Preferences.MAX_KEY_LENGTH) {
            key = chopRegistryKey(key);
        }
        try {
            return prefs.get(key, defaultValue);
        } catch (NullPointerException npEx) {
            throw npEx;
        } catch (IllegalStateException isEx) {
            throw isEx;
        }
    }

    private static String chopRegistryKey(String key) {
        // We chop the part before the first point (.) in the key
        // If there is no point, we chop the complete key
        int firstPointIndex = key.indexOf('.');
        String firstPart = firstPointIndex > 0 ? key.substring(0, firstPointIndex) : key;
        String lastPart = firstPointIndex > 0 ? key.substring(key.indexOf('.')) : "";

        int maxLengthOfFirstPart = Preferences.MAX_KEY_LENGTH - lastPart.length();
        firstPart = firstPart.substring(0, maxLengthOfFirstPart);

        return firstPart + lastPart;
    }


    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getMd5Hash(byte[] input, int salt) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(toByteArray(salt / 3));
            messageDigest.update(input);
            messageDigest.update(toByteArray(salt));
            byte[] digest = messageDigest.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new ApplicationException(e);

        }
    }

    public static String getMd5Hash(String input, int salt) {
        return getMd5Hash(input.getBytes(), salt);
    }

    public static byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static byte[] toByteArray(double d) {
        long l = Double.doubleToRawLongBits(d);
        return new byte[]{
                (byte) ((l >> 56) & 0xff),
                (byte) ((l >> 48) & 0xff),
                (byte) ((l >> 40) & 0xff),
                (byte) ((l >> 32) & 0xff),
                (byte) ((l >> 24) & 0xff),
                (byte) ((l >> 16) & 0xff),
                (byte) ((l >> 8) & 0xff),
                (byte) ((l >> 0) & 0xff),
        };
    }

    public static File getOsUserHomeDirectory() {
        String userProfile = System.getenv("USERPROFILE");
        File result = checkPath(userProfile);
        if (result == null) {
            result = checkPath(System.getProperty("user.home"));
        }
        return result;
    }

    private static File checkPath(String path) {
        if (path != null && !"".equals(path.trim())) {
            File result = new File(path);
            if (result.exists()) {
                return result;
            }
        }
        return null;
    }

    public static boolean isOracleReservedWord(String name) {
        OracleReservedWords[] oracleReservedWords = OracleReservedWords.values();
        for (int i = 0; i < oracleReservedWords.length; i++) {
            if (oracleReservedWords[i].toString().equals(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public static String translateEnum(Enum value, boolean classNameAsPrefix) {
        if (value != null) {
            return localizedName(value, classNameAsPrefix);
        } else {
            return null;
        }
    }

    /**
     * Tests if two objects are equal.
     * Contains special logic for BigDecimal and Date.
     * Two BigDecimal objects are considered equal
     * if they represent the same value, regardless of scale.
     * Two Date objects are considerd equal if they represent
     * the same point in time , regardless whether they are
     * instances of java.util.Date, java.sql.Date or java.sql.Timestamp
     * Otherwise this is equivalent to first.equals(second)
     *
     * @param first  first object to test for equality
     * @param second second object to test for equality
     * @return true if equal, false otherwise
     */
    public static boolean areEqual(Object first, Object second) {
        if (first == null) {
            return second == null;
        }
        if (second == null) {
            return false;
        }
        if ((first instanceof BigDecimal) && (second instanceof BigDecimal)) {
            return ((BigDecimal) first).compareTo((BigDecimal) second) == 0;
        }
        if ((first instanceof Date) && (second instanceof Date)) {
            return ((Date) first).getTime() == ((Date) second).getTime();
        } else {
            return first.equals(second);
        }
    }

    private static String localizedName(Enum value, boolean classNameAsPrefix) {
        if (value instanceof LocalizableEnum) {
            LocalizableEnum localizableEnum = (LocalizableEnum) value;
            return localizableEnum.getLocalizedName();
        } else {
            return localizedValue(value, classNameAsPrefix);
        }
    }

    public static String localizedValue(Enum value, boolean classNameAsPrefix) {
        String translationKey = getTranslationKey(value, classNameAsPrefix);
        return TranslatorProvider.instance.get().getTranslator().getTranslation(translationKey);
    }

    private static String getTranslationKey(Enum value, boolean classNameAsPrefix) {
        String translationKey = value.toString();
        if (classNameAsPrefix) {
            String prefix = value.getClass().getSimpleName();
            if (isNull(prefix)) { // is the case when the Enum class is an anonymous inner class
                prefix = value.getClass().getName();
                // eg. "com.energyict.mdc.tasks.ConnectionTaskFactoryImpl$ConnectionTaskDiscriminator$1"
                // determine the classname as the (last) part between two $ characters:
                prefix = prefix.substring(0, prefix.lastIndexOf('$'));
                prefix = prefix.substring(prefix.lastIndexOf('$') + 1);
            }
            String original = prefix + "." + translationKey;
            translationKey = original.substring(0, 1).toLowerCase() + original.substring(1);
        }
        return translationKey;
    }

    private enum OracleReservedWords {
        SHARE,
        RAW,
        DROP,
        BETWEEN,
        FROM,
        DESC,
        OPTION,
        PRIOR,
        LONG,
        THEN,
        DEFAULT,
        ALTER,
        IS,
        INTO,
        MINUS,
        INTEGER,
        NUMBER,
        GRANT,
        IDENTIFIED,
        ALL,
        TO,
        ORDER,
        ON,
        FLOAT,
        DATE,
        HAVING,
        CLUSTER,
        NOWAIT,
        RESOURCE,
        ANY,
        TABLE,
        INDEX,
        FOR,
        UPDATE,
        WHERE,
        CHECK,
        SMALLINT,
        WITH,
        DELETE,
        BY,
        ASC,
        REVOKE,
        LIKE,
        SIZE,
        RENAME,
        NOCOMPRESS,
        NULL,
        GROUP,
        VALUES,
        AS,
        IN,
        VIEW,
        EXCLUSIVE,
        COMPRESS,
        SYNONYM,
        SELECT,
        INSERT,
        EXISTS,
        NOT,
        TRIGGER,
        ELSE,
        CREATE,
        INTERSECT,
        PCTFREE,
        DISTINCT,
        CONNECT,
        SET,
        MODE,
        OF,
        UNIQUE,
        VARCHAR2,
        VARCHAR,
        LOCK,
        OR,
        CHAR,
        DECIMAL,
        UNION,
        PUBLIC,
        AND,
        START
    }

}

