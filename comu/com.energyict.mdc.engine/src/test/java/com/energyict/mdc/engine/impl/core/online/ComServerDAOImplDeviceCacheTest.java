package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdw.core.EndDeviceCache;
import com.energyict.mdw.core.DeviceCacheFactory;
import com.energyict.mdw.shadow.DeviceCacheShadow;
import com.energyict.test.MockEnvironmentTranslations;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl#createOrUpdateDeviceCache(int, DeviceCacheShadow)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-04 (10:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerDAOImplDeviceCacheTest {

    private final int DEVICE_ID = 654;

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private ServerManager manager;
    @Mock
    private MdwInterface mdwInterface;
    @Mock
    private DeviceCacheFactory deviceCacheFactory;

    @Before
    public void initializeMocksAndFactories () throws SQLException, BusinessException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.getDeviceCacheFactory()).thenReturn(this.deviceCacheFactory);
        when(this.manager.getMdwInterface()).thenReturn(this.mdwInterface);
        ManagerFactory.setCurrent(this.manager);
    }

    @Test
    public void testCreation () throws BusinessException, SQLException {
        ComServerDAO comServerDAO = new ComServerDAOImpl(serviceProvider);
        DeviceCacheShadow shadow = new DeviceCacheShadow();
        shadow.setRtuId(DEVICE_ID);

        // Business method
        comServerDAO.createOrUpdateDeviceCache(DEVICE_ID, shadow);

        // Asserts
        verify(this.deviceCacheFactory, times(1)).findByDeviceId(DEVICE_ID);
        verify(this.deviceCacheFactory, times(1)).create(shadow);
    }

    @Test
    public void testUpdate () throws BusinessException, SQLException {
        ComServerDAO comServerDAO = new ComServerDAOImpl(serviceProvider);
        DeviceCacheShadow shadow = new DeviceCacheShadow();
        shadow.setRtuId(DEVICE_ID);
        EndDeviceCache deviceCache = mock(EndDeviceCache.class);
        when(deviceCache.getRtuId()).thenReturn(DEVICE_ID);
        when(deviceCache.getShadow()).thenReturn(shadow);
        when(this.deviceCacheFactory.findByDeviceId(DEVICE_ID)).thenReturn(deviceCache);

        // Business method
        comServerDAO.createOrUpdateDeviceCache(DEVICE_ID, shadow);

        // Asserts
        verify(this.deviceCacheFactory, times(1)).findByDeviceId(DEVICE_ID);
        verify(this.deviceCacheFactory, times(0)).create(shadow);
        verify(deviceCache, times(1)).update(shadow);
    }

    private void mockMdwInterfaceTransactionExecutor () throws BusinessException, SQLException {
        when(this.mdwInterface.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer (InvocationOnMock invocation) throws Throwable {
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                transaction.doExecute();
                return null;
            }
        });
    }

}