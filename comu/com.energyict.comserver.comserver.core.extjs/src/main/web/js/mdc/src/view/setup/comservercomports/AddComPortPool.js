Ext.define('Mdc.view.setup.comservercomports.AddComPortPool', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'addComPortPool',
    itemId: 'addComPortPoolToComPort',

    requires: [
        'Mdc.view.setup.comservercomports.AddComPortPoolsGrid'
    ],

    content: [
        {
            xtype: 'panel',
            title: Uni.I18n.translate('comPortPool.addComPortPool','MDC','Add communication port pool'),
            ui: 'large',
            items: [
                {
                    xtype: 'add-com-port-pools-grid',
                    itemId: 'addComPortPoolsGrid'
                }
            ]
        }
    ],

    updateCancelHref: function (comServerId, comPortId) {
        this.down('add-com-port-pools-grid').updateCancelHref(comServerId, comPortId);
    }
});


