Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.registerMappingsSetup',
    autoScroll: true,
    itemId: 'registerMappingSetup',
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
                    html: 'Device type',
                    margins: '10 10 0 20'
                },
                {
                    xtype: 'component',
                    html: '<h1>Register types</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerTypeTitle'
                },
                {
                    border: false,
                    tbar: [
                        '->',
                        {
                            text: 'Add register types',
                            itemId: 'addRegisterMappingBtn',
                            xtype: 'button',
                            href: '',
                            hrefTarget: '_self',
                            action: 'addRegisterMapping'
                        },
                        {
                            text: 'Bulk action',
                            itemId: 'registerMappingsBulkAction',
                            xtype: 'button'
                        }]
                },
                {
                    xtype: 'registerMappingsGrid'
                },
                {
                    xtype: 'component',
                    height: 50
                },
                {
                    xtype: 'registerMappingPreview'
                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});


