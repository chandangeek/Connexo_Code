package com.elster.jupiter.metering;

import com.elster.jupiter.util.YesNoAnswer;

public interface UsagePointDetailBuilder {

    UsagePointDetail create();

    UsagePointDetailBuilder withCollar(YesNoAnswer collar);

    void validate();
}
