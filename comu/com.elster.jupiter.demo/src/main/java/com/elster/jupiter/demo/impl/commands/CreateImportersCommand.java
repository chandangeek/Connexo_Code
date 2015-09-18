package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.FileImporterTpl;
import com.elster.jupiter.nls.Thesaurus;

/**
 *
 * Purpose for this command is to install default Importers in the demo system, with their
 * respective properties so they can be used without additional configuration.
 *
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 10:47
 */
public class CreateImportersCommand {

    public CreateImportersCommand(){}

    public void run(){
        for (FileImporterTpl importerTpl : FileImporterTpl.values()) {
            Builders.from(importerTpl).get();
        }
    }
}
