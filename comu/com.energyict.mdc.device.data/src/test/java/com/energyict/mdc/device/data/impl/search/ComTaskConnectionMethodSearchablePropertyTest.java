package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ComTaskConnectionMethodSearchablePropertyTest extends ConnectionMethodSearchablePropertyTest {

    SearchablePropertyGroup parentGroup;

    @Override
    public void initializeMocks() {
        super.initializeMocks();
        this.parentGroup = new ComTaskSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(ComTaskSearchablePropertyGroup.GROUP_NAME);
    }

    private SearchableProperty getTestInstance() {
        return new ComTaskConnectionMethodSearchableProperty(protocolPluggableService, propertySpecService, thesaurus).init(domain, parentGroup);
    }
}
