package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER;

/**
 * Examples of usage:
 * <ul>
 * <li>create Example deviceType.id 23 deviceConfiguration.id 54: creates a device group (named Example) that will contain devices whose type is 23 and configuration is 54</li>
 * </ul>.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-10 (12:08)
 */
@Component(name = "com.energyict.mdc.device.data.mdcgroups", service = MdcDeviceGroups.class,
        property = {
                "osgi.command.scope=mdcgroups",
                "osgi.command.function=createOr",
                "osgi.command.function=createAnd",
                "osgi.command.function=contents"},
        immediate = true)
@SuppressWarnings("unused")
public class MdcDeviceGroups {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile DeviceService deviceService;

    @SuppressWarnings("unused")
    public void createOr(String name, String mRID, String... fieldsAndValues) {
        this.transactionService.execute(() -> this.doCreate(BooleanOperand.OR, name, mRID, fieldsAndValues));
    }

    @SuppressWarnings("unused")
    public void createAnd(String name, String mRID, String... fieldsAndValues) {
        this.transactionService.execute(() -> this.doCreate(BooleanOperand.AND, name, mRID, fieldsAndValues));
    }

    private EndDeviceGroup doCreate(BooleanOperand operand, String name, String mRID, String... fieldsAndValues) {
        if (this.numberOfFieldsAndValuesMatch(fieldsAndValues)) {
            this.setPrincipal();
            Condition condition = this.conditionFromFieldsAndValues(operand, fieldsAndValues);
            QueryEndDeviceGroup queryEndDeviceGroup = this.meteringGroupsService.createQueryEndDeviceGroup(condition)
                    .setMRID(mRID)
                    .setName(name)
                    .setQueryProviderName(DEVICE_ENDDEVICE_QUERYPROVIDER)
                    .create();
            System.out.println("Create group with id: " + queryEndDeviceGroup.getId());
            return queryEndDeviceGroup;
        }
        else {
            System.out.println("The number of fields and values must match, i.e. every field must have a value");
            System.out.println("Usage:\n\t mdcgroups:create <name> <mRID> <fieldsAndValues>");
            return null;
        }
    }

    private Condition conditionFromFieldsAndValues(BooleanOperand operand, String[] fieldsAndValues) {
        Condition condition = operand.initial();
        Iterator<String> iterator = Arrays.asList(fieldsAndValues).iterator();
        while (iterator.hasNext()) {
            String field = iterator.next();
            String value = iterator.next();
            condition = operand.applyTo(condition, where(field).isEqualTo(value));
        }
        return condition;
    }

    private boolean numberOfFieldsAndValuesMatch(String[] fieldsAndValues) {
        return fieldsAndValues.length % 2 == 0;
    }

    public void contents(String mRID) {
        Optional<EndDeviceGroup> deviceGroup = this.meteringGroupsService.findEndDeviceGroup(mRID);
        if (deviceGroup.isPresent()) {
            if (deviceGroup.get() instanceof QueryEndDeviceGroup) {
                QueryEndDeviceGroup queryDeviceGroup = (QueryEndDeviceGroup) deviceGroup.get();
                this.deviceService.findAllDevices(queryDeviceGroup.getCondition()).stream()
                    .forEach(this::print);
            }
            else {
                System.out.println("Currently only support for " + QueryEndDeviceGroup.class.getName() + " but got " + deviceGroup.getClass().getName());
            }
        }
        else {
            System.out.println("Device group with mRID '" + mRID + "' does not exist");
        }
    }

    private void print(Device device) {
        System.out.println("\t" + device.getId() + " - " + device.getmRID());
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return () -> "gogo-console";
    }

    @Reference
    @SuppressWarnings("unused")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    private enum BooleanOperand {
        OR {
            @Override
            public Condition initial() {
                return Condition.FALSE;
            }

            @Override
            public Condition applyTo(Condition c1, Condition c2) {
                return c1.or(c2);
            }
        },

        AND {
            @Override
            public Condition initial() {
                return Condition.TRUE;
            }

            @Override
            public Condition applyTo(Condition c1, Condition c2) {
                return c1.and(c2);
            }
        };

        public abstract Condition applyTo(Condition c1, Condition c2);

        public abstract Condition initial();
    }

}