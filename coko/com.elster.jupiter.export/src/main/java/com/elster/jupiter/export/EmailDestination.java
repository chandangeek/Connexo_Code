/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EmailDestination extends DataExportDestination {

    String TYPE_IDENTIFIER = "EMAIL";

    String getRecipients();

    String getFileName();

    String getFileExtension();

    String getSubject();

    void setRecipients(String recipients);

    void setSubject(String subject);

    void setAttachmentExtension(String fileExtension);

    void setAttachmentName(String fileName);
}
