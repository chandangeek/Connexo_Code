package com.elster.jupiter.nls;

import com.elster.jupiter.util.HasName;

public interface HasTranslatableName extends HasName {

    String getTranslationKey();
}
