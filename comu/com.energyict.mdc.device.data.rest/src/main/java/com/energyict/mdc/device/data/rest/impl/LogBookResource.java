package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.cbo.IllegalEnumValueException;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.time.Interval;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class LogBookResource {

    private static final String INTERVAL_START = "intervalStart";
    private static final String INTERVAL_END = "intervalEnd";
    private static final String DOMAIN = "domain";
    private static final String SUB_DOMAIN = "subDomain";
    private static final String EVENT_OR_ACTION = "eventOrAction";

    private static final Comparator<LogBook> LOG_BOOK_COMPARATOR_BY_NAME = new LogBookComparator();

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;

    @Inject
    public LogBookResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getAllLogBooks(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<LogBook> allLogBooks = device.getLogBooks();
        List<LogBook> logBooksOnPage = ListPager.of(allLogBooks, LOG_BOOK_COMPARATOR_BY_NAME).from(queryParameters).find();
        List<LogBookInfo> logBookInfos = LogBookInfo.from(logBooksOnPage, thesaurus);
        return Response.ok(PagedInfoList.fromPagedList("data", logBookInfos, queryParameters)).build();
    }

    @GET
    @Path("{lbid}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getLogBook(@PathParam("mRID") String mrid, @PathParam("lbid") long logBookId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LogBook logBook = findLogBookOrThrowException(device, logBookId);
        return Response.ok(LogBookInfo.from(logBook, thesaurus)).build();
    }

    @GET
    @Path("{lbid}/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getLogBookDataForSpecificLogbook(@PathParam("mRID") String mrid, @PathParam("lbid") long logBookId, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters){
        return this.getLogBookData(mrid, (d, f) -> {
            LogBook logBook = findLogBookOrThrowException(d, logBookId);
            return logBook.getEndDeviceEventsByFilter(f);
        }, jsonQueryFilter, queryParameters);
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getLogBookDataForDevice(@PathParam("mRID") String mrid, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters){
        return this.getLogBookData(mrid, Device::getDeviceEventsByFilter, jsonQueryFilter, queryParameters);
    }

    private Response getLogBookData(String mrid, BiFunction<Device, EndDeviceEventRecordFilterSpecification, List<EndDeviceEventRecord>> eventProvider, JsonQueryFilter jsonQueryFilter, JsonQueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        try {
            EndDeviceEventRecordFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
            List<EndDeviceEventRecord> endDeviceEvents = eventProvider.apply(device, filter);
            List<EndDeviceEventRecord> pagedEndDeviceEvents = ListPager.of(endDeviceEvents).from(queryParameters).find();
            return Response.ok(PagedInfoList.fromPagedList("data", LogBookDataInfo.from(pagedEndDeviceEvents, thesaurus), queryParameters)).build();
        } catch (IllegalArgumentException | IllegalEnumValueException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private EndDeviceEventRecordFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) {
        EndDeviceEventRecordFilterSpecification filter = new EndDeviceEventRecordFilterSpecification();
        Instant intervalStart = null;
        Instant intervalEnd = null;
        if (jsonQueryFilter.hasProperty(INTERVAL_START)) {
            intervalStart = jsonQueryFilter.getInstant(INTERVAL_START);
        }
        if (jsonQueryFilter.hasProperty(INTERVAL_END)) {
            intervalEnd = jsonQueryFilter.getInstant(INTERVAL_END);
        }
        filter.range = Interval.of(intervalStart, intervalEnd).toOpenClosedRange();
        if (jsonQueryFilter.hasProperty(DOMAIN)) {
            filter.domain = jsonQueryFilter.getProperty(DOMAIN, new EndDeviceDomainAdapter());
        }
        if (jsonQueryFilter.hasProperty(SUB_DOMAIN)) {
            filter.subDomain = jsonQueryFilter.getProperty(SUB_DOMAIN, new EndDeviceSubDomainAdapter());
        }
        if (jsonQueryFilter.hasProperty(EVENT_OR_ACTION)) {
            filter.eventOrAction = jsonQueryFilter.getProperty(EVENT_OR_ACTION, new EndDeviceEventOrActionAdapter());
        }
        return filter;
    }

    public static class LogBookComparator implements Comparator<LogBook> {

        @Override
        public int compare(LogBook o1, LogBook o2) {
            return o1.getLogBookType().getName().compareToIgnoreCase(o2.getLogBookType().getName());
        }
    }

    private LogBook findLogBookOrThrowException(Device device, long logBookId) {
        for (LogBook logBook : device.getLogBooks()) {
            if (logBook.getId() == logBookId) {
                return logBook;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LOG_BOOK_ON_DEVICE, device.getmRID(), logBookId);
    }
}
