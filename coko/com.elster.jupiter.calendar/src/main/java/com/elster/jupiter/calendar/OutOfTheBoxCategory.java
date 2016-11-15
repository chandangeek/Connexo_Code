package com.elster.jupiter.calendar;

import com.elster.jupiter.nls.Thesaurus;

public enum OutOfTheBoxCategory {

    TOU("TOU"), WORKFORCE("Workforce"), COMMANDS("Commands");

    private final String displayName;

    OutOfTheBoxCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDefaultDisplayName() {
        return displayName;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString("calendar.category." + this.name().toLowerCase(), getDefaultDisplayName());

    }
}
