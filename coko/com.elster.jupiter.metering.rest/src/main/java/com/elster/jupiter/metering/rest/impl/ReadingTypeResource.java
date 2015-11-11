package com.elster.jupiter.metering.rest.impl;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Path("/readingtypes")
public class ReadingTypeResource {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final TransactionService transactionService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    
    @Inject
    public ReadingTypeResource(MeteringService meteringService, ExceptionFactory exceptionFactory, TransactionService transactionService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getReadingTypes(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingTypeInfo> readingTypeInfos = meteringService.findReadingTypes(ReadingTypeFilterFactory.from(jsonQueryFilter))
                .from(queryParameters)
                .stream()
                .map(ReadingTypeInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("readingTypes", readingTypeInfos, queryParameters);
    }
    
    @GET
	@Path("/{mRID}/")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public ReadingTypeInfos getReadingType(@PathParam("mRID") String mRID) {
    	return meteringService.getReadingType(mRID)
    		.map(readingType -> new ReadingTypeInfos(readingType))
    		.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{mRID}/calculated")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingTypeInfos getCalculatedReadingType(@PathParam("mRID") String mRID) {
        return meteringService.getReadingType(mRID)
                .map(rt -> rt.getCalculatedReadingType())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))
                .map(readingType -> new ReadingTypeInfos(readingType)).orElse(new ReadingTypeInfos());
    }

