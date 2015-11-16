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
public class ChannelReadingTypeUnitOfMeasureSearchablePropertyTest extends AbstractReadingTypeUnitOfMeasureSearchablePropertyTest {

    private RegisterSearchablePropertyGroup channelGroup;

    @Before
    @Override
    public void initializeMocks() {
        super.initializeMocks();
        this.channelGroup = new RegisterSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(RegisterSearchablePropertyGroup.GROUP_NAME);
    }

    protected SearchableProperty getTestInstance() {
        return new ChannelReadingTypeUnitOfMeasureSearchableProperty(meteringService, propertySpecService, thesaurus).init(this.domain, channelGroup);
    }
}
