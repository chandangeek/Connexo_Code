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
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types') + '</h1>'
                },
                {
                    xtype: 'deviceTypesGrid'
                },
                {
                    xtype: 'component',
                    height: 25
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


