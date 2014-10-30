package com.elster.jupiter.export.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.Range;

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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.time.RelativeField.*;

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
        RelativeDate startOfTheYearBeforeLastYear = new RelativeDate(
                YEAR.minus(2),
                MONTH.equalTo(1),
                DAY.equalTo(1),
                HOUR.equalTo(0),
                MINUTES.equalTo(0)
        );
        RelativeDate startOfLastYear = new RelativeDate(
                YEAR.minus(1),
                MONTH.equalTo(1),
                DAY.equalTo(1),
                HOUR.equalTo(0),
                MINUTES.equalTo(0)
        );
        RelativeDate startOfThisYear = new RelativeDate(
                MONTH.equalTo(1),
                DAY.equalTo(1),
                HOUR.equalTo(0),
                MINUTES.equalTo(0)
        );

        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        //List<? extends ReadingTypeDataExportTask> list = queryTasks(params);

        DataExportTaskInfos infos = new DataExportTaskInfos(/*params.clipToLimit(list)*/);
        infos.dataExportTasks = new ArrayList<>();

        DataExportTaskInfo dataExportTaskInfo = new DataExportTaskInfo();
        ReadingTypeInfo readingTypeInfo = new ReadingTypeInfo();
        readingTypeInfo.mRID = "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18";
        dataExportTaskInfo.readingTypes.add(readingTypeInfo);

        PropertyInfo prop = new PropertyInfo();
        prop.key = "fileformat.prefix";
        prop.required = true;
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = PropertyType.NUMBER;
        prop.propertyTypeInfo = propertyTypeInfo;
        PropertyValueInfo propertyValueInfo = new PropertyValueInfo();
        propertyValueInfo.value = 6;
        propertyValueInfo.defaultValue = 0;
        propertyValueInfo.inheritedValue = 0;
        prop.propertyValueInfo = propertyValueInfo;
        dataExportTaskInfo.properties.add(prop);

        dataExportTaskInfo.exportperiod = new RelativePeriodInfo(new RelativePeriod() {
            @Override
            public String getName() {
                return "the year before last";
            }

            @Override
            public RelativeDate getRelativeDateFrom() {
                return startOfTheYearBeforeLastYear;
            }

            @Override
            public RelativeDate getRelativeDateTo() {
                return startOfLastYear;
            }

            @Override
            public Range<ZonedDateTime> getInterval(ZonedDateTime referenceDate) {
                return null;
            }

            @Override
            public List<RelativePeriodCategory> getRelativePeriodCategories() {
                return Collections.emptyList();
            }

            @Override
            public void addRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {
            }

            @Override
            public void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) throws Exception {
            }

            @Override
            public long getId() {
                return 27;
            }

            @Override
            public long getVersion() {
                return 5651;
            }

            @Override
            public Instant getCreateTime() {
                return null;
            }

            @Override
            public Instant getModTime() {
                return null;
            }

            @Override
            public String getUserName() {
                return null;
            }

            @Override
            public void save() {
            }

            @Override
            public void update() {
            }

            @Override
            public void delete() {
            }
        });
        dataExportTaskInfo.updatePeriod = dataExportTaskInfo.exportperiod;
        dataExportTaskInfo.deviceGroupInfo = new DeviceGroupInfo();
        dataExportTaskInfo.deviceGroupInfo.name = "My Device Group";
        dataExportTaskInfo.deviceGroupInfo.id = 35;

        dataExportTaskInfo.lastExportOccurenceInfo = new LastExportOccurenceInfo();
        dataExportTaskInfo.lastExportOccurenceInfo.status = DataExportStatus.SUCCESS;
        dataExportTaskInfo.lastExportOccurenceInfo.lastRun = Instant.now().minus(25, ChronoUnit.MINUTES).toEpochMilli();
        dataExportTaskInfo.lastExportOccurenceInfo.startedOn = Instant.now().minus(50, ChronoUnit.MINUTES).toEpochMilli();
        dataExportTaskInfo.lastExportOccurenceInfo.finishedOn = dataExportTaskInfo.lastExportOccurenceInfo.lastRun;
        dataExportTaskInfo.lastExportOccurenceInfo.duration = dataExportTaskInfo.lastExportOccurenceInfo.finishedOn - dataExportTaskInfo.lastExportOccurenceInfo.startedOn;
        dataExportTaskInfo.lastExportOccurenceInfo.status = DataExportStatus.SUCCESS;


        infos.dataExportTasks.add(dataExportTaskInfo);
        infos.total = params.determineTotal(/*list.size()*/ 1);

        return infos;
    }

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportTaskInfos getDataExportTasks(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends ReadingTypeDataExportTask> list = queryTasks(params);

        DataExportTaskInfos infos = new DataExportTaskInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return infos;
    }*/


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
                .setEndDeviceGroup(endDeviceGroup(info.deviceGroupInfo.id))
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
