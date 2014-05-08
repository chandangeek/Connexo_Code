Ext.define('Mdc.view.setup.registertype.RegisterTypeSetup', {
    //extend: 'Ext.panel.Panel',
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeSetup',
    itemId: 'registerTypeSetup',
    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Mdc.view.setup.registertype.RegisterTypeFilter'
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
                    html: '<h1>' + Uni.I18n.translate('registerType.registerTypes', 'MDC', 'Register types') + '</h1>',
                    itemId: 'registerTypeTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerTypeGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'registerTypePreview'
                }
            ]}
    ],

    /*   side: [
     {
     xtype: 'registerTypeFilter',
     name: 'filter'
     }
     ],
     */

    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerTypeGridContainer').add(
            {
                xtype: 'registerTypeGrid'
            }
        );
    }
});


