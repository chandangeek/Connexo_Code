Ext.define('Mdc.view.setup.devicegroup.DevicesOfDeviceGroupGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    xtype: 'devicesOfDeviceGroupGrid',
    itemId: 'allDevicesOfDeviceGroupGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DevicesOfDeviceGroup'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'DevicesOfDeviceGroup',

    initComponent: function () {
        var me = this;
        //me.store = Ext.StoreMgr.lookup('DevicesOfDeviceGroup');
        this.columns = [
            {
                header: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID'),
                dataIndex: 'mRID',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#/devices/' + record.get('mRID') + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypeName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                dataIndex: 'deviceConfigurationName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            }

        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page')
            }
        ];

        this.callParent();
    }
});



