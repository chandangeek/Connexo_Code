package com.energyict.mdc.task.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.task.MessagesTask;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.PENDING_MESSAGES_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SENT_MESSAGES_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation of a {@link com.energyict.mdc.task.MessagesTask}
 *
 * @author gna
 * @since 2/05/12 - 13:39
 */
class MessagesTaskImpl extends ProtocolTaskImpl implements MessagesTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(PENDING_MESSAGES_FLAG, SENT_MESSAGES_FLAG, SLAVE_DEVICES_FLAG);

    enum Fields {
        ALL_CATEGORIES("allCategories"),
        DEVICE_MESSAGE_USAGES("deviceMessageUsages");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }
    private long id;
    private List<MessagesTaskTypeUsage> deviceMessageUsages = new ArrayList<>();
    private boolean allCategories;

    @Inject
    public MessagesTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public List<DeviceMessageCategory> getDeviceMessageCategories() {
        List<DeviceMessageCategory> deviceMessageCategories = new ArrayList<>(this.deviceMessageUsages.size());
        for (MessagesTaskTypeUsage deviceMessageCategoriesUsage : deviceMessageUsages) {
            if (deviceMessageCategoriesUsage.hasDeviceMessageCategory()) {
                deviceMessageCategories.add(deviceMessageCategoriesUsage.getDeviceMessageCategory());
            }
        }
        return deviceMessageCategories;
    }

    @Override
    public void setDeviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories) {

    }

    @Override
    public void setDeviceMessageSpecs(List<DeviceMessageSpec> deviceMessageSpecs) {

    }

    @Override
    public List<DeviceMessageSpec> getDeviceMessageSpecs() {
        List<DeviceMessageSpec> deviceMessageSpecs = new ArrayList<>(this.deviceMessageUsages.size());
        for (MessagesTaskTypeUsage deviceMessageSpecUsage : deviceMessageUsages) {
            if (deviceMessageSpecUsage.hasDeviceMessageSpec()) {
                deviceMessageSpecs.add(deviceMessageSpecUsage.getDeviceMessageSpec());
            }
        }
        return deviceMessageSpecs;
    }

    @Override
    public boolean isAllCategories() {
        return allCategories;
    }

    @Override
    public void setAllCategories(boolean allCategories) {
        this.allCategories = allCategories;
    }

    void deleteDependents() {
        this.deviceMessageUsages.clear();
    }

    public void toJournalMessageDescription(DescriptionBuilder builder) {
        if (this.allCategories) {
            builder.addLabel("All categories and messages");
        }
        else {
            this.writeCategoriesToJournalDescription(builder);
            this.writeMessagesSpecsToJournalDescription(builder);
        }
    }

    private void writeCategoriesToJournalDescription (DescriptionBuilder builder) {
        PropertyDescriptionBuilder messageCategoriesBuilder = builder.addListProperty("messageCategories");
        for (DeviceMessageCategory deviceMessageCategory : this.getDeviceMessageCategories()) {
            messageCategoriesBuilder = messageCategoriesBuilder.append(deviceMessageCategory.getName()).next();
        }
    }

    private void writeMessagesSpecsToJournalDescription (DescriptionBuilder builder) {
        PropertyDescriptionBuilder messageSpecsBuilder = builder.addListProperty("messageSpecs");
        for (DeviceMessageSpec deviceMessageSpec : this.getDeviceMessageSpecs()) {
            messageSpecsBuilder = messageSpecsBuilder.append(deviceMessageSpec.getName()).next();
        }
    }

}