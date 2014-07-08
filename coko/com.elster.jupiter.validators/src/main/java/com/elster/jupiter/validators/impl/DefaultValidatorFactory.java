package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validators.MessageSeeds;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(name = "com.elster.jupiter.validators.impl.DefaultValidatorFactory", service = {ValidatorFactory.class, InstallService.class}, property = "name=" + MessageSeeds.COMPONENT_NAME, immediate = true)
public class DefaultValidatorFactory implements ValidatorFactory, InstallService {

    public static final String THRESHOLD_VALIDATOR = "com.elster.jupiter.validators.impl.ThresholdValidator";
    private volatile Thesaurus thesaurus;

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public void install() {
        List<Translation> translations = new ArrayList<>(ValidatorDefinition.values().length);
        for (ValidatorDefinition validatorDefinition : ValidatorDefinition.values()) {
            IValidator validator = validatorDefinition.createTemplate(thesaurus);
            Translation translation = SimpleTranslation.translation(validator.getNlsKey(), Locale.ENGLISH, validator.getDefaultFormat());
            translations.add(translation);
            for (String key : validator.getRequiredKeys()) {
                translations.add(SimpleTranslation.translation(validator.getPropertyNlsKey(key), Locale.ENGLISH, validator.getPropertyDefaultFormat(key)));
            }
            for (String key : validator.getOptionalKeys()) {
                translations.add(SimpleTranslation.translation(validator.getPropertyNlsKey(key), Locale.ENGLISH, validator.getPropertyDefaultFormat(key)));
            }
        }
        thesaurus.addTranslations(translations);
    }

    private enum ValidatorDefinition {
        THRESHOLD(THRESHOLD_VALIDATOR) {
            @Override
            Validator create(Thesaurus thesaurus, Map<String, Quantity> props) {
                return new ThresholdValidator(thesaurus, props);
            }

            @Override
            IValidator createTemplate(Thesaurus thesaurus) {
                return new ThresholdValidator(thesaurus);
            }
        };

        private final String implementation;

        String getImplementation() {
            return implementation;
        }

        ValidatorDefinition(String implementation) {
            this.implementation = implementation;
        }

        abstract Validator create(Thesaurus thesaurus, Map<String, Quantity> props);
        abstract IValidator createTemplate(Thesaurus thesaurus);
    }

    @Override
    public List<String> available() {
        List<String> result = new ArrayList<>();
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            result.add(definition.getImplementation());
        }
        return result;
    }

    @Override
    public Validator create(String implementation, Map<String, Quantity> props) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.create(thesaurus, props);
            }
        }
        return null;
    }

    @Override
    public Validator createTemplate(String implementation) {
        for (ValidatorDefinition definition : ValidatorDefinition.values()) {
            if (definition.getImplementation().equals(implementation)) {
                return definition.createTemplate(thesaurus);
            }
        }
        return null;
    }
}
