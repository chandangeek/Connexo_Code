package com.elster.jupiter.util.cron.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * Provides a parser and evaluator for unix-like cron expressions. Cron
 * expressions provide the ability to specify complex time combinations such as
 * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
 * last Friday of the month&quot;.
 * <P>
 * Cron expressions are comprised of 6 required fields and one optional field
 * separated by white space. The fields respectively are described as follows:
 * <p/>
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L W</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-12 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>empty, 1970-2199</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * <P>
 * The '*' character is used to specify all values. For example, &quot;*&quot;
 * in the minute field means &quot;every minute&quot;.
 * <P>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fields, but not the other.
 * <P>
 * The '-' character is used to specify ranges For example &quot;10-12&quot; in
 * the hour field means &quot;the hours 10, 11 and 12&quot;.
 * <P>
 * The ',' character is used to specify additional values. For example
 * &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
 * Wednesday, and Friday&quot;.
 * <P>
 * The '/' character is used to specify increments. For example &quot;0/15&quot;
 * in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And
 * &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
 * 50&quot;.  Specifying '*' before the  '/' is equivalent to specifying 0 is
 * the value to start with. Essentially, for each field in the expression, there
 * is a set of numbers that can be turned on or off. For seconds and minutes,
 * the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
 * 31, and for months 1 to 12. The &quot;/&quot; character simply helps you turn
 * on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
 * month field only turns on month &quot;7&quot;, it does NOT mean every 6th
 * month, please note that subtlety.
 * <P>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for &quot;last&quot;, but it has different
 * meaning in each of the two fields. For example, the value &quot;L&quot; in
 * the day-of-month field means &quot;the last day of the month&quot; - day 31
 * for January, day 28 for February on non-leap years. If used in the
 * day-of-week field by itself, it simply means &quot;7&quot; or
 * &quot;SAT&quot;. But if used in the day-of-week field after another value, it
 * means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
 * means &quot;the last friday of the month&quot;. When using the 'L' option, it
 * is important not to specify lists, or ranges of values, as you'll get
 * confusing results.
 * <P>
 * The 'W' character is allowed for the day-of-month field.  This character
 * is used to specify the weekday (Monday-Friday) nearest the given day.  As an
 * example, if you were to specify &quot;15W&quot; as the value for the
 * day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
 * the month&quot;. So if the 15th is a Saturday, the trigger will fire on
 * Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
 * 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
 * However if you specify &quot;1W&quot; as the value for day-of-month, and the
 * 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not
 * 'jump' over the boundary of a month's days.  The 'W' character can only be
 * specified when the day-of-month is a single day, not a range or list of days.
 * <P>
 * The 'L' and 'W' characters can also be combined for the day-of-month
 * expression to yield 'LW', which translates to &quot;last weekday of the
 * month&quot;.
 * <P>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify &quot;the nth&quot; XXX day of the month. For example, the
 * value of &quot;6#3&quot; in the day-of-week field means the third Friday of
 * the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month).
 * Other examples: &quot;2#1&quot; = the first Monday of the month and
 * &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
 * &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
 * no firing will occur that month.  If the '#' character is used, there can
 * only be one expression in the day-of-week field (&quot;3#1,6#3&quot; is
 * not valid, since there are two expressions).
 * <P>
 * <!--The 'C' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "calendar". This means values are
 * calculated against the associated calendar, if any. If no calendar is
 * associated, then it is equivalent to having an all-inclusive calendar. A
 * value of "5C" in the day-of-month field means "the first day included by the
 * calendar on or after the 5th". A value of "1C" in the day-of-week field
 * means "the first day included by the calendar on or after Sunday".-->
 * <P>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 * <p/>
 * <p>
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in one of these fields).
 * </li>
 * <li>Overflowing ranges is supported - that is, having a larger number on
 * the left hand side than the right. You might do 22-2 to catch 10 o'clock
 * at night until 2 o'clock in the morning, or you might have NOV-FEB. It is
 * very important to note that overuse of overflowing ranges creates ranges
 * that don't make sense and no effort has been made to determine which
 * interpretation CronExpression chooses. An example would be
 * "0 0 14-6 ? * FRI-MON". </li>
 * </ul>
 * </p>
 *
 * @author Sharada Jambula, James House
 * @author Contributions from Mads Henderson
 * @author Refactoring from CronTrigger to CronExpression by Aaron Craven
 */
class QuartzCronExpression implements Serializable {

    private static final long serialVersionUID = 12423409423L;

