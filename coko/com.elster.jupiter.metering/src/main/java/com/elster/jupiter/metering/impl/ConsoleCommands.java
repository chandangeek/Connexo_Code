package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.conditions.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.metering.console", service = ConsoleCommands.class, property = {"osgi.command.scope=metering", "osgi.command.function=printDdl", "osgi.command.function=meters", "osgi.command.function=readingTypes"}, immediate = true)
public class ConsoleCommands {

    private volatile MeteringService meteringService;
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

    public void meters() {
        meteringService.getMeterQuery().select(Condition.TRUE).stream()
                .map(meter -> meter.getId() + " " + meter.getMRID())
                .forEach(System.out::println);
    }

    public void readingTypes() {
        meteringService.getAvailableReadingTypes().stream()
                .map(IdentifiedObject::getMRID)
                .forEach(System.out::println);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
        this.dataModel = ((MeteringServiceImpl) meteringService).getDataModel();
    }

}
