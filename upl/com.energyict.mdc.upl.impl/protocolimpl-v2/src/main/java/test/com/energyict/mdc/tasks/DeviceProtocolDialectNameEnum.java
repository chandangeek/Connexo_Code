package test.com.energyict.mdc.tasks;

/**
 * Specifies the possible DeviceProtocolDialect names.
 *
 * <p>
 * Each name should be unique, as it is used in RelationTypes! <BR>
 * The length of the name is limited to 24 characters!
 * </p>
 *
 * @author: sva
 * @since: 17/10/12 (10:23)
 */
public enum DeviceProtocolDialectNameEnum {

    CTR_DEVICE_PROTOCOL_DIALECT_NAME("CtrDialect");

    DeviceProtocolDialectNameEnum(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getName() {
        return uniqueName;
    }

    private String uniqueName;
}
