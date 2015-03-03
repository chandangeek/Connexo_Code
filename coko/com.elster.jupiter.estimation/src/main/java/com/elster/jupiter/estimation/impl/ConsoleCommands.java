package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component(name = "com.elster.jupiter.estimation.console",
        service = ConsoleCommands.class,
        property = {"osgi.command.scope=estimation",
                "osgi.command.function=estimationBlocks"
        },
        immediate = true)
public class ConsoleCommands {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private volatile MeteringService meteringService;

    public void estimationBlocks(long meterId) {
        EstimationEngine estimationEngine = new EstimationEngine();
        Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
        meter.getCurrentMeterActivation().ifPresent(meterActivation -> {
            meterActivation.getChannels().stream()
                    .flatMap(channel -> channel.getReadingTypes().stream())
                    .flatMap(readingType -> estimationEngine.findBlocksToEstimate(meterActivation, readingType).stream())
                    .map(this::print)
                    .forEach(System.out::println);
        });
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    private String print(EstimationBlock estimationBlock) {
        StringBuilder builder = new StringBuilder();
        builder.append("Block channel : ").append(estimationBlock.getChannel().getId()).append('\n')
                .append("  readingType : ").append(estimationBlock.getReadingType().getMRID()).append('\n');

        estimationBlock.estimatable().stream()
                .map(Estimatable::getTimestamp)
                .map(instant -> ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .map(FORMATTER::format)
                .forEach(formattedTime -> builder.append('\t').append(formattedTime).append('\n'));

        return builder.toString();
    }
}
