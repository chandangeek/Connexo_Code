package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

public interface ServerMetrologyConfigurationService extends MetrologyConfigurationService {

    DataModel getDataModel();

    Thesaurus getThesaurus();

    ReadingTypeTemplate createReadingTypeTemplate(DefaultReadingTypeTemplate defaultTemplate);
}
