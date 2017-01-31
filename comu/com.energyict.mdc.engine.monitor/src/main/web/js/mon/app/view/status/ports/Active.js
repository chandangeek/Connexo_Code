/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.status.ports.Active', {
    extend: 'Ext.grid.Panel',
    xtype: 'activePorts',
    layout: 'fit',
    itemId: 'activePorts',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.ports.Active',
    columns: [
        {
            header: '<b>Ports in use</b>',
            dataIndex: 'name',
            sortable: true,
            flex: 8,
            renderer: function(value, meta, record) {
                meta.tdAttr = 'data-qtip="' + record.get('description') + '"';
                var iconFileName;
                if (record.get('inbound')) {
                    iconFileName = 'inbound_comport.png';
                } else {
                    iconFileName = 'outbound_comport.png';
                }
                var comPortName = record.data.name;
                return '<img height=16 src="resources/images/' + iconFileName + '"/> <a class="ports" href="#logging/comm/portid='+comPortName+' " target="_blank">' + value +'</a>';
            }
        },
        {
            header: '<b>Last port poll date</b>',
            dataIndex: 'lastSeen',
            sortable: true,
            flex: 6,
            renderer: function(value, meta, record) {
                if (record.get('lastSeen')) {
                    return record.get('lastSeen');
                }
            }
        },
        {
            header: '<b>Threads</b>',
            dataIndex: 'threads',
            sortable: false,
            flex: 2
        }
    ]
});
