Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerMappingsSetup',
    autoScroll: true,
    itemId: 'registerMappingSetup',
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingPreview',
        'Uni.view.breadcrumb.Trail'
    ],
    controllers:[
        'Mdc.controller.setup.RegisterMappings',
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-container',
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
                    html: Uni.I18n.translate('registerMapping.deviceType', 'MDC', 'Device type'),
                    margins: '10 10 0 20'
                },
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerTypeTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingPreviewContainer'

                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerMappingGridContainer').add(
            {
                xtype: 'registerMappingsGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
        this.down('#registerMappingPreviewContainer').add(
            {
                xtype: 'registerMappingPreview',
                deviceTypeId: this.deviceTypeId
            }
        );

    }
});


