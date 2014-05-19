package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.core.ServiceProvider;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.Checks.is;

/**
 * Parses Strings to {@link Request}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (17:57)
 */
public class RequestParser {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("(?i)register request (?:for (text|binary) events )?for (.*):\\s*(.*)");
    private List<RequestType> requestTypes;
    private final ServiceProvider serviceProvider;

    public RequestParser(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Request parse (String message) throws RequestParseException {
        Matcher matcher = COMMAND_PATTERN.matcher(message);
        if (matcher.matches()) {
            String messageType = matcher.group(1);
            String narrowType = matcher.group(2);
            String narrowSpecs = matcher.group(3);
            RequestType requestType = this.parseRequestType(narrowType, matcher.start(2));
            Request request = requestType.parse(narrowSpecs);
            request.setBinaryEvents(this.isBinary(messageType));
            return request;
        }
        else {
            throw new UnexpectedRequestFormatException(COMMAND_PATTERN.toString());
        }
    }

    /**
     * Tests in a null pointer safe way if the String
     * is equals to (case insensitive) "binary".
     *
     * @param aString The String that will be tested for equality
     * @return The test result
     */
    private boolean isBinary (String aString) {
        return !is(aString).empty() && "binary".equalsIgnoreCase(aString);
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