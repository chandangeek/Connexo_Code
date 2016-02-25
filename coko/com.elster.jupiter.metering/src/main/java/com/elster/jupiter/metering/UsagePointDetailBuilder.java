package com.elster.jupiter.metering;

import java.util.Optional;

public interface UsagePointDetailBuilder {

    UsagePointDetail create();

    UsagePointDetailBuilder withCollar(Boolean collar);

    Optional<Boolean> getCollar();

    void validate();
}
