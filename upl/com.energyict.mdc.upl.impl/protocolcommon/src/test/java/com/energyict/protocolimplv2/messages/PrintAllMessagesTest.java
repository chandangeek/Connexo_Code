package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsMessageFormat;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.propertyspec.MockPropertySpecService;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 9/26/13
 * Time: 10:26 AM
 */
@Ignore // Only useful to get the full list of all possible messages, but doesn't test anything
@RunWith(MockitoJUnitRunner.class)
public class PrintAllMessagesTest {

    private PropertySpecService propertySpecService = new MockPropertySpecService();
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Before
    public void before() {
        Thesaurus thesaurus = mock(Thesaurus.class);

        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            NlsMessageFormat format = mock(NlsMessageFormat.class);
            when(format.format()).thenReturn(((TranslationKey) args[0]).getDefaultFormat());
            when(format.format(anyObject())).thenReturn(((TranslationKey) args[0]).getDefaultFormat());
            return format;
        });

        when(nlsService.getThesaurus(anyString())).thenReturn(thesaurus);
    }

    @Test
    public void printAllMessagesTest() {
        for (DeviceMessageCategories deviceMessageCategory : DeviceMessageCategories.values()) {
            System.out.println("Category : " + deviceMessageCategory);
            for (DeviceMessageSpec deviceMessageSpec : deviceMessageCategory.get(this.propertySpecService, this.nlsService, this.converter).getMessageSpecifications()) {
                System.out.println("\t - DeviceMessage: " + deviceMessageSpec.getName());
                for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                    System.out.println("\t\t - Attribute: " + propertySpec.getName());
                }
            }
        }
    }
}