    private static final int SECOND = 0;
    private static final int MINUTE = 1;
    private static final int HOUR = 2;
    private static final int DAY_OF_MONTH = 3;
    private static final int MONTH = 4;
    private static final int DAY_OF_WEEK = 5;
    private static final int YEAR = 6;
    private static final int ALL_SPEC_INT = 99; // '*'
    private static final int NO_SPEC_INT = 98; // '?'
    private static final Integer ALL_SPEC = ALL_SPEC_INT;
    private static final Integer NO_SPEC = NO_SPEC_INT;
    private static final int YEAR_TO_GIVEUP_SCHEDULING_AT = 2199;
    private static final int MILLIS_PER_SECOND = 1000;

    private enum Months {JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC}

    private enum DayOfWeek {SUN, MON, TUE, WED, THU, FRI, SAT}

    private String cronExpression;
    private TimeZone timeZone;
    private transient TreeSet<Integer> seconds;
    private transient TreeSet<Integer> minutes;
    private transient TreeSet<Integer> hours;
    private transient TreeSet<Integer> daysOfMonth;
    private transient TreeSet<Integer> months;
    private transient TreeSet<Integer> daysOfWeek;
    private transient TreeSet<Integer> years;

    private transient boolean lastdayOfWeek;
    private transient int nthdayOfWeek;
    private transient boolean lastdayOfMonth;
    private transient boolean nearestWeekday;

    /**
     * Constructs a new <CODE>CronExpression</CODE> based on the specified
     * parameter.
     *
     * @param cronExpression String representation of the cron expression the
     *                       new object should represent
     * @throws java.text.ParseException if the string expression cannot be parsed into a valid
     *                                  <CODE>CronExpression</CODE>
     */
    public QuartzCronExpression(String cronExpression) {
        if (cronExpression == null) {
            throw new IllegalArgumentException("cronExpression cannot be null");
        }

        this.cronExpression = cronExpression.toUpperCase(Locale.US);

        buildExpression(this.cronExpression);
    }

    /**
     * Indicates whether the given date satisfies the cron expression. Note that
     * milliseconds are ignored, so two Dates falling on different milliseconds
     * of the same second will always have the same result here.
     *
     * @param date the date to evaluate
     * @return a boolean indicating whether the given date satisfies the cron
     *         expression
     */
    public boolean isSatisfiedBy(Date date) {
        Calendar testDateCal = Calendar.getInstance(getTimeZone());
        testDateCal.setTime(date);
        testDateCal.set(Calendar.MILLISECOND, 0);
        Date originalDate = testDateCal.getTime();

        testDateCal.add(Calendar.SECOND, -1);

        Date timeAfter = getTimeAfter(testDateCal.getTime());

        return ((timeAfter != null) && (timeAfter.equals(originalDate)));
    }

    /**
     * Returns the next date/time <I>after</I> the given date/time which
     * satisfies the cron expression.
     *
     * @param date the date/time at which to begin the search for the next valid
     *             date/time
     * @return the next valid date/time
     */
    public Date getNextValidTimeAfter(Date date) {
        return getTimeAfter(date);
    }

    /**
     * Returns the next date/time <I>after</I> the given date/time which does
     * <I>not</I> satisfy the expression
     *
     * @param date the date/time at which to begin the search for the next
     *             invalid date/time
     * @return the next valid date/time
     */
    public Date getNextInvalidTimeAfter(Date date) {
        long difference = MILLIS_PER_SECOND;

        //move back to the nearest second so differences will be accurate
        Calendar adjustCal = Calendar.getInstance(getTimeZone());
        adjustCal.setTime(date);
        adjustCal.set(Calendar.MILLISECOND, 0);
        Date lastDate = adjustCal.getTime();

        Date newDate;

        //TODO: (QUARTZ-481) IMPROVE THIS! The following is a BAD solution to this problem. Performance will be very bad here, depending on the cron expression. It is, however A solution.

        //keep getting the next included time until it's farther than one second
        // apart. At that point, lastDate is the last valid fire time. We return
        // the second immediately following it.
        while (difference == MILLIS_PER_SECOND) {
            newDate = getTimeAfter(lastDate);

            difference = newDate.getTime() - lastDate.getTime();

            if (difference == MILLIS_PER_SECOND) {
                lastDate = newDate;
            }
        }

        return new Date(lastDate.getTime() + MILLIS_PER_SECOND);
    }

