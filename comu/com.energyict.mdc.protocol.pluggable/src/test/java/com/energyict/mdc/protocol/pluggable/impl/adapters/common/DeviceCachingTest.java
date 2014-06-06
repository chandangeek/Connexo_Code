package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolAdapterImpl;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link DeviceProtocolAdapterImpl} component
 *
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
    private DeviceCacheMarshallingService deviceCacheMarshallingService;

    @Test
    public void getCacheAlwaysChangedTest(){

        /*
        We should always get a DeviceProtocolCache with the change option to true
         */

        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, mock(ProtocolPluggableService.class), mock(SecuritySupportAdapterMappingFactory.class), this.dataModel, issueService, deviceCacheMarshallingService);

        // business method
        DeviceProtocolCache deviceProtocolCache = meterProtocolAdapter.getDeviceCache();

        // assert that the content is not null and always changed so we always update it in the database
        assertNotNull(deviceProtocolCache);
        assertTrue(deviceProtocolCache.contentChanged());
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
    public void getCacheAdapterMethodCallTest(){
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, mock(ProtocolPluggableService.class), mock(SecuritySupportAdapterMappingFactory.class), this.dataModel, issueService, deviceCacheMarshallingService);

        // business method
        DeviceProtocolCache deviceProtocolCache = meterProtocolAdapter.getDeviceCache();

        // assert that the adapter forwarded the call to the getCache of the meterProtocol
        verify(meterProtocol).getCache();
    }

    @Test
    public void setDeviceCacheTest(){
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, mock(ProtocolPluggableService.class), mock(SecuritySupportAdapterMappingFactory.class), this.dataModel, issueService, deviceCacheMarshallingService);
        BaseDevice device = mock(BaseDevice.class);
        String jsonCache = "MyTestJsonCache";
        when(deviceCacheMarshallingService.unMarshallCache(jsonCache)).thenReturn(device);
        DeviceProtocolCacheAdapter deviceCacheAdapter = new DeviceProtocolCacheAdapter();
        // we set an Device object as cache object so we can validate that this was set to the adapter
        deviceCacheAdapter.setLegacyJsonCache(jsonCache);

        // business method
        meterProtocolAdapter.setDeviceCache(deviceCacheAdapter);

        // assert that the proper meterProtocol.setCache(...) is called
        verify(meterProtocol).setCache(device);
    }

    @Test
    public void verifyCacheIsNotCorrectInstanceTest(){
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapterImpl(meterProtocol, mock(ProtocolPluggableService.class), mock(SecuritySupportAdapterMappingFactory.class), this.dataModel, issueService, deviceCacheMarshallingService);
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
    }

}
