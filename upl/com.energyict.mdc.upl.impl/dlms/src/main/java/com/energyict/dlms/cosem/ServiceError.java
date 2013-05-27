package com.energyict.dlms.cosem;

/**
 * Copyrights EnergyICT
 * Date: 10/10/12
 * Time: 11:06 AM
 */
public enum ServiceError {

    APP_REFERENCE_OTHER(0, 0, "Application-reference", "Other"),
    APP_REFERENCE_TIMEOUT(0, 1, "Application-reference", "Time out since request sent"),
    APP_REFERENCE_UNREACHABLE(0, 2, "Application-reference", "Peer AEi not reachable"),
    APP_REFERENCE_ADDRESSING_ERROR(0, 3, "Application-reference", "Addressing trouble"),
    APP_REFERENCE__APP_CONTEXT_INCOMPATIBLE(0, 4, "Application-reference", "Application-context incompatibility"),
    APP_REFERENCE_EQUIPMENT_ERROR(0, 5, "Application-reference", "Error at the local or distant equipment"),
    APP_REFERENCE_CIPHER_ERROR(0, 6, "Application-reference", "Error detected by the deciphering function"),

    HARDWARE_RESOURCE_OTHER(1, 0, "Hardware-resource", "Other"),
    HARDWARE_RESOURCE_MEMORY(1, 1, "Hardware-resource", "Memory unavailable"),
    HARDWARE_RESOURCE_PROCESSOR(1, 2, "Hardware-resource", "Processor-resource unavailable"),
    HARDWARE_RESOURCE_MASS_STORAGE(1, 3, "Hardware-resource", "Mass-storage unavailable"),
    HARDWARE_RESOURCE_OTHER_RESOURCE(1, 4, "Hardware-resource", "Other resource unavailable"),

    VDE_STATE_ERROR_OTHER(2, 0, "VDE-State-error", "Other"),
    VDE_STATE_ERROR_NO_DLMS_CONTEXT(2, 1, "VDE-State-error", "No DLMS context"),
    VDE_STATE_ERROR_LOADING_DATA_SET(2, 2, "VDE-State-error", "Loading data-set"),
    VDE_STATE_ERROR_STATUS_NOCHANGE(2, 3, "VDE-State-error", "Status nochange"),
    VDE_STATE_ERROR_STATUS_INOPERABLE(2, 4, "VDE-State-error", "Status inoperable"),

    SERVICE_OTHER(3, 0, "Service", "Other"),
    SERVICE_PDU_SIZE(3, 1, "Service", "PDU size to long"),
    SERVICE_UNSUPPORTED(3, 2, "Service", "Service unsupported"),

    DEFINITION_OTHER(4, 0, "Definition", "Other"),
    DEFINITION_OBJECT_UNDEFINED(4, 1, "Definition", "Object undefined"),
    DEFINITION_CLASS_INCONSISTENT(4, 2, "Definition", "Object class inconsistent"),
    DEFINITION_ATTRIBUTE_INCONSISTENT(4, 3, "Definition", "Object attribute inconsistent"),

    ACCESS_OTHER(5, 0, "Access", "Other"),
    ACCESS_SCOPE_OF_ACCESS_VIOLATED(5, 1, "Access", "Scope of access violated"),
    ACCESS_OBJECT_ACCESS(5, 2, "Access", "Object access violated"),
    ACCESS_HARDWARE(5, 3, "Access", "Hardware-fault"),
    ACCESS_OBJECT_UNAVAILABLE(5, 4, "Access", "Object unavailable"),

    INITIATE_OTHER(6, 0, "Initiate", "Other"),
    INITIATE__DLMS_VERSION(6, 1, "Initiate", "DLMS version too low"),
    INITIATE_INCOMPATIBLE_CONFORMANCE(6, 2, "Initiate", "Incompatible conformance"),
    INITIATE_PDU_SIZE_TOO_SHORT(6, 3, "Initiate", "PDU size too short"),
    INITIATE_REFUSED_BY_VDE_HANDLER(6, 4, "Initiate", "Refused by the VDE Handler"),

    LOAD_DATA_SET_OTHER(7, 0, "Load-Data-Set", "Other"),
    LOAD_DATA_SET_OUT_OF_SEQ(7, 1, "Load-Data-Set", "Primitive out of sequence"),
    LOAD_DATA_SET_NOT_LOADABLE(7, 2, "Load-Data-Set", "Not loadable"),
    LOAD_DATA_SET_TOO_LARGE(7, 3, "Load-Data-Set", "Evaluated data set size too large"),
    LOAD_DATA_SET_SEGMENT_NOT_AWAITED(7, 4, "Load-Data-Set", "Proposed segment not awaited"),
    LOAD_DATA_SET_SEGMENT_INTERPRETATION_ERROR(7, 5, "Load-Data-Set", "Segment interpretation error"),
    LOAD_DATA_SET_SEGMENT_STORAGE_ERROR(7, 6, "Load-Data-Set", "Segment storage error"),
    LOAD_DATA_SET_INCORRECT_UPLOAD_STATE(7, 7, "Load-Data-Set", "Data set not in correct state for uploading"),

    CHANGE_SCOPE(8, 0, "Change Scope", "Other"),

    TASK_OTHER(9, 0, "Task", "Other"),
    TASK_REMOTE_PARAMETER_DISABLED(9, 1, "Task", "Remote control parameter set to FALSE"),
    TASK_STOPPED(9, 2, "Task", "TI in stopped state"),
    TASK_RUNNING(9, 3, "Task", "TI in running state"),
    TASK_UNUSABLE(9, 4, "Task", "TI in unusable state"),

    OTHER(10, 0, "Other", "Other");

    private final int categoryCode;
    private final int detailCode;
    private final String categoryMessage;
    private final String detailMessage;

    private ServiceError(final int categoryCode, final int detailCode, final String categoryMessage, final String detailMessage) {
        this.categoryCode = categoryCode;
        this.detailCode = detailCode;
        this.categoryMessage = categoryMessage;
        this.detailMessage = detailMessage;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public int getDetailCode() {
        return detailCode;
    }

    public String getCategoryMessage() {
        return categoryMessage;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public final String getDescription() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getCategoryMessage()).append('[').append(getCategoryCode()).append("] - ");
        sb.append(getDetailMessage()).append('[').append(getDetailCode()).append(']');
        return sb.toString();
    }

    public static ServiceError getServiceErrorByCode(final int categoryCode, final int detailCode) {
        boolean categoryFound = false;
        for (final ServiceError serviceError : values()) {
            if (serviceError.getCategoryCode() == categoryCode) {
                categoryFound = true;
                if (serviceError.getDetailCode() == detailCode) {
                    return serviceError;
                }
            }
        }

        if (categoryFound && detailCode != 0) {
            return getServiceErrorByCode(categoryCode, 0);
        }
        return OTHER;
    }

}
