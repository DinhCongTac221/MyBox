package mara.mybox.data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
/*
   "1970-01-01 08:00:00 AD" = 0
   "0 AD" = "1 BC" = "0" = "-0" = "0000" = "-0000" = "0001-01-01 00:00:00 BC" = -62167420800000
   "1 AD" = "1" = "0001" = "0001-01-01 00:00:00" = "0001-01-01 00:00:00 AD" = -62135798400000
   "202 BC" = "-203" = "-0203" = "-0203-01-01 00:00:00" = "0202-01-01 00:00:00 BC" = -68510476800000
   "202 AD" = "202" = "0202" = "0202-01-01 00:00:00" = "0202-01-01 00:00:00 AD" = -55792742400000
 */
public class Era {

    protected long value = AppValues.InvalidLong;
    protected Format format = Format.Datetime;
    protected boolean ignoreAD = true;

    public static enum Format {
        Datetime, Date, Year, Month, Time, TimeMs, DatetimeMs, DatetimeZone, DatetimeMsZone
    }

    public Era(long value) {
        this.value = value;
    }

    public Era(long value, Format format, boolean ignoreAD) {
        this.value = value;
        this.format = format;
        this.ignoreAD = ignoreAD;
    }

    public Era(String s) {
        Date d = DateTools.encodeEra(s);
        if (d != null) {
            value = d.getTime();
        }
    }

    public String text() {
        if (value == AppValues.InvalidLong) {
            return null;
        }
        return DateTools.textEra(value, format, ignoreAD);
    }

    public int formatValue() {
        return format(this.format);
    }

    /*
        static methods
     */
    public static Map<Object, String> values() {
        Map<Object, String> values = new LinkedHashMap<>();
        for (Format format : Format.values()) {
            values.put(format(format), Languages.message(format.name()));
        }
        return values;
    }

    public static Format format(short value) {
        switch (value) {
            case 2:
                return Format.Date;
            case 3:
                return Format.Year;
            case 4:
                return Format.Month;
            case 5:
                return Format.Time;
            case 6:
                return Format.TimeMs;
            case 7:
                return Format.DatetimeMs;
            case 8:
                return Format.DatetimeZone;
            case 9:
                return Format.DatetimeMsZone;
            default:
                return Format.Datetime;
        }
    }

    public static short format(Format format) {
        if (format == null) {
            return 0;
        }
        switch (format) {
            case Date:
                return 2;
            case Year:
                return 3;
            case Month:
                return 4;
            case Time:
                return 5;
            case TimeMs:
                return 6;
            case DatetimeMs:
                return 7;
            case DatetimeZone:
                return 8;
            case DatetimeMsZone:
                return 9;
            default:
                return 0;
        }
    }

    /*
        get/set
     */
    public long getValue() {
        return value;
    }

    public Era setValue(long value) {
        this.value = value;
        return this;
    }

    public Format getFormat() {
        return format;
    }

    public Era setFormat(Format format) {
        this.format = format;
        return this;
    }

    public boolean isIgnoreAD() {
        return ignoreAD;
    }

    public Era setIgnoreAD(boolean ignoreAD) {
        this.ignoreAD = ignoreAD;
        return this;
    }

}
