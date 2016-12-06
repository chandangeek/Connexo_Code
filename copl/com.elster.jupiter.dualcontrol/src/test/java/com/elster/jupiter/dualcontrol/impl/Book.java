package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.UnderDualControl;

import java.util.Optional;

class Book implements UnderDualControl<BookChange> {

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
