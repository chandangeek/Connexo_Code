package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 19/02/14
 * Time: 9:12
 * To change this template use File | Settings | File Templates.
 */
public interface UsagePointDetail extends Effectivity {

    Optional<Boolean> getCollar();

    void setCollar(Optional<Boolean> collar);

    boolean isCurrent();

    boolean conflictsWith(UsagePointDetail other);

    UsagePoint getUsagePoint();

    void update();
}
