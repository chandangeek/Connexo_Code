package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.metering.console", service = ConsoleCommands.class, property = {"osgi.command.scope=metering", "osgi.command.function=printDdl"}, immediate = true)
public class ConsoleCommands {

    private volatile DataModel dataModel;

    public void printDdl() {
        try {
            for (Table<?> table : dataModel.getTables()) {
                for (Object s : table.getDdl()) {
                    System.out.println(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.dataModel = ((MeteringServiceImpl) meteringService).getDataModel();
    }

}
