package com.energyict.protocols.util;

import java.io.IOException;
import java.util.List;

public interface EventMapper {

    List map2MeterEvent(String event) throws IOException;

}
