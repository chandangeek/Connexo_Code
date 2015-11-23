package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChannelReadingTypeNameSearchablePropertyTest extends AbstractReadingTypeNameSearchablePropertyTest {

    private SearchablePropertyGroup channelGroup;

    @Before
    @Override
    public void initializeMocks() {
        super.initializeMocks();
        this.channelGroup = new ChannelSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(ChannelSearchablePropertyGroup.GROUP_NAME);
    }

    @Override
    protected ChannelReadingTypeNameSearchableProperty getTestInstance() {
        return new ChannelReadingTypeNameSearchableProperty(propertySpecService, thesaurus).init(domain, channelGroup);
    }
}
