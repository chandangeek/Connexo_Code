Ext.define('Apr.view.appservers.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appservers-setup',
    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Apr.view.appservers.Grid',
        'Apr.view.appservers.Preview',
        'Apr.view.appservers.ActionMenu'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.applicationServers', 'APR', 'Application servers'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'appservers-grid',
                        itemId: 'grd-application-servers',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-app-server',
                        title: Uni.I18n.translate('appServers.empty.title', 'APR', 'No application servers found'),
                        reasons: [
                            Uni.I18n.translate('appServers.empty.list.item1', 'APR', 'There are no application servers in the system')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addApplicationServer', 'APR', 'Add application server'),
                                itemId: 'empty-grid-add-app-server',
                                privileges: Apr.privileges.AppServer.admin,
                                href: '#/administration/appservers/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'appservers-preview',
                        itemId: 'pnl-appserver-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});