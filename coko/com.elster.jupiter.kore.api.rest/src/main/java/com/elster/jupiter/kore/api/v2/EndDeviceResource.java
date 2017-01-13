package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/enddevices")
public class EndDeviceResource {

    private final EndDeviceInfoFactory endDeviceInfoFactory;
    private final MeterReadingsFactory meterReadingsFactory;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public EndDeviceResource(EndDeviceInfoFactory endDeviceInfoFactory, MeterReadingsFactory meterReadingsFactory, MeteringService meteringService, ExceptionFactory exceptionFactory, Clock clock) {
        this.endDeviceInfoFactory = endDeviceInfoFactory;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.meterReadingsFactory = meterReadingsFactory;
        this.clock = clock;
    }

    /**
     * @param meterId Unique identifier of the meter
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The meter
     * @summary fetch a specific meter
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{meterId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public EndDeviceInfo getEndDevice(@PathParam("meterId") long meterId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Meter meter = meteringService.findMeterById(meterId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_DEVICE));
        return endDeviceInfoFactory.from(meter, uriInfo, fieldSelection.getFields());
    }

    /**
     * @param meterId Unique identifier of the meter
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The meter
     * @summary fetch a specific meter
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{meterId}/readings")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public MeterReadingsInfos getEndDeviceReadings(@PathParam("meterId") Long meterId, @QueryParam("from") Long from, @QueryParam("to") Long to, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Meter meter = meteringService.findMeterById(meterId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_DEVICE));

        Range<Instant> range = Range.all();
        if (from != null) {
            if (to != null) {
                range = Range.openClosed(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));
            } else {
                range = Range.atLeast(Instant.ofEpochMilli(from));
            }
        } else {
            if (to != null) {
                range = Range.lessThan(Instant.ofEpochMilli(to));
            }
        }

        MeterReadingsInfos readings = new MeterReadingsInfos();
        readings.readings = new ArrayList<>();
        readings.intervalBlocks = new ArrayList<>();

        for (ReadingType readingType : meter.getReadingTypes(range)) {
            if (readingType.isRegular()) {
                MeterReadingsIntervalBlockInfo intervalBlockInfo = new MeterReadingsIntervalBlockInfo();
                intervalBlockInfo.readingType = readingType.getMRID();
                intervalBlockInfo.intervalReadings = meter.getReadings(range, readingType)
                        .stream()
                        .map(reading -> meterReadingsFactory.asInfo(null, reading.getTimeStamp(), reading.getValue()))
                        .collect(Collectors.toList());
                readings.intervalBlocks.add(intervalBlockInfo);
            } else {
                readings.readings.addAll(meter.getReadings(range, readingType)
                        .stream()
                        .map(reading -> meterReadingsFactory.asInfo(readingType.getMRID(), reading.getTimeStamp(), reading.getValue()))
                        .collect(Collectors.toList()));
            }
        }

        return readings;
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return endDeviceInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
