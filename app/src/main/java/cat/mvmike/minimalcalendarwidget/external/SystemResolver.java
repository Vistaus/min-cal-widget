package cat.mvmike.minimalcalendarwidget.external;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.RemoteViews;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import cat.mvmike.minimalcalendarwidget.R;
import cat.mvmike.minimalcalendarwidget.service.dto.InstanceDto;
import cat.mvmike.minimalcalendarwidget.service.enums.Colour;
import cat.mvmike.minimalcalendarwidget.service.enums.ConfigurableItem;
import cat.mvmike.minimalcalendarwidget.service.enums.DayOfWeek;

@SuppressWarnings({
    "PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal",
    "PMD.AvoidSynchronizedAtMethodLevel",
    "PMD.AvoidUsingVolatile"
})
public class SystemResolver {

    private static final Set<Locale> SUPPORTED_LOCALES = new HashSet<>(Arrays.asList(
        Locale.ENGLISH,
        new Locale("ca", "ES"),
        new Locale("es", "ES"),
        new Locale("ru", "RU")
    ));

    private static final Clock CLOCK_UTC_TZ = Clock.systemUTC();

    private static final Clock CLOCK_SYS_TZ = Clock.systemDefaultZone();

    private static volatile SystemResolver instance;

    private SystemResolver() {
        // only purpose is to defeat external instantiation
    }

    public static synchronized SystemResolver get() {

        if (instance == null) {
            instance = new SystemResolver();
        }
        return instance;
    }

    // LOCALE

    public Locale getSafeLocale(final Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        return SUPPORTED_LOCALES.contains(locale) ? locale : Locale.ENGLISH;
    }

    // CLOCK

    public Instant getInstant() {
        return CLOCK_UTC_TZ.instant();
    }

    public LocalDate getSystemLocalDate() {
        return LocalDate.now(CLOCK_SYS_TZ);
    }

    public LocalDateTime getSystemLocalDateTime() {
        return LocalDateTime.now(CLOCK_SYS_TZ);
    }

    // CALENDAR CONTRACT

    public Set<InstanceDto> getInstances(final Context context, final long begin, final long end) {

        Cursor instanceCursor = CalendarContract.Instances.query(context.getContentResolver(), InstanceDto.FIELDS, begin, end);

        if (instanceCursor == null || instanceCursor.getCount() == 0) {
            return null;
        }

        Set<InstanceDto> instances = new HashSet<>();
        while (instanceCursor.moveToNext()) {

            instances.add(new InstanceDto(
                instanceCursor.getLong(0),
                instanceCursor.getLong(1),
                instanceCursor.getInt(2),
                instanceCursor.getInt(3)
            ));
        }

        instanceCursor.close();
        return instances;
    }

    // CONTEXT COMPAT

    public boolean isReadCalendarPermitted(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    // MONTH YEAR HEADER

    public void createMonthYearHeader(final RemoteViews widgetRemoteView, final String monthAndYear, final float headerRelativeYearSize) {

        SpannableString ss = new SpannableString(monthAndYear);
        ss.setSpan(new RelativeSizeSpan(headerRelativeYearSize), monthAndYear.length() - 4, monthAndYear.length(), 0);

        widgetRemoteView.setTextViewText(R.id.month_year_label, ss);
    }

    // DAY HEADER

    public RemoteViews createHeaderRow(final Context context) {
        return getById(context, R.layout.row_header);
    }

    public void addHeaderDayToHeader(final Context context, final RemoteViews headerRowRv, final String text, final int layoutId) {

        RemoteViews dayRv = getById(context, layoutId);
        dayRv.setTextViewText(android.R.id.text1, text);

        headerRowRv.addView(R.id.row_container, dayRv);
    }

    public void addHeaderRowToWidget(final RemoteViews widgetRv, final RemoteViews headerRowRv) {
        widgetRv.addView(R.id.calendar_widget, headerRowRv);
    }

    // DAY

    public RemoteViews createRow(final Context context) {
        return getById(context, R.layout.row_week);
    }

    public RemoteViews createDay(final Context context, final int specificDayLayout) {
        return getById(context, specificDayLayout);
    }

    public void addRowToWidget(final RemoteViews widgetRv, final RemoteViews rowRv) {
        widgetRv.addView(R.id.calendar_widget, rowRv);
    }

    public int getColorInstancesTodayId(final Context context) {
        return ContextCompat.getColor(context, R.color.instances_today);
    }

    public int getColorInstancesId(final Context context, final Colour colour) {
        return ContextCompat.getColor(context, colour.getHexValue());
    }

    public void addDayCellRemoteView(final RemoteViews rowRv, final RemoteViews cellRv, final String spanText, final boolean isToday,
                                     final float symbolRelativeSize, final int instancesColor) {

        SpannableString daySpSt = new SpannableString(spanText);
        daySpSt.setSpan(new StyleSpan(Typeface.BOLD), spanText.length() - 1, spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (isToday) {
            daySpSt.setSpan(new StyleSpan(Typeface.BOLD), 0, spanText.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        daySpSt.setSpan(new ForegroundColorSpan(instancesColor), spanText.length() - 1, spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        daySpSt.setSpan(new RelativeSizeSpan(symbolRelativeSize), spanText.length() - 1, spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        cellRv.setTextViewText(android.R.id.text1, daySpSt);

        rowRv.addView(R.id.row_container, cellRv);
    }

    // TRANSLATIONS

    public String[] getDayOfWeekTranslatedValues(final Context context) {
        return new String[]{
            ConfigurableItem.getDisplayValue(context.getString(R.string.monday)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.tuesday)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.wednesday)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.thursday)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.friday)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.saturday)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.sunday))
        };
    }

    public String getAbbreviatedDayOfWeekTranslated(final Context context, final DayOfWeek dayOfWeek) {

        switch (dayOfWeek) {
            case MONDAY:
                return context.getString(R.string.monday_abb).toUpperCase(Locale.ENGLISH);
            case TUESDAY:
                return context.getString(R.string.tuesday_abb).toUpperCase(Locale.ENGLISH);
            case WEDNESDAY:
                return context.getString(R.string.wednesday_abb).toUpperCase(Locale.ENGLISH);
            case THURSDAY:
                return context.getString(R.string.thursday_abb).toUpperCase(Locale.ENGLISH);
            case FRIDAY:
                return context.getString(R.string.friday_abb).toUpperCase(Locale.ENGLISH);
            case SATURDAY:
                return context.getString(R.string.saturday_abb).toUpperCase(Locale.ENGLISH);
            case SUNDAY:
                return context.getString(R.string.sunday_abb).toUpperCase(Locale.ENGLISH);
        }
        return null;
    }

    public String[] getThemeTranslatedValues(final Context context) {
        return new String[]{
            ConfigurableItem.getDisplayValue(context.getString(R.string.black)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.grey)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.white))
        };
    }

    public String[] getInstancesSymbolsTranslatedValues(final Context context) {
        return new String[]{
            ConfigurableItem.getDisplayValue(context.getString(R.string.minimal)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.vertical)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.circles)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.numbers)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.roman)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.binary))
        };
    }

    public String[] getInstancesSymbolsColourTranslatedValues(final Context context) {
        return new String[]{
            ConfigurableItem.getDisplayValue(context.getString(R.string.cyan)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.mint)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.blue)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.green)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.yellow)),
            ConfigurableItem.getDisplayValue(context.getString(R.string.white))
        };
    }

    // INTERNAL UTILS

    private static RemoteViews getById(final Context context, final int layoutId) {
        return new RemoteViews(context.getPackageName(), layoutId);
    }
}
