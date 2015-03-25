Ext.define('Mdc.view.setup.devicetype.DeviceTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceTypesGrid',
    overflowY: 'auto',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceTypes',
        'Mdc.view.setup.devicetype.DeviceTypeActionMenu',
        'Ext.ux.exporter.ExporterButton'
    ],
    store: 'Mdc.store.DeviceTypes',
    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        me.columns = [
            {
                header: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/devicetypes/' + record.get('id') + '">' + value + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol'),
                dataIndex: 'deviceProtocolPluggableClass',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
                items: 'Mdc.view.setup.devicetype.DeviceTypeActionMenu'
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device types'),
                displayMoreMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device types'),
                emptyMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device types to display'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetype.createDeviceType', 'MDC', 'Add device type'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
                        itemId: 'createDeviceType',
                        xtype: 'button',
                        action: 'createDeviceType'
                    }
                    ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('devicetype.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device types per page'),
            }
        ];

        me.callParent();
    }
});
