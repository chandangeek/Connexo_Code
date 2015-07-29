package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class FileImportRecordContext {
    private Thesaurus thesaurus;
    private Logger logger;
    private boolean hasWarnings = false;
    private List<String> headers;

    FileImportRecordContext(Thesaurus thesaurus, Logger logger, List<String> headers) {
        this.thesaurus = thesaurus;
        this.logger = logger;
        this.headers = headers;
    }

    boolean hasWarnings() {
        return this.hasWarnings;
    }

    public void warning(TranslationKey message, Object... arguments) {
        if (!hasWarnings) {
            hasWarnings = true;
        }
        String msg = thesaurus.getString(message.getKey(), message.getDefaultFormat());
        if (arguments != null && arguments.length > 0) {
            msg = MessageFormat.format(msg, arguments);
        }
        logger.info(msg);
    }

    public List<String> getHeaders(){
        return Collections.unmodifiableList(this.headers);
    }

    public String getHeaderColumn(int position){
        if (position >= 0 && position < this.headers.size()){
            return this.headers.get(position);
        }
        return "#" + (position+1);
    }

    public String translate(String key){
        return this.thesaurus.getStringBeyondComponent(key, key);
    }
}
