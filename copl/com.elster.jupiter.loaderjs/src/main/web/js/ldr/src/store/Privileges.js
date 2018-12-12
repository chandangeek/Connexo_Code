/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ldr.store.Privileges
 */
Ext.define('Ldr.store.Privileges', {
    extend: 'Ext.data.Store',
    model: 'Ldr.model.Privilege',
    storeId: 'userPrivileges',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,
    remoteFilter: false
});
