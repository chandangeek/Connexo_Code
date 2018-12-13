/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

import com.elster.jupiter.system.utils.DependenciesParser;
import com.elster.jupiter.system.utils.SubsystemModel;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class DependenciesParserTest {

    @Test
    public void testParsePomBomXml() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        SubsystemModel model = new SubsystemModel();
        DependenciesParser dependenciesParser = new DependenciesParser(model);

        dependenciesParser.loadThirdPartyBundlesProperties(this.getClass().getResource("third-parties-bundle.properties"));
        dependenciesParser.parse(this.getClass().getResource("subsystem-pom.xml").toURI().toString());

        List<Component> components = model.mergeDependencies().stream()
                .sorted((c1, c2) -> c1.getSymbolicName().compareTo(c2.getSymbolicName())).collect(toList());

        assertThat(components).hasSize(4);
        Component component;

        component = components.get(0);
        assertThat(component.getSymbolicName()).isEqualTo("com.elster.jupiter.artifactIdWithVersion");
        assertThat(component.getVersion()).isEqualTo("1.0.1");
        assertThat(component.getBundleType()).isEqualTo(BundleType.APPLICATION_SPECIFIC);

        component = components.get(1);
        assertThat(component.getSymbolicName()).isEqualTo("com.elster.jupiter.artifactIdWithVersionProp");
        assertThat(component.getVersion()).isEqualTo("1.2.5");
        assertThat(component.getBundleType()).isEqualTo(BundleType.APPLICATION_SPECIFIC);

        component = components.get(2);
        assertThat(component.getSymbolicName()).isEqualTo("com.energyict.artifactId");
        assertThat(component.getVersion()).isEqualTo("1.2.3");
        assertThat(component.getBundleType()).isEqualTo(BundleType.APPLICATION_SPECIFIC);

        component = components.get(3);
        assertThat(component.getSymbolicName()).isEqualTo("com.google.inject");
        assertThat(component.getVersion()).isEqualTo("0.0.12_3");
        assertThat(component.getBundleType()).isEqualTo(BundleType.THIRD_PARTY);
    }
}
