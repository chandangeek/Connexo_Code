package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.PendingUpdate;
import com.elster.jupiter.dualcontrol.State;
import com.elster.jupiter.dualcontrol.UnderDualControl;
import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.security.thread.impl.ThreadPrincipalServiceImpl;
import com.elster.jupiter.users.User;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DualControlServiceTest {

    static class BookChange implements PendingUpdate {

        private boolean activation;
        private boolean removal;

        private String name;
        private int weeksToLend;

        @Override
        public boolean isActivation() {
            return activation;
        }

        @Override
        public boolean isRemoval() {
            return removal;
        }

        public static BookChange activation() {
            BookChange bookChange = new BookChange();
            bookChange.activation = true;
            return bookChange;
        }

        public void setTitle(String title) {
            this.name = title;
        }

        public void setWeeksToLend(int weeksToLend) {
            this.weeksToLend = weeksToLend;
        }

        public String getName() {
            return name;
        }

        public int getWeeksToLend() {
            return weeksToLend;
        }

        public static BookChange deactivation() {
            BookChange bookChange = new BookChange();
            bookChange.removal = true;
            return bookChange;
        }
    }

    static class Book implements UnderDualControl<BookChange> {

        private final DualControlService dualControlService;

        private Monitor monitor;

        private String name;
        private int weeksToLend;
        private boolean active = false;
        private BookChange pending;

        public String getName() {
            return name;
        }

        public int getWeeksToLend() {
            return weeksToLend;
        }

        Book(DualControlService dualControlService, String name, int weeks) {
            this.dualControlService = dualControlService;
            this.name = name;
            this.weeksToLend = weeks;
        }

        @Override
        public Monitor getMonitor() {
            if (monitor == null) {
                monitor = dualControlService.createMonitor();
            }
            return monitor;
        }

        public boolean isActive() {
            return active;
        }

        @Override
        public Optional<BookChange> getPendingUpdate() {
            return Optional.ofNullable(pending);
        }

        @Override
        public void setPendingUpdate(BookChange pendingUpdate) {
            pending = pendingUpdate;
        }

        @Override
        public void applyUpdate() {
            getPendingUpdate().ifPresent(bookChange -> {
                if (bookChange.isActivation()) {
                    this.active = true;
                    return;
                }
                if (bookChange.isRemoval()) {
                    this.active = false;
                    return;
                }
                name = bookChange.getName();
                weeksToLend = bookChange.getWeeksToLend();
            });
        }

        @Override
        public void clearUpdate() {
            pending = null;
        }

        public void request(BookChange bookChange) {
            getMonitor().request(bookChange, this);
        }


        public void approvePending() { // pull up?
            getMonitor().approve(this);
        }

        public void rejectPending() {
            getMonitor().reject(this);
        }
    }

    @Test
    public void testActivation() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(0);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(1);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testActivationRejected() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(0);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(1);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testDoubleActivationApprovalBySameUser() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(0);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(1);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isPresent();
        }
    }

    @Test
    public void testRejectionAfterApprovalBySameUser() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(1);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(0);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(2);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(1);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(3);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(2);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver1);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApprovedUpdate() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(3);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(4);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(5);
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
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(3);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(4);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getName()).isEqualTo("The Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(3);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApproveDeactivation() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(3);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(4);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testRejectDeactivation() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(3);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(4);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(5);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.REJECT);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }

    @Test
    public void testApprovedChangedUpdate() {
        ThreadPrincipalServiceImpl threadPrincipalService = new ThreadPrincipalServiceImpl();
        DualControlService dualControlService = new DualControlServiceImpl(threadPrincipalService);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(4);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(3);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(5);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(4);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(6);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(5);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(7);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(6);
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
            assertThat(book.getMonitor().getUserOperations()).hasSize(8);
            UserOperation userOperation = book.getMonitor().getUserOperations().get(7);
            assertThat(userOperation.getAction()).isEqualTo(UserAction.APPROVE);
            assertThat(userOperation.getUser()).isEqualTo(approver2);
            assertThat(book.getName()).isEqualTo("Jungle Book");
            assertThat(book.getWeeksToLend()).isEqualTo(5);
            assertThat(book.getPendingUpdate()).isEmpty();
        }
    }


}