Ext.define('Mdc.view.setup.devicetopology.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceTopologyGrid',
    itemId: 'deviceTopologyGrid',
    store: 'Mdc.store.DeviceTopology',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceTopology'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.mRID', 'MDC', 'MRID'),
                dataIndex: 'mRID',
                renderer: function (value, meta, record) {
                    var href = me.router.getRoute('devices/device').buildUrl({mRID: record.get('mRID')});
                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypeName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.configuration', 'MDC', 'Configuration'),
                dataIndex: 'deviceConfigurationName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.addedOn', 'MDC', 'Added on'),
                dataIndex: 'creationTime',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                deferLoading: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                deferLoading: true,
                store: me.store,
                defaultPageSize: 15,
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page'),
                dock: 'bottom',
                pageSizeStore: Ext.create('Ext.data.Store', {
                    fields: ['value'],
                    data: [
                        {value: '15'},
                        {value: '30'},
                        {value: '50'},
                        {value: '100'}
                    ]
                })
            }
        ];

        me.callParent(arguments);
        me.maxHeight = 560;
    }
});