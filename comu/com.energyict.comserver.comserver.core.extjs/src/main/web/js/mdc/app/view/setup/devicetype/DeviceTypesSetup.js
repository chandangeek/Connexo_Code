Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceTypesSetup',
    autoScroll: true,
    itemId: 'deviceTypeSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
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
                    html: '<h1>Device types</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'deviceTypesGrid'
                },
                {
                    xtype: 'component',
                    height : 25
                },
                {
                    xtype: 'deviceTypePreview'
                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});


