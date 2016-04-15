package com.elster.jupiter.search.location;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface SearchLocationService {

    Map<Long, String> findLocations(String locationPart);

}