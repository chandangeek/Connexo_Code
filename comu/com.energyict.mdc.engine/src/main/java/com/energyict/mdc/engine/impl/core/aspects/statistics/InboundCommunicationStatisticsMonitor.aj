package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.protocol.inbound.aspects.statistics.StatisticsMonitoringHttpServletRequest;
import com.energyict.mdc.engine.impl.protocol.inbound.aspects.statistics.StatisticsMonitoringHttpServletResponse;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import org.joda.time.Duration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Defines pointcuts and advice to monitor the execution time
 * of inbound communication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-24 (11:37)
 */
public privileged aspect InboundCommunicationStatisticsMonitor extends AbstractCommunicationStatisticsMonitor {
    declare precedence :
            CommunicationStatisticsMonitor,
            com.energyict.comserver.aspects.events.InboundConnectionEventPublisher,
            com.energyict.mdc.inbound.aspects.logging.ComPortDiscoveryLogging;

    private StopWatch InboundCommunicationHandler.discovering;

    /* Ideally, we would put advise around the InboundDiscoveryContext as follows:
     * private pointcut inboundDiscoveryContextConstruction (HttpServletRequest request, HttpServletResponse response):
     *     execution(InboundDiscoveryContext.new(HttpServletRequest, HttpServletResponse))
     *  && args(request, response);

     * InboundDiscoveryContext around (HttpServletRequest request, HttpServletResponse response) : inboundDiscoveryContextConstruction (request, response) {
     *     return proceed(new StatisticsMonitoringHttpServletRequest(request), new StatisticsMonitoringHttpServletResponse(response));
     * }
     * However that would require that the mdw module that contains
     * the InboundDiscoveryContext would need to be woven at compile time too
     * and we want to avoid that.
     * We could use call instead of execution but all calls to the constructor
     * are not made in this module so that does not solve the problem either.
     * Therefore, we need to intercept this at the start of the handle method,
     * replace the request and response when necessary with monitoring
     * wrapper objects of the request and response.
     */
    private pointcut handle (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContext context):
            execution(void handle (InboundDeviceProtocol, InboundDiscoveryContext))
         && target(handler)
         && args(inboundDeviceProtocol, context);

    before (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContext context) : handle(handler, inboundDeviceProtocol, context) {
        if (this.inWebContext(context)) {
            HttpServletRequest request = context.getServletRequest();
            HttpServletResponse response = context.getServletResponse();
            context.setServletRequest(new StatisticsMonitoringHttpServletRequest(request));
            context.setServletResponse(new StatisticsMonitoringHttpServletResponse(response));
        }
    }

    private pointcut doDiscovery (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol):
           execution(InboundDeviceProtocol.DiscoverResultType doDiscovery(InboundDeviceProtocol))
        && target(handler)
        && args(inboundDeviceProtocol);

    before (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol): doDiscovery(handler, inboundDeviceProtocol) {
        handler.discovering = new StopWatch();
    }

    after (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol): doDiscovery(handler, inboundDeviceProtocol) {
        handler.discovering.stop();
    }

    private pointcut closeContext (InboundCommunicationHandler handler):
            execution(void closeContext())
         && target(handler);

    after (InboundCommunicationHandler handler): closeContext(handler) {
        InboundDiscoveryContextImpl context = handler.getContext();
        if (this.inWebContext(handler)) {
            ComSessionBuilder comSessionBuilder = context.getComSessionBuilder();
            comSessionBuilder.connectDuration(new Duration(0));
            StatisticsMonitoringHttpServletRequest request = this.getMonitoringRequest(handler);
            StatisticsMonitoringHttpServletResponse response = this.getMonitoringResponse(handler);
            long talkMillis = handler.discovering.getElapsed() + request.getTalkTime() + response.getTalkTime();
            comSessionBuilder.talkDuration(Duration.millis(talkMillis));
            comSessionBuilder.addReceivedBytes(request.getBytesRead()).addSentBytes(response.getBytesSent());
            comSessionBuilder.addReceivedPackets(1).addSentPackets(1);
        }
        else {
            ComSessionBuilder comSessionBuilder = context.getComSessionBuilder();
            comSessionBuilder.connectDuration(new Duration(0));
            ComPortRelatedComChannelImpl comChannel = this.getComChannel(handler);
            long talkMillis = handler.discovering.getElapsed() + comChannel.talking.getElapsed();
            comSessionBuilder.talkDuration(Duration.millis(talkMillis));
            Counters sessionCounters = this.getComChannelSessionCounters(comChannel);
            Counters taskSessionCounters = this.getComChannelTaskSessionCounters(comChannel);
            comSessionBuilder.addReceivedBytes(sessionCounters.getBytesRead() + taskSessionCounters.getBytesRead());
            comSessionBuilder.addSentBytes(sessionCounters.getBytesSent() + taskSessionCounters.getBytesSent());
            comSessionBuilder.addReceivedPackets(sessionCounters.getPacketsRead() + taskSessionCounters.getPacketsRead());
            comSessionBuilder.addSentPackets(sessionCounters.getPacketsSent() + taskSessionCounters.getPacketsSent());
        }
    }

    private boolean inWebContext (InboundCommunicationHandler handler) {
        return this.inWebContext(handler.getContext());
    }

    private boolean inWebContext (InboundDiscoveryContext context) {
        return context.getServletRequest() != null;
    }

    private StatisticsMonitoringHttpServletRequest getMonitoringRequest (InboundCommunicationHandler handler) {
        return (StatisticsMonitoringHttpServletRequest) handler.getContext().getServletRequest();
    }

    private StatisticsMonitoringHttpServletResponse getMonitoringResponse (InboundCommunicationHandler handler) {
        return (StatisticsMonitoringHttpServletResponse) handler.getContext().getServletResponse();
    }

    private ComPortRelatedComChannelImpl getComChannel (InboundCommunicationHandler handler) {
        return (ComPortRelatedComChannelImpl) handler.getContext().getComChannel();
    }

}