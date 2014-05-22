Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationsSetup',
    deviceTypeId: null,
    itemId: 'deviceConfigurationsSetup',

    requires: [
        'Mdc.view.setup.devicetype.DeviceTypeMenu'
    ],


    content: [
        {
            ui: 'large',
            xtype: 'panel',
            title: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),
            items: [
                {
                    xtype: 'container',
                    itemId: 'DeviceConfigurationsGridContainer'
                },
                {
                    xtype: 'deviceConfigurationPreview'
                }
            ]
        }
    ],


    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceTypeMenu',
                itemId: 'stepsMenu',
                deviceTypeId: this.deviceTypeId,
                toggle: 4
            }
        ];
        this.callParent(arguments);
        this.down('#DeviceConfigurationsGridContainer').add(
            {
                xtype: 'deviceConfigurationsGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
    }
});