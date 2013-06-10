package com.elster.jupiter.util;

import com.google.common.base.Function;

public enum To {
    ;

    public static Function<HasName, String> Name = new Function<HasName, String>() {
        @Override
        public String apply(HasName hasName) {
            return hasName == null ? null : hasName.getName();
        }
    };
}
