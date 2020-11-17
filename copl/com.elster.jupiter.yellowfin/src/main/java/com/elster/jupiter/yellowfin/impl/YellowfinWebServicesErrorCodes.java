package com.elster.jupiter.yellowfin.impl;

import java.util.*;

/**
 * This enum holds the YellowFin web-service error codes and their description.
 *
 * The source can be found in JavaDoc of YellowFin-WS:
 *      yellowfin-ws-7.4.jar/javadoc/com/hof/mi/web/service/WebserviceException.html
 */
public enum YellowfinWebServicesErrorCodes {

    UNKNOWN_ERROR(-2, "An unknown error occurred"),
    CANNOT_CONNECT(-1, "The client cannot connect to the web service"),
    NO_ERROR (0, "Success"),
    USER_NOT_AUTHENTICATED ( 1, "The user could not be authenticated"),
    NO_WEBSERVICE_ACCESS ( 2, "The user does not have access to web services"),
    PERSON_REQUIRED ( 3, "The person input parameter was not specified"),
    COULD_NOT_CREATE_PERSON ( 4, "An error occurred creating a user"),
    COULD_NOT_RELOAD_LICENCE ( 5, "An error occurred reloading the licence"),
    LOGIN_ALREADY_IN_USE ( 6, "A user already exists with the userid, in a call to create a new user"),
    COULD_NOT_DELETE_PERSON ( 7, "An error occurred deleting a user"),
    COULD_NOT_FIND_PERSON ( 8, "The specified user could not be found"),
    LICENCE_BREACH ( 9, "The licence is breached, or the requested action would breach the licence"),
    COULD_NOT_LOAD_REPORT_ACCESS ( 10, "An error occurred loading report access"),
    COULD_NOT_LOAD_REPORT_LIST ( 11, "An error occurred loading reports for a user"),
    COULD_NOT_FIND_GROUP ( 12, "Ahe specified group could not be found"),
    GROUP_EXISTS ( 13, "A group already exists with the group name, in a call to create a new group"),
    BIRT_OBJECT_NULL ( 14, "The report passed was null, in a call to load a BIRT report"),
    BIRT_OBJECT_NO_DATA ( 15, "The report passed was empty, in a call to load a BIRT report"),
    BIRT_SOURCE_MISSING ( 16, "The data source was not specified or could not be found, in a call to load a BIRT report."),
    BIRT_COULD_NOT_SAVE ( 17, "An error occurred saving the BIRT report."),
    BIRT_COULD_NOT_SAVE_BIRT_FILE ( 18, "An error occurred saving the BIRT report"),
    COULD_NOT_UPDATE_PASSWORD ( 19, "An error occurred updating a user's password"),
    UNKNOWN_WEBSERVICE_FUNCTION ( 20, "An unknown web service function was requested"),
    INVALID_CLIENT_REFERENCE ( 21, "An invalid client reference id was specified"),
    CLIENT_EXISTS ( 22, "A client already exists with the client reference id, in a call to create a new client"),
    COULD_NOT_FIND_REPORT ( 23, "The report could not be found"),
    REPORT_IS_DRAFT ( 24, "The requested report is in draft mode"),
    COULD_NOT_AUTHENTICATE_USER ( 25, "The specified user could not be authenticated"),
    UNSECURE_LOGON_NOT_ENABLED ( 26, "Unsecure logon has not been enabled at the database level"),
    ROLE_NOT_FOUND ( 27, "The role assigned to a user during create or alter is invalid"),
    COULD_NOT_LOAD_FAVOURITES ( 28, " a persons favourites (and inbox contents) could not be loaded"),
    RESPONSE_IS_TOO_LARGE (29, "Report or other webservice response has breached a set threshold"),
    SOURCE_NOT_FOUND ( 30, "The data source was not specified or could not be found"),
    EMPTY_RECIPIENT_LIST ( 31, "No recipients were provided for distribution"),
    BROADCAST_FAILED ( 32, "A report broadcast failed"),
    FILTERVALUES_FAILED ( 33, "Filter values could not be retrieved"),
    CLIENT_ORGS_DISABLED ( 34, "A client org call was made, but client orgs functionality is disabled"),
    DASHBOARD_TAB_NOT_FOUND ( 35, "Dashboard tab not found"),
    SCHEDULE_NULL ( 36, "A schedule record passed was null"),
    UNKNOWN_STATUS_CODE ( 37, "A passed status code was invalid"),
    PASSWORD_REQUIREMENTS_NOT_MET ( 38, "Password requirements were not met"),
    LOGIN_MAXIMUM_ATTEMPTS ( 39, "A user has already attempted to login the maximum times unsuccessfully"),

    UNABLE_TO_EXPORT (40, "Error occurred during export"),
    IMPORT_FILE_NOT_FOUND (41, "The import file was not passed to the webservice"),
    IMPORT_VERSION_INCOMPATIBLE (42, "The import file is incompatible with the server"),
    IMPORT_IS_EMPTY (43, "The import file doesn't contain any importable items"),
    COULD_NOT_FIND_VIEW ( 44, "The specified view cannot be found"),
    COULD_NOT_FIND_SOURCE (45, "The specified source cannot be found"),
    COULD_NOT_FIND_CONTENT (46, "The specified content couldnt be found"),
    PARAMETER_NOT_PASSED (47, "A required parameter was not passed"),
    EMAIL_ADDRESS_IN_USE (48, "A required parameter was not passed"),
    QUERY_WEBSERVICE_NOT_ENABLED (49, "The query webservice is not enabled"),
    PASSWORD_NOT_SET (50, "A password parameter is not set"),
    USER_IS_INACTIVE (51, "The specified user is inactive"),
    LINK_CLIENT_PRIMARY_DATASOURCE (52, "A linking client datasource to primary datasource has been failed."),
    COULD_NOT_LOAD_SOURCE (53, "A datasource is found but can't be loaded"),
    COULD_NOT_LOAD_CLIENT_SOURCE (54, "Datasource is loaded but the linked client source(s) can't be loaded"),
    COULD_NOT_SAVE_DATASOURCE (55, "Saving datasource has failed"),
    COULD_NOT_UPDATE_ACLL (56, "Updating ACLL has failed"),
    GROUPID_REQUIRED (57, "A groupID is required"),
    
    
    ;

    private final int errorCode;
    private final String description;

    YellowfinWebServicesErrorCodes(int errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    public String getName(){
        return this.name();
    }

    public static Optional<YellowfinWebServicesErrorCodes> getError(int errorCode){
        return Arrays.stream(values()).filter(e -> e.errorCode == errorCode).findFirst();
    }
}
