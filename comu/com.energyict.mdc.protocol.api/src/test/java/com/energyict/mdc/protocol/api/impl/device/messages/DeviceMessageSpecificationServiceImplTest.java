package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import java.util.Optional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
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
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DataVault dataVault;
    @Mock
    private LegacyDataVaultProvider dataVaultProvider;

    @Before
    public void setupDataVaultProvider () {
        when(this.dataVaultProvider.getKeyVault()).thenReturn(this.dataVault);
        LegacyDataVaultProvider.instance.set(this.dataVaultProvider);
    }

    @After
    public void tearDownDataVaultProvider () {
        LegacyDataVaultProvider.instance.set(null);
    }

    @Before
    public void setupNslService () {
        when(this.nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(this.thesaurus);
    }

    @Test
    public void testAllCategories () {
        // Business method
        List<DeviceMessageCategory> categories = this.newService().allCategories();

        // Asserts
        assertThat(categories).isNotEmpty();
    }

    @Test
    public void testAllCategoriesHaveAnId() {
        // Business method
        List<Integer> primaryKeys =
                this.newService().allCategories().stream().
                    map(DeviceMessageCategory::getId).
                    collect(Collectors.toList());

        // Asserts
        assertThat(primaryKeys).isNotEmpty();
        assertThat(primaryKeys).doesNotContainNull();
    }

    @Test
    public void testAllCategoriesHaveAUniqueId() {
        Set<Integer> uniqueIds = new HashSet<>();
        for (DeviceMessageCategory category : this.newService().allCategories()) {
            if (!uniqueIds.add(category.getId())) {
                fail("DeviceMessageCategory " + category.getName() + " does not have a unique id:" + category.getId());
            }
        }
    }

    @Test
    public void testCategoryNameUseThesaurus () {
        // Business method
        this.newService().allCategories().
            stream().
            forEach(DeviceMessageCategory::getName);

        // Asserts
        verify(this.thesaurus, atLeastOnce()).getString(anyString(), anyString());
    }

    @Test
    public void testAllMessageSpecsHaveAUniqueId () {
        List<DeviceMessageSpec> deviceMessageSpecs = this.newService().allCategories().stream().
                flatMap(category -> category.getMessageSpecifications().stream()).
                collect(Collectors.toList());

        Set<DeviceMessageId> uniqueIds = EnumSet.noneOf(DeviceMessageId.class);
        for (DeviceMessageSpec messageSpec : deviceMessageSpecs) {
            if (!uniqueIds.add(messageSpec.getId())) {
                fail("Message spec " + messageSpec.getName() + " does not have a unique DeviceMessageId:" + messageSpec.getId());
            }
        }
    }

    @Test
    public void testAllMessageSpecsNamesUseThesaurus () {
        // Business method
        this.newService().allCategories().
            stream().
            flatMap(category -> category.getMessageSpecifications().stream()).
            forEach(DeviceMessageSpec::getName);

        // Asserts
        verify(this.thesaurus, atLeastOnce()).getString(anyString(), anyString());
    }

    @Test
    public void testAllMessageSpecsLinkToTheParentCategory () {
        this.newService().allCategories().stream().
            filter(category -> !category.getMessageSpecifications().isEmpty()).
            forEach(this::doTestAllMessageSpecsLinkToTheParentCategory);
    }

    private void doTestAllMessageSpecsLinkToTheParentCategory(DeviceMessageCategory category) {
        Set<DeviceMessageCategory> categories = category.getMessageSpecifications().stream().
                map(DeviceMessageSpec::getCategory).
                collect(Collectors.toSet());

        // Asserts
        assertThat(categories).
            as("Not all message of the category " + category.getName() + " return that category in the getCategory() method").
            hasSize(1);
    }

    @Test
    public void testNoCategoriesHaveUserfileProperties () {
        this.testNoCategoriesHaveUnsupportedReferenceProperties(FactoryIds.USERFILE);
    }

    @Test
    public void testNoCategoriesHaveCodeTableProperties () {
        this.testNoCategoriesHaveUnsupportedReferenceProperties(FactoryIds.CODE);
    }

    private void testNoCategoriesHaveUnsupportedReferenceProperties (FactoryIds unsupportedFactoryId) {
        List<DeviceMessageCategory> categories = this.newServiceWithRealPropertSpecService().allCategories();

        // Business method
        List<PropertySpec> propertySpecs = categories.stream().
                flatMap(category -> category.getMessageSpecifications().stream()).
                flatMap(messageSpec -> messageSpec.getPropertySpecs().stream()).
                collect(Collectors.toList());

        // Asserts
        assertThat(propertySpecs).doNotHave(new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                if (propertySpec.isReference()) {
                    JupiterReferenceFactory valueFactory = (JupiterReferenceFactory) propertySpec.getValueFactory();
                    return valueFactory.getObjectFactoryId() == unsupportedFactoryId.id();
                }
                else {
                    return false;
                }
            }
        });
    }

    private DeviceMessageSpecificationService newService () {
        return new DeviceMessageSpecificationServiceImpl(propertySpecService, nlsService);
    }

    private DeviceMessageSpecificationService newServiceWithRealPropertSpecService () {
        PropertySpecServiceImpl propertySpecService = new PropertySpecServiceImpl(new com.elster.jupiter.properties.impl.PropertySpecServiceImpl());
        propertySpecService.addFactoryProvider(new FinderProvider());
        return new DeviceMessageSpecificationServiceImpl(propertySpecService, nlsService);
    }

    private class LoadProfile implements BaseLoadProfile<BaseChannel>, HasId {
        @Override
        public long getId() {
            return 0;
        }

        @Override
        public ObisCode getDeviceObisCode() {
            return null;
        }

        @Override
        public BaseDevice getDevice() {
            return null;
        }

        @Override
        public List<BaseChannel> getAllChannels() {
            return null;
        }

        @Override
        public long getLoadProfileTypeId() {
            return 0;
        }

        @Override
        public ObisCode getLoadProfileTypeObisCode() {
            return null;
        }
    }

    private class LoadProfileFinder implements CanFindByLongPrimaryKey<LoadProfile> {
        @Override
        public FactoryIds factoryId() {
            return FactoryIds.LOADPROFILE;
        }

        @Override
        public Class<LoadProfile> valueDomain() {
            return LoadProfile.class;
        }

        @Override
        public Optional<LoadProfile> findByPrimaryKey(long id) {
            return Optional.empty();
        }
    }

    private class LogBook implements HasId {
        @Override
        public long getId() {
            return 0;
        }
    }

    private class LogBookFinder implements CanFindByLongPrimaryKey<LogBook> {
        @Override
        public FactoryIds factoryId() {
            return FactoryIds.LOGBOOK;
        }

        @Override
        public Class<LogBook> valueDomain() {
            return LogBook.class;
        }

        @Override
        public Optional<LogBook> findByPrimaryKey(long id) {
            return Optional.empty();
        }
    }

    private class FinderProvider implements ReferencePropertySpecFinderProvider {
        public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
            List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
            finders.add(new LoadProfileFinder());
            finders.add(new LogBookFinder());
            return finders;
        }

    }
}