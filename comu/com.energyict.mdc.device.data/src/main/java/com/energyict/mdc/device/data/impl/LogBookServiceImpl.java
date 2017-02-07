/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link LogBookService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
public class LogBookServiceImpl implements ServerLogBookService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public LogBookServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public Optional<LogBook> findById(long id) {
        return this.deviceDataModelService.dataModel().mapper(LogBook.class).getOptional(id);
    }

    @Override
    public List<LogBook> findLogBooksByDevice(Device device) {
        return this.deviceDataModelService.dataModel().mapper(LogBook.class).find("device", device);
    }

}