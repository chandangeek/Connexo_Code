package com.elster.jupiter.time;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by borunova on 01.10.2014.
 */
public interface RelativeDate {
    static String SEPARATOR = ";";
    ZonedDateTime getRelativeDate(ZonedDateTime referenceDate);
    void setPattern(List<RelativeOperation> operationList);

}
