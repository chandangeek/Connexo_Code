package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validators.MessageSeeds;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DefaultValidatorFactoryTest {
    public static final Quantity MINIMUM = Quantity.create(BigDecimal.valueOf(1000L), 1, "Wh");
    public static final Quantity MAXIMUM = Quantity.create(BigDecimal.valueOf(5000L), 1, "Wh");
    private DefaultValidatorFactory defaultValidatorFactory;

    @Before
    public void setUp() throws Exception {
        defaultValidatorFactory = new DefaultValidatorFactory();
        defaultValidatorFactory.setPropertySpecService(new PropertySpecServiceImpl());
    }

    @Test
    public void testAvailable() {
        List<String> available = defaultValidatorFactory.available();
        assertThat(available).contains(ThresholdValidator.class.getName());
    }

    @Test
    public void testCreateThresholdValidator() {
        ImmutableMap<String, Object> properties = ImmutableMap.of("minimum", (Object) MINIMUM, "maximum", MAXIMUM);

        Validator validator = defaultValidatorFactory.create(ThresholdValidator.class.getName(), properties);

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test
    public void testCreateThresholdValidatorTemplate() {
        Validator validator = defaultValidatorFactory.createTemplate(ThresholdValidator.class.getName());

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUnavailableValidator() {
        ImmutableMap<String, Object> properties = ImmutableMap.of("minimum", (Object) MINIMUM, "maximum", MAXIMUM);

        Validator validator = defaultValidatorFactory.create("unsupported", properties);

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUnavailableValidatorTemplate() {
        Validator validator = defaultValidatorFactory.createTemplate("unavailable");

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test
    public void testInstall() {
        NlsService nlsService = mock(NlsService.class);
        Thesaurus thesaurus = mock(Thesaurus.class);
        when(nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        DefaultValidatorFactory validatorFactory = defaultValidatorFactory;
        validatorFactory.setNlsService(nlsService);

        int expectedTranslations = 0;
        for (String implementation : validatorFactory.available()) {
            expectedTranslations++;
            Validator validator = validatorFactory.createTemplate(implementation);
            expectedTranslations += validator.getPropertySpecs().size();
        }

        validatorFactory.install();

        ArgumentCaptor<Iterable> iterableCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(thesaurus).addTranslations(iterableCaptor.capture());

        assertThat(iterableCaptor.getValue()).hasSize(expectedTranslations);
    }

}