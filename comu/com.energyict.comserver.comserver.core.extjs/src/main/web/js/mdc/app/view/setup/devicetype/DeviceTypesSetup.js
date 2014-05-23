Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypesSetup',
    itemId: 'deviceTypeSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview'
    ],

    content: [
        {
            ui: 'large',
            xtype: 'panel',
            title: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            items: [
                {
                    xtype: 'deviceTypesGrid'
                },
                {
                    xtype: 'deviceTypePreview'
                }
            ]
        }
    ],



    initComponent: function () {
        this.callParent(arguments);
    }
});


