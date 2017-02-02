/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */
package com.energyict.mdc.device.config.impl.gogo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.ServerDeviceConfigurationService;
import com.energyict.mdc.protocol.api.DeviceMessageFile;

import com.google.common.base.MoreObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (12:59)
 */
@Component(name = "com.energyict.mdc.device.type.messagefiles",
        service = DeviceMessageFileCommands.class,
        property = {
                "osgi.command.scope=mdc.device.message",
                "osgi.command.function=listFiles",
                "osgi.command.function=addFile"},
        immediate = true)
@SuppressWarnings("unused")
public class DeviceMessageFileCommands {
    private volatile ServerDeviceConfigurationService deviceConfigurationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @SuppressWarnings("unused")
    public void listFiles() {
        System.out.println("listFiles <device type id>");
    }

    @SuppressWarnings("unused")
    public void listFiles(long deviceTypeId) {
        DeviceType deviceType = this.findDeviceTypeOrThrowException(deviceTypeId);
        System.out.println(
                deviceType
                        .getDeviceMessageFiles()
                        .stream()
                        .map(this::toString)
                        .collect(Collectors.joining("\n")));
    }

    private DeviceType findDeviceTypeOrThrowException(long deviceTypeId) {
        return this.deviceConfigurationService
                    .findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Devicetype with id " + deviceTypeId + " does not exist"));
    }

    private String toString(DeviceMessageFile deviceMessageFile) {
        return MoreObjects.toStringHelper(deviceMessageFile)
                .add("id", deviceMessageFile.getId())
                .add("name", deviceMessageFile.getName())
                .toString();
    }

    @SuppressWarnings("unused")
    public void addFile() {
        System.out.println("addFile <device type id> <path to file>");
    }

    @SuppressWarnings("unused")
    public void addFile(long deviceTypeId, String pathToFile) {
        this.threadPrincipalService.set(() -> "DeviceMessageFileCommands");
        DeviceType deviceType = this.findDeviceTypeOrThrowException(deviceTypeId);
        try (TransactionContext context = this.transactionService.getContext()) {
            deviceType.addDeviceMessageFile(Paths.get(pathToFile));
            context.commit();
        } finally {
            this.threadPrincipalService.clear();
        }
    }

}