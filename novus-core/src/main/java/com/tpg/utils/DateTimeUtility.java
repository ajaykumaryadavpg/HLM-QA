package com.tpg.utils;

import com.github.javafaker.service.RandomService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.text.WordUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtility {

    public static final String ERROR_MESSAGE = "error parsing date";

    public static final DateTimeFormatter EEEE_MMMM_D_YYYY_H_MM_A_WITH_COMMAS = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a");
    /**
     * Example: {@code Thursday August 06 2020 02:28:21 EDT}
     */
    public static final DateTimeFormatter EEE_MMM_DD_HH_MM_SS_ZZZ_YYYY_WITHOUT_COMMAS = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
    /**
     * Example: {@code Thu Jan 30 00:00:00 IST 1964}
     */
    public static final DateTimeFormatter EEEE_MMMM_DD_YYYY_HH_MM_A_WITH_COMMAS = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy hh:mm a");
    /**
     * Example: {@code Thursday August 6 2020 2:28 PM}
     */
    public static final DateTimeFormatter EEEE_MMMM_D_YYYY_H_MM_A_WITHOUT_COMMAS = DateTimeFormatter.ofPattern("EEEE MMMM d yyyy h:mm a");
    /**
     * Example: {@code Thursday August 06 2020 02:28 PM}
     */
    public static final DateTimeFormatter EEEE_MMMM_DD_YYYY_HH_MM_A_WITHOUT_COMMAS = DateTimeFormatter.ofPattern("EEEE MMMM dd yyyy hh:mm a");
    /**
     * Example: {@code September 1, 1939 03:22 AM}
     */
    public static final DateTimeFormatter FULL_DAY_WITH_TIME = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm:ss a");
    /**
     * Example: {@code 06/02/2023 3:15PM EDT}
     */
    public static final DateTimeFormatter MMDDYYYY_H_MMA_WITH_SLASHES = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mma");
    /**
     * Example: {@code 06/02/2023 3:15 PM}
     */
    public static final DateTimeFormatter MMDDYYYY_HH_MM_A_WITH_SLASHES = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    /**
     * Example: {@code September 28, 2023 12:48:17 AM}
     */
    public static final DateTimeFormatter MMMMDDYYYY_HH_MM_SS_A = DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mm:ss a");
    /**
     * Example: {@code 2018-04-20--17.33.22}
     */
    public static final DateTimeFormatter DATE_TIME_FILENAME_SAFE = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH.mm.ss");
    /**
     * Example: {@code 2014-01-30}
     */
    public static final DateTimeFormatter YYYYMMDD_WITH_DASHES = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * Example: {@code 01-30-2014}
     */
    public static final DateTimeFormatter MMDDYYYY_WITH_DASHES = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    /**
     * Example: {@code 01-30-14}
     */
    public static final DateTimeFormatter MMDDYY__WITH_DASHES = DateTimeFormatter.ofPattern("MM-dd-yy");
    /**
     * Example: {@code 01/30/2014}
     */
    public static final DateTimeFormatter MMDDYYYY_WITH_SLASHES = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    /**
     * Example: {@code 1/30/2014}
     */
    public static final DateTimeFormatter MDDYYYY_WITH_SLASHES = DateTimeFormatter.ofPattern("M/dd/yyyy");
    /**
     * Example: {@code 01/30/14}
     */
    public static final DateTimeFormatter MMDDYY_WITH_SLASHES = DateTimeFormatter.ofPattern("MM/dd/yy");
    /**
     * Example: {@code 1/30/14}
     */
    public static final DateTimeFormatter MDYY_WITH_SLASHES = DateTimeFormatter.ofPattern("M/d/yy");
    /**
     * Example: {@code 1/30/2014}
     */
    public static final DateTimeFormatter MDYYYY_WITH_SLASHES = DateTimeFormatter.ofPattern("M/d/yyyy");
    /**
     * Example: {@code January 3, 2014}
     */
    public static final DateTimeFormatter MMMMDYYYY_COMMA_YEAR = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    /**
     * Example: {@code January 03, 2014}
     */
    public static final DateTimeFormatter MMMMDDYYYY_COMMA_YEAR = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    /**
     * Example: {@code January 3 2014}
     */
    public static final DateTimeFormatter MMMMDYYYY_NOCOMMA_YEAR = DateTimeFormatter.ofPattern("MMMM d yyyy");
    /**
     * Example: {@code January 03 2014}
     */
    public static final DateTimeFormatter MMMMDDYYYY_NOCOMMA_YEAR = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    /**
     * Example: {@code Jan 3, 2014}
     */
    public static final DateTimeFormatter MMMDYYYY_COMMA_YEAR = DateTimeFormatter.ofPattern("MMM d, yyyy");
    /**
     * Example: {@code Jan 3 2014}
     */
    public static final DateTimeFormatter MMMDYYYY_NOCOMMA_YEAR = DateTimeFormatter.ofPattern("MMM d yyyy");
    /**
     * Example: {@code Jan 30, 2014}
     */
    public static final DateTimeFormatter MMMDDYYYY_COMMA_YEAR = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    /**
     * Example: {@code Jan 30 2014}
     */
    public static final DateTimeFormatter MMMDDYYYY_NOCOMMA_YEAR = DateTimeFormatter.ofPattern("MMM dd yyyy");
    /**
     * Example: {@code 01 30, 2014}
     */
    public static final DateTimeFormatter MMDDYYYY = DateTimeFormatter.ofPattern("MM dd, yyyy");
    /**
     * Example: {@code 1:10 PM}
     */
    public static final DateTimeFormatter TIME_12HR = DateTimeFormatter.ofPattern("h:mm a");
    /**
     * Example: {@code 13:10}
     */
    public static final DateTimeFormatter TIME_24HR = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * Example: {@code 2015-09-10T12:05:00-0400}
     */
    public static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    /**
     * Example: {@code 2015-09-16T04:00:00.000Z}
     */
    public static final DateTimeFormatter ISO_DATE_DECIMAL_SECONDS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    /**
     * Example: {@code 20180518}
     */
    public static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
     * Example: {@code 2018}
     */
    public static final DateTimeFormatter FOUR_DIGIT_YEAR = DateTimeFormatter.ofPattern("yyyy");
    /**
     * Example: {@code 18}
     */
    public static final DateTimeFormatter TWO_DIGIT_YEAR = DateTimeFormatter.ofPattern("yy");

    private static final List<DateTimeFormatter> dateFormats = List.of(
        DateTimeFormatter.ofPattern("MM dd, yyyy"),   // 01 01, 2017
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),              // 01-01-2017
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),              // 2017-01-01
        DateTimeFormatter.ofPattern("yyyy-MM-dd'Z'"),           // 2017-01-01Z
        DateTimeFormatter.ofPattern("MM-dd-yy"),                // 01-01-17
        DateTimeFormatter.ofPattern("MM/dd/yy"),                // 01/01/17
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),              // 01/01/2017
        DateTimeFormatter.ofPattern("M/d/yy"),                  // 1/1/17
        DateTimeFormatter.ofPattern("M/d/yyyy"),                // 1/1/2017
        DateTimeFormatter.ofPattern("MMM d, yyyy"),             // Jan 1, 2017
        DateTimeFormatter.ofPattern("MMM dd, yyyy"),            // Jan 01, 2017
        DateTimeFormatter.ofPattern("MMM dd yyyy"),             // Jan 01 2017
        DateTimeFormatter.ofPattern("MMMM dd, yyyy"),           // January 01, 2017
        DateTimeFormatter.ofPattern("MMMM dd yyyy"),            // January 01 2017
        DateTimeFormatter.ofPattern("MMMM d, yyyy"),            // January 1, 2017
        DateTimeFormatter.ofPattern("MMddyyyy"),                // 01012017
        DateTimeFormatter.ofPattern("yyyy-MM-dd'z'"),           // 2017-01-01z
        DateTimeFormatter.ofPattern("yyyyMMdd")                 // 20180518
    );
    private static final List<DateTimeFormatter> timeFormats = List.of(
        DateTimeFormatter.ofPattern("hh:mm a"),      // 01:01 PM
        DateTimeFormatter.ofPattern("hh:mm"),        // 01:01
        DateTimeFormatter.ofPattern("h:mm a"),       // 1:01 PM
        DateTimeFormatter.ofPattern("h:mm:ss a"),    // 1:01:01 PM
        DateTimeFormatter.ofPattern("HH:mm:ss")      // 13:01:01
    );
    private static final List<DateTimeFormatter> dateTimeFormats = List.of(
        DateTimeFormatter.ofPattern("MMMM dd yyyy hh:mm:ss a"),   // January 01, 2017 01:01:01 PM and January 01, 2017, 01:01:01 PM
        DateTimeFormatter.ofPattern("MMMM dd yyyy hh:mm a"),                // January 01, 2017 01:01 PM and January 01, 2017, 01:01 PM
        DateTimeFormatter.ofPattern("MMMM dd yyyy h:mm a"),                 // January 01, 2017 01:01 PM and January 01, 2017, 01:01 PM
        DateTimeFormatter.ofPattern("MMMM dd yyyy h:mm:ss a"),              // January 01, 2017 1:01:01 PM and January 01, 2017, 1:01:01 PM
        DateTimeFormatter.ofPattern("MMMM d yyyy hh:mm:ss a"),              // January 1, 2017 01:01:01 PM and January 1, 2017, 01:01:01 PM
        DateTimeFormatter.ofPattern("MMMM d yyyy hh:mm a"),                 // January 1, 2017 01:01 PM and January 1, 2017, 01:01 PM
        DateTimeFormatter.ofPattern("MMMM d yyyy h:mm a"),                  // January 1, 2017 01:01 PM and January 1, 2017, 01:01 PM
        DateTimeFormatter.ofPattern("MMMM d yyyy h:mm:ss a"),               // January 1, 2017 1:01:01 PM and January 1, 2017, 1:01:01 PM
        DateTimeFormatter.ofPattern("MMM dd yyyy h:mm a"),                  // Jan 01, 2017, 1:01 PM
        DateTimeFormatter.ofPattern("EEEE MM/d/yyyy hh:mm a"),              // Tuesday 01/1/2017 01:01 PM
        DateTimeFormatter.ofPattern("EEEE MMMM dd yyyy hh:mm:ss a"),        // Tuesday January 01, 2017 01:01:01 PM and Tuesday January 01, 2017, 01:01:01 PM
        EEEE_MMMM_DD_YYYY_HH_MM_A_WITHOUT_COMMAS,                           // Thursday, January 01, 2017 01:01 AM
        DateTimeFormatter.ofPattern("EEEE MMMM dd yyyy h:mm:ss a"),         // Tuesday January 01, 2017 1:01:01 PM and Tuesday January 01, 2017, 1:01:01 PM
        DateTimeFormatter.ofPattern("EEEE MMMM dd yyyy HH:mm:ss"),          // Tuesday January 01, 2017 13:01:01 and Tuesday January 01, 2017, 13:01:01
        DateTimeFormatter.ofPattern("EEEE MMMM d yyyy hh:mm:ss a"),         // Tuesday January 1, 2017 1:01:01 PM and Tuesday January 1, 2017, 1:01:01 PM
        EEEE_MMMM_D_YYYY_H_MM_A_WITHOUT_COMMAS,                             // Thursday, January 1, 2017 1:01 AM
        DateTimeFormatter.ofPattern("EEEE MMMM d yyyy h:mm:ss a"),          // Tuesday January 1, 2017 01:01:01 PM and Tuesday January 1, 2017, 01:01:01 PM
        DateTimeFormatter.ofPattern("EEEE MMMM d yyyy h:mm a"),             // Tuesday January 1, 2017 01:01:01 PM and Tuesday January 1, 2017, 01:01:01 PM
        DateTimeFormatter.ofPattern("EEEE MMMM d yyyy HH:mm:ss"),           // Tuesday January 1, 2017 13:01:01 and Tuesday January 1, 2017, 13:01:01
        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy"),          // Tue Jan 1 13:01:01 GMT 2017 << Headless chrome displays this on visit overview
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ssZ"),              // 2017-01-01T13:01:01+0000
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.S'Z'"),          // 2017-01-01T13:01:01Z
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SZ"),            // 2017-01-01T13:01:01.1234+0000
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),                    // 01/01/2017 13:23
        DateTimeFormatter.ofPattern("MM/dd/yy h:mm a"),                     // 01/01/17 1:23 AM
        EEE_MMM_DD_HH_MM_SS_ZZZ_YYYY_WITHOUT_COMMAS                         // Sat Sep 11 00:00:00 EDT 1971
    );

    /**
     * Takes the date specified and then parses it against all the different variations of date in {@link #dateFormats} if it cannot find one, throw an exception
     *
     * @param date a String of the date to parse
     * @return a LocalDate of the parsed string
     */
    public static LocalDate parseDate(String date) {
        final var cleanDate = cleanDateTimeString(date);
        return dateFormats.stream()
            .map(fmt -> tryParseDate(cleanDate, fmt))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new DateTimeException(ERROR_MESSAGE));
    }

    /**
     * Takes the time specified and then parses it against all the different variations of time in {@link #timeFormats} if it cannot find one, throw an exception
     *
     * @param time a String of the time to parse
     * @return a LocalTime of the parsed string
     */
    public static LocalTime parseTime(String time) {
        final var upper = time.toUpperCase();
        return timeFormats.stream()
            .map(fmt -> tryParseTime(upper, fmt))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new DateTimeException(ERROR_MESSAGE));
    }

    /**
     * Takes the dateTime specified and then parses it against all the different variations of time in {@link #dateTimeFormats} if it cannot find one, throw an exception
     *
     * @param dateTime a String of the date time string to parse
     * @return a LocalDateTime of the parsed string
     */
    public static LocalDateTime parseDateTime(String dateTime) {
        final var cleanDateTime = cleanDateTimeString(dateTime);
        return dateTimeFormats.stream()
            .map(fmt -> tryParseDateTime(cleanDateTime, fmt))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new DateTimeException(ERROR_MESSAGE));
    }

    private static String cleanDateTimeString(String dateTime) {
        return Pattern.compile("(Am|Pm|Gmt|Est|Edt)")
            .matcher(WordUtils.capitalize(dateTime.trim().toLowerCase()))
            .replaceAll(m -> m.group().toUpperCase())
            .replace(",", "");
    }

    private static Optional<LocalTime> tryParseTime(String toParse, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalTime.parse(toParse, formatter));
        } catch (Exception dateException) {
            return Optional.empty();
        }
    }

    private static Optional<LocalDate> tryParseDate(String toParse, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDate.parse(toParse, formatter));
        } catch (Exception dateException) {
            return Optional.empty();
        }
    }

    private static Optional<LocalDateTime> tryParseDateTime(String toParse, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDateTime.parse(toParse, formatter));
        } catch (Exception dateException) {
            return Optional.empty();
        }
    }

    public static LocalDate randomDate() {
        return LocalDate.now().minusYears(new RandomService().nextInt(20, 30)).minusDays(new RandomService().nextInt(1, 30));
    }

    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }
}
