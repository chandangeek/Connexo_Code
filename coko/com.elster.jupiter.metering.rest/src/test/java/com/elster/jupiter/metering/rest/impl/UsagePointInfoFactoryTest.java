package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.rest.UsagePointInfo;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/9/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class UsagePointInfoFactoryTest {

    @Mock
    Thesaurus thesaurus;
    @Mock
    NlsService nlsService;

    @Before
    public void setUp() throws Exception {
        when(nlsService.getThesaurus(anyString(), anyObject())).thenReturn(thesaurus);

    }

    @Test
    public void testModelMapsToInfoFields() throws Exception {
        UsagePointInfoFactory factory = new UsagePointInfoFactory();
        factory.setNlsService(nlsService);
        factory.modelStructure().stream().forEach(prop -> {
            try {
                UsagePointInfo.class.getField(prop.propertyName);
                assertTrue("Missing translation for " + prop.propertyName, Arrays.stream(TranslationSeeds.values()).anyMatch(key -> key.getKey().equals(prop.propertyName)));
            } catch (NoSuchFieldException e) {
                fail("Expected UsagePointInfo to have a field "+prop.propertyName+" as defined in the info-structure/model");
            }
        });

    }
}
