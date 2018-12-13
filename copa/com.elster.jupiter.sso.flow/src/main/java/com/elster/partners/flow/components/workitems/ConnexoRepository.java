/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.flow.components.workitems;

import org.guvnor.common.services.project.events.NewProjectEvent;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.StandardOpenOption;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

@ApplicationScoped
public class ConnexoRepository {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    public void createGlobalDirOnNewProject(@Observes NewProjectEvent newProjectEvent) {
        KieProject project = (KieProject) newProjectEvent.getProject();
        String projectPath = org.uberfire.backend.server.util.Paths.convert(project.getRootPath()).toUri().toString();
        String separator = org.uberfire.backend.server.util.Paths.convert(project.getRootPath())
                .getFileSystem()
                .getSeparator();
        String globalDirPath = projectPath + separator + "global";
        String resourcesDirPath = projectPath + separator + "src" + separator + "main" + separator + "resources";
        Path globalDirVFSPath = ioService.get(URI.create(globalDirPath));
        Path workItemDefinitions = ioService.get(URI.create(resourcesDirPath + separator + "WorkDefinitions.wid"));

        while (!ioService.exists(globalDirVFSPath) || !ioService.exists(workItemDefinitions)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Project directory structure created; adjust configuration for Connexo
        ioService.copy(getClass().getResourceAsStream("/icons/connexo.png"), ioService.get(URI.create(globalDirPath + separator + "connexo.png")));

        ioService.write(workItemDefinitions,
                "import org.drools.core.process.core.datatype.impl.type.StringDataType;\n" +
                        "import org.drools.core.process.core.datatype.impl.type.ObjectDataType;\n" +
                        "\n" +
                        "[\n" +
                        "  [\n" +
                        "    \"name\" : \"Email\",\n" +
                        "    \"parameters\" : [\n" +
                        "      \"From\" : new StringDataType(),\n" +
                        "      \"To\" : new StringDataType(),\n" +
                        "      \"Subject\" : new StringDataType(),\n" +
                        "      \"Body\" : new StringDataType()\n" +
                        "    ],\n" +
                        "    \"displayName\" : \"Email\",\n" +
                        "    \"icon\" : \"defaultemailicon.gif\"\n" +
                        "  ],\n" +
                        "\n" +
                        "  [\n" +
                        "    \"name\" : \"Log\",\n" +
                        "    \"parameters\" : [\n" +
                        "      \"Message\" : new StringDataType()\n" +
                        "    ],\n" +
                        "    \"displayName\" : \"Log\",\n" +
                        "    \"icon\" : \"defaultlogicon.gif\"\n" +
                        "  ],\n" +
                        "\n" +
                        "  [\n" +
                        "    \"name\" : \"WebService\",\n" +
                        "    \"parameters\" : [\n" +
                        "        \"Url\" : new StringDataType(),\n" +
                        "         \"Namespace\" : new StringDataType(),\n" +
                        "         \"Interface\" : new StringDataType(),\n" +
                        "         \"Operation\" : new StringDataType(),\n" +
                        "         \"Parameter\" : new StringDataType(),\n" +
                        "         \"Endpoint\" : new StringDataType(),\n" +
                        "         \"Mode\" : new StringDataType()\n" +
                        "    ],\n" +
                        "    \"results\" : [\n" +
                        "        \"Result\" : new ObjectDataType(),\n" +
                        "    ],\n" +
                        "    \"displayName\" : \"WS\",\n" +
                        "    \"icon\" : \"defaultservicenodeicon.png\"\n" +
                        "  ],\n" +
                        "\n" +
                        "  [\n" +
                        "    \"name\" : \"Rest\",\n" +
                        "    \"parameters\" : [\n" +
                        "        \"Url\" : new StringDataType(),\n" +
                        "        \"Method\" : new StringDataType(),\n" +
                        "        \"ConnectTimeout\" : new StringDataType(),\n" +
                        "        \"ReadTimeout\" : new StringDataType(),\n" +
                        "        \"Username\" : new StringDataType(),\n" +
                        "        \"Password\" : new StringDataType()\n" +
                        "    ],\n" +
                        "    \"results\" : [\n" +
                        "        \"Result\" : new ObjectDataType(),\n" +
                        "    ],\n" +
                        "    \"displayName\" : \"REST\",\n" +
                        "    \"icon\" : \"defaultservicenodeicon.png\"\n" +
                        "  ],\n" +
                        "\n" +
                        "  [\n" +
                        "   \"name\" : \"ConnexoREST\",\n" +
                        "   \"parameters\" : [\n" +
                        "       \"Url\" : new StringDataType(),\n" +
                        "       \"Method\" : new StringDataType(),\n" +
                        "       \"ConnectTimeout\" : new StringDataType(),\n" +
                        "       \"ReadTimeout\" : new StringDataType()\n" +
                        "   ],\n" +
                        "   \"results\" : [\n" +
                        "       \"Result\" : new ObjectDataType()\n" +
                        "   ],\n" +
                        "   \"displayName\" : \"Connexo REST\",\n" +
                        "   \"icon\" : \"connexo.png\"\n" +
                        "  ]\n" +
                        "\n" +
                        "]\n"
                , StandardOpenOption.CREATE_NEW);

    }
}
