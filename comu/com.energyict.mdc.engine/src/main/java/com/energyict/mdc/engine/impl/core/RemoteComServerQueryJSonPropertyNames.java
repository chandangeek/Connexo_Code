/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;

/**
 * Defines constants that represent the property names of the JSon object
 * that will pass query information back and forth between
 * a remote and online com server.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (14:50)
 */
public final class RemoteComServerQueryJSonPropertyNames {

    /**
     * The name of the property that specifies a type String
     * that is passed as an argument to a remote com server query.
     */
    public static final String TYPE = "type";
    // or
    public static final String XML_TYPE = "xml-type";

    /**
     * The name of the property that specifies
     * which remote com server query to execute.
     */
    public static final String METHOD = "method";

    /**
     * The name of the property that uniquely identifies
     * the query in the client context.
     */
    public static final String QUERY_ID = "query-id";

    /**
     * The if of the property that uniquely identifies
     * the user in the client context.
     */
    public static final String USER_ID = "user-id";

    /**
     * The name of the property that contains the result
     * of a query that returns a single object.
     */
    public static final String SINGLE_OBJECT_RESULT = "single-value";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link ComServer}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMSERVER = "com-server";

    /**
     * The name of the property that specifies the host name
     * of a {@link RemoteComServer}
     * that is passed as an argument to a remote com server query.
     */
    public static final String HOSTNAME = "host-name";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link ComPort}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMPORT = "com-port";

    /**
     * The name of the property that specifies the current high priority load
     * that is passed as an argument to a remote com server query.
     */
    public static final String CURRENT_HIGH_PRIORITY_LOAD = "current-high-priority-load";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link ComTask}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMTASK = "com-task";


    /**
     * The name of the property that specifies the unique identifier
     * of a {@link ComTaskExecution}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMTASKEXECUTION = "com-task-execution";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link PriorityComTaskExecutionLink}
     * that is passed as an argument to a remote com server query.
     */
    public static final String HIGH_PRIORITY_COMTASKEXECUTION = "hi-prio-com-task-execution";

    /**
     * The name of the property that specifies a collection of unique identifiers
     * of {@link ComTaskExecution}s
     * that are passed as an argument to a remote com server query.
     */
    public static final String COMTASKEXECUTION_COLLECTION = "com-task-executions";

    /**
     * The name of the property that specifies a collection of unique identifiers
     * of {@link PriorityComTaskExecutionLink}s
     * that are passed as an argument to a remote com server query.
     */
    public static final String HIGH_PRIORITY_COMTASKEXECUTION_COLLECTION = "hi-prio-com-task-executions";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link ConnectionTask}
     * that is passed as an argument to a remote com server query.
     */
    public static final String CONNECTIONTASK = "connection-task";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link com.energyict.mdc.upl.meterdata.Device device}
     * that is passed as an argument to a remote com server query.
     */
    public static final String DEVICE = "device";

    /**
     * The id property that specifies the unique identifier
     * of a {@link com.energyict.mdc.upl.meterdata.Device device}
     * that is passed as an argument to a remote com server query.
     */
    public static final String DEVICE_IDENTIFIER = "device-identifier";

    /**
     * The ID that specifies the unique SecurityPropertySet
     */
    public static final String SECURITY_PROPERTY_SET_IDENTIFIER = "security-property-set-identifier";

    /**
     * The name of the property that specifies the current date
     * of an object who's unique identifier is passed as an argument
     * to a remote com server query.
     * Note that the value of the property is expected to be
     * the number of milliseconds since Jan 1st, 1970 (UTC).
     */
    public static final String CURRENT_DATE = "current-date";

    /**
     * The name of the property that specifies the modification date
     * of an object who's unique identifier is passed as an argument
     * to a remote com server query.
     * Note that the value of the property is expected to be
     * the number of milliseconds since Jan 1st, 1970 (UTC).
     */
    public static final String MODIFICATION_DATE = "mod-date";

    /**
     * The name of the property that specifies the maximum number
     * of tries for a {@link ScheduledConnectionTask}.
     */
    public static final String MAX_NR_OF_TRIES = "max-tries";

    /**
     * The name of the property that specifies when we need to reschedule the ComtaskExecution
     */
    public static final String RESCHEDULE_DATE = "reschedule-time";

    /**
     * The name of the property that specifies the name of the protocol property
     */
    public static final String PROTOCOL_PROPERTY_NAME = "protocol-property-name";

    /**
     * The name of the property that specifies the protocol properties to update
     */
    public static final String PROTOCOL_TYPED_PROPERTIES = "protocol-typed-properties";

    /**
     * A full description of offline the device context
     */
    public static final String OFFLINE_DEVICE_CONTEXT = "offline-device-context";

    /**
     * The name of the property that specifies the device cache
     * that is passed as an argument to a remote com server query.
     */
    public static final String DEVICE_CACHE = "device-cache";

    /**
     * The name of the property that specifies the {@link com.energyict.protocol.MeterReadingData}
     * that is passed as an argument to a remote com server query.
     */
    public static final String METER_READING = "meter-reading";

    /**
     * The name of the property that specifies the {@link CollectedLoadProfile}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COLLECTED_LOADPROFILE = "collected-loadprofile";
    /**
     * The name of the property that specifies the {@link CollectedLogBook}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COLLECTED_LOGBOOK = "collected-logbook";

    /**
     * The name of the property that specifies the {@link CollectedLogBook}
     * last reading argument to a remote com server query.
     */
    public static final String LOGBOOK_LAST_READING = "logbook-last-reading";

    /**
     * The name of the property that specifies the
     * last reading argument to a remote com server query.
     */
    public static final String LAST_READINGS = "last-readings";

    /**
     * The name of the property that specifies the
     * last logbooks argument to a remote com server query.
     */
    public static final String LAST_LOGBOOKS = "last-logbooks";

    /**
     * The name of the property that specifies the unique
     * {@link com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier} of a devices Register
     * that is passed as an argument to a remote com server query.
     */
    public static final String REGISTER_IDENTIFIER = "register-identifier";

    /**
     * The name of the property that specifies the date
     * of an object who's unique identifier is passed as an argument
     * to a remote com server query.
     */
    public static final String WHEN = "when";

    /**
     * The name of the property that specifies the unique
     * {@link com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier} of a devices LoadProfile
     * that is passed as an argument to a remote com server query.
     */
    public static final String LOADPROFILE_IDENTIFIER = "loadprofile-identifier";

    /**
     * The name of the property that specifies the unique
     * {@link com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier} of a devices LogBook
     * that is passed as an argument to a remote com server query.
     */
    public static final String LOGBOOK_IDENTIFIER = "logbook-identifier";

    /**
     * The name of the property that specifies the unique
     * {@link com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier} of a DeviceMessage
     * that is passed as an argument to a remote com server query.
     */
    public static final String MESSAGE_IDENTIFIER = "message-identifier";

    /**
     * The name of the property that specifies the unique
     * {@link com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier DeviceIdentifier} of the gateway device
     * that is passed as an argument to a remote com server query.
     */
    public static final String GATEWAY_DEVICE_IDENTIFIER = "gateway-identifier";

    /**
     * The name of the property that specifies a date format
     * that is passed as an argument to a remote com server query.
     * Note that the value of the property is expected to be
     * the pattern of the date format.
     */
    public static final String DATE_FORMAT = "date-format";

    /**
     * The name of the property that specifies a file extension
     * that is passed as an argument to a remote com server query.
     */
    public static final String FILE_EXTENSION = "file-extension";

    /**
     * The name of the property that specifies a byte array
     * that is passed as an argument to a remote com server query.
     */
    public static final String HEX_DATA = "hex-data";

    /**
     * The name of the file
     */
    public static final String FILE_NAME = "file-name";

    /**
     * The name of the property that specifies the {@link com.energyict.mdc.upl.messages.DeviceMessageStatus} of a DeviceMessage
     * that is passed as an argument to a remote com server query.
     */
    public static final String MESSAGE_STATUS = "message-status";

    /**
     * The name of the property that specifies the protocol information String of a DeviceMessage
     * that is passed as an argument to a remote com server query.
     */
    public static final String MESSAGE_INFORMATION = "message-information";

    /**
     * The name of the property that specifies the sent date
     * of an object who's unique identifier is passed as an argument
     * to a remote com server query.
     */
    public static final String SENT_DATE = "sent-date";

    /**
     * The name of the property that specifies the number of confirmed DeviceMessages
     * that is passed as an argument to a remote com server query.
     */
    public static final String MESSAGE_CONFIRMATION_COUNT = "message-confirmation-count";


    public static final String COMSESSION_BUILDER = "comsession_bilder";

    public static final String COMSESSION_STOP_DATE = "comsession_stop_date";

    public static final String COMSESSION_SUCCESS_INDICATOR = "comsession_success_indicator";

    /**
     * The name of the property that contains the result
     * of a query that returns a single object.
     */
    public static final String SINGLE_OBJECT_VALUE = "single-value";

    public static final String AUTH_DATA = "auth_data";

    // Hide utility class constructor
    private RemoteComServerQueryJSonPropertyNames() {
    }

}