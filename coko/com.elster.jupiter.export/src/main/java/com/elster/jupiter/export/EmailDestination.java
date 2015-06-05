package com.elster.jupiter.export;

import java.util.List;

public interface EmailDestination extends DataExportDestination {

    String TYPE_IDENTIFIER = "EMAIL";

    void send(List<FormattedExportData> data);

    String getRecipients();

    String getFileName();

    String getFileExtension();

    String getSubject();

    void setRecipients(String recipients);

    void setSubject(String subject);

    void setAttachmentExtension(String fileExtension);

    void setAttachmentName(String fileName);
}
