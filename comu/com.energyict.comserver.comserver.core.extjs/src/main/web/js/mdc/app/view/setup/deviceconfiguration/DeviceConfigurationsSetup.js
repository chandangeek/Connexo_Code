Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceConfigurationsSetup',
    autoScroll: true,
    itemId: 'deviceConfigurationsSetup',
    requires: [
        'Uni.view.breadcrumb.Trail'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',
//    border: 0,
//    region: 'center',

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },
                {
                    xtype: 'component',
                    html: '<h1>'+ I18n.translate('deviceconfig.deviceConfigurations', 'MDC', 'Device configurations')+'</h1>',
                    margins: '10 10 10 10'
                }
//                {
//                    xtype: 'deviceTypesGrid'
//                },
//                {
//                    xtype: 'component',
//                    height : 25
//                },
//                {
//                    xtype: 'deviceTypePreview'
//                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});


