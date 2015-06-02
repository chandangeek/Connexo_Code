package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.finders.LogBookFinder;
import java.util.Optional;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

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
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new LogBookFinder(this.deviceDataModelService.dataModel()));
        return finders;
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