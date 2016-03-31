package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.List;
import java.util.Optional;

/**
 * Adds behavior to the {@link MetrologyConfigurationService} interface
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-22 (08:48)
 */
public interface ServerMetrologyConfigurationService extends MetrologyConfigurationService {

    DataModel getDataModel();

    Thesaurus getThesaurus();

    MetrologyPurpose createMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose);

    Optional<MetrologyPurpose> findMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose);

    ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter createReadingTypeTemplate(DefaultReadingTypeTemplate defaultTemplate);

    Optional<Formula> findFormula(long id);

    List<Formula> findFormulas();

    FormulaBuilder newFormulaBuilder(Formula.Mode mode);

}