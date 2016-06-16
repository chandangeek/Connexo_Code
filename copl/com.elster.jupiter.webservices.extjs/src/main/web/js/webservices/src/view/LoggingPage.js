Ext.define('Wss.view.LoggingPage', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservice-logging-page',
    requires: [
        'Wss.view.Menu',
        'Wss.view.LoggingSetup'
    ],

    router: null,
    record: null,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'webservices-menu',
                        itemId: 'webservices-menu',
                        router: me.router,
                        record: me.record
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    title: Uni.I18n.translate('general.Logging', 'WSS', 'Logging'),
                    flex: 1,
                    items: {
                        xtype: 'wss-logging-setup',
                        router: me.router,
                        margin: '0 0 0 100'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});