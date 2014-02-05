Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.registerMappingsSetup',
    autoScroll: true,
    requires: [
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingPreview'
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
            items:[
                {
                    xtype: 'component',
                    html: '<h1>Register mappings</h1>',
                    margins: '10 10 10 10'
                },
                {
                    border: false,
                    tbar: [
                        '->',
                        {
                            text: 'Create new register',
                            itemId: 'createRegisterMapping',
                            xtype: 'button',
                            href: '#/setup/createregistermapping',
                            hrefTarget: '_self'
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
                    height : 50
                },
                {
                    xtype: 'registerMappingPreview'
                }
    ]}],


    initComponent: function () {
        this.callParent(arguments);
    }
});


