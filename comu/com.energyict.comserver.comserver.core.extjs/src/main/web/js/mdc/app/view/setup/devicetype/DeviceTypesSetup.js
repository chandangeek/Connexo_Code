Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceTypesSetup',
    autoScroll: true,
    itemId: 'deviceTypeSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Uni.view.breadcrumb.Trail'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-container',
    items: [
        {
            xtype: 'breadcrumbTrail',
            region: 'north',
            padding: 6
        },
        {
            xtype: 'component',
            html: '<h1>' + Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types') + '</h1>',
            margins: '10 10 10 10'
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
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});


