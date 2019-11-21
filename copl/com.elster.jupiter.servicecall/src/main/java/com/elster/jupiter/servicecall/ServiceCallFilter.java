/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceCallFilter {
    public Set<Long> ids = new HashSet<>();
    public String reference;
    public List<String> types = new ArrayList<>();
    public List<String> states = new ArrayList<>();
    public Instant receivedDateFrom;
    public Instant receivedDateTo;
    public Instant modificationDateFrom;
    public Instant modificationDateTo;
    public ServiceCall parent;
    public Object targetObject;
    public String appKey;
}
