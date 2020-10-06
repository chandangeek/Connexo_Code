/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.rest.whiteboard.RestCallExecutedEvent;
import com.elster.jupiter.transaction.SqlEvent;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.time.StopWatch;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class EventServletWrapper extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AtomicReference<HttpServlet> servlet;
    private final WhiteBoard whiteBoard;

    public EventServletWrapper(HttpServlet servlet, WhiteBoard whiteBoard) {
        this.servlet = new AtomicReference<>(servlet);
        this.whiteBoard = whiteBoard;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        servlet.get().init(getServletConfig());
    }

    public void destroy() {
        servlet.get().destroy();
        super.destroy();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Tracker tracker = new Tracker(request.getMethod(), new URL(request.getRequestURL().toString()));
        Registration threadSubscriber = whiteBoard.getPublisher().addThreadSubscriber(tracker);
        try {
             servlet.get().service(request, response);
        } finally {
            tracker.stop();
            threadSubscriber.unregister();
        }
        whiteBoard.fire(tracker);
    }

    private static class Tracker implements Subscriber, RestCallExecutedEvent {

        private final String method;
        private final URL url;
        private StopWatch stopWatch;
        private int transactionCount;
        private int sqlCount;
        private int fetchCount;
        private int failedCount;

        Tracker(String method, URL url) {
            stopWatch = new StopWatch(true);
            this.url = url;
            this.method = method;
        }

        @Override
        public StopWatch getStopWatch() {
            return stopWatch;
        }

        @Override
        public void handle(Object notification, Object... notificationDetails) {
            if (notification instanceof SqlEvent) {
                SqlEvent event = (SqlEvent) notification;
                sqlCount++;
                fetchCount += event.getFetchCount();
            }
            if (notification instanceof TransactionEvent) {
                TransactionEvent event = (TransactionEvent) notification;
                transactionCount++;
                if (event.hasFailed()) {
                    failedCount++;
                }
            }
        }

        @Override
        public Class<?>[] getClasses() {
            return new Class<?>[]{SqlEvent.class, TransactionEvent.class};
        }

        @Override
        public URL getUrl() {
            return url;
        }

        private void stop() {
            this.stopWatch.stop();
        }

        @Override
        public int getSqlCount() {
            return sqlCount;
        }

        @Override
        public int getTransactionCount() {
            return transactionCount;
        }

        @Override
        public int getFailedCount() {
            return failedCount;
        }

        @Override
        public int getFetchCount() {
            return fetchCount;
        }

        @Override
        public String toString() {
            return
                    "Rest call to " + method + " " + url + " took " + stopWatch.getElapsed() / 1000L + " \u00B5s, executed " +
                            getSqlCount() + " sql statements, fetched " + getFetchCount() + " tuples and executed " + getTransactionCount() +
                            " transactions";
        }

        @Override
        public Event toOsgiEvent() {
            ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
            builder
                    .put("url", method + " " + url.toString())
                    .put("elapsed", stopWatch.getElapsed() / 1000L)
                    .put("sqlCount", sqlCount)
                    .put("fetchCount", fetchCount)
                    .put("txCount", transactionCount);
            return new Event("com/elster/jupiter/rest/INVOCATION", builder.build());
        }
    }

}
