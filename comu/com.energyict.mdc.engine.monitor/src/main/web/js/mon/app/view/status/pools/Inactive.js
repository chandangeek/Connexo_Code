/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.pools.Inactive', {
    extend: 'Ext.grid.Panel',
    xtype: 'inactivePools',
    layout: 'fit',
    itemId: 'inactivePools',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.pools.Inactive',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Pools not in use</b>',
                dataIndex: 'name',
                sortable: true,
                flex: 1,
                renderer: function(value, meta, record) {
                    meta.tdAttr = 'data-qtip="' + record.get('description')+ '"';
                    if (record.get('inbound')) {
                        iconFileName = 'inbound_comportpool.png';
                    } else {
                        iconFileName = 'outbound_comportpool.png';
                    }
                    return '<img height=16 src="resources/images/' + iconFileName + '"/> ' + value;
                }
            }
        ];

        this.callParent(arguments);
    }
});