    /**
     * Returns the time zone for which this <code>CronExpression</code>
     * will be resolved.
     */
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    /**
     * Sets the time zone for which  this <code>CronExpression</code>
     * will be resolved.
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Returns the string representation of the <CODE>CronExpression</CODE>
     *
     * @return a string representation of the <CODE>CronExpression</CODE>
     */
    public String toString() {
        return cronExpression;
    }

    /**
     * Indicates whether the specified cron expression can be parsed into a
     * valid cron expression
     *
     * @param cronExpression the expression to evaluate
     * @return a boolean indicating whether the given expression is a valid cron
     *         expression
     */
    public static boolean isValidExpression(String cronExpression) {

        try {
            new QuartzCronExpression(cronExpression);
        } catch (IllegalArgumentException pe) {
            return false;
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Expression Parsing Functions
    //
    ////////////////////////////////////////////////////////////////////////////

    private void buildExpression(String expression) {

        initSets();

        int exprOn = SECOND;

        StringTokenizer exprsTok = new StringTokenizer(expression, " \t",
                false);

        while (exprsTok.hasMoreTokens() && exprOn <= YEAR) {
            String expr = exprsTok.nextToken().trim();

            // throw an exception if L is used with other days of the month
            if (exprOn == DAY_OF_MONTH && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(",")) {
                throw new IllegalArgumentException("Support for specifying 'L' and 'LW' with other days of the month is not implemented");
            }
            // throw an exception if L is used with other days of the week
            if (exprOn == DAY_OF_WEEK && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(",")) {
                throw new IllegalArgumentException("Support for specifying 'L' with other days of the week is not implemented");
            }

            StringTokenizer vTok = new StringTokenizer(expr, ",");
            while (vTok.hasMoreTokens()) {
                String v = vTok.nextToken();
                storeExpressionVals(0, v, exprOn);
            }

            exprOn++;
        }

        if (exprOn <= DAY_OF_WEEK) {
            throw new IllegalArgumentException("Unexpected end of expression.");
        }

        if (exprOn <= YEAR) {
            storeExpressionVals(0, "*", YEAR);
        }

        TreeSet<Integer> dow = getSet(DAY_OF_WEEK);
        TreeSet<Integer> dom = getSet(DAY_OF_MONTH);

        // Copying the logic from the UnsupportedOperationException below
        boolean dayOfMSpec = !dom.contains(NO_SPEC);
        boolean dayOfWSpec = !dow.contains(NO_SPEC);

        if (dayOfMSpec == dayOfWSpec) {
            throw new IllegalArgumentException("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
        }
    }

    private void initSets() {
        if (seconds == null) {
            seconds = new TreeSet<>();
        }
        if (minutes == null) {
            minutes = new TreeSet<>();
        }
        if (hours == null) {
            hours = new TreeSet<>();
        }
        if (daysOfMonth == null) {
            daysOfMonth = new TreeSet<>();
        }
        if (months == null) {
            months = new TreeSet<>();
        }
        if (daysOfWeek == null) {
            daysOfWeek = new TreeSet<>();
        }
        if (years == null) {
            years = new TreeSet<>();
        }
    }

    private int storeExpressionVals(int pos, String s, int type) {

        int incr = 0;
        int i = skipWhiteSpace(pos, s);
        if (i >= s.length()) {
            return i;
        }
        char c = s.charAt(i);
        if ((c >= 'A') && (c <= 'Z') && (!"L".equals(s)) && (!"LW".equals(s))) {
            String sub = s.substring(i, i + 3);
            int sval;
            int eval = -1;
            if (type == MONTH) {
                sval = getMonthNumber(sub) + 1;
                if (sval <= 0) {
                    throw new IllegalArgumentException("Invalid Month value: '" + sub + "'");
                }
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getMonthNumber(sub) + 1;
                        if (eval <= 0) {
                            throw new IllegalArgumentException("Invalid Month value: '" + sub + "'");
                        }
                    }
                }
            } else if (type == DAY_OF_WEEK) {
                sval = getDayOfWeekNumber(sub);
                if (sval < 0) {
                    throw new IllegalArgumentException("Invalid Day-of-Week value: '");
                }
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getDayOfWeekNumber(sub);
                        if (eval < 0) {
                            throw new IllegalArgumentException("Invalid Day-of-Week value: '" + sub + "'");
                        }
                    } else if (c == '#') {
                        try {
                            i += 4;
                            nthdayOfWeek = Integer.parseInt(s.substring(i));
                            if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("A numeric value between 1 and 5 must follow the '#' option");
                        }
                    } else if (c == 'L') {
                        lastdayOfWeek = true;
                        i++;
                    }
                }

            } else {
                throw new IllegalArgumentException("Illegal characters for this position: '" + sub + "'");
            }
            if (eval != -1) {
                incr = 1;
            }
            addToSet(sval, eval, incr, type);
            return (i + 3);
        }

