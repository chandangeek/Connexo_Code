package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.PendingUpdate;

class BookChange implements PendingUpdate {

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
