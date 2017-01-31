/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.whiteboard.ReferenceInfo;
import com.elster.jupiter.servicecall.rest.impl.ServiceCallCustomPropertySetInfo;
import com.elster.jupiter.servicecall.rest.impl.ServiceCallChildrenInfo;
import java.util.List;

public class ServiceCallInfo {
    public long id;
    public String name;
    public long version;
    public long creationTime;
    public long lastModificationTime;
    public long lastCompletedTime;
    public IdWithDisplayValueInfo<String> state;
    public String origin;
    public String externalReference;
    public ReferenceInfo targetObject;
    public List<IdWithNameInfo> parents;
    public List<ServiceCallChildrenInfo> children;
    public String type;
    public long typeId;
    public long numberOfChildren;
    public List<ServiceCallCustomPropertySetInfo> customPropertySets;
    public boolean canCancel;

    public ServiceCallInfo() {

    }

}
