Ext.define('Mdc.view.setup.register.RegisterMappingAdd', {
    extend: 'Ext.container.Container',
    alias: 'widget.registerMappingAdd',
    autoScroll: true,
    requires: [
        'Mdc.view.setup.register.RegisterMappingAddGrid'
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
                    xtype: 'component',
                    html: '<h1>Add register types</h1>',
                    margins: '10 10 10 10'
                },
                {
                    border: false,
                    tbar: [
                        '->',
                        {
                            text: 'Manage register types',
                            itemId: 'manageRegisterMappingBtn',
                            xtype: 'button',
                            href: '',
                            hrefTarget: '_self'
                        },
                        {
                            text: 'Create register types',
                            itemId: 'createRegisterMappingBtn',
                            xtype: 'button',
                            href: '',
                            hrefTarget: '_self'

                        }]
                },
                {
                    xtype: 'registerMappingAddGrid'
                },
                {
                    xtype: 'component',
                    height: 50
                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});


