package com.elster.jupiter.export.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/dataexporttask")
public class DataExportTaskResource {

    private final DataExportService dataExportService;
    private final RestQueryService queryService;
    private final TimeService timeService;
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final Thesaurus thesaurus;

    @Inject
    public DataExportTaskResource(RestQueryService queryService, DataExportService dataExportService, TimeService timeService, MeteringService meteringService, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus) {
        this.queryService = queryService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportTaskInfos getDataExportTasks(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends ReadingTypeDataExportTask> list = queryTasks(params);

        DataExportTaskInfos infos = new DataExportTaskInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return infos;
    }


    private List<? extends ReadingTypeDataExportTask> queryTasks(QueryParameters queryParameters) {
        Query<? extends ReadingTypeDataExportTask> query = dataExportService.getReadingTypeDataExportTaskQuery();
        RestQuery<? extends ReadingTypeDataExportTask> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }


    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportTaskInfo getDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new DataExportTaskInfo(fetchDataExportTask(id, securityContext));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReadingTypeDataExportTask(DataExportTaskInfo info) {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(info.name)
                .setDataProcessorName(info.dataProcessor)
                .setScheduleExpression(info.schedule.asTemporalExpression())
                .setExportPeriod(getRelativePeriod(info.exportperiod))
                .setUpdatePeriod(getRelativePeriod(info.updatePeriod))
                .setValidatedDataOption(info.validatedDataOption)
                .setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id))
                .exportContinuousData(info.exportContinuousData)
                .exportUpdate(info.exportUpdate);

        List<PropertySpec<?>> propertiesSpecs = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor);
        PropertyUtils propertyUtils = new PropertyUtils();

        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    builder.addProperty(spec.getName()).withValue(value);
                });

        info.readingTypes.stream()
                .forEach(r -> {
                    meteringService.getReadingType(r.mRID).ifPresent(builder::addReadingType);
                });

        ReadingTypeDataExportTask dataExportTask = builder.build();
        return Response.status(Response.Status.CREATED).entity(new DataExportTaskInfo(dataExportTask)).build();
    }

    @GET
    @Path("/processors")
    @Produces(MediaType.APPLICATION_JSON)
    public ProcessorInfos getAvailableProcessors(@Context UriInfo uriInfo) {
        ProcessorInfos infos = new ProcessorInfos();
        List<DataProcessorFactory> processors = dataExportService.getAvailableProcessors();
        PropertyUtils propertyUtils = new PropertyUtils();
        for (DataProcessorFactory processor : processors) {
            infos.add(
                processor.getName(),
                thesaurus.getString(processor.getName(), processor.getName()),
                propertyUtils.convertPropertySpecsToPropertyInfos(processor.getProperties()));
        }
        infos.total = processors.size();
        return infos;
    }

    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private RelativePeriod getRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        if (relativePeriodInfo == null) {
            return null;
        }
        return timeService.findRelativePeriod(relativePeriodInfo.id).orElse(null);
    }

    private ReadingTypeDataExportTask fetchDataExportTask(long id, SecurityContext securityContext) {
        return dataExportService.findExportTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
