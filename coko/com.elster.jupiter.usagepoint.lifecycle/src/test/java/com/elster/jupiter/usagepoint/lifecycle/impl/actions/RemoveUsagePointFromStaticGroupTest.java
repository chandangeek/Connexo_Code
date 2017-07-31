package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleServiceImpl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by h165708 on 7/21/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class RemoveUsagePointFromStaticGroupTest {

    private static final long USAGE_POINT_ID = 97L;
    private static final long END_USAGE_POINT_ID = 103L;

    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private State state;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC1, effectiveMC2;
    @Mock
    private UsagePointMetrologyConfiguration mc1, mc2;
    @Mock
    private MetrologyContract contract1, contract21, contract22;
    @Mock
    private ChannelsContainer container1, container2;

    private RemoveUsagePointFromStaticGroup action;

    @Before
    public void initializeMocks() {
        when(this.usagePoint.getId()).thenReturn(USAGE_POINT_ID);
        when(this.usagePoint.getMRID()).thenReturn(String.valueOf(USAGE_POINT_ID));
        when(this.usagePoint.getId()).thenReturn(END_USAGE_POINT_ID);

        action = new RemoveUsagePointFromStaticGroup(meteringService, meteringGroupsService);
        action.setThesaurus(NlsModule.SimpleThesaurus.from(new UsagePointLifeCycleServiceImpl().getKeys()));
    }

    @Test
    public void testInfo() {
        assertThat(action.getKey()).isEqualTo(RemoveUsagePointFromStaticGroup.class.getSimpleName());
        assertThat(action.getName()).isEqualTo(MicroActionTranslationKeys.REMOVE_USAGE_POINT_FROM_STATIC_GROUP_NAME.getDefaultFormat());
        assertThat(action.getDescription()).isEqualTo(MicroActionTranslationKeys.REMOVE_USAGE_POINT_FROM_STATIC_GROUP_DESCRIPTION.getDefaultFormat());
    }

    @Test
    public void testCategory() {
        assertThat(action.getCategory()).isEqualTo(MicroCategory.REMOVE.name());
        assertThat(action.getCategoryName()).isEqualTo(MicroCategoryTranslationKeys.REMOVE_NAME.getDefaultFormat());
    }



    @Test
    public void executeWhenUsagePointNotUsedInStaticGroups() {
        RemoveUsagePointFromStaticGroup microAction = this.getTestInstance();
        when(this.meteringService.findUsagePointByMRID(String.valueOf(USAGE_POINT_ID))).thenReturn(Optional.of(this.usagePoint));
        when(this.meteringGroupsService.findEnumeratedUsagePointGroupsContaining(this.usagePoint)).thenReturn(Collections.emptyList());

        // Business method
        microAction.execute(this.usagePoint, Instant.now(), Collections.emptyMap());

        // Asserts
        verify(this.meteringGroupsService).findEnumeratedUsagePointGroupsContaining(this.usagePoint);
    }

    @Test
    public void executeWhenUsagePointUsedInMultipleStaticGroups() {
        RemoveUsagePointFromStaticGroup microAction = this.getTestInstance();
        when(this.meteringService.findUsagePointByMRID(String.valueOf(USAGE_POINT_ID))).thenReturn(Optional.of(this.usagePoint));
        EnumeratedGroup.Entry<UsagePoint> group1Entry = mock(EnumeratedGroup.Entry.class);
        when(group1Entry.getMember()).thenReturn(this.usagePoint);
        EnumeratedUsagePointGroup group1 = mock(EnumeratedUsagePointGroup.class);
        doReturn(Arrays.asList(group1Entry)).when(group1).getEntries();
        EnumeratedGroup.Entry<UsagePoint> group2Entry = mock(EnumeratedGroup.Entry.class);
        when(group2Entry.getMember()).thenReturn(this.usagePoint);
        EnumeratedUsagePointGroup group2 = mock(EnumeratedUsagePointGroup.class);
        doReturn(Arrays.asList(group2Entry)).when(group2).getEntries();
        when(this.meteringGroupsService.findEnumeratedUsagePointGroupsContaining(this.usagePoint)).thenReturn(Arrays.asList(group1, group2));

        // Business method
        microAction.execute(this.usagePoint, Instant.now(), Collections.emptyMap());

        // Asserts
        verify(this.meteringGroupsService).findEnumeratedUsagePointGroupsContaining(this.usagePoint);
        verify(group1).remove(group1Entry);
        verify(group2).remove(group2Entry);
    }

    private RemoveUsagePointFromStaticGroup getTestInstance() {
        return new RemoveUsagePointFromStaticGroup( this.meteringService, this.meteringGroupsService);
    }
}