        if (c == '?') {
            i++;
            if ((i + 1) < s.length()
                    && (s.charAt(i) != ' ' && s.charAt(i + 1) != '\t')) {
                throw new IllegalArgumentException("Illegal character after '?': " + s.charAt(i));
            }
            if (type != DAY_OF_WEEK && type != DAY_OF_MONTH) {
                throw new IllegalArgumentException("'?' can only be specfied for Day-of-Month or Day-of-Week.");
            }
            if (type == DAY_OF_WEEK && !lastdayOfMonth) {
                int val = daysOfMonth.last();
                if (val == NO_SPEC_INT) {
                    throw new IllegalArgumentException("'?' can only be specfied for Day-of-Month -OR- Day-of-Week.");
                }
            }

            addToSet(NO_SPEC_INT, -1, 0, type);
            return i;
        }

        if (c == '*' || c == '/') {
            if (c == '*' && (i + 1) >= s.length()) {
                addToSet(ALL_SPEC_INT, -1, incr, type);
                return i + 1;
            } else if (c == '/'
                    && ((i + 1) >= s.length() || s.charAt(i + 1) == ' ' || s
                    .charAt(i + 1) == '\t')) {
                throw new IllegalArgumentException("'/' must be followed by an integer.");
            } else if (c == '*') {
                i++;
            }
            c = s.charAt(i);
            if (c == '/') { // is an increment specified?
                i++;
                if (i >= s.length()) {
                    throw new IllegalArgumentException("Unexpected end of string.");
                }

                incr = getNumericValue(s, i);

                i++;
                if (incr > 10) {
                    i++;
                }
                if (incr > 59 && (type == SECOND || type == MINUTE)) {
                    throw new IllegalArgumentException("Increment > 60 : " + incr);
                } else if (incr > 23 && (type == HOUR)) {
                    throw new IllegalArgumentException("Increment > 24 : " + incr);
                } else if (incr > 31 && (type == DAY_OF_MONTH)) {
                    throw new IllegalArgumentException("Increment > 31 : " + incr);
                } else if (incr > 7 && (type == DAY_OF_WEEK)) {
                    throw new IllegalArgumentException("Increment > 7 : " + incr);
                } else if (incr > 12 && (type == MONTH)) {
                    throw new IllegalArgumentException("Increment > 12 : " + incr);
                }
            } else {
                incr = 1;
            }

            addToSet(ALL_SPEC_INT, -1, incr, type);
            return i;
        } else if (c == 'L') {
            i++;
            if (type == DAY_OF_MONTH) {
                lastdayOfMonth = true;
            }
            if (type == DAY_OF_WEEK) {
                addToSet(7, 7, 0, type);
            }
            if (type == DAY_OF_MONTH && s.length() > i) {
                c = s.charAt(i);
                if (c == 'W') {
                    nearestWeekday = true;
                    i++;
                }
            }
            return i;
        } else if (c >= '0' && c <= '9') {
            int val = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                addToSet(val, -1, -1, type);
            } else {
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(val, s, i);
                    val = vs.value;
                    i = vs.pos;
                }
                i = checkNext(i, s, val, type);
                return i;
            }
        } else {
            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        return i;
    }

    private int checkNext(int pos, String s, int val, int type) {

        int end = -1;
        int i = pos;

        if (i >= s.length()) {
            addToSet(val, end, -1, type);
            return i;
        }

        char c = s.charAt(pos);

        if (c == 'L') {
            if (type == DAY_OF_WEEK) {
                if (val < 1 || val > 7) {
                    throw new IllegalArgumentException("Day-of-Week values must be between 1 and 7");
                }
                lastdayOfWeek = true;
            } else {
                throw new IllegalArgumentException("'L' option is not valid here. (pos=" + i + ")");
            }
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == 'W') {
            if (type == DAY_OF_MONTH) {
                nearestWeekday = true;
            } else {
                throw new IllegalArgumentException("'W' option is not valid here. (pos=" + i + ")");
            }
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == '#') {
            if (type != DAY_OF_WEEK) {
                throw new IllegalArgumentException("'#' option is not valid here. (pos=" + i + ")");
            }
            i++;
            try {
                nthdayOfWeek = Integer.parseInt(s.substring(i));
                if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("A numeric value between 1 and 5 must follow the '#' option");
            }

            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == '-') {
            i++;
            c = s.charAt(i);
            int v = Integer.parseInt(String.valueOf(c));
            end = v;
            i++;
            if (i >= s.length()) {
                addToSet(val, end, 1, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v, s, i);
                end = vs.value;
                i = vs.pos;
            }
            if (i < s.length() && (s.charAt(i) == '/')) {
                i++;
                c = s.charAt(i);
                int v2 = Integer.parseInt(String.valueOf(c));
                i++;
                if (i >= s.length()) {
                    addToSet(val, end, v2, type);
                    return i;
                }
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(v2, s, i);
                    int v3 = vs.value;
                    addToSet(val, end, v3, type);
                    i = vs.pos;
                    return i;
                } else {
                    addToSet(val, end, v2, type);
                    return i;
                }
            } else {
                addToSet(val, end, 1, type);
                return i;
            }
        }

        if (c == '/') {
            i++;
            c = s.charAt(i);
            int v2 = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                addToSet(val, end, v2, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v2, s, i);
                int v3 = vs.value;
                addToSet(val, end, v3, type);
                i = vs.pos;
                return i;
            } else {
                throw new IllegalArgumentException("Unexpected character '" + c + "' after '/'");
            }
        }

        addToSet(val, end, 0, type);
        i++;
        return i;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getExpressionSummary() {
        StringBuffer buf = new StringBuffer();

        buf.append("seconds: ");
        buf.append(getExpressionSetSummary(seconds));
        buf.append("\n");
        buf.append("minutes: ");
        buf.append(getExpressionSetSummary(minutes));
        buf.append("\n");
        buf.append("hours: ");
        buf.append(getExpressionSetSummary(hours));
        buf.append("\n");
        buf.append("daysOfMonth: ");
        buf.append(getExpressionSetSummary(daysOfMonth));
        buf.append("\n");
        buf.append("months: ");
        buf.append(getExpressionSetSummary(months));
        buf.append("\n");
        buf.append("daysOfWeek: ");
        buf.append(getExpressionSetSummary(daysOfWeek));
        buf.append("\n");
        buf.append("lastdayOfWeek: ");
        buf.append(lastdayOfWeek);
        buf.append("\n");
        buf.append("nearestWeekday: ");
        buf.append(nearestWeekday);
        buf.append("\n");
        buf.append("NthDayOfWeek: ");
        buf.append(nthdayOfWeek);
        buf.append("\n");
        buf.append("lastdayOfMonth: ");
        buf.append(lastdayOfMonth);
        buf.append("\n");
        buf.append("years: ");
        buf.append(getExpressionSetSummary(years));
        buf.append("\n");

        return buf.toString();
    }

    private String getExpressionSetSummary(java.util.Set set) {

        if (set.contains(NO_SPEC)) {
            return "?";
        }
        if (set.contains(ALL_SPEC)) {
            return "*";
        }

        StringBuilder buf = new StringBuilder();

        Iterator itr = set.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = (Integer) itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    private String getExpressionSetSummary(java.util.ArrayList list) {

        if (list.contains(NO_SPEC)) {
            return "?";
        }
        if (list.contains(ALL_SPEC)) {
            return "*";
        }

        StringBuilder buf = new StringBuilder();

        Iterator itr = list.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = (Integer) itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    private int skipWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t'); i++) {
            ;
        }

        return i;
    }

    private int findNextWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) != ' ' || s.charAt(i) != '\t'); i++) {
            ;
        }

        return i;
    }

    private void addToSet(int val, int end, int incr, int type) {

        TreeSet<Integer> set = getSet(type);

        if (type == SECOND || type == MINUTE) {
            if ((val < 0 || val > 59 || end > 59) && (val != ALL_SPEC_INT)) {
                throw new IllegalArgumentException("Minute and Second values must be between 0 and 59");
            }
        } else if (type == HOUR) {
            if ((val < 0 || val > 23 || end > 23) && (val != ALL_SPEC_INT)) {
                throw new IllegalArgumentException("Hour values must be between 0 and 23");
            }
        } else if (type == DAY_OF_MONTH) {
            if ((val < 1 || val > 31 || end > 31) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT)) {
                throw new IllegalArgumentException("Day of month values must be between 1 and 31");
            }
        } else if (type == MONTH) {
            if ((val < 1 || val > 12 || end > 12) && (val != ALL_SPEC_INT)) {
                throw new IllegalArgumentException("Month values must be between 1 and 12");
            }
        } else if (type == DAY_OF_WEEK) {
            if ((val == 0 || val > 7 || end > 7) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT)) {
                throw new IllegalArgumentException("Day-of-Week values must be between 1 and 7");
            }
        }

        if ((incr == 0 || incr == -1) && val != ALL_SPEC_INT) {
            if (val != -1) {
                set.add(val);
            } else {
                set.add(NO_SPEC);
            }

            return;
        }

        int startAt = val;
        int stopAt = end;

        if (val == ALL_SPEC_INT && incr <= 0) {
            incr = 1;
            set.add(ALL_SPEC); // put in a marker, but also fill values
        }

        if (type == SECOND || type == MINUTE) {
            if (stopAt == -1) {
                stopAt = 59;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 0;
            }
        } else if (type == HOUR) {
            if (stopAt == -1) {
                stopAt = 23;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 0;
            }
        } else if (type == DAY_OF_MONTH) {
            if (stopAt == -1) {
                stopAt = 31;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == MONTH) {
            if (stopAt == -1) {
                stopAt = 12;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == DAY_OF_WEEK) {
            if (stopAt == -1) {
                stopAt = 7;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == YEAR) {
            if (stopAt == -1) {
                stopAt = YEAR_TO_GIVEUP_SCHEDULING_AT;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1970;
            }
        }

        // if the end of the range is before the start, then we need to overflow into 
        // the next day, month etc. This is done by adding the maximum amount for that 
        // type, and using modulus max to determine the value being added.
        int max = -1;
        if (stopAt < startAt) {
            switch (type) {
                case SECOND:
                    max = 60;
                    break;
                case MINUTE:
                    max = 60;
                    break;
                case HOUR:
                    max = 24;
                    break;
                case MONTH:
                    max = 12;
                    break;
                case DAY_OF_WEEK:
                    max = 7;
                    break;
                case DAY_OF_MONTH:
                    max = 31;
                    break;
                case YEAR:
                    throw new IllegalArgumentException("Start year must be less than stop year");
                default:
                    throw new IllegalArgumentException("Unexpected type encountered");
            }
            stopAt += max;
        }

        for (int i = startAt; i <= stopAt; i += incr) {
            if (max == -1) {
                // ie: there's no max to overflow over
                set.add(i);
            } else {
                // take the modulus to get the real value
                int i2 = i % max;

                // 1-indexed ranges should not include 0, and should include their max
                if (i2 == 0 && (type == MONTH || type == DAY_OF_WEEK || type == DAY_OF_MONTH)) {
                    i2 = max;
                }

                set.add(i2);
            }
        }
    }

    private TreeSet<Integer> getSet(int type) {
        switch (type) {
            case SECOND:
                return seconds;
            case MINUTE:
                return minutes;
            case HOUR:
                return hours;
            case DAY_OF_MONTH:
                return daysOfMonth;
            case MONTH:
                return months;
            case DAY_OF_WEEK:
                return daysOfWeek;
            case YEAR:
                return years;
            default:
                return null;
        }
    }

    private ValueSet getValue(int v, String s, int i) {
        char c = s.charAt(i);
        String s1 = String.valueOf(v);
        while (c >= '0' && c <= '9') {
            s1 += c;
            i++;
            if (i >= s.length()) {
                break;
            }
            c = s.charAt(i);
        }
        ValueSet val = new ValueSet();

        val.pos = (i < s.length()) ? i : i + 1;
        val.value = Integer.parseInt(s1);
        return val;
    }

    private int getNumericValue(String s, int i) {
        int endOfVal = findNextWhiteSpace(i, s);
        String val = s.substring(i, endOfVal);
        return Integer.parseInt(val);
    }

    private int getMonthNumber(String s) {
        return Months.valueOf(s).ordinal();
    }

    private int getDayOfWeekNumber(String s) {
        return DayOfWeek.valueOf(s).ordinal() + 1;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Computation Functions
    //
    ////////////////////////////////////////////////////////////////////////////

    private Date getTimeAfter(Date afterTime) {

        // Computation is based on Gregorian year only.
        Calendar cl = new java.util.GregorianCalendar(getTimeZone());

        // move ahead one second, since we're computing the time *after* the
        // given time
        afterTime = new Date(afterTime.getTime() + 1000);
        // CronTrigger does not deal with milliseconds
        cl.setTime(afterTime);
        cl.set(Calendar.MILLISECOND, 0);

        boolean gotOne = false;
        // loop until we've computed the next time, or we've past the endTime
        while (!gotOne) {

            //if (endTime != null && cl.getTime().after(endTime)) return null;
            if (cl.get(Calendar.YEAR) > 2999) { // prevent endless loop...
                return null;
            }

            int sec = cl.get(Calendar.SECOND);
            int min = cl.get(Calendar.MINUTE);

            // get second.................................................
            SortedSet<Integer> st = seconds.tailSet(sec);
            if (st != null && !st.isEmpty()) {
                sec = st.first();
            } else {
                sec = seconds.first();
                min++;
                cl.set(Calendar.MINUTE, min);
            }
            cl.set(Calendar.SECOND, sec);

            min = cl.get(Calendar.MINUTE);
            int hr = cl.get(Calendar.HOUR_OF_DAY);
            int t = -1;

            // get minute.................................................
            st = minutes.tailSet(min);
            if (st != null && !st.isEmpty()) {
                t = min;
                min = st.first();
            } else {
                min = minutes.first();
                hr++;
            }
            if (min != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, min);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.MINUTE, min);

            hr = cl.get(Calendar.HOUR_OF_DAY);
            int day = cl.get(Calendar.DAY_OF_MONTH);
            t = -1;

            // get hour...................................................
            st = hours.tailSet(hr);
            if (st != null && !st.isEmpty()) {
                t = hr;
                hr = st.first();
            } else {
                hr = hours.first();
                day++;
            }
            if (hr != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.DAY_OF_MONTH, day);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.HOUR_OF_DAY, hr);

            day = cl.get(Calendar.DAY_OF_MONTH);
            int mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            t = -1;
            int tmon = mon;

            // get day...................................................
            boolean dayOfMSpec = !daysOfMonth.contains(NO_SPEC);
            boolean dayOfWSpec = !daysOfWeek.contains(NO_SPEC);
            if (dayOfMSpec && !dayOfWSpec) { // get day by day of month rule
                st = daysOfMonth.tailSet(day);
                if (lastdayOfMonth) {
                    if (!nearestWeekday) {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    } else {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                        java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
                        tcal.set(Calendar.SECOND, 0);
                        tcal.set(Calendar.MINUTE, 0);
                        tcal.set(Calendar.HOUR_OF_DAY, 0);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

                        int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        int dow = tcal.get(Calendar.DAY_OF_WEEK);

                        if (dow == Calendar.SATURDAY && day == 1) {
                            day += 2;
                        } else if (dow == Calendar.SATURDAY) {
                            day -= 1;
                        } else if (dow == Calendar.SUNDAY && day == ldom) {
                            day -= 2;
                        } else if (dow == Calendar.SUNDAY) {
                            day += 1;
                        }

                        tcal.set(Calendar.SECOND, sec);
                        tcal.set(Calendar.MINUTE, min);
                        tcal.set(Calendar.HOUR_OF_DAY, hr);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        Date nTime = tcal.getTime();
                        if (nTime.before(afterTime)) {
                            day = 1;
                            mon++;
                        }
                    }
                } else if (nearestWeekday) {
                    t = day;
                    day = daysOfMonth.first();

                    java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
                    tcal.set(Calendar.SECOND, 0);
                    tcal.set(Calendar.MINUTE, 0);
                    tcal.set(Calendar.HOUR_OF_DAY, 0);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

                    int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    int dow = tcal.get(Calendar.DAY_OF_WEEK);

                    if (dow == Calendar.SATURDAY && day == 1) {
                        day += 2;
                    } else if (dow == Calendar.SATURDAY) {
                        day -= 1;
                    } else if (dow == Calendar.SUNDAY && day == ldom) {
                        day -= 2;
                    } else if (dow == Calendar.SUNDAY) {
                        day += 1;
                    }


                    tcal.set(Calendar.SECOND, sec);
                    tcal.set(Calendar.MINUTE, min);
                    tcal.set(Calendar.HOUR_OF_DAY, hr);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    Date nTime = tcal.getTime();
                    if (nTime.before(afterTime)) {
                        day = daysOfMonth.first();
                        mon++;
                    }
                } else if (st != null && !st.isEmpty()) {
                    t = day;
                    day = st.first();
                    // make sure we don't over-run a short month, such as february
                    int lastDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    if (day > lastDay) {
                        day = daysOfMonth.first();
                        mon++;
                    }
                } else {
                    day = daysOfMonth.first();
                    mon++;
                }

                if (day != t || mon != tmon) {
                    cl.set(Calendar.SECOND, 0);
                    cl.set(Calendar.MINUTE, 0);
                    cl.set(Calendar.HOUR_OF_DAY, 0);
                    cl.set(Calendar.DAY_OF_MONTH, day);
                    cl.set(Calendar.MONTH, mon - 1);
                    // '- 1' because calendar is 0-based for this field, and we
                    // are 1-based
                    continue;
                }
            } else if (dayOfWSpec && !dayOfMSpec) { // get day by day of week rule
                if (lastdayOfWeek) { // are we looking for the last XXX day of
                    // the month?
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // did we already miss the
                        // last one?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    }

                    // find date of last occurance of this day in this month...
                    while ((day + daysToAdd + 7) <= lDay) {
                        daysToAdd += 7;
                    }

                    day += daysToAdd;

                    if (daysToAdd > 0) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are not promoting the month
                        continue;
                    }

                } else if (nthdayOfWeek != 0) {
                    // are we looking for the Nth XXX day in the month?
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    } else if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    boolean dayShifted = false;
                    if (daysToAdd > 0) {
                        dayShifted = true;
                    }

                    day += daysToAdd;
                    int weekOfMonth = day / 7;
                    if (day % 7 > 0) {
                        weekOfMonth++;
                    }

                    daysToAdd = (nthdayOfWeek - weekOfMonth) * 7;
                    day += daysToAdd;
                    if (daysToAdd < 0
                            || day > getLastDayOfMonth(mon, cl
                            .get(Calendar.YEAR))) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0 || dayShifted) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are NOT promoting the month
                        continue;
                    }
                } else {
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    st = daysOfWeek.tailSet(cDow);
                    if (st != null && !st.isEmpty()) {
                        dow = st.first();
                    }

                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // will we pass the end of
                        // the month?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0) { // are we swithing days?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day + daysToAdd);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' because calendar is 0-based for this field,
                        // and we are 1-based
                        continue;
                    }
                }
            } else { // dayOfWSpec && !dayOfMSpec
                throw new UnsupportedOperationException(
                        "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
                // TODO:
            }
            cl.set(Calendar.DAY_OF_MONTH, day);

            mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            int year = cl.get(Calendar.YEAR);
            t = -1;

            // test for expressions that never generate a valid fire date,
            // but keep looping...
            if (year > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                return null;
            }

            // get month...................................................
            st = months.tailSet(mon);
            if (st != null && !st.isEmpty()) {
                t = mon;
                mon = st.first();
            } else {
                mon = months.first();
                year++;
            }
            if (mon != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, mon - 1);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.MONTH, mon - 1);
            // '- 1' because calendar is 0-based for this field, and we are
            // 1-based

            year = cl.get(Calendar.YEAR);

            // get year...................................................
            st = years.tailSet(year);
            if (st != null && !st.isEmpty()) {
                t = year;
                year = st.first();
            } else {
                return null; // ran out of years...
            }

            if (year != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, 0);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.YEAR, year);

            gotOne = true;
        } // while( !done )

        return cl.getTime();
    }

    /**
     * Advance the calendar to the particular hour paying particular attention
     * to daylight saving problems.
     *
     * @param cal
     * @param hour
     */
    private void setCalendarHour(Calendar cal, int hour) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        if (cal.get(java.util.Calendar.HOUR_OF_DAY) != hour && hour != 24) {
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour + 1);
        }
    }

    private boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0));
    }

    private int getLastDayOfMonth(int monthNum, int year) {

        switch (monthNum) {
            case 1:
                return 31;
            case 2:
                return (isLeapYear(year)) ? 29 : 28;
            case 3:
                return 31;
            case 4:
                return 30;
            case 5:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case 8:
                return 31;
            case 9:
                return 30;
            case 10:
                return 31;
            case 11:
                return 30;
            case 12:
                return 31;
            default:
                throw new IllegalArgumentException("Illegal month number: " + monthNum);
        }
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException, ClassNotFoundException {

        stream.defaultReadObject();
        try {
            buildExpression(cronExpression);
        } catch (Exception ignore) {
        } // never happens
    }

    private static class ValueSet {

        public int value;

        public int pos;
    }
}