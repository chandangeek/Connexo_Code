package com.energyict.mdc.issue.issue.datavalidation.rest;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class IssueResourceTest extends IssueDataValidationApplicationJerseyTest {

    @Test
    public void testGetAllIssuesNominalCase() {
        mockStatus("open", "Open", false);
        mockStatus("inprogress", "In progress", false);
        mockDevice(2L, "Meter");
        mockAssignee(3L, "User", "USER");
        mockReason("IssueReason", "Reason", getDefaultIssueType());
        OpenIssueDataValidation issue = getDefaultIssue();
        Finder<? extends IssueDataValidation> finder = mock(Finder.class);
        doAnswer(invocationOnMock -> {
                DataValidationIssueFilter filter = (DataValidationIssueFilter) invocationOnMock.getArguments()[0];
                assertThat(filter.getStatuses()).hasSize(2);
                assertThat(filter.getStatuses().get(0).getKey()).isEqualTo("open");
                assertThat(filter.getStatuses().get(1).getKey()).isEqualTo("inprogress");
                assertThat(filter.getAssignee()).isPresent();
                assertThat(filter.getAssignee().get().getId()).isEqualTo(3L);
                assertThat(filter.getDevice()).isPresent();
                assertThat(filter.getDevice().get().getId()).isEqualTo(2L);
                assertThat(filter.getIssueReason()).isPresent();
                assertThat(filter.getIssueReason().get().getKey()).isEqualTo("IssueReason");
                return finder;
        }).when(issueDataValidationService).findAllDataValidationIssues(Matchers.any());
        doReturn(Collections.singletonList(issue)).when(finder).find();

        String response = target("/issues")
                .queryParam("status", "open")
                .queryParam("status", "inprogress")
                .queryParam("meter", "Meter")
                .queryParam("assigneeType", "USER")
                .queryParam("assigneeId", 3L)
                .queryParam("reason", "IssueReason")
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .queryParam("sort", "-modTime")
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].version")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].creationDate")).isEqualTo(0);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].dueDate")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].reason.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].reason.name")).isEqualTo("Reason");
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].status.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].status.name")).isEqualTo("open");
        assertThat(jsonModel.<Boolean>get("$.dataValidationIssues[0].status.allowForClosing")).isEqualTo(false);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].assignee.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].assignee.name")).isEqualTo("Admin");
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].assignee.type")).isEqualTo(IssueAssignee.Types.USER);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].device.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.dataValidationIssues[0].device.serialNumber")).isEqualTo("0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].device.name")).isNull();
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].device.usagePoint")).isNull();
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].device.serviceLocation")).isNull();
        assertThat(jsonModel.<String>get("$.dataValidationIssues[0].device.serviceCategory")).isNull();
    }

    @Test
    public void testGetIssueById() {
        IssueDataValidation issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(1);

        ReadingType readingType = mockReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType calculatedReadingType = mockReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.of(calculatedReadingType));
        Device device = mock(Device.class);
        when(deviceService.findDeviceById(1)).thenReturn(Optional.of(device));
        Channel channel = mock(Channel.class);
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getId()).thenReturn(5L);
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        Instant now = Instant.now();
        NotEstimatedBlock block1 = mockNotEstimatedBlock(now, now.plus(30, ChronoUnit.MINUTES), readingType);
        NotEstimatedBlock block2 = mockNotEstimatedBlock(now.plus(45, ChronoUnit.MINUTES), now.plus(60, ChronoUnit.MINUTES), calculatedReadingType);
        when(issue.getNotEstimatedBlocks()).thenReturn(Arrays.asList(block1, block2));

        String response = target("/issues/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.creationDate")).isEqualTo(0);
        assertThat(jsonModel.<Number>get("$.dueDate")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.reason.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.reason.name")).isEqualTo("Reason");
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.status.name")).isEqualTo("open");
        assertThat(jsonModel.<Boolean>get("$.status.allowForClosing")).isEqualTo(false);
        assertThat(jsonModel.<Number>get("$.assignee.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.assignee.name")).isEqualTo("Admin");
        assertThat(jsonModel.<String>get("$.assignee.type")).isEqualTo(IssueAssignee.Types.USER);
        assertThat(jsonModel.<Number>get("$.device.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.device.serialNumber")).isEqualTo("0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<String>get("$.device.name")).isNull();
        assertThat(jsonModel.<String>get("$.device.usagePoint")).isNull();
        assertThat(jsonModel.<String>get("$.device.serviceLocation")).isNull();
        assertThat(jsonModel.<String>get("$.device.serviceCategory")).isNull();

        assertThat(jsonModel.<List<?>>get("$.notEstimatedData")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.notEstimatedData[*].channelId")).containsExactly(5, 5);
        assertThat(jsonModel.<List<String>>get("$.notEstimatedData[*].readingType.mRID")).containsExactly("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(jsonModel.<List<?>>get("$.notEstimatedData[0].notEstimatedBlocks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].startTime")).isEqualTo(now.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].endTime")).isEqualTo(now.plus(30, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].amountOfSuspects")).isEqualTo(2);

        assertThat(jsonModel.<List<?>>get("$.notEstimatedData[1].notEstimatedBlocks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[1].notEstimatedBlocks[0].startTime")).isEqualTo(now.plus(45, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[1].notEstimatedBlocks[0].endTime")).isEqualTo(now.plus(60, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[1].notEstimatedBlocks[0].amountOfSuspects")).isEqualTo(1);
    }

    @Test
    public void testGetIssueByIdWithRegisterNotEstimatedBlocks() {
        Instant now = Instant.now();

        IssueDataValidation issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(1);

        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        Device device = mock(Device.class);
        when(deviceService.findDeviceById(1)).thenReturn(Optional.of(device));
        Register register = mock(Register.class);
        when(device.getChannels()).thenReturn(Collections.emptyList());
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getRegisterSpecId()).thenReturn(5L);
        when(register.getReadingType()).thenReturn(readingType);
        com.elster.jupiter.metering.Channel channel = mock(com.elster.jupiter.metering.Channel.class);
        BaseReadingRecord reading1 = mock(BaseReadingRecord.class);
        when(reading1.getTimeStamp()).thenReturn(now.plus(30, ChronoUnit.MINUTES));
        BaseReadingRecord reading2 = mock(BaseReadingRecord.class);
        when(reading2.getTimeStamp()).thenReturn(now.plus(90, ChronoUnit.MINUTES));
        when(channel.getReadings(Range.openClosed(Instant.EPOCH, now.plus(30, ChronoUnit.MINUTES)))).thenReturn(Collections.singletonList(reading1));
        when(channel.getReadings(Range.openClosed(now.plus(45, ChronoUnit.MINUTES), now.plus(90, ChronoUnit.MINUTES)))).thenReturn(Collections.singletonList(reading2));

        NotEstimatedBlock block1 = mockNotEstimatedBlock(Instant.EPOCH, now.plus(30, ChronoUnit.MINUTES), readingType);
        when(block1.getChannel()).thenReturn(channel);
        NotEstimatedBlock block2 = mockNotEstimatedBlock(now.plus(45, ChronoUnit.MINUTES), now.plus(90, ChronoUnit.MINUTES), readingType);
        when(block2.getChannel()).thenReturn(channel);
        when(issue.getNotEstimatedBlocks()).thenReturn(Arrays.asList(block1, block2));

        String response = target("/issues/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);

        assertThat(jsonModel.<List<?>>get("$.notEstimatedData")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].registerId")).isEqualTo(5);
        assertThat(jsonModel.<String>get("$.notEstimatedData[0].readingType.mRID")).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<List<?>>get("$.notEstimatedData[0].notEstimatedBlocks")).hasSize(2);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].startTime")).isEqualTo(now.plus(30, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].endTime")).isEqualTo(now.plus(30, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].amountOfSuspects")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[1].startTime")).isEqualTo(now.plus(90, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[1].endTime")).isEqualTo(now.plus(90, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[1].amountOfSuspects")).isEqualTo(1);
    }

    @Test
    public void testGetNonexistentIssueById() {
        when(issueDataValidationService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("/issues/1").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllComments() {
        OpenIssueDataValidation issue = mock(OpenIssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(1L);
        List<IssueComment> comments = Arrays.asList(mockComment(1L, "My comment", getDefaultUser()));
        Query<IssueComment> commentsQuery = mock(Query.class);
        when(commentsQuery.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyVararg())).thenReturn(comments);
        when(issueService.query(IssueComment.class, User.class)).thenReturn(commentsQuery);

        String response = target("/issues/1/comments").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.comments")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.comments[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.comments[0].comment")).isEqualTo("My comment");
        assertThat(jsonModel.<Number>get("$.comments[0].creationDate")).isEqualTo(0);
        assertThat(jsonModel.<Number>get("$.comments[0].version")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.comments[0].author.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.comments[0].author.name")).isEqualTo("Admin");
    }

    @Test
    public void testPostComment() {
        OpenIssueDataValidation issue = mock(OpenIssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(1L);
        IssueComment comment = mock(IssueComment.class);
        when(issue.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.of(comment));
        when(comment.getCreateTime()).thenReturn(Instant.now());
        User user = mockUser(1, "user");
        when(comment.getUser()).thenReturn(user);
        Map<String, String> params = new HashMap<>(1);
        params.put("comment", "Comment");
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/issues/1/comments").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testPostEmptyComment() {
        OpenIssueDataValidation issue = mock(OpenIssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(1L);
        mock(IssueComment.class);
        when(issue.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.empty());
        Map<String, String> params = new HashMap<>(0);
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/issues/1/comments").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCloseAction() {
        CloseIssueRequest request = new CloseIssueRequest();
        EntityReference issueRef = new EntityReference();
        issueRef.setId(1L);
        request.issues = Collections.singletonList(issueRef);
        request.status = "resolved";
        IssueStatus status = mockStatus("resolved", "Resolved", true);
        OpenIssueDataValidation openIssueDataValidation = getDefaultIssue();
        doReturn(Optional.of(openIssueDataValidation)).when(issueDataValidationService).findIssue(1L);
        doReturn(Optional.of(openIssueDataValidation)).when(issueDataValidationService).findOpenIssue(1L);
        Entity<CloseIssueRequest> json = Entity.json(request);

        Response response = target("issues/close").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(openIssueDataValidation).close(status);
    }

    @Test
    public void testAssignAction() {
        Entity<AssignIssueRequest> json = Entity.json(new AssignIssueRequest());
        Response response = target("issues/assign").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPerformAction() {
        Optional<IssueDataValidation> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataValidationService).findIssue(1);

        Optional<IssueActionType> mockActionType = Optional.of(getDefaultIssueActionType());
        when(issueActionService.findActionType(1)).thenReturn(mockActionType);

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;

        Response response = target("issues/1/actions/1").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPerformActionOnUnexistingIssue() {
        when(issueDataValidationService.findIssue(1123)).thenReturn(Optional.empty());

        Response response = target("issues/1123/action").request().put(Entity.json(new PerformActionRequest()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPerformNonexistingAction() {
        Optional<IssueDataValidation> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataValidationService).findIssue(1);
        when(issueActionService.findActionType(1)).thenReturn(Optional.empty());

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;

        Response response = target("issue/1/action").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    private ReadingType mockReadingType(String mrid){
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getAliasName()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1,2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1,2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.<ReadingType>empty());
        when(readingType.isCumulative()).thenReturn(true);
        return readingType;
    }

    private NotEstimatedBlock mockNotEstimatedBlock(Instant from, Instant to, ReadingType readingType) {
        NotEstimatedBlock block = mock(NotEstimatedBlock.class);
        when(block.getStartTime()).thenReturn(from);
        when(block.getEndTime()).thenReturn(to);
        when(block.getReadingType()).thenReturn(readingType);
        return block;
    }
}
