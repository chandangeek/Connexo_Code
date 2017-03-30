/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.servers.Active' ,{
    extend: 'Ext.grid.Panel',
    xtype: 'activeServers',
    layout: 'fit',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.servers.Active',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Active servers</b>',
                dataIndex: 'name',
                sortable: true,
                flex: 1
            },
            {
                header: '<b>Last communication date</b>',
                dataIndex: 'lastSeen',
                sortable: false,
                flex: 1,
                renderer: function(value, meta, record) {
                    if (record.get('lastSeen')) {
                        return record.get('lastSeen');
                    }
                }
            }
        ];

        this.callParent(arguments);
    }
});