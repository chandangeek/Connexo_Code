/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Path("/readingtypes")
public class ReadingTypeResource {

    private static final int MRID_FIELD_COUNT = 18;
    private static final int INTER_HARMONIC_NUMERATOR_INDEX = 7;
    private static final int INTER_HARMONIC_DENOMINATOR_INDEX = 8;
    private static final int ARGUMENT_NUMERATOR_INDEX = 9;
    private static final int ARGUMENT_DENOMINATOR_INDEX = 10;

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final TransactionService transactionService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final ReadingTypeFilterFactory readingTypeFilterFactory;

    @Inject
    public ReadingTypeResource(MeteringService meteringService, ExceptionFactory exceptionFactory, TransactionService transactionService,
                               ConcurrentModificationExceptionFactory conflictFactory, ReadingTypeInfoFactory readingTypeInfoFactory,
                               ReadingTypeFilterFactory readingTypeFilterFactory) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
        this.conflictFactory = conflictFactory;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.readingTypeFilterFactory = readingTypeFilterFactory;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getReadingTypes(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(getReadingTypeFilterCondition(searchText));
            List<ReadingTypeInfo> infos = meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .map(readingTypeInfoFactory::from)
                    .collect(Collectors.toList());
            return PagedInfoList.fromPagedList("readingTypes", infos, queryParameters);
        }

