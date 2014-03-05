Ext.define('Mdc.view.setup.devicecommunicationprotocol.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupDeviceCommunicationProtocols',
    itemId: 'devicecommunicationprotocolgrid',
    title: 'All device communication protocols',
    store: 'DeviceCommunicationProtocols',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    controllers: [
        'Mdc.controller.setup.DeviceCommunicationProtocol'
    ],

    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Id', dataIndex: 'id'},
            { header: 'Name', dataIndex: 'name'},
            { header: 'Java class name',
                renderer: function (value, meta, record) {
                return record.getLicensedProtocol().get('protocolJavaClassName');
            }},
            { header: 'Version', dataIndex: 'deviceProtocolVersion'}
        ]
    },
    initComponent: function () {
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top'
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom'
            }
        ];
        this.callParent(arguments);
    }
});