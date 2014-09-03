package com.energyict.mdc.device.data.rest.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.IllegalEnumValueException;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;

public class LogBookResource {
    
    private static final Comparator<LogBook> LOG_BOOK_COMPARATOR_BY_NAME = new LogBookComparator();
    
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final NlsService nlsService;

    @Inject
    public LogBookResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, NlsService nlsService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.nlsService = nlsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLogBooks(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<LogBook> allLogBooks = device.getLogBooks();
        List<LogBook> logBooksOnPage = ListPager.of(allLogBooks, LOG_BOOK_COMPARATOR_BY_NAME).from(queryParameters).find();
        List<LogBookInfo> logBookInfos = LogBookInfo.from(logBooksOnPage, nlsService);
        return Response.ok(PagedInfoList.asJson("data", logBookInfos, queryParameters)).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lbid}")
    public Response getLogBook(@PathParam("mRID") String mrid, @PathParam("lbid") long logBookId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LogBook logBook = findLogBookOrThrowException(device, logBookId);
        return Response.ok(LogBookInfo.from(logBook, nlsService)).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lbid}/data")
    public Response getLogBookData(@PathParam("mRID") String mrid, @PathParam("lbid") long logBookId,
                                   @QueryParam("intervalStart") Long intervalStart, @QueryParam("intervalEnd") Long intervalEnd,
                                   @QueryParam("domain") Integer domainId, @QueryParam("subDomain") Integer subDomainId,
                                   @QueryParam("eventOrAction") Integer eventOrActionId, @BeanParam QueryParameters queryParameters)
    {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        LogBook logBook = findLogBookOrThrowException(device, logBookId);
        try {
            Date start = intervalStart == null ? null : new Date(intervalStart);
            Date end = intervalEnd == null ? null : new Date(intervalEnd);
            EndDeviceDomain domain = domainId != null ? EndDeviceDomain.get(domainId) : null;
            EndDeviceSubDomain subDomain = subDomainId != null ? EndDeviceSubDomain.get(subDomainId) : null;
            EndDeviceEventorAction eventOrAction = eventOrActionId != null ? EndDeviceEventorAction.get(eventOrActionId) : null;

            List<EndDeviceEventRecord> endDeviceEvents = logBook.getEndDeviceEvents(new Interval(start, end), domain, subDomain, eventOrAction);
            List<EndDeviceEventRecord> pagedEndDeviceEvents = ListPager.of(endDeviceEvents).from(queryParameters).find();
            return Response.ok(PagedInfoList.asJson("data", LogBookDataInfo.from(pagedEndDeviceEvents, nlsService), queryParameters)).build();
        } catch (IllegalArgumentException | IllegalEnumValueException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
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
