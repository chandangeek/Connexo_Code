Ext.define('Mdc.view.setup.comportpoolcomports.AddComPortView', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'addComportToComportPoolView',
    itemId: 'addComportToComportPoolView',

    requires: [
        'Mdc.view.setup.comportpoolcomports.AddComPortGrid'
    ],

    poolId: null,
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
        var me = this;
        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'comportpoolsidemenu',
                    itemId: 'comportpoolsidemenu',
                    poolId: me.poolId
                }
            ]
        };
        me.callParent(arguments)
    },

    updateCancelHref: function (comPortPoolId) {
        this.down('addComportToComportPoolGrid').updateCancelHref(comPortPoolId);
    }
});