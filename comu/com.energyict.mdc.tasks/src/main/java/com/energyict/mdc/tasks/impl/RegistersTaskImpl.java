package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.tasks.RegistersTask;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;

import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.REGISTERS_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.RegistersTask}.
 *
 * @author gna
 * @since 30/04/12 - 13:26
 */
class RegistersTaskImpl extends ProtocolTaskImpl implements RegistersTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(REGISTERS_FLAG, SLAVE_DEVICES_FLAG);

    enum Fields {
        REGISTER_GROUP_USAGES("registerGroupUsages");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private List<RegisterGroupUsage> registerGroupUsages = new ArrayList<>();

    @Inject
    public RegistersTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public List<RegisterGroup> getRegisterGroups() {
        List<RegisterGroup> registerGroups = new ArrayList<>(registerGroupUsages.size());
        for (RegisterGroupUsage registerGroupUsage : registerGroupUsages) {
            registerGroups.add(registerGroupUsage.getRegistersGroup());
        }

        return registerGroups;
    }

    @Override
    public void setRegisterGroups(Collection<RegisterGroup> registerGroups) {
        // TODO implement
    }

    /**
     * Update the RegisterGroupUsage map
     * based on the given RegistersTaskShadow. The shadow will contain the <i>new</i> List of RegisterGroups
     * which must be mapped to this RegistersTask.<br>
     * We need to check:<ul>
     * <li>if some references already exist, keep them</li>
     * <li>if some references have to be deleted</li>
     * <li>if some references have to be added</li>
     * </ul>
     *
     * @param registerGroups the RegisterGroups
     */
    protected void updateRegisterGroups (final List<RegisterGroup> registerGroups) throws SQLException {
        // TODO Implement
    }


    void deleteDependents() {
        this.registerGroupUsages.clear();
    }

}
