package com.energyict.mdc.engine.offline.gui.table.renderer;

/**
 * Renderers implementing this interface will hide the contents if the user
 * has not the required 'privilege'
 * Copyrights EnergyICT
 * Date: 15-sep-2010
 * Time: 9:56:18
 */
public interface ProtectableRenderer {

//    void setPrivilege(Role role, UserAction action, TypeId typeId);

    boolean isAuthorized();

}
