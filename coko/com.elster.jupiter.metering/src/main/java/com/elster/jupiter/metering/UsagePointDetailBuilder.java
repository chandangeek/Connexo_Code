package com.elster.jupiter.metering;

import java.util.Optional;

public interface UsagePointDetailBuilder {

    UsagePointDetail build();

    UsagePointDetailBuilder withCollar(Boolean collar);

    Optional<Boolean> getCollar();

    void validate();
}
