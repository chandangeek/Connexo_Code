package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.nls.console", service = ConsoleCommands.class, property = {"osgi.command.scope=nls",
        "osgi.command.function=load",
        "osgi.command.function=listKeys",
        "osgi.command.function=translate",
        "osgi.command.function=translations"}, immediate = true)
public class ConsoleCommands {

    private static final String MISSING = "Translation is missing from current thesaurus";

    private volatile NlsService nlsService;

    private Thesaurus thesaurus;
    private String module;
    private Layer layer;

    public void load(String moduleName, String layerName) {
        layer = Layer.valueOf(layerName);
        thesaurus = nlsService.getThesaurus(moduleName, layer);
    }

    public void listKeys() {
        if (thesaurus != null) {
            thesaurus.getTranslations().keySet().stream()
                    .forEach(System.out::println);
        }
    }

    public void translate(String key) {
        if (thesaurus != null) {
            System.out.println(thesaurus.getString(key, MISSING));
        }
    }

    public void translations() {
        if (thesaurus != null) {
            thesaurus.getTranslations().entrySet().stream()
                    .map(entry -> entry.getKey() + " -> " + entry.getValue())
                    .forEach(System.out::println);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }
}
