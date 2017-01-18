package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolAdapterImpl;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the {@link DeviceProtocolAdapterImpl} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 15:03
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceCachingTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private IssueService issueService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private MeteringService meteringService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Before
    public void initBefore() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.protocolPluggableService.marshallDeviceProtocolCache(any())).thenReturn("JustADummyCache");
    }

    @Test
    public void getCacheChangedTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, this.propertySpecService, this.protocolPluggableService, mock(SecuritySupportAdapterMappingFactory.class), mock(CapabilityAdapterMappingFactory.class), mock(MessageAdapterMappingFactory.class), this.dataModel, issueService, collectedDataFactory, meteringService, thesaurus, deviceMessageSpecificationService);

        // business method
        DeviceProtocolCache deviceProtocolCache = meterProtocolAdapter.getDeviceCache();

        // assert that the content is not null and always changed so we always update it in the database
        assertNotNull(deviceProtocolCache);
        assertTrue(deviceProtocolCache.contentChanged());
    }

    @Test
    public void getCacheNotChangedForDlmsCacheTest() {

        /*
        We should always get a DeviceProtocolCache with the change option to true
         */

        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, this.propertySpecService, this.protocolPluggableService, mock(SecuritySupportAdapterMappingFactory.class), mock(CapabilityAdapterMappingFactory.class), mock(MessageAdapterMappingFactory.class), this.dataModel, issueService, collectedDataFactory, meteringService, thesaurus, deviceMessageSpecificationService);

        when(protocolPluggableService.marshallDeviceProtocolCache(any())).thenReturn("ThisIsADummyDlmsCache<changed>false</changed>WithSomeTrailingStuff");

        // business method
        DeviceProtocolCache deviceProtocolCache = meterProtocolAdapter.getDeviceCache();

        // assert that the content is not null and always changed so we always update it in the database
        assertNotNull(deviceProtocolCache);
        assertFalse(deviceProtocolCache.contentChanged());
    }

    /**
     * Creates a Mocked {@link MeterProtocol} which also implements the {@link DeviceSecuritySupport}
     * Otherwise we need to add it to the adapter map, and hence it is mocked, we don't know the className ...
     *
     * @return a mocked MeterProtocol
     */
    private MeterProtocol getMockedMeterProtocol() {
        return mock(MeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
    }

    @Test
    public void getCacheAdapterMethodCallTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, this.propertySpecService, this.protocolPluggableService, mock(SecuritySupportAdapterMappingFactory.class), mock(CapabilityAdapterMappingFactory.class), mock(MessageAdapterMappingFactory.class), this.dataModel, issueService, collectedDataFactory, meteringService, thesaurus, deviceMessageSpecificationService);

        // business method
        meterProtocolAdapter.getDeviceCache();

        // assert that the adapter forwarded the call to the getCache of the meterProtocol
        verify(meterProtocol).getCache();
    }

    @Test
    public void setDeviceCacheTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, this.propertySpecService, this.protocolPluggableService, mock(SecuritySupportAdapterMappingFactory.class), mock(CapabilityAdapterMappingFactory.class), mock(MessageAdapterMappingFactory.class), this.dataModel, issueService, collectedDataFactory, meteringService, thesaurus, deviceMessageSpecificationService);
        Device device = mock(Device.class);
        String jsonCache = "MyTestJsonCache";
        when(this.protocolPluggableService.unMarshallDeviceProtocolCache(jsonCache)).thenReturn(Optional.of(device));
        DeviceProtocolCacheAdapter deviceCacheAdapter = new DeviceProtocolCacheAdapter();
        // we set a Device as cache object so we can validate that this was set to the adapter
        deviceCacheAdapter.setLegacyJsonCache(jsonCache);

        // business method
        meterProtocolAdapter.setDeviceCache(deviceCacheAdapter);

        // assert that the proper meterProtocol.setCache(...) is called
        verify(meterProtocol).setCache(device);
    }

    @Test
    public void verifyCacheIsNotCorrectInstanceTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, this.propertySpecService, this.protocolPluggableService, mock(SecuritySupportAdapterMappingFactory.class), mock(CapabilityAdapterMappingFactory.class), mock(MessageAdapterMappingFactory.class), this.dataModel, issueService, collectedDataFactory, meteringService, thesaurus, deviceMessageSpecificationService);
        NotTheCorrectDeviceProtocolCache deviceCache = new NotTheCorrectDeviceProtocolCache();

        // business method
        meterProtocolAdapter.setDeviceCache(deviceCache);

        // assert that the setCache was NOT called
        verify(meterProtocol, times(0)).setCache(any(DeviceProtocolCache.class));
    }

    private class NotTheCorrectDeviceProtocolCache implements DeviceProtocolCache {

        @Override
        public boolean contentChanged() {
            return false;
        }

        @Override
        public void setContentChanged(boolean changed) {

        }
    }

}
