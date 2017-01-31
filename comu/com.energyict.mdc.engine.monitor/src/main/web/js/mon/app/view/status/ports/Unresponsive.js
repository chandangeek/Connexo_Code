/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.ports.Unresponsive', {
    extend: 'Ext.grid.Panel',
    xtype: 'unresponsivePorts',
    layout: 'fit',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.ports.Unresponsive',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Unresponsive ports</b>',
                dataIndex: 'name',
                flex: 1,
                renderer: function(value, meta, record) {
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