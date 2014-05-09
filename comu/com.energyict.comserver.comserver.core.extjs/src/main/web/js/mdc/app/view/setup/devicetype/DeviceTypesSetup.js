Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypesSetup',
    itemId: 'deviceTypeSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Uni.view.breadcrumb.Trail'
    ],

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<H1>' + Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types') + '</H1>'
                    //margins: '10 0 10 0'
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


