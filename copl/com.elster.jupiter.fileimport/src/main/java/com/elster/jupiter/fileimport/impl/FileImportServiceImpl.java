package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
import oracle.jdbc.aq.AQMessage;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

@Component(name = "com.elster.jupiter.fileimport", service = {InstallService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class FileImportServiceImpl implements InstallService, ServiceLocator, FileImportService {

    private volatile LogService logService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile OrmClient ormClient;
    private volatile Clock clock;

    private Thread thread;
    private CronExpressionScheduler cronExpressionScheduler;

    @Override
    public void install() {
        new InstallerImpl().install();
    }

    @Override
    public LogService getLogService() {
        return logService;
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public CronExpressionParser getCronExpressionParser() {
        return cronExpressionParser;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter File Import");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void activate(ComponentContext context) {
        try {
			Bus.setServiceLocator(this);
            CronExpression cronExpression = Bus.getCronExpressionParser().parse("0/10 * * * * ? *");
            DestinationSpec spec = new DestinationSpec() {
                @Override
                public QueueTableSpec getQueueTableSpec() {
                    //TODO automatically generated method body, provide implementation.
                    return null;
                }

                @Override
                public void activate() {
                    //TODO automatically generated method body, provide implementation.

                }

                @Override
                public void deactivate() {
                    //TODO automatically generated method body, provide implementation.

                }

                @Override
                public String getName() {
                    //TODO automatically generated method body, provide implementation.
                    return null;
                }

                @Override
                public boolean isTopic() {
                    //TODO automatically generated method body, provide implementation.
                    return false;
                }

                @Override
                public boolean isQueue() {
                    //TODO automatically generated method body, provide implementation.
                    return false;
                }

                @Override
                public String getPayloadType() {
                    //TODO automatically generated method body, provide implementation.
                    return null;
                }

                @Override
                public boolean isActive() {
                    //TODO automatically generated method body, provide implementation.
                    return false;
                }

                @Override
                public void send(String text) {
                    //TODO automatically generated method body, provide implementation.

                }

                @Override
                public void send(byte[] bytes) {
                    //TODO automatically generated method body, provide implementation.

                }

                @Override
                public void send(AQMessage message) throws SQLException {
                    //TODO automatically generated method body, provide implementation.

                }

                @Override
                public List<SubscriberSpec> getConsumers() {
                    //TODO automatically generated method body, provide implementation.
                    return null;
                }

                @Override
                public SubscriberSpec subscribe(String name, int workerCount) {
                    //TODO automatically generated method body, provide implementation.
                    return null;
                }
            };
            ImportScheduleImpl importSchedule = new ImportScheduleImpl(cronExpression, spec, new File("C:/Users/tgr/Work/Temp/import"), new File("C:/Users/tgr/Work/Temp/inprogress"), new File("C:/Users/tgr/Work/Temp/error"), new File("C:/Users/tgr/Work/Temp/success"));
            cronExpressionScheduler = new CronExpressionScheduler(1);
            cronExpressionScheduler.submit(new ImportScheduleJob(importSchedule));
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void deactivate(ComponentContext context) {
        thread.interrupt();
        cronExpressionScheduler.shutdown();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bus.setServiceLocator(null);
    }

    @Override
    public ImportScheduleBuilder newBuilder() {
        return new DefaultImportScheduleBuilder();
    }
}
