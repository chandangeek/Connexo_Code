package com.energyict.mdc.device.data.rest.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.cbo.IllegalEnumValueException;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLogBooks(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<LogBook> allLogBooks = device.getLogBooks();
        List<LogBook> logBooksOnPage = ListPager.of(allLogBooks, LOG_BOOK_COMPARATOR_BY_NAME).from(queryParameters).find();
        List<LogBookInfo> logBookInfos = LogBookInfo.from(logBooksOnPage, thesaurus);
        return Response.ok(PagedInfoList.asJson("data", logBookInfos, queryParameters)).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lbid}")
    public Response getLogBook(@PathParam("mRID") String mrid, @PathParam("lbid") long logBookId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LogBook logBook = findLogBookOrThrowException(device, logBookId);
        return Response.ok(LogBookInfo.from(logBook, thesaurus)).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lbid}/data")
    public Response getLogBookData(@PathParam("mRID") String mrid, @PathParam("lbid") long logBookId, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam QueryParameters queryParameters)
    {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LogBook logBook = findLogBookOrThrowException(device, logBookId);
        try {
            EndDeviceEventRecordFilterSpecification filter = buildFilterFromJsonQuery(jsonQueryFilter);
            List<EndDeviceEventRecord> endDeviceEvents = logBook.getEndDeviceEventsByFilter(filter);
            List<EndDeviceEventRecord> pagedEndDeviceEvents = ListPager.of(endDeviceEvents).from(queryParameters).find();
            return Response.ok(PagedInfoList.asJson("data", LogBookDataInfo.from(pagedEndDeviceEvents, thesaurus), queryParameters)).build();
        } catch (IllegalArgumentException | IllegalEnumValueException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    private EndDeviceEventRecordFilterSpecification buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) {
        EndDeviceEventRecordFilterSpecification filter = new EndDeviceEventRecordFilterSpecification();
        Map<String, String> filterProperties = jsonQueryFilter.getFilterProperties();
        Date intervalStart = null;
        Date intervalEnd = null;
        if (filterProperties.containsKey(INTERVAL_START)) {
            intervalStart = new Date(Long.parseLong(filterProperties.get(INTERVAL_START)));
        }
        if (filterProperties.containsKey(INTERVAL_END)) {
            intervalEnd = new Date(Long.parseLong(filterProperties.get(INTERVAL_END)));
        }
        filter.interval = new Interval(intervalStart, intervalEnd);
        if (filterProperties.containsKey(DOMAIN)) {
            filter.domain = jsonQueryFilter.getProperty(DOMAIN, new EndDeviceDomainAdapter());
        }
        if (filterProperties.containsKey(SUB_DOMAIN)) {
            filter.subDomain = jsonQueryFilter.getProperty(SUB_DOMAIN, new EndDeviceSubDomainAdapter());
        }
        if (filterProperties.containsKey(EVENT_OR_ACTION)) {
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
