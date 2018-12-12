/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.State;
import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.impl.ThreadPrincipalServiceImpl;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DualControlServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrmService ormService;
    @Mock
    private UserService userService;
    @Mock
    private UpgradeService upgradeService;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testActivation() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getOperations().get(0);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getOperations().get(1);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.ACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testActivationRejected() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getOperations().get(0);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getOperations().get(1);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::rejectPending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testDoubleActivationApprovalBySameUser() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getOperations().get(0);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getOperations().get(1);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }
    }

    @Test
    public void testRejectionAfterApprovalBySameUser() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getOperations().get(0);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_ACTIVATION);
            assertThat(book.getMonitor().getOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getOperations().get(1);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }

        threadPrincipalService.withContextAdded(book::rejectPending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApprovedUpdate() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    BookChange bookChange = new BookChange();
                    bookChange.setTitle("Jungle Book");
                    bookChange.setWeeksToLend(4);
                    book.request(bookChange);
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.ACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getName()).isEqualTo("Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(4);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testRejectedUpdate() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    BookChange bookChange = new BookChange();
                    bookChange.setTitle("Jungle Book");
                    bookChange.setWeeksToLend(4);
                    book.request(bookChange);
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::rejectPending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.ACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApproveRemoval() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.removal());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.OBSOLETE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApproveDeactivation() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.deactivation());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.INACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testRejectDeactivation() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.deactivation());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::rejectPending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.ACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testRejectRemoval() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.removal());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::rejectPending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.ACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApprovedChangedUpdate() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.activation());
                },
                operator
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();

        threadPrincipalService.withContextAdded(() -> {
                    BookChange bookChange = new BookChange();
                    bookChange.setTitle("Jungle Book");
                    bookChange.setWeeksToLend(4);
                    book.request(bookChange);
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getOperations().get(3);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getOperations().get(4);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(() -> {
                    BookChange bookChange = new BookChange();
                    bookChange.setTitle("Jungle Book");
                    bookChange.setWeeksToLend(5);
                    book.request(bookChange);
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver1
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.PENDING_UPDATE);
            assertThat(book.getMonitor().getOperations()).hasSize(7);
            UserOperation userOperation = book.getMonitor().getOperations().get(6);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isPresent();
        }
        threadPrincipalService.withContextAdded(book::approvePending,
                approver2
        ).run();
        {
            assertThat(book.isActive()).isTrue();
            assertThat(book.getMonitor().getState()).isEqualTo(State.ACTIVE);
            assertThat(book.getMonitor().getOperations()).hasSize(8);
            UserOperation userOperation = book.getMonitor().getOperations().get(7);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getName()).isEqualTo("Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(5);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testRemoveInactiveDoesntNeedApproval() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService, ormService, userService, upgradeService);
        User operator = mock(User.class);
        User approver1 = mock(User.class);
        User approver2 = mock(User.class);

        Book book = new Book(dualControlService, "The Jungle Book", 3);

        threadPrincipalService.withContextAdded(() -> {
                    book.request(BookChange.removal());
                },
                operator
        ).run();
        {
            assertThat(book.isActive()).isFalse();
            assertThat(book.getMonitor().getState()).isEqualTo(State.OBSOLETE);
            assertThat(book.getMonitor().getOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getOperations().get(0);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REQUEST);
            assertThat(userOperation.getUser()).isEqualTo(operator);
            assertThat(book.isObsolete()).isTrue();
        }
    }


}