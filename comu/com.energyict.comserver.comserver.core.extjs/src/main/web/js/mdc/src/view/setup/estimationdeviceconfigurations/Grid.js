Ext.define('Mdc.view.setup.estimationdeviceconfigurations.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.estimation-deviceconfigurations-grid',
    store: 'Mdc.store.EstimationDeviceConfigurations',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({deviceTypeId: record.get('deviceTypeId'), deviceConfigurationId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                dataIndex: 'deviceTypeName',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationDeviceConfigurations.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device configurations'),
                displayMoreMsg: Uni.I18n.translate('estimationDeviceConfigurations.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device configurations'),
                emptyMsg: Uni.I18n.translate('estimationDeviceConfigurations.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device configurations to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-deviceconfigurations-button',
                        text: Uni.I18n.translate('estimationDeviceConfigurations.addDeviceConfigurations', 'MDC', 'Add device configurations'),
                        href: me.router.getRoute('administration/estimationrulesets/estimationruleset/deviceconfigurations/add').buildUrl(),
                        privileges: Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfiguration
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationDeviceConfigurations.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device configurations per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