    @GET
    @Path("/codes/{field}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCodes(@PathParam("field") String field, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingTypeCodeInfo> infoList;
        switch (field) {
            case "macroPeriod":
                infoList = ReadingTypeCodes.getMacroPeriod();
                break;
            case "aggregate":
                infoList = ReadingTypeCodes.getAggregate();
                break;
            case "measuringPeriod":
                infoList = ReadingTypeCodes.getMeasurementPeriod();
                break;
            case "accumulation":
                infoList = ReadingTypeCodes.getAccumulation();
                break;
            case "flowDirection":
                infoList = ReadingTypeCodes.getFlowDirection();
                break;
            case "commodity":
                infoList = ReadingTypeCodes.getCommodity();
                break;
            case "measurementKind":
                infoList = ReadingTypeCodes.getMeasurementKind();
                break;
            case "interHarmonicNumerator":
                infoList = ReadingTypeCodes.getInterHarmonicNumerator();
                break;
            case "interHarmonicDenominator":
                infoList = ReadingTypeCodes.getInterHarmonicDenominator();
                break;
            case "argumentNumerator":
                infoList = ReadingTypeCodes.getArgumentNumerator();
                break;
            case "argumentDenominator":
                infoList = ReadingTypeCodes.getArgumentDenominator();
                break;
            case "tou":
                infoList = ReadingTypeCodes.getTou();
                break;
            case "cpp":
                infoList = ReadingTypeCodes.getCpp();
                break;
            case "consumptionTier":
                infoList = ReadingTypeCodes.getConsumptionTier();
                break;
            case "phases":
                infoList = ReadingTypeCodes.getPhases();
                break;
            case "metricMultiplier":
                infoList = ReadingTypeCodes.getMultiplier();
                break;
            case "unit":
                infoList = ReadingTypeCodes.getUnit();
                break;
            case "currency":
                infoList = ReadingTypeCodes.getCurrency();
                break;
            default:
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.FIELD_NOT_FOUND, field);
        }
        return PagedInfoList.fromCompleteList(field + "Codes", infoList, queryParameters);
    }

    @POST
    @Path("/count")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response createReadingTypeCount(CreateReadingTypeInfo createReadingTypeInfo) {

        Integer count;

        if (!Checks.is(createReadingTypeInfo.mRID).emptyOrOnlyWhiteSpace()) {
            if (meteringService.getReadingType(createReadingTypeInfo.mRID).isPresent()) {
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_ALREADY_EXISTS, createReadingTypeInfo.mRID);
            }
            count = 1;
        } else {

            long countVariations = createReadingTypeInfo.countVariations();

            if (countVariations > 1000) {
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TOO_MANY_READINGTYPES, countVariations);
            }

            List<String> codes = new ReadingTypeListFactory(createReadingTypeInfo).getCodeStringList();
            System.err.println(codes);
            final List<String> existsMrids = meteringService.findReadingTypes(codes).stream().map(ReadingType::getMRID).collect(Collectors.toList());
            count = codes.size() - (int) codes.stream().filter(existsMrids::contains).count();
        }
        return Response.ok().entity(Pair.of("countReadingTypesToCreate", count).asMap()).build();
    }

    @POST
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response createReadingType(CreateReadingTypeInfo createReadingTypeInfo) {
        List<String> mRIDs = new ArrayList<>();
        if (!Checks.is(createReadingTypeInfo.mRID).emptyOrOnlyWhiteSpace()) {
            if (meteringService.getReadingType(createReadingTypeInfo.mRID).isPresent()) {
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_ALREADY_EXISTS, createReadingTypeInfo.mRID);
            }
            mRIDs.add(createReadingTypeInfo.mRID);
        } else {
            long countVariations = createReadingTypeInfo.countVariations();

            if (countVariations > 1000) {
                throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TOO_MANY_READINGTYPES, countVariations);
            }

            List<String> codes = new ReadingTypeListFactory(createReadingTypeInfo).getCodeStringList();

            codes.stream().filter(c -> !meteringService.getReadingType(c).isPresent()).collect(Collectors.toList());
            System.out.println(codes);
            final List<String> existsMrids = meteringService.findReadingTypes(codes).stream().map(ReadingType::getMRID).collect(Collectors.toList());
            mRIDs = codes.stream().filter(e -> !existsMrids.contains(e)).collect(Collectors.toList());
        }

        int createdCount = 0;
        try (TransactionContext context = transactionService.getContext()) {
            for (String mRID : mRIDs) {
                meteringService.createReadingType(mRID, createReadingTypeInfo.aliasName);
                createdCount++;
            }
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
        return Response.ok().entity(Pair.of("countCreatedReadingTypes", createdCount).asMap()).build();
    }

    @PUT
    @Path("/{mRID}/activate/")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingType activateReadingType(@PathParam("mRID") String mRID, ReadingTypeInfo readingTypeInfo) {
        try (TransactionContext context = transactionService.getContext()) {

            ReadingType readingType = meteringService.findAndLockReadingTypeByIdAndVersion(readingTypeInfo.mRID, readingTypeInfo.version).orElseThrow(conflictFactory.contextDependentConflictOn(readingTypeInfo.name)
                    .withActualVersion(() -> meteringService.getReadingType(readingTypeInfo.mRID).map(ReadingType::getVersion).orElse(null))
                    .supplier());

            readingType = activate(readingType, readingTypeInfo.active);

            System.out.println("updated active for rt " + readingType.getMRID());
            context.commit();

            return readingType;
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
    }

    @PUT
    @Path("/activate/")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response activateReadingTypesByList(ReadingTypeBulkEditInfo readingTypeBulkEditInfo, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingType> readingTypes;
        if (readingTypeBulkEditInfo.mRIDs != null && !readingTypeBulkEditInfo.mRIDs.isEmpty()) {
            readingTypes = meteringService.findReadingTypes(readingTypeBulkEditInfo.mRIDs);
        } else {
            readingTypes = meteringService.findReadingTypes(ReadingTypeFilterFactory.from(jsonQueryFilter)).find();
        }
        try (TransactionContext context = transactionService.getContext()) {
            for (ReadingType readingType : readingTypes) {
                activate(readingType, readingTypeBulkEditInfo.active);
            }
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
        return Response.ok().build();
    }

    private ReadingType activate(ReadingType readingType, boolean active) {
        if (active) {
            readingType.activate();
            readingType.update();
            return readingType;
        } else {
            readingType.deactivate();
            readingType.update();
            return readingType;
        }
    }

    @PUT
    @Path("/{mRID}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingType updateReadingType(@PathParam("mRID") String mRID, ReadingTypeInfo readingTypeInfo) {
        try (TransactionContext context = transactionService.getContext()) {

            ReadingType readingType = meteringService.findAndLockReadingTypeByIdAndVersion(readingTypeInfo.mRID, readingTypeInfo.version).orElseThrow(conflictFactory.contextDependentConflictOn(readingTypeInfo.name)
                    .withActualVersion(() -> meteringService.getReadingType(readingTypeInfo.mRID).map(ReadingType::getVersion).orElse(null))
                    .supplier());

            readingType.setAliasName(readingTypeInfo.aliasName);
            readingType.update();
            System.out.println("updated alias for rt " + readingType.getMRID());
            context.commit();

            return readingType;
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
    }

    @PUT
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response updateReadingTypesByList(ReadingTypeBulkEditInfo readingTypeBulkEditInfo, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingType> readingTypes;
        if (readingTypeBulkEditInfo.mRIDs != null && !readingTypeBulkEditInfo.mRIDs.isEmpty()) {
            readingTypes = meteringService.findReadingTypes(readingTypeBulkEditInfo.mRIDs);
        } else {
            readingTypes = meteringService.findReadingTypes(ReadingTypeFilterFactory.from(jsonQueryFilter)).find();
        }
        try (TransactionContext context = transactionService.getContext()) {
            for (ReadingType readingType : readingTypes) {
                readingType.setAliasName(readingTypeBulkEditInfo.aliasName);
                readingType.update();
                System.out.println("updated alias for rt " + readingType.getMRID());
            }
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
        return Response.ok().build();
    }
}
