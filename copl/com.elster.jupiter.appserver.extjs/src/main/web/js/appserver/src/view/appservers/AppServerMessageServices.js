Ext.define('Apr.view.appservers.AppServerMessageServices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appserver-message-services',
    requires: [
        'Apr.view.appservers.Menu'
    ],

    router: null,
    appServerName: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                flex: 1,
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'appservers-menu',
                        itemId: 'apr-menu',
                        router: me.router,
                        appServerName: me.appServerName
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});