/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.State;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DualControlIT {

    private static final String ORIGINAL_TITLE = "The Origin of Species";
    private static final String NEW_TITLE = "Through the Looking Glass";
    private static final int ORIGINAL_WEEKS = 2;
    private static final int NEW_WEEKS = 3;
    private static DualControlInMemoryBootstrapModule inMemoryBootstrapModule = new DualControlInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private static User operator;
    private static User approver1;
    private static User approver2;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        UserService userService = inMemoryBootstrapModule.getUserService();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            operator = userService.createUser("operator", "");
            approver1 = userService.createUser("approver1", "");
            approver2 = userService.createUser("approver2", "");
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testCreateMonitor() {
        DualControlService dualControlService = inMemoryBootstrapModule.getDualControlService();
        Monitor monitor = dualControlService.createMonitor();

        assertThat(monitor.getId()).isNotEqualTo(0);
        assertThat(monitor.getState()).isEqualTo(State.INACTIVE);
        assertThat(monitor.getOperations()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateMonitorForBookAndRequestActivationWhichIsThenApproved() {
        DualControlService dualControlService = inMemoryBootstrapModule.getDualControlService();
        ThreadPrincipalService threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();

        Book book = new Book(dualControlService, ORIGINAL_TITLE, ORIGINAL_WEEKS);

        threadPrincipalService.set(operator);
        book.request(BookChange.activation());
        threadPrincipalService.clear();

        {
            Optional<Monitor> found = dualControlService.getMonitor(book.getMonitor().getId());

            assertThat(found).isPresent();

            Monitor monitor = found.get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_ACTIVATION);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(userOperation.isRequest()).isTrue();
            assertThat(book.isActive()).isFalse();
        }

        threadPrincipalService.set(approver1);
        book.approvePending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_ACTIVATION);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(userOperation.isApproval()).isTrue();
            assertThat(book.isActive()).isFalse();
        }

        threadPrincipalService.set(approver2);
        book.approvePending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.ACTIVE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(userOperation.isApproval()).isTrue();
            assertThat(book.isActive()).isTrue();
        }

    }

    @Test
    @Transactional
    public void testUpdateForBookWhichIsThenApproved() {
        DualControlService dualControlService = inMemoryBootstrapModule.getDualControlService();
        ThreadPrincipalService threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();

        Book book = createActiveBook();

        threadPrincipalService.set(operator);
        BookChange bookChange = new BookChange();
        bookChange.setTitle(NEW_TITLE);
        bookChange.setWeeksToLend(NEW_WEEKS);
        book.request(bookChange);
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_UPDATE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(userOperation.isRequest()).isTrue();
            assertThat(book.getName()).isEqualTo(ORIGINAL_TITLE);
            assertThat(book.getWeeksToLend()).isEqualTo(ORIGINAL_WEEKS);
        }

        threadPrincipalService.set(approver1);
        book.approvePending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_UPDATE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(userOperation.isApproval()).isTrue();
            assertThat(book.getName()).isEqualTo(ORIGINAL_TITLE);
            assertThat(book.getWeeksToLend()).isEqualTo(ORIGINAL_WEEKS);
        }

        threadPrincipalService.set(approver2);
        book.approvePending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.ACTIVE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(userOperation.isApproval()).isTrue();
            assertThat(book.getName()).isEqualTo(NEW_TITLE);
            assertThat(book.getWeeksToLend()).isEqualTo(NEW_WEEKS);
        }
    }

    @Test
    @Transactional
    public void testUpdateForBookWhichIsThenRejected() {
        DualControlService dualControlService = inMemoryBootstrapModule.getDualControlService();
        ThreadPrincipalService threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();

        Book book = createActiveBook();

        threadPrincipalService.set(operator);
        BookChange bookChange = new BookChange();
        bookChange.setTitle(NEW_TITLE);
        bookChange.setWeeksToLend(NEW_WEEKS);
        book.request(bookChange);
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_UPDATE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(userOperation.isRequest()).isTrue();
            assertThat(book.getName()).isEqualTo(ORIGINAL_TITLE);
            assertThat(book.getWeeksToLend()).isEqualTo(ORIGINAL_WEEKS);
        }

        threadPrincipalService.set(approver1);
        book.rejectPending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.ACTIVE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(userOperation.isRejection()).isTrue();
            assertThat(book.getName()).isEqualTo(ORIGINAL_TITLE);
            assertThat(book.getWeeksToLend()).isEqualTo(ORIGINAL_WEEKS);
        }

    }

    @Test
    @Transactional
    public void testDeactivationForBookWhichIsThenApproved() {
        DualControlService dualControlService = inMemoryBootstrapModule.getDualControlService();
        ThreadPrincipalService threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();

        Book book = createActiveBook();

        threadPrincipalService.set(operator);
        book.request(BookChange.removal());
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_UPDATE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(userOperation.isRequest()).isTrue();
            assertThat(book.isActive()).isTrue();
        }

        threadPrincipalService.set(approver1);
        book.approvePending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.PENDING_UPDATE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(userOperation.isApproval()).isTrue();
            assertThat(book.isActive()).isTrue();
        }

        threadPrincipalService.set(approver2);
        book.approvePending();
        threadPrincipalService.clear();

        {
            Monitor monitor = dualControlService.getMonitor(book.getMonitor().getId()).get();

            assertThat(monitor.getState()).isEqualTo(State.OBSOLETE);
            UserOperation userOperation = lastOf(monitor.getOperations()).get();
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(userOperation.isApproval()).isTrue();
            assertThat(book.isActive()).isFalse();
        }
    }

    <T> Optional<T> lastOf(List<T> list) {
        return Lists.reverse(list).stream().findAny();
    }

    private Book createActiveBook() {
        Book book = new Book(inMemoryBootstrapModule.getDualControlService(), ORIGINAL_TITLE, ORIGINAL_WEEKS);
        ThreadPrincipalService threadPrincipalService = inMemoryBootstrapModule.getThreadPrincipalService();
        threadPrincipalService.set(operator);
        book.request(BookChange.activation());
        threadPrincipalService.clear();
        threadPrincipalService.set(approver1);
        book.approvePending();
        threadPrincipalService.clear();
        threadPrincipalService.set(approver2);
        book.approvePending();
        threadPrincipalService.clear();
        return book;
    }
}
