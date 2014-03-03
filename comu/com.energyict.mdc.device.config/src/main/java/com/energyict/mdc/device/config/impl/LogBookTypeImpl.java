package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

/**
 * Copyrights EnergyICT
 * Date: 24/10/12
 * Time: 10:35
 */
public class LogBookTypeImpl extends PersistentNamedObject<LogBookType> implements LogBookType {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED_KEY + "}")
    private String obisCodeString;
    private ObisCode obisCode;
    private String description;

    private DeviceConfigurationService deviceConfigurationService;

    @Inject
    public LogBookTypeImpl(DataModel dataModel, EventService eventService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus) {
        super(LogBookType.class, dataModel, eventService, thesaurus);
        this.deviceConfigurationService = deviceConfigurationService;
    }

    LogBookTypeImpl initialize(String name, ObisCode obisCode) {
        this.setName(name);
        this.setObisCode(obisCode);
        return this;
    }

    static LogBookTypeImpl from (DataModel dataModel, String name, ObisCode obisCode) {
        return dataModel.getInstance(LogBookTypeImpl.class).initialize(name, obisCode);
    }

    protected void post() {
        this.validateDeviceConfigurations();
        super.post();
    }

    private void validateDeviceConfigurations() {
        for (DeviceConfiguration each : this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(this)) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateLogBookType(this);
        }
    }

    @Override
    protected void validateDelete() {
        // Validate that the LogBookType is not in use by a DeviceType
        List<DeviceTypeLogBookTypeUsage> logBookTypeUsages = this.dataModel.mapper(DeviceTypeLogBookTypeUsage.class).find("logBookType", this);
        if (!logBookTypeUsages.isEmpty()) {
            List<DeviceType> deviceTypes = new ArrayList<>(logBookTypeUsages.size());
            for (DeviceTypeLogBookTypeUsage logBookTypeUsage : logBookTypeUsages) {
                deviceTypes.add(logBookTypeUsage.getDeviceType());
            }
            throw CannotDeleteBecauseStillInUseException.logBookTypeIsStillInUseByDeviceType(this.getThesaurus(), this, deviceTypes);
        }
    }

    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.LOGBOOKTYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.LOGBOOKTYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.LOGBOOKTYPE;
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.logBookTypeAlreadyExists(this.getThesaurus(), name);
    }

    @Override
    public ObisCode getObisCode() {
        if (this.obisCode == null) {
            this.obisCode = ObisCode.fromString(this.obisCodeString);
        }
        return this.obisCode;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            // javax.validation will throw ConstraintValidationException in the end
            this.obisCodeString = null;
            this.obisCode = null;
        }
        else {
            if (!is(this.obisCodeString).equalTo(obisCode.toString())) {
                if (!canChangeObisCode()) {
                    throw new CannotUpdateObisCodeWhenLogBookTypeIsInUseException(this.getThesaurus(), this);
                }
            }
            this.obisCodeString = obisCode.toString();
            this.obisCode = obisCode;
        }
    }

    private boolean canChangeObisCode() {
        return !this.inUseByLogBookSpecs();
    }

    private boolean inUseByLogBookSpecs() {
        List<LogBookSpecImpl> logBookTypes = this.dataModel.mapper(LogBookSpecImpl.class).find("logBookType", this);
        return !logBookTypes.isEmpty();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}


