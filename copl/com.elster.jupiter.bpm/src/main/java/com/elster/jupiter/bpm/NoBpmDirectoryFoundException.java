package com.elster.jupiter.bpm;

import com.elster.jupiter.bpm.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NoBpmDirectoryFoundException extends LocalizedException {

    public NoBpmDirectoryFoundException(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.NO_BPM_NAME_FOUND, name);
    }
}
