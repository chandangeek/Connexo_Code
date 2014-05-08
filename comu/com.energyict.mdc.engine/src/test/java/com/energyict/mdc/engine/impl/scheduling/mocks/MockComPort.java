package com.energyict.mdc.engine.impl.scheduling.mocks;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ComPortType;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

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

    protected MockComPort (ComServer comServer, String name) {
        this.setName(name) ;
        this.comServer = comServer;
    }

    protected MockComPort (ComServer comServer, int id, String name) {
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

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public AtomicBoolean getDirty() {
        return dirty;
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

    public boolean isDirty(){
        return dirty.get();
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
}