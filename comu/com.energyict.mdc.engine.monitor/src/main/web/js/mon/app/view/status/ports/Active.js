Ext.define('CSMonitor.view.status.ports.Active', {
    extend: 'Ext.grid.Panel',
    xtype: 'activePorts',
    layout: 'fit',
    border: false,
    enableColumnHide: false, // no column choosing
    disableSelection: true, // no selection allowed

    store: 'status.ports.Active',

    initComponent: function() {
        this.columns = [
            {
                header: '<b>Id</b>',
                dataIndex: 'id',
                sortable: true,
                flex: 1
            },
            {
                header: '<b>Active ports</b>',
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
                    return '<img height=16 src="resources/images/' + iconFileName + '"/> ' + value;
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
        ];
        this.callParent(arguments);
    }
});
