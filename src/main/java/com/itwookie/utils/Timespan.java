package com.itwookie.utils;

import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Timespan {

    long a, b, dif;

    public Timespan(long t0, long t1) {
        if (t0 < 0 || t1 < 0) throw new IllegalArgumentException("Negative times not supported");
        a = Math.min(t0, t1);
        b = Math.max(t0, t1);
        dif = b - a;
    }

    public Timespan(Date d0, Date d1) {
        this(d0.getTime(), d1.getTime());
    }

    public Timespan(Instant i0, Instant i1) {
        this(i0.toEpochMilli(), i1.toEpochMilli());
    }

    public Timespan(Calendar c0, Calendar c1) {
        this(c0.getTimeInMillis(), c1.getTimeInMillis());
    }

    public Timespan(Object early, Object late) {
        this(xToLong(early), xToLong(late));
    }

    private static long xToLong(Object x) {
        if (x instanceof Number) {
            return ((Number) x).longValue();
        } else if (x instanceof Date) {
            return ((Date) x).getTime();
        } else if (x instanceof Instant) {
            return ((Instant) x).toEpochMilli();
        } else if (x instanceof Calendar) {
            return ((Calendar) x).getTimeInMillis();
        } else {
            throw new IllegalArgumentException("Unsupported time representation");
        }
    }

    public void append(long timems) {
        if (timems < -dif)
            throw new RuntimeException("Appending " + timems + "ms would result in negative timespan");
        b += timems;
        dif += timems;
    }

    public void prepend(long timems) {
        if (timems > a)
            throw new RuntimeException("Can't calculate timespans before unix 0");
        if (timems < -dif)
            throw new RuntimeException("Prepending " + timems + "ms would result in negative timespan");
        a -= timems;
        dif += timems;
    }

    public long getMillis() {
        return dif;
    }

    public int getMillisModulo() {
        return (int) (dif % 1000);
    }

    public long getSeconds() {
        return dif / 1000;
    }

    public int getSecondsModulo() {
        return (int) ((dif / 1000) % 60);
    }

    public long getMinutes() {
        return dif / 60_000;
    }

    public int getMinutesModulo() {
        return (int) ((dif / 60_000) % 60);
    }

    public long getHours() {
        return dif / 3_600_000;
    }

    public int getHoursModulo() {
        return (int) ((dif / 3_600_000) % 24);
    }

    public long getDays() {
        return dif / 86_400_000;
    }

    /**
     * since months have different length this returns a compromise that might not be 100% corretct.<br>
     * <pre>
     * cnt = day_of_month(end)+(length_of_month(start)-day_of_month(start));
     * return cnt > length_of_month(end) ? cnt - length_of_month(end) : cnt;
     * </pre>
     * in words:<br>
     * * build the sum of days passed in the end month (basically the result)<br>
     * * add the days passed in the first month since start<br>
     * * if the result completes the end month, subtract that months worth of days<br>
     */
    public int getDaysModulo() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(a);
        int len_start = Month.of(c.get(Calendar.MONTH) + 1)
                .length(Year.of(c.get(Calendar.YEAR)).isLeap());
        int dom_start = c.get(Calendar.DAY_OF_MONTH);
        c.setTimeInMillis(b);
        int len_end = Month.of(c.get(Calendar.MONTH) + 1)
                .length(Year.of(c.get(Calendar.YEAR)).isLeap());
        int dom_end = c.get(Calendar.DAY_OF_MONTH);
        //if dom_end == len_end the whole month passed -> no modulo part
        if (dom_end == len_end) {
            //pick length of next month for modulo-ing start days
            //this might be relevant if start month passed 30 of 31 days and
            // the next month contains less (february)
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.add(Calendar.MONTH, 1);
            len_end = Month.of(c.get(Calendar.MONTH) + 1)
                    .length(Year.of(c.get(Calendar.YEAR)).isLeap());
            dom_end = 0;//nothing to count
        }

        int days = dom_end +
                (dom_start > 1 ? len_start - dom_start : 0); //if dom_start == 1 the whole month passed -> no modulo part
        return days > len_end ? days - len_end : days;
    }

    /**
     * since months have different length this returns a compromise that might not be 100% corretct.<br>
     * <pre>
     * cnt = day_of_month(end)+(days_in_month(start)-day_of_month(start));
     * return cnt > days_in_month(end) ? months+1 : months;
     * </pre>
     */
    public long getMonths() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(a);
        int year_start = c.get(Calendar.YEAR);
        int month_start = c.get(Calendar.MONTH) + 1;

        int len_start = Month.of(month_start)
                .length(Year.of(year_start).isLeap());
        int dom_start = c.get(Calendar.DAY_OF_MONTH);

        c.setTimeInMillis(b);
        int year_end = c.get(Calendar.YEAR);
        int month_end = c.get(Calendar.MONTH) + 1;

        int len_end = Month.of(month_end)
                .length(Year.of(year_end).isLeap());
        int dom_end = c.get(Calendar.DAY_OF_MONTH);

        long months = (year_end - year_start) * 12L;
        if (months > 0) { //for the first and last year we cant assume that they add up to 12 months
            // (dec 01 to jan 02 -> 1 year dif != 12 months
            months -= 12;
            //months to end of first year
            months += 12 - month_start - 1; //except the start month (might be fraction)
            //months from start of last year
            months += month_end - 1; //except the end month (might be running)
        }

        //if dom_end == len_end the whole month passed
        if (dom_end == len_end) {
            //pick length of next month for modulo-ing start days
            //this might be relevant if start month passed 30 of 31 days and
            // the next month contains less (february)
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.add(Calendar.MONTH, 1);
            len_end = Month.of(c.get(Calendar.MONTH) + 1)
                    .length(Year.of(c.get(Calendar.YEAR)).isLeap());
            dom_end = 0;//nothing to count
            months++;
        }
        int days = dom_end +
                (dom_start > 1 ? len_start - dom_start : 0); //if dom_start == 1 the whole month passed
        if (days > len_end) months++;
        return months;
    }

    public int getMonthsModulo() {
        return (int) (getMonths() % 12);
    }

    public int getYears() {
        return (int) (getMonths() / 12);
    }

    @Override
    public String toString() {
        int[] values = {getYears(), getMonthsModulo(), getDaysModulo(), getHoursModulo(), getMinutesModulo(), getSecondsModulo(), getMillisModulo()};
        String[][] units = {
                {"year", "years"},
                {"month", "months"},
                {"day", "days"},
                {"hour", "hours"},
                {"minute", "minutes"},
                {"second", "seconds"},
                {"millisecond", "milliseconds"}
        };
        String glue = ", ";
        String glueLast = " and ";
        List<String> timeunits = new LinkedList<>();
        int first = 0;
        int last = values.length;
        for (int i = 0; i < values.length - 2; i++) //always show at least the last 2
            if (values[i] > 0) {
                first = i;
                break;
            }
        for (int i = values.length - 1; i > first; i--) //always show at least the last 2
            if (values[i] > 0) {
                last = i + 1;
                break;
            }
        for (int i = first; i < last; i++)
            timeunits.add(String.format("%d %s", values[i], units[i][values[i] == 1 ? 0 : 1]));
        StringBuilder result = new StringBuilder();
        for (; timeunits.size() > 0; ) {
            if (result.length() > 0) {
                result.append(timeunits.size() > 1 ? glue : glueLast);
            }
            result.append(timeunits.remove(0));
        }
        return result.toString();
    }

    public String toShortString() {
        int[] values = {getYears(), getMonthsModulo(), getDaysModulo(), getHoursModulo(), getMinutesModulo(), getSecondsModulo(), getMillisModulo()};
        String[] units = {
                "Y",
                "M",
                "D",
                "h",
                "m",
                "s",
                "ms"
        };
        String glue = ", ";
        List<String> timeunits = new LinkedList<>();
        int first = 0;
        int last = values.length;
        for (int i = 0; i < values.length - 2; i++) //always show at least the last 2
            if (values[i] > 0) {
                first = i;
                break;
            }
        for (int i = values.length - 1; i > first; i--) //always show at least the last 2
            if (values[i] > 0) {
                last = i + 1;
                break;
            }
        for (int i = first; i < last; i++)
            timeunits.add(String.format("%d %s", values[i], units[i]));
        StringBuilder result = new StringBuilder();
        for (; timeunits.size() > 0; ) {
            if (result.length() > 0) {
                result.append(glue);
            }
            result.append(timeunits.remove(0));
        }
        return result.toString();
    }

    /**
     * @return the 3 biggest units in order after the first biggest non-0 unit
     */
    public String toProminentString() {
        int[] values = {getYears(), getMonthsModulo(), getDaysModulo(), getHoursModulo(), getMinutesModulo(), getSecondsModulo(), getMillisModulo()};
        String[][] units = {
                {"year", "years"},
                {"month", "months"},
                {"day", "days"},
                {"hour", "hours"},
                {"minute", "minutes"},
                {"second", "seconds"},
                {"millisecond", "milliseconds"}
        };
        String glue = ", ";
        String glueLast = " and ";
        List<String> timeunits = new LinkedList<>();
        int first = 0;
        int last;
        for (int i = 0; i < values.length - 3; i++) //always show at least the last 2
            if (values[i] > 0) {
                first = i;
                break;
            }
        last = first + 3;
        for (int i = first; i < last; i++)
            timeunits.add(String.format("%d %s", values[i], units[i][values[i] == 1 ? 0 : 1]));
        StringBuilder result = new StringBuilder();
        for (; timeunits.size() > 0; ) {
            if (result.length() > 0) {
                result.append(timeunits.size() > 1 ? glue : glueLast);
            }
            result.append(timeunits.remove(0));
        }
        return result.toString();
    }
}