        List<ReadingTypeInfo> readingTypeInfos = meteringService.findReadingTypes(readingTypeFilterFactory.from(jsonQueryFilter))
                .from(queryParameters)
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("readingTypes", readingTypeInfos, queryParameters);
    }

    @GET
    @Path("/{mRID}/")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingTypeInfos getReadingType(@PathParam("mRID") String mRID) {
        return meteringService.getReadingType(mRID)
                .map(readingType -> readingTypeInfoFactory.from(Collections.singletonList(readingType)))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{mRID}/calculated")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingTypeInfos getCalculatedReadingType(@PathParam("mRID") String mRID) {
        return meteringService.getReadingType(mRID)
                .map(ReadingType::getCalculatedReadingType)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))
                .map(readingType -> readingTypeInfoFactory.from(Collections.singletonList(readingType))).orElse(new ReadingTypeInfos());
    }

    @GET
    @Path("/codes/{field}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCodes(@PathParam("field") String field, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingTypeCodeInfo> infoList = Arrays.stream(ReadingTypeFields.values())
                .filter(candidate -> candidate.getFieldName().equalsIgnoreCase(field))
                .map(c -> meteringService.getReadingTypeFieldCodesFactory()
                        .getCodeFields(c.getFieldName()).entrySet()
                        .stream().map(e -> new ReadingTypeCodeInfo(e.getKey(), e.getValue())))
                .flatMap(Function.identity()).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList(field + "Codes", infoList, queryParameters);
    }

    @POST
    @Path("/count")
    @RolesAllowed({Privileges.Constants.ADMINISTER_READINGTYPE})
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
            final List<String> existsMrids = meteringService.findReadingTypes(codes).stream().map(ReadingType::getMRID).collect(Collectors.toList());
            count = codes.size() - (int) codes.stream().filter(existsMrids::contains).count();
        }
        return Response.ok().entity(Pair.of("countReadingTypesToCreate", count).asMap()).build();
    }

    @POST
    @RolesAllowed({Privileges.Constants.ADMINISTER_READINGTYPE})
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
            List<String> codes =
                    new ReadingTypeListFactory(createReadingTypeInfo)
                            .getCodeStringList()
                            .stream()
                            .filter(c -> !meteringService.getReadingType(c).isPresent())
                            .collect(Collectors.toList());
            final List<String> existsMrids = meteringService.findReadingTypes(codes).stream().map(ReadingType::getMRID).collect(Collectors.toList());
            mRIDs = codes.stream().filter(e -> !existsMrids.contains(e)).collect(Collectors.toList());
        }

        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        for (String mRIDvalidation : mRIDs) {
            String[] mRIDtokens = mRIDvalidation.split("[.]");
            if (mRIDtokens.length == MRID_FIELD_COUNT) {
                if (!mRIDtokens[INTER_HARMONIC_NUMERATOR_INDEX].equals("0") && mRIDtokens[INTER_HARMONIC_DENOMINATOR_INDEX].equals("0")) {
                    validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.DENOMINATOR_CANNOT_BE_ZERO, "interHarmonicDenominator"));
                }
                if (!mRIDtokens[ARGUMENT_NUMERATOR_INDEX].equals("0") && mRIDtokens[ARGUMENT_DENOMINATOR_INDEX].equals("0")) {
                    validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.DENOMINATOR_CANNOT_BE_ZERO, "argumentDenominator"));
                }
            }
        }
        validationBuilder.validate();

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
    @RolesAllowed({Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingType activateReadingType(@PathParam("mRID") String mRID, ReadingTypeInfo readingTypeInfo) {
        try (TransactionContext context = transactionService.getContext()) {

            ReadingType readingType = meteringService.findAndLockReadingTypeByIdAndVersion(readingTypeInfo.mRID, readingTypeInfo.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(readingTypeInfo.name)
                            .withActualVersion(() -> meteringService.getReadingType(readingTypeInfo.mRID).map(ReadingType::getVersion).orElse(null))
                            .supplier());

            readingType = activate(readingType, readingTypeInfo.active);

            context.commit();

            return readingType;
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
    }

    @PUT
    @Path("/activate/")
    @RolesAllowed({Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response activateReadingTypesByList(ReadingTypeBulkEditInfo readingTypeBulkEditInfo, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingType> readingTypes;
        if (readingTypeBulkEditInfo.mRIDs != null && !readingTypeBulkEditInfo.mRIDs.isEmpty()) {
            readingTypes = meteringService.findReadingTypes(readingTypeBulkEditInfo.mRIDs);
        } else {
            readingTypes = meteringService.findReadingTypes(readingTypeFilterFactory.from(jsonQueryFilter)).find();
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
    @RolesAllowed({Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingType updateReadingType(@PathParam("mRID") String mRID, ReadingTypeInfo readingTypeInfo) {
        try (TransactionContext context = transactionService.getContext()) {

            ReadingType readingType = meteringService.findAndLockReadingTypeByIdAndVersion(readingTypeInfo.mRID, readingTypeInfo.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(readingTypeInfo.name)
                            .withActualVersion(() -> meteringService.getReadingType(readingTypeInfo.mRID).map(ReadingType::getVersion).orElse(null))
                            .supplier());

            readingType.setAliasName(readingTypeInfo.aliasName);
            readingType.update();
            context.commit();

            return readingType;
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
    }

    @PUT
    @RolesAllowed({Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response updateReadingTypesByList(ReadingTypeBulkEditInfo readingTypeBulkEditInfo, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingType> readingTypes;
        if (readingTypeBulkEditInfo.mRIDs != null && !readingTypeBulkEditInfo.mRIDs.isEmpty()) {
            readingTypes = meteringService.findReadingTypes(readingTypeBulkEditInfo.mRIDs);
        } else {
            readingTypes = meteringService.findReadingTypes(readingTypeFilterFactory.from(jsonQueryFilter)).find();
        }
        try (TransactionContext context = transactionService.getContext()) {
            for (ReadingType readingType : readingTypes) {
                readingType.setAliasName(readingTypeBulkEditInfo.aliasName);
                readingType.update();
            }
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.READINGTYPE_CREATING_FAIL);
        }
        return Response.ok().build();
    }

    private Condition getReadingTypeFilterCondition(String dbSearchText) {
        String regex = "*" + dbSearchText.replace(" ", "*") + "*";
        return Where.where("fullAliasName").likeIgnoreCase(regex)
                .and(mrIdMatchOfNormalRegisters()
                        .or(mrIdMatchOfBillingRegisters())
                        .or(mrIdMatchOfPeriodRelatedRegisters()));
    }

    private Condition mrIdMatchOfPeriodRelatedRegisters() {
        return Where.where("mRID").matches("^[11-13]\\.\\[1-24]\\.0", "");
    }

    private Condition mrIdMatchOfBillingRegisters() {
        return Where.where("mRID").matches("^8\\.\\d+\\.0", "");
    }

    private Condition mrIdMatchOfNormalRegisters() {
        return Where.where("mRID").matches("^0\\.\\d+\\.0", "");
    }
}
