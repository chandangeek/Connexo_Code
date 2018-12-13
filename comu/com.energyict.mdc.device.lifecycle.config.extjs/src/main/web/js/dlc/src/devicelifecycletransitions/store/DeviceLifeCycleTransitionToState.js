/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionToState', {
    extend: 'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState',
    constructor: function () {
        var me = this, parent;
        me.callParent(arguments);
        parent = Ext.getStore('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState');
        parent.on('load', function (store, records) {
            me.loadData(records);
        }, me);
    }
});
