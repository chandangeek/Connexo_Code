package com.elster.jupiter.util;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.time.Clock;

public class UtilModule extends AbstractModule {
	
	private final Clock clock;
    private final FileSystem fileSystem;

    public UtilModule(Clock clock, FileSystem fileSystem) {
        this.clock = clock;
        this.fileSystem = fileSystem;
    }

    public UtilModule(Clock clock) {
        this(clock, FileSystems.getDefault());
    }

    public UtilModule(FileSystem fileSystem) {
        this(Clock.systemDefaultZone(), fileSystem);
    }

    public UtilModule() {
        this(Clock.systemDefaultZone(), FileSystems.getDefault());
    }

    @Override
    protected void configure() {
        bind(JsonService.class).to(JsonServiceImpl.class).in(Scopes.SINGLETON);
        bind(BeanService.class).to(BeanServiceImpl.class).in(Scopes.SINGLETON);
        bind(CronExpressionParser.class).to(DefaultCronExpressionParser.class).in(Scopes.SINGLETON);
        bind(Clock.class).toInstance(clock);
        bind(FileSystem.class).toInstance(fileSystem);
    }
}
