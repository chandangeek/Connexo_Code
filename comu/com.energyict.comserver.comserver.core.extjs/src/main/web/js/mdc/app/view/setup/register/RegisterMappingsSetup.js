Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.registerMappingsSetup',
    autoScroll: true,
    itemId: 'registerMappingSetup',
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingPreview',
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
                    html: I18n.translate('registerMapping.deviceType','MDC','Device type'),
                    margins: '10 10 0 20'
                },
                {
                    xtype: 'component',
                    html: '<h1>' + I18n.translate('registerMapping.registerTypes','MDC','Register types') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerTypeTitle'
                },
                {
                    border: false,
                    tbar: [
                        '->',
                        {
                            text: I18n.translate('registerMapping.addRegisterMapping','MDC','Add register types'),
                            itemId: 'addRegisterMappingBtn',
                            xtype: 'button',
                            href: '',
                            hrefTarget: '_self',
                            action: 'addRegisterMapping'
                        },
                        {
                            text: I18n.translate('general.bulkAction','MDC','Bulk action'),
                            itemId: 'registerMappingsBulkAction',
                            xtype: 'button'
                        }]
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
                    xtype: 'registerMappingPreview'
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
    }
});


