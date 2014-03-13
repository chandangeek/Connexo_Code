Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceConfigurationsSetup',
    autoScroll: true,
    deviceTypeId: null,
    itemId: 'deviceConfigurationsSetup',
    cls: 'content-container',
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
                    html: '<h1>'+ Uni.I18n.translate('deviceconfig.deviceConfigurations', 'MDC', 'Device configurations')+'</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'container',
                    itemId: 'DeviceConfigurationsGridContainer'
                },
                {
                    xtype: 'component',
                    height : 25
                },
                {
                    xtype: 'deviceConfigurationPreview'
                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#DeviceConfigurationsGridContainer').add(
            {
                xtype: 'deviceConfigurationsGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
    }
});


