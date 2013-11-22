package com.energyict.mdc.common;

import java.util.Locale;

public interface MainComponent extends ApplicationComponent {

    public FormatPreferences getFormatPreferences();

    public BusinessEventManager createEventManager();

    public Locale getLocale();

}