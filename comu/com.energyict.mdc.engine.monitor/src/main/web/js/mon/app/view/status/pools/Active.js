/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.pools.Active', {
    extend: 'Ext.grid.Panel',
    xtype: 'activePools',
    layout: 'fit',
    itemId: 'activePools',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.pools.Active',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Pools in use</b>',
                flex: 1,
                dataIndex: 'name',
                sortable: true,
                renderer: function(value, meta, record) {
                    meta.tdAttr = 'data-qtip="' + record.get('description')+ '"';
                    var iconFileName;
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