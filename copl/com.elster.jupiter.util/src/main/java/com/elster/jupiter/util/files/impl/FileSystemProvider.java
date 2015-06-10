package com.elster.jupiter.util.files.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@Component(name = "com.elster.jupiter.file.filesystem", immediate = true)
public class FileSystemProvider {

    private volatile ServiceRegistration<FileSystem> registration;

    public FileSystemProvider() {
    }

    @Activate
    public void activate(BundleContext context) {
        registration = context.registerService(FileSystem.class, FileSystems.getDefault(), null);
    }

    @Deactivate
    public void deactivate() {
        registration.unregister();
    }

}
