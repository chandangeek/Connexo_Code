package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.fileimport", service = {InstallService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class FileImportServiceImpl implements InstallService, ServiceLocator {

    private volatile LogService logService;

    private Thread thread;

    @Override
    public void install() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public LogService getLogService() {
        return logService;
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public void activate(ComponentContext context) {
        Bus.setServiceLocator(this);
        FolderScanner scanner = new PollingFolderScanner(Paths.get("T:/Tom D/dump"), 1, TimeUnit.MINUTES);
        PathHandler handler = new DefaultPathHandler();
        thread = new Thread(new FolderScanningJob(scanner, handler));
        thread.start();
    }

    public void deactivate(ComponentContext context) {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bus.setServiceLocator(null);
    }
}
