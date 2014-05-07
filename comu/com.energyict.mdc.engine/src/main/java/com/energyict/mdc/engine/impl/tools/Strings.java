package com.energyict.mdc.engine.impl.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides various methods to manipulate/test Strings.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (16:13)
 */
public class Strings {

    /**
     * Returns the length of the specified String in a null-safe way.
     * The length of a <code>null</code> String is zero.
     *
     * @param strings The String
     * @return The length of the String
     */
    public static int length (String... strings) {
        if (strings == null) {
            return 0;
        }
        else {
            int totalLength = 0;
            for (String string : strings) {
                if (string != null) {
                    totalLength = totalLength + string.length();
                }
            }
            return totalLength;
        }
    }

    /**
     * Capitalizes the specified String, assuming it does not contain any spaces.
     * Replaces the fist character with the uppercase alternative.
     *
     * @param notCapitalized The String that will be capitalized
     * @return The capitalized String
     */
    public static String capitalizeWord (String notCapitalized) {
        char chars[] = notCapitalized.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Returns a String that contains the specified number of space characters.<br>
     * Note that this will throw an IllegalArgumentException when
     * the specified number of space characters is negative.
     *
     * @param numberOfSpaces The number of spaces, must be 0 or greater
     * @return A String that contains the specified number of space characters
     */
    public static String spaces (int numberOfSpaces) {
        if (numberOfSpaces < 0) {
            throw new IllegalArgumentException();
        }
        char[] chars = new char[numberOfSpaces];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    /**
     * Reverses the given string.<br>
     * Example StringManipulator.reverse("NotAPalindrome") returns "emordnilaPAtoN".
     * Example StringManipulator.reverse("was it a cat i saw") returns "was it a cat i saw", with a little imagination.
     *
     * @param aString The String that will be reversed
     * @return The reversed string
     */
    public static String reverse (String aString) {
        StringBuilder reverser = new StringBuilder(aString);
        reverser.reverse();
        return reverser.toString();
    }

    /**
     * Trims leading and trailing whitespace from the specified String.
     * If the String is <code>null</code> then null is returned.
     *
     * @param notTrimmedOrNull The String that is presumed to contain spaces or <code>null</code>
     * @return The trimmed String or <code>null</code> if null was specified.
     */
    public static String trim (String notTrimmedOrNull) {
        if (notTrimmedOrNull != null) {
            return notTrimmedOrNull.trim();
        }
        else {
            return null;
        }
    }

    /**
     * Removes any whitespace characters from then specified String.
     * The definition of a whitespace character is taken from the java Character class.
     *
     * @param withWhiteSpace The String that is presumed to contain whitespace characters
     * @return The String with all whitespace characters removed.
     * @see Character#isWhitespace(char)
     */
    public static String removeWhiteSpace (String withWhiteSpace) {
        if (isEmpty(withWhiteSpace)) {
            return withWhiteSpace;
        }
        else {
            int length = withWhiteSpace.length();
            StringBuilder withoutWhiteSpace = new StringBuilder(length);
            removeWhiteSpace(withWhiteSpace, withoutWhiteSpace);
            return withoutWhiteSpace.toString();
        }
    }

    /**
     * Removes any whitespace characters from the specified String,
     * appending non whitespace characters to the {@link StringBuilder StringBuilder}.
     *
     * @param withWhiteSpace The String that is presumed to contain whitespace characters
     * @param sb The StringBuilder
     * @see #removeWhiteSpace(String) For more information on whitespace characters
     */
    public static void removeWhiteSpace (String withWhiteSpace, StringBuilder sb) {
        if (!isEmpty(withWhiteSpace)) {
            int length = withWhiteSpace.length();
            for (int i = 0; i < length; i++) {
                char c = withWhiteSpace.charAt(i);
                if (!Character.isWhitespace(c)) {
                    /* This is a NON whitespace character. */
                    sb.append(c);
                }
            }
        }
    }

    /**
     * Removes all \n characters from the specified String.
     *
     * @param string The String from which \n characters will be removed
     * @return a String that is the given String minus all \n characters
     */
    public static String removeNewLines (String string) {
        Matcher matcher = Pattern.compile("\\n").matcher(string); //$NON-NLS-1$
        if (matcher.find()) {
            return matcher.replaceAll("");
        }
        else {
            return string;
        }
    }
    /**
     * Replaces all tab characters (i.e. \t) by the specified number of space characters.
     *
     * @param string The String in which tab characters will be replaced by space characters
     * @param numberOfSpaces The number of space characters
     * @return A String that is the given String with all tab characters replaced by the given amount of spaces
     */
    public static String replaceTabs (String string, int numberOfSpaces) {
        String replacement = spaces(numberOfSpaces);
        Matcher matcher = Pattern.compile("\\t").matcher(string); //$NON-NLS-1$
        if (matcher.find()) {
            return matcher.replaceAll(replacement);
        }
        else {
            return string;
        }
    }

    /**
     * Chops a string up into smaller chunks.
     * As an example:
     * <code><pre>
     * for (String each : String.chopUp("012345678901234567890123456789", 10)) {
     *     System.out.println(each);
     * }
     * </pre></code>
     * Produces:
     * <code><pre>
     * 0123456789
     * 0123456789
     * 0123456789
     * </pre></code>
     *
     * @param string The String that needs chopping up
     * @param chunkLength The length of each chunk
     * @return The chunks
     */
    public static List<String> chopUp (String string, int chunkLength) {
        if (Strings.isEmpty(string)) {
            return new ArrayList<String>(0);
        }
        else {
            List<String> chunks = new ArrayList<String>(estimateNumberOfChunks(string, chunkLength));
            final int stringLength = string.length();
            for (int i = 0; i < stringLength; i = i + chunkLength) {
                chunks.add(string.substring(i, Math.min(i + chunkLength, stringLength)));
            }
            return chunks;
        }
    }

    private static int estimateNumberOfChunks (String string, int chunkLength) {
        return (string.length() / chunkLength) + 1;
    }

    /**
     * Tests if the specified String is empty.
     * A String is considered to be empty if it is either <code>null</code> or if the length is zero.
     *
     * @param mistery The String that is tested
     * @return A flag that indicates if the specified String is empty.
     */
    public static boolean isEmpty (String mistery) {
        return ((mistery == null) || (mistery.isEmpty()));
    }

    /**
     * Tests if the specified String is empty.
     * Trimming the string is only considered when the String does not seem to be empty at first sight.
     * A String is considered to be empty if it is either <code>null</code> or if the length is zero.
     *
     * @param mistery The String that is tested
     * @param considerTrimming A flag that indicates if this test should consider trimming the String to check if it is empty.
     *        If set to <code>true</code>, <code>String.isEmpty("  ", true)</code> will return <code>true</code>
     *        If set to <code>false</code>, <code>String.isEmpty("  ", false)</code> will return <code>false</code>
     * @return A flag that indicates if the specified String is empty.
     */
    public static boolean isEmpty (String mistery, boolean considerTrimming) {
        boolean empty = isEmpty(mistery);
        return empty || considerTrimming && isEmpty(mistery.trim());
    }

    /**
     * Tests if the first String is equal to the second String.
     * If both are null, they are considered being equal.
     *
     * @param first The first String
     * @param second The second String
     * @return A flag that indicates if the first string is equals to the second
     */
    public static boolean isEqual (String first, String second) {
        return Equality.equalityHoldsFor(first).and(second);
    }

    /**
     * Returns a substring of the base string, which is its last n characters.<br>
     * e.g.
     * <ul><li>right("abcdef", 3) == "def"</li><li>right("abcdef", 8) == "abcdef"</li><li>right(null, 8) == null</li><li>right("", 8) == ""</li></ul>
     * @param base the base {@link String}
     * @param n the number of characters
     * @return a substring of the base string, which is its last n characters, or the base string itself if it is shorter than n characters or null.
     */
    public static String right(String base, int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        if (base == null || base.length() <= n) {
            return base;
        }
        return base.substring(base.length() - n, base.length());
    }

    public static String left(String base, int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        if (base == null || base.length() <= n) {
            return base;
        }
        return base.substring(0, n);
    }

    public static String padLeft(String base, int resultLength, char padChar) {
        return fill(padLength(base, resultLength), padChar) + base;
    }

    private static int padLength(String base, int resultLength) {
        return Math.max(0, resultLength - base.length());
    }

    public static Object padRight(String base, int resultLength, char padChar) {
        return base + fill(padLength(base, resultLength), padChar);
    }

    /**
     * @param numberOfChars
     *            the number of times the given char must be repeated, must be 0 or greater
     * @param c
     *            the character to repeat
     * @return a String consisting of the given character only of the given length
     */
    public static String fill(int numberOfChars, char c) {
        if (numberOfChars < 0) {
            throw new IllegalArgumentException();
        }
        char[] chars = new char[numberOfChars];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    /**
     * Produces a String that is a concatenation of the elements' toString() separated by a given delimiter.
     *
     * @param elements the elements to concatenate
     * @param delimiter the delimiter to use, may not be null
     * @return a non null string (may be the empty String)
     */
    public static String concatDelimited(Iterable<?> elements, String delimiter) {
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder builder = new StringBuilder();
        for (Iterator<?> iterator = elements.iterator(); iterator.hasNext();) {
            builder.append(iterator.next().toString());
            if (iterator.hasNext()) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    private Strings () {super();}

}