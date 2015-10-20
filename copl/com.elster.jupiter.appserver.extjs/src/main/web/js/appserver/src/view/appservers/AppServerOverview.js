Ext.define('Apr.view.appservers.AppServerOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appserver-overview',
    requires: [
        'Apr.view.appservers.Menu',
        'Apr.view.appservers.PreviewForm',
        'Apr.view.appservers.ActionMenu'
    ],

    router: null,
    appServerName: null,



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

        me.content = {
            xtype: 'container',
                layout: 'hbox',
                items: [
                {
                    ui: 'large',
                    title: Uni.I18n.translate('general.overview', 'APR', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'appservers-preview-form',
                        margin: '0 0 0 100',
                        router: me.router
                    }
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'APR', 'Actions'),
                    iconCls: 'x-uni-action-iconD',
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'appservers-action-menu'
                    }
                }
            ]
        },

        this.callParent(arguments);
    }
});