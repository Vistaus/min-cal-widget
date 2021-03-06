package cat.mvmike.minimalcalendarwidget;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.TimeZone;

import cat.mvmike.minimalcalendarwidget.external.SystemResolver;
import cat.mvmike.minimalcalendarwidget.service.enums.Colour;
import cat.mvmike.minimalcalendarwidget.service.enums.DayOfWeek;
import cat.mvmike.minimalcalendarwidget.service.enums.Symbol;
import cat.mvmike.minimalcalendarwidget.service.enums.Theme;

import static cat.mvmike.minimalcalendarwidget.service.enums.Colour.CYAN;
import static cat.mvmike.minimalcalendarwidget.service.enums.ConfigurableItem.FIRST_DAY_OF_WEEK;
import static cat.mvmike.minimalcalendarwidget.service.enums.ConfigurableItem.INSTANCES_SYMBOLS;
import static cat.mvmike.minimalcalendarwidget.service.enums.ConfigurableItem.INSTANCES_SYMBOLS_COLOUR;
import static cat.mvmike.minimalcalendarwidget.service.enums.ConfigurableItem.THEME;
import static cat.mvmike.minimalcalendarwidget.service.enums.DayOfWeek.MONDAY;
import static cat.mvmike.minimalcalendarwidget.service.enums.Symbol.MINIMAL;
import static cat.mvmike.minimalcalendarwidget.service.enums.Theme.BLACK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public abstract class BaseTest {

    private static final String PREFERENCES_ID = "mincal_prefs";

    protected final Context context = mock(Context.class);

    protected final SharedPreferences sharedPreferences = mock(SharedPreferences.class);

    protected final SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);

    protected final SystemResolver systemResolver = mock(SystemResolver.class);

    @BeforeAll
    static void beforeAll() {

        // force a different timezone than UTC for testing
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
    }

    protected static void mockTheme(final SharedPreferences sharedPreferences, final Theme theme) {
        when(sharedPreferences.getString(THEME.key(), BLACK.name())).thenReturn(theme.name());
    }

    protected static void mockStartWeekDay(final SharedPreferences sharedPreferences, final DayOfWeek dayOfWeek) {
        when(sharedPreferences.getString(FIRST_DAY_OF_WEEK.key(), MONDAY.name())).thenReturn(dayOfWeek.name());
    }

    protected static void mockInstancesSymbolsColour(final SharedPreferences sharedPreferences, final Colour colour) {
        when(sharedPreferences.getString(INSTANCES_SYMBOLS_COLOUR.key(), CYAN.name())).thenReturn(colour.name());
    }

    protected static void mockInstancesSymbols(final SharedPreferences sharedPreferences, final Symbol symbol) {
        when(sharedPreferences.getString(INSTANCES_SYMBOLS.key(), MINIMAL.name())).thenReturn(symbol.name());
    }

    @BeforeEach
    void beforeEach() {

        reset(context, sharedPreferences, editor);

        when(context.getSharedPreferences(PREFERENCES_ID, Context.MODE_PRIVATE)).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.clear()).thenReturn(editor);
        when(editor.commit()).thenReturn(true);

        try {
            Field instance = SystemResolver.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(instance, systemResolver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
