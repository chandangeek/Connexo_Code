Ext.define('Mdc.view.setup.registertype.RegisterTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeSetup',
    itemId: 'registerTypeSetup',

    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Mdc.view.setup.registertype.RegisterTypeFilter'
    ],

    hidden: true,

    content: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
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


