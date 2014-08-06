Ext.define('Mdc.view.setup.comportpoolcomports.AddComPortView', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'addComportToComportPoolView',
    itemId: 'addComportToComportPoolView',

    requires: [
        'Mdc.view.setup.comportpoolcomports.AddComPortGrid'
    ],

    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('setup.comportpoolcomports.AddComPortView.side.title', 'MDC', 'Communication port pools'),
        items: [
            {
                xtype: 'comportpoolsubmenu',
                itemId: 'comportpoolsubmenu'
            }
        ]
    },

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comPortPoolComPort.addComPort', 'MDC', 'Add communication port'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'addComportToComportPoolGrid'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});