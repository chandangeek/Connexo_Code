package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.util.time.Clock;

public interface ServiceLocator {

    Clock getClock();
}
