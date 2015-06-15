Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConfigurationsGrid',
    overflowY: 'auto',
    itemId: 'deviceconfigurationsgrid',
    deviceTypeId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceConfigurations',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationActionMenu'
    ],

    store: 'DeviceConfigurations',



    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceconfiguration.status', 'MDC', 'Status'),
                dataIndex: 'active',
                renderer: function (value, b, record) {
                    return value === true ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                },
                flex: 1
            },

            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'device-configuration-action-menu'
                }
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device configurations'),
                displayMoreMsg: Uni.I18n.translate('deviceconfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device configurations'),
                emptyMsg: Uni.I18n.translate('deviceconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device configurations to display'),
                items: [
                    {
                        text: Uni.I18n.translate('deviceconfiguration.createDeviceConfiguration', 'MDC', 'Add device configuration'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'createDeviceConfiguration',
                        xtype: 'button',
                        action: 'createDeviceConfiguration'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                //todo: check if this works
                params: [
                    {deviceType: this.deviceTypeId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device configurations per page'),
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});

