package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Strings to {@link Request}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (17:57)
 */
public class RequestParser {

    public interface ServiceProvider {

        public DeviceDataService deviceDataService();

        public EngineModelService engineModelService();

    }

    private static final Pattern COMMAND_PATTERN = Pattern.compile("(?i)register request (?:for events )?for (.*):\\s*(.*)");
    private List<RequestType> requestTypes;
    private final ServiceProvider serviceProvider;

    public RequestParser(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Request parse (String message) throws RequestParseException {
        Matcher matcher = COMMAND_PATTERN.matcher(message);
        if (matcher.matches()) {
            String narrowType = matcher.group(1);
            String narrowSpecs = matcher.group(2);
            RequestType requestType = this.parseRequestType(narrowType, matcher.start(1));
            return requestType.parse(narrowSpecs);
        }
        else {
            throw new UnexpectedRequestFormatException(COMMAND_PATTERN.toString());
        }
    }

    private RequestType parseRequestType (String requestType, int requestTypeOffset) throws RequestTypeParseException {
        if (this.requestTypes == null) {
            this.initializeRequestTypes();
        }
        for (RequestType type : this.requestTypes) {
            if (type.canParse(requestType)) {
                return type;
            }
        }
        throw new RequestTypeParseException(requestType, requestTypeOffset);
    }

    private void initializeRequestTypes () {
        this.requestTypes =
                Arrays.asList(
                        new ErrorLoggingRequestType(),
                        new WarningLoggingRequestType(),
                        new InfoLoggingRequestType(),
                        new DebugLoggingRequestType(),
                        new TraceLoggingRequestType(),
                        new DeviceRequestType(serviceProvider.deviceDataService()),
                        new ConnectionTaskRequestType(serviceProvider.deviceDataService()),
                        new ComTaskExecutionRequestType(serviceProvider.deviceDataService()),
                        new ComPortRequestType(serviceProvider.engineModelService()),
                        new ComPortPoolRequestType(serviceProvider.engineModelService()));
    }

}