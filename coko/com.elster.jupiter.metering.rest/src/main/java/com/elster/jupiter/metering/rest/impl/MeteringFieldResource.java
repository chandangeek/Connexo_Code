/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.KorePagedInfoList;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.util.streams.DecoratedStream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Path("/fields")
public class MeteringFieldResource {

    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Thesaurus thesaurus;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public MeteringFieldResource(MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService, Thesaurus thesaurus, ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.thesaurus = thesaurus;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    @GET
    @Path("/unitsofmeasure")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getUnitsOfMeasure(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList(
                "unitsOfMeasure",
                DecoratedStream.decorate(meteringService.getAvailableReadingTypes().stream())
                        .map(rt -> new UnitOfMeasureFieldInfo(rt.getMultiplier(), rt.getUnit()))
                        .distinct(i -> i.name)
                        .sorted((c1, c2) -> c1.name.compareToIgnoreCase(c2.name))
                        .collect(toList()),
                queryParameters);
    }

    @GET
    @Path("/intervals")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public IntervalFieldInfos getIntervals() {
        IntervalFieldInfos intervalFieldInfos = new IntervalFieldInfos();
        Set<TimeAttribute> timeAttributes = meteringService.getAvailableReadingTypes().stream()
                .map(ReadingType::getMeasuringPeriod)
                .collect(Collectors.<TimeAttribute>toSet());
        Set<MacroPeriod> macroPeriods = meteringService.getAvailableReadingTypes().stream()
                .map(ReadingType::getMacroPeriod)
                .collect(Collectors.<MacroPeriod>toSet());
        intervalFieldInfos.from(timeAttributes, macroPeriods, thesaurus);

        return intervalFieldInfos;
    }

    @GET
    @Path("/timeofuse")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getTou(@BeanParam JsonQueryParameters queryParameters) {
        List<TimeOfUseFieldInfo> touList = IntStream.rangeClosed(0, 8)
                .mapToObj(tou -> new TimeOfUseFieldInfo(tou))
                .collect(toList());
        return PagedInfoList.fromCompleteList("timeOfUse", touList, queryParameters);
    }

    @GET
    @Path("/readingtypes")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getReadingTypes(@BeanParam JsonQueryFilter queryFilter, @BeanParam QueryParameters queryParameters) {
        List<ReadingType> readingTypes = meteringService.getAvailableReadingTypes().stream()
                .filter(getReadingTypeFilterPredicate(queryFilter))
                .sorted((c1, c2) -> (c1.getFullAliasName().compareToIgnoreCase(c2.getFullAliasName())))
                .collect(Collectors.<ReadingType>toList());
        List<ReadingTypeInfo> pagedReadingTypes = ListPager.of(readingTypes).from(queryParameters).find().stream().map(readingTypeInfoFactory::from).collect(toList());
        return Response.ok(KorePagedInfoList.asJson("readingTypes", pagedReadingTypes, queryParameters, readingTypes.size())).build();
    }

    private Predicate<ReadingType> getReadingTypeFilterPredicate(JsonQueryFilter queryFilter) {
        Predicate<ReadingType> filter = rt -> true; // unfiltered
        if (queryFilter.hasFilters()) {
            if (queryFilter.hasProperty("mRID")) {
                filter = filter.and(rt -> rt.getMRID().toLowerCase().contains(queryFilter.getString("mRID").toLowerCase()));
            }
            if (queryFilter.hasProperty("name")) {
                filter = filter.and(rt -> rt.getFullAliasName().toLowerCase().contains(queryFilter.getString("name").toLowerCase()));
            }
            if (queryFilter.hasProperty("tou")) {
                filter = filter.and(rt -> rt.getTou() == queryFilter.getInteger("tou"));
            }
            if (queryFilter.hasProperty("unitOfMeasure")) {
                filter = filter.and(rt -> rt.getUnit().getId() == queryFilter.getLong("unitOfMeasure"));
            }
            if (queryFilter.hasProperty("time")) {
                filter = filter.and(rt -> rt.getMeasuringPeriod().getId() == queryFilter.getInteger("time"));
            }
            if (queryFilter.hasProperty("macro")) {
                filter = filter.and(rt -> rt.getMacroPeriod().getId() == queryFilter.getInteger("macro"));
            }
            if (queryFilter.hasProperty("multiplier")) {
                filter = filter.and(rt -> rt.getMultiplier().getMultiplier() == queryFilter.getInteger("multiplier"));
            }
            if (queryFilter.hasProperty("selectedReadings")) {
                filter = filter.and(rt -> !queryFilter.getStringList("selectedReadings").contains(rt.getMRID().toLowerCase()));
            }
        }
        return filter;
    }

    @GET
    @Path("/connectionstates")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getConnectionStates(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo<String>> connectionStates = Arrays.stream(UsagePointConnectedKind.values())
                .map(connectionKind -> new IdWithDisplayValueInfo<>(connectionKind.name(), connectionKind.getDisplayName(this.thesaurus)))
                .sorted(Comparator.comparing(ck -> ck.displayValue))
                .collect(toList());
        return PagedInfoList.fromCompleteList("connectionStates", connectionStates, queryParameters);
    }

    @GET
    @Path("/amibilling")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAmiBillings(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo<String>> billings = Arrays.stream(AmiBillingReadyKind.values())
                .map(billingKind -> new IdWithDisplayValueInfo<>(billingKind.name(), billingKind.getDisplayName(this.thesaurus)))
                .sorted(Comparator.comparing(bk -> bk.displayValue))
                .collect(toList());
        return PagedInfoList.fromCompleteList("amiBillings", billings, queryParameters);
    }

    @GET
    @Path("/servicecategory")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getServiceCategories(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo<String>> categories = Arrays.stream(ServiceKind.values())
                .map(serviceKind -> new IdWithDisplayValueInfo<>(serviceKind.name(), serviceKind.getDisplayName(this.thesaurus)))
                .sorted(Comparator.comparing(sk -> sk.displayValue))
                .collect(toList());
        return PagedInfoList.fromCompleteList("categories", categories, queryParameters);
    }

    @GET
    @Path("/phasecodes")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getPhaseCodes(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("phaseCodes", Arrays.stream(PhaseCode.values())
                .distinct()
                .map(pc -> new IdWithDisplayValueInfo<>(pc.name(), pc.getValue()))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Path("/bypassstatus")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getBypassStatus(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("bypassStatus", Arrays.stream(BypassStatus.values())
                .map(bs -> new IdWithDisplayValueInfo<>(bs.name(), bs.getDisplayValue(thesaurus)))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Path("/usagepointtype")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getUsagePointType(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointTypeInfo> infos = Arrays.stream(UsagePointTypeInfo.UsagePointType.values())
                .filter(t -> !t.isVirtual)  //Cannot create virtual usage point
                .map(t -> new UsagePointTypeInfo(t,thesaurus))
                .collect(Collectors.toList());
        infos.sort((type1, type2) -> type1.displayName.compareTo(type2.displayName));
        return PagedInfoList.fromCompleteList("usagePointTypes", infos, queryParameters);
    }

    @GET
    @Path("/metrologyconfigurations")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = metrologyConfigurationService.findAllMetrologyConfigurations().stream()
                .map(IdWithNameInfo::new)
                .collect(toList());
        return PagedInfoList.fromCompleteList("metrologyConfigurations", infos, queryParameters);
    }

    @GET
    @Path("/metrologypurposes")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyPurposes(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = metrologyConfigurationService.getMetrologyPurposes().stream()
                .map(IdWithNameInfo::new)
                .collect(toList());
        return PagedInfoList.fromCompleteList("metrologyPurposes", infos, queryParameters);
    }
}