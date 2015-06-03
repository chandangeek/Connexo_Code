Ext.define('Mdc.view.setup.estimationdeviceconfigurations.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.estimation-deviceconfigurations-preview',
    router: null,

    items: {
        xtype: 'form',
        itemId: 'estimation-deviceconfigurations-preview-form',
        defaults: {
            xtype: 'displayfield',
            labelWidth: 250
        },
        items: [
            {
                fieldLabel: Uni.I18n.translate('estimationDeviceConfigurations.deviceConfiguration', 'MDC', 'Device configuration'),
                name: 'name',
                itemId: 'config-name',
                renderer: function (value) {
                    var record = this.up('#estimation-deviceconfigurations-preview-form').getRecord();
                    if (record) {
                        var url = this.up('estimation-deviceconfigurations-preview').router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({deviceTypeId: record.get('deviceTypeId'), deviceConfigurationId: record.get('id')});
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('estimationDeviceConfigurations.deviceType', 'MDC', 'Device type'),
                name: 'deviceTypeName',
                itemId: 'device-type-name'
            },
            {
                fieldLabel: Uni.I18n.translate('estimationDeviceConfigurations.configurationStatus', 'MDC', 'Configuration status'),
                name: 'config_active',
                itemId: 'config-active'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('estimationDeviceConfigurations.dataSources', 'MDC', 'Data sources'),
                itemId: 'data-sources-field',
                items: [
                    {
                        xtype: 'container',
                        itemId: 'data-sources',
                        items: [
                        ]
                    }
                ]
            }
        ]
    }
});
