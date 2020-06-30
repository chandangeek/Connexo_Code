package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsMessageFormat;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.propertyspec.MockPropertySpecService;
import com.energyict.protocolimplv2.messages.enums.DaysOfMonth;
import com.energyict.protocolimplv2.messages.enums.DaysOfWeek;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageCategoriesTest extends TestCase {

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
    public void testDateTimeEnums(){
        String[] daysOfMonth = DaysOfMonth.getDaysOfMonthValues();
        String[] daysOfWeek = DaysOfWeek.getDaysOfWeek();

        assertEquals(1,DaysOfMonth.getDlmsEncoding("01"));
        assertEquals(15,DaysOfMonth.getDlmsEncoding("15"));
        assertEquals(31,DaysOfMonth.getDlmsEncoding("31"));
        assertEquals(0xFD,DaysOfMonth.getDlmsEncoding(DaysOfMonth.DLMSEncodings.SECOND_LAST_DAY_OF_THE_MONTH.getDescription()));
        assertEquals(0xFE,DaysOfMonth.getDlmsEncoding(DaysOfMonth.DLMSEncodings.LAST_DAY_OF_THE_MONTH.getDescription()));
        assertEquals(0xFF,DaysOfMonth.getDlmsEncoding(DaysOfMonth.DLMSEncodings.ALL_DAYS.getDescription()));
        assertEquals(DaysOfMonth.DLMS_NOT_SPECIFIED,DaysOfMonth.getDlmsEncoding("0"));
        assertEquals(DaysOfMonth.DLMS_NOT_SPECIFIED,DaysOfMonth.getDlmsEncoding("ddd"));

        assertEquals(1, DaysOfWeek.getDlmsEncoding("Monday"));
        assertEquals(2, DaysOfWeek.getDlmsEncoding("Tuesday"));
        assertEquals(6, DaysOfWeek.getDlmsEncoding("Saturday"));
        assertEquals(7, DaysOfWeek.getDlmsEncoding("Sunday"));
        assertEquals(DaysOfWeek.DLMS_NOT_SPECIFIED, DaysOfWeek.getDlmsEncoding(DaysOfWeek.ALL_DAYS));
        assertEquals(DaysOfWeek.DLMS_NOT_SPECIFIED, DaysOfWeek.getDlmsEncoding("random string"));
    }

    @Test
    public void testMessageUniqueIds() {
        List<Long> messageIds;
        for (DeviceMessageCategories categories : DeviceMessageCategories.values()) {
            messageIds = new ArrayList<>();
            for (DeviceMessageSpec deviceMessageSpec : categories.get(this.propertySpecService, this.nlsService, this.converter).getMessageSpecifications()) {
                boolean condition = messageIds.contains(deviceMessageSpec.getId());
                if (condition) {
                    Logger logger = Logger.getLogger(DeviceMessageCategoriesTest.class.getSimpleName());
                    logger.severe("Unique message ID (" + deviceMessageSpec.getId() + ") violation: " + deviceMessageSpec.getName() + " in category " + categories.toString());
                }
                assertFalse(condition);
                messageIds.add(deviceMessageSpec.getId());
            }
        }
    }

}