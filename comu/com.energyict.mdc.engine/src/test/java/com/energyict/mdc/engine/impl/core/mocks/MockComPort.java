package com.energyict.mdc.engine.impl.core.mocks;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ComPortType;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a mock implementation for the {@link ComPort} interface
 * for demo purposes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-04 (08:45)
 */
public abstract class MockComPort implements ComPort, Cloneable {

    private ComServer comServer;
    private boolean active;
    private String description;
    private long id;
    private AtomicBoolean dirty = new AtomicBoolean(false);
    private AtomicReference<Date> obsoleteDate = new AtomicReference();

    protected MockComPort (ComServer comServer, String name) {
        this.setName(name) ;
        this.comServer = comServer;
    }

    protected MockComPort (ComServer comServer, long id, String name) {
        this.setId(id);
        this.setName(name);
        this.comServer = comServer;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Date getModificationDate() {
        return new Date();
    }

    public ComServer getComServer () {
        return comServer;
    }

    public void setComServer (ComServer comServer) {
        this.comServer = comServer;
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        if (active != this.active) {
            this.becomeDirty();
        }
        this.active = active;
    }

    @Override
    public void makeObsolete() {
        this.obsoleteDate.set(new Date());
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate.get() != null;
    }

    @Override
    public Date getObsoleteDate() {
        return this.obsoleteDate.get();
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public AtomicBoolean getDirty() {
        return dirty;
    }

    public boolean isDirty(){
        return dirty.get();
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }

    protected void becomeDirty () {
        this.setDirty(true);
    }

    protected void becomeClean () {
        this.setDirty(false);
    }

    @Override
    public boolean isInbound() {
        return false;
    }

    @Override
    public int getNumberOfSimultaneousConnections() {
        return 1;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {

    }

    @Override
    public ComPortType getComPortType() {
        return ComPortType.TCP;
    }

    @Override
    public void setComPortType(ComPortType type) {

    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void save() {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockComPort that = (MockComPort) o;

        if (id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}