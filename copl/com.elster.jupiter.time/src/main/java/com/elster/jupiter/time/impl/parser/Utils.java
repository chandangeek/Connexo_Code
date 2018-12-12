/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:18:58
 */
final class Utils {

    public static final String EMPTY = "";

    private Thesaurus thesaurus;

    public Utils(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    /**
     * @param hoursExpression
     * @param minutesExpression
     * @return
     */
    public String formatTime(String hoursExpression, String minutesExpression) {
        return formatTime(hoursExpression, minutesExpression, "");
    }

    /**
     * @param hoursExpression
     * @param minutesExpression
     * @param secondsExpression
     * @return
     */
    public String formatTime(String hoursExpression, String minutesExpression, String secondsExpression) {
        /*int hour = Integer.parseInt(hoursExpression);
        String amPM = hour >= 12 ? thesaurus.getFormat(TranslationKeys.time_pm).format() : thesaurus.getFormat(TranslationKeys.time_am).format();
        if (hour > 12) {
            hour -= 12;
        }
        String minute = String.valueOf(Integer.parseInt(minutesExpression));
        String second = "";
        if (!isEmpty(secondsExpression)) {
            second = ":" + Utils.leftPad(String.valueOf(Integer.parseInt(secondsExpression)), 2, '0');
        }
        return MessageFormat.format("{0}:{1}{2} {3}", String.valueOf(hour), Utils.leftPad(minute, 2, '0'), second, amPM);*/
        if (hoursExpression.length() == 1) {
            hoursExpression = "0" + hoursExpression;
        }
        if (minutesExpression.length() == 1) {
            minutesExpression = "0" + minutesExpression;
        }
        if (secondsExpression.length() == 1) {
            secondsExpression = "0" + secondsExpression;
        }
        String result = hoursExpression + ":" + minutesExpression;
        if (!secondsExpression.isEmpty()) {
            result = result + ":" + secondsExpression;
        }
        return result;
    }

    public static String getDayOfWeekName(int dayOfWeek, Locale locale) {
        return DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * @param minutesExpression
     * @return
     * @since https://github.com/RedHogs/cron-parser/issues/2
     */
    public static String formatMinutes(String minutesExpression) {
        if (Utils.contains(minutesExpression, ",")) {
            StringBuilder formattedExpression = new StringBuilder();
            for (String minute : Utils.split(minutesExpression, ',')) {
                formattedExpression.append(Utils.leftPad(minute, 2, '0'));
                formattedExpression.append(",");
            }
            return formattedExpression.toString();
        }
        return Utils.leftPad(minutesExpression, 2, '0');
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(char... searchChars) {
        return searchChars == null || searchChars.length == 0;
    }

    public static boolean isNumeric(CharSequence cs) {
        if(isEmpty(cs)) {
            return false;
        } else {
            int sz = cs.length();

            for(int i = 0; i < sz; ++i) {
                if(!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean contains(CharSequence seq, CharSequence searchSeq) {
        return seq != null && searchSeq != null? seq.toString().indexOf(searchSeq.toString()) >= 0:false;
    }


    public static String leftPad(String str, int size, char padChar) {
        if(str == null) {
            return null;
        } else {
            int pads = size - str.length();
            return pads <= 0?str:(pads > 8192?leftPad(str, size, String.valueOf(padChar)):repeat(padChar, pads).concat(str));
        }
    }

    public static String leftPad(String str, int size, String padStr) {
        if(str == null) {
            return null;
        } else {
            if(isEmpty(padStr)) {
                padStr = " ";
            }

            int padLen = padStr.length();
            int strLen = str.length();
            int pads = size - strLen;
            if(pads <= 0) {
                return str;
            } else if(padLen == 1 && pads <= 8192) {
                return leftPad(str, size, padStr.charAt(0));
            } else if(pads == padLen) {
                return padStr.concat(str);
            } else if(pads < padLen) {
                return padStr.substring(0, pads).concat(str);
            } else {
                char[] padding = new char[pads];
                char[] padChars = padStr.toCharArray();

                for(int i = 0; i < pads; ++i) {
                    padding[i] = padChars[i % padLen];
                }

                return (new String(padding)).concat(str);
            }
        }
    }

    public static String repeat(char ch, int repeat) {
        char[] buf = new char[repeat];

        for(int i = repeat - 1; i >= 0; --i) {
            buf[i] = ch;
        }

        return new String(buf);
    }

    public static String[] split(String str, char separatorChar) {
        return splitWorker(str, separatorChar, false);
    }

    private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
        if(str == null) {
            return null;
        } else {
            int len = str.length();
            if(len == 0) {
                return new String[0];
            } else {
                ArrayList list = new ArrayList();
                int i = 0;
                int start = 0;
                boolean match = false;
                boolean lastMatch = false;

                while(true) {
                    while(i < len) {
                        if(str.charAt(i) == separatorChar) {
                            if(match || preserveAllTokens) {
                                list.add(str.substring(start, i));
                                match = false;
                                lastMatch = true;
                            }

                            ++i;
                            start = i;
                        } else {
                            lastMatch = false;
                            match = true;
                            ++i;
                        }
                    }

                    if(match || preserveAllTokens && lastMatch) {
                        list.add(str.substring(start, i));
                    }

                    return (String[])list.toArray(new String[list.size()]);
                }
            }
        }
    }

    public static boolean containsAny(CharSequence cs, char... searchChars) {
        if(!isEmpty(cs) && !Utils.isEmpty(searchChars)) {
            int csLength = cs.length();
            int searchLength = searchChars.length;
            int csLast = csLength - 1;
            int searchLast = searchLength - 1;

            for(int i = 0; i < csLength; ++i) {
                char ch = cs.charAt(i);

                for(int j = 0; j < searchLength; ++j) {
                    if(searchChars[j] == ch) {
                        if(!Character.isHighSurrogate(ch)) {
                            return true;
                        }

                        if(j == searchLast) {
                            return true;
                        }

                        if(i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static boolean isNumber(String str) {
        if(Utils.isEmpty(str)) {
            return false;
        } else {
            char[] chars = str.toCharArray();
            int sz = chars.length;
            boolean hasExp = false;
            boolean hasDecPoint = false;
            boolean allowSigns = false;
            boolean foundDigit = false;
            int start = chars[0] == 45?1:0;
            int i;
            if(sz > start + 1 && chars[start] == 48) {
                if(chars[start + 1] == 120 || chars[start + 1] == 88) {
                    i = start + 2;
                    if(i == sz) {
                        return false;
                    }

                    while(i < chars.length) {
                        if((chars[i] < 48 || chars[i] > 57) && (chars[i] < 97 || chars[i] > 102) && (chars[i] < 65 || chars[i] > 70)) {
                            return false;
                        }

                        ++i;
                    }

                    return true;
                }

                if(Character.isDigit(chars[start + 1])) {
                    for(i = start + 1; i < chars.length; ++i) {
                        if(chars[i] < 48 || chars[i] > 55) {
                            return false;
                        }
                    }

                    return true;
                }
            }

            --sz;

            for(i = start; i < sz || i < sz + 1 && allowSigns && !foundDigit; ++i) {
                if(chars[i] >= 48 && chars[i] <= 57) {
                    foundDigit = true;
                    allowSigns = false;
                } else if(chars[i] == 46) {
                    if(hasDecPoint || hasExp) {
                        return false;
                    }

                    hasDecPoint = true;
                } else if(chars[i] != 101 && chars[i] != 69) {
                    if(chars[i] != 43 && chars[i] != 45) {
                        return false;
                    }

                    if(!allowSigns) {
                        return false;
                    }

                    allowSigns = false;
                    foundDigit = false;
                } else {
                    if(hasExp) {
                        return false;
                    }

                    if(!foundDigit) {
                        return false;
                    }

                    hasExp = true;
                    allowSigns = true;
                }
            }

            return i < chars.length?(chars[i] >= 48 && chars[i] <= 57?true:(chars[i] != 101 && chars[i] != 69?(chars[i] == 46?(!hasDecPoint && !hasExp?foundDigit:false):(!allowSigns && (chars[i] == 100 || chars[i] == 68 || chars[i] == 102 || chars[i] == 70)?foundDigit:(chars[i] != 108 && chars[i] != 76?false:foundDigit && !hasExp && !hasDecPoint))):false)):!allowSigns && foundDigit;
        }
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static String upperCase(String str) {
        return str == null?null:str.toUpperCase();
    }

    public static String capitalize(String str) {
        int strLen;
        if(str != null && (strLen = str.length()) != 0) {
            char firstChar = str.charAt(0);
            return Character.isTitleCase(firstChar)?str:(new StringBuilder(strLen)).append(Character.toTitleCase(firstChar)).append(str.substring(1)).toString();
        } else {
            return str;
        }
    }



}
