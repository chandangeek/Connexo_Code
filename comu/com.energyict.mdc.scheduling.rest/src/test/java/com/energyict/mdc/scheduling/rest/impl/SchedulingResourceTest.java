package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class SchedulingResourceTest extends JerseyTest {

    private static NlsService nlsService;
    private static Thesaurus thesaurus;
    private static Clock clock;
    private static SchedulingService schedulingService;
    private static DeviceDataService deviceDataService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        schedulingService = mock(SchedulingService.class);
        deviceDataService = mock(DeviceDataService.class);
        clock = mock(Clock.class);
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset();
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                SchedulingResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(schedulingService).to(SchedulingService.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(nlsService).to(NlsService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(clock).to(Clock.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

    @Test
    public void testGetEmptyDeviceTypeList() throws Exception {
        List<ComSchedule> comSchedules = new ArrayList<>();
        ListPager<ComSchedule> comSchedulePage = ListPager.of(comSchedules);
        when(schedulingService.findAllSchedules(any(Calendar.class))).thenReturn(comSchedulePage);
        when(clock.getTimeZone()).thenReturn(Calendar.getInstance().getTimeZone());

        Map<String, Object> map = target("/schedules/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List<?>)map.get("schedules")).isEmpty();
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }


    class SomeLocalizedException extends LocalizedException {

        protected SomeLocalizedException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }
    }

}
