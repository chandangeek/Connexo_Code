/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface BpmProcessPrivilege {

    String getPrivilegeName();

    void setPrivilegeName(String privilegeName);

    String getApplication();

    void setApplication(String application);

    long getProcessId();

    void setProcessId(long processId);

    void persist();

    void delete();
}
