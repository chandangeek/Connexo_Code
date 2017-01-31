/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.servers.Inactive', {
    extend: 'Ext.grid.Panel',
    xtype: 'inactiveServers',
    layout: 'fit',
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed
    border: false,
    store: 'status.servers.Inactive',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Inactive servers</b>',
                sortable: true,
                dataIndex: 'name',
                flex: 1
            }
        ];

        this.callParent(arguments);
    }
});