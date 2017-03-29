package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.impl.ServerProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConverterImpl;
import com.energyict.mdc.upl.nls.NlsMessageFormat;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceMessageSpecificationServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-12 (14:45)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageSpecificationServiceImplTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService = new MockPropertySpecService();
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock
    private TimeService timeService;
    @Mock
    private ServerProtocolPluggableService serverProtocolPluggableService;

    @Before
    public void setUp() throws Exception {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);

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

    @After
    public void tearDownDataVaultProvider() {
        LegacyDataVaultProvider.instance.set(null);
    }

    @Before
    public void setupNslService() {
        when(this.nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME)).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        doReturn(messageFormat).when(this.thesaurus).getFormat(any(TranslationKey.class));
    }

    @Test
    public void testAllCategories() {
        // Business method
        List<DeviceMessageCategory> categories = this.newService().filteredCategoriesForUserSelection();

        // Asserts
        assertThat(categories).isNotEmpty();
    }

    @Test
    public void testAllCategoriesHaveAnId() {
        // Business method
        List<Integer> primaryKeys =
                this.newService().filteredCategoriesForUserSelection().stream().
                        map(DeviceMessageCategory::getId).
                        collect(Collectors.toList());

        // Asserts
        assertThat(primaryKeys).isNotEmpty();
        assertThat(primaryKeys).doesNotContainNull();
    }

    @Test
    public void testAllCategoriesHaveAUniqueId() {
        Set<Integer> uniqueIds = new HashSet<>();
        for (DeviceMessageCategory category : this.newService().filteredCategoriesForUserSelection()) {
            if (!uniqueIds.add(category.getId())) {
                fail("DeviceMessageCategory " + category.getName() + " does not have a unique id:" + category.getId());
            }
        }
    }

    @Test
    public void testCategoryNameUseThesaurus() {
        // Business method
        this.newService().filteredCategoriesForUserSelection().
                stream().
                forEach(DeviceMessageCategory::getName);

        // Asserts
        verify(this.nlsService, atLeastOnce()).getThesaurus(any(String.class));
        Thesaurus id = nlsService.getThesaurus("ID");
        verify(id, atLeastOnce()).getFormat(any(TranslationKey.class));
    }

    @Test
    public void testAllMessageSpecsHaveAUniqueId() {
        List<DeviceMessageSpec> deviceMessageSpecs = this.newService().filteredCategoriesForUserSelection().stream().
                flatMap(category -> category.getMessageSpecifications().stream()).
                map(each -> each).
                collect(Collectors.toList());

        Set<DeviceMessageId> uniqueIds = EnumSet.noneOf(DeviceMessageId.class);
        for (DeviceMessageSpec messageSpec : deviceMessageSpecs) {
            if (!uniqueIds.add(messageSpec.getId())) {
                fail("Message spec " + messageSpec.getName() + " does not have a unique DeviceMessageId:" + messageSpec.getId());
            }
        }
    }

    @Test
    public void testAllMessageSpecsNamesUseThesaurus() {
        // Business method
        this.newService().filteredCategoriesForUserSelection().
                stream().
                flatMap(category -> category.getMessageSpecifications().stream()).
                map(each -> each).
                forEach(DeviceMessageSpec::getName);

        // Asserts
        verify(this.nlsService, atLeastOnce()).getThesaurus(any(String.class));
        Thesaurus id = nlsService.getThesaurus("ID");
        verify(id, atLeastOnce()).getFormat(any(TranslationKey.class));
    }

    @Test
    public void testAllMessageSpecsLinkToTheParentCategory() {
        this.newService().filteredCategoriesForUserSelection().stream().
                filter(category -> !category.getMessageSpecifications().isEmpty()).
                forEach(this::doTestAllMessageSpecsLinkToTheParentCategory);
    }

    private void doTestAllMessageSpecsLinkToTheParentCategory(DeviceMessageCategory category) {
        Set<DeviceMessageCategory> categories = category.getMessageSpecifications().stream().
                map(each -> each).
                map(DeviceMessageSpec::getCategory).
                collect(Collectors.toSet());

        // Asserts
        assertThat(categories).
                as("Not all message of the category " + category.getName() + " return that category in the getCategory() method").
                hasSize(1);
    }

    private DeviceMessageSpecificationService newService() {
        return new DeviceMessageSpecificationServiceImpl(new ConverterImpl(), this.nlsService, this.propertySpecService);
    }
}