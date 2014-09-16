package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.text.MessageFormat;
import java.util.Locale;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class DeviceConfigurationJerseyTest extends JerseyTest {

    @Mock
    MeteringService meteringService;
    @Mock
    MasterDataService masterDataService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    ValidationService validationService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    NlsService nlsService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    EngineModelService engineModelService;
    @Mock
    DeviceDataService deviceDataService;
    @Mock
    UserService userService;
    @Mock
    MdcPropertyUtils mdcPropertyUtils;
    @Mock
    PropertyUtils propertyUtils;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(masterDataService, protocolPluggableService, engineModelService, deviceDataService, validationService);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeeds messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            return (String) invocationOnMock.getArguments()[1];
        });
        NlsMessageFormat nlsFormat = mock(NlsMessageFormat.class);
        when(nlsFormat.format(anyObject())).thenReturn("xxx");
        when(thesaurus.getFormat(any(MessageSeeds.class))).thenAnswer(invocationOnMock -> new NlsMessageFormat() {
            @Override
            public String format(Object... args) {
                return new MessageFormat(((MessageSeeds)invocationOnMock.getArguments()[0]).getDefaultFormat()).format(args);
            }

            @Override
            public String format(Locale locale, Object... args) {
                return null;
            }
        });
    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ResourceHelper.class,
                DeviceTypeResource.class,
                DeviceConfigurationResource.class,
                SecurityPropertySetResource.class,
                ExecutionLevelResource.class,
                ValidationRuleSetResource.class,
                RegisterConfigurationResource.class,
                RegisterTypeResource.class,
                LoadProfileConfigurationResource.class,
                LoadProfileTypeResource.class,
                ConnectionMethodResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(masterDataService).to(MasterDataService.class);
                bind(validationService).to(ValidationService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(meteringService).to(MeteringService.class);
                bind(nlsService).to(NlsService.class);
                bind(userService).to(UserService.class);
                bind(ResourceHelper.class).to(ResourceHelper.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
                bind(SecurityPropertySetInfoFactory.class).to(SecurityPropertySetInfoFactory.class);
                bind(ExecutionLevelInfoFactory.class).to(ExecutionLevelInfoFactory.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(PropertyUtils.class).to(PropertyUtils.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

}
