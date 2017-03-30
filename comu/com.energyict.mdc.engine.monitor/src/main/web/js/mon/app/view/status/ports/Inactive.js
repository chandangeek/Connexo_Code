/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.ports.Inactive', {
    extend: 'Ext.grid.Panel',
    xtype: 'inactivePorts',
    layout: 'fit',
    itemId: 'inactivePorts',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.ports.Inactive',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Ports not in use</b>',
                dataIndex: 'name',
                flex: 8,
                sortable: true,
                renderer: function(value, meta, record) {
                    meta.tdAttr = 'data-qtip="' + record.get('description') + '"';
                    var iconFileName;
                    if (record.get('inbound')) {
                        iconFileName = 'inbound_comport.png';
                    } else {
                        iconFileName = 'outbound_comport.png';
                    }
                    return '<img height=16 src="resources/images/' + iconFileName + '"/> ' + value;
                }
            }
        ];

        this.callParent(arguments);
    }
});