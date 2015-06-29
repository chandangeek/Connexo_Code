Ext.define('Apr.view.appservers.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appservers-setup',
    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Apr.view.appservers.Grid',
        'Apr.view.appservers.Preview',
        'Apr.view.appservers.ActionMenu',
        'Apr.view.appservers.SortingToolbar'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.applicationServers', 'APR', 'Application servers'),
            items: [
                {
                    itemId: 'appservers-sorting-toolbar',
                    xtype: 'appservers-sorting-toolbar',
                    hideEmpty: false
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'appservers-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('appServers.empty.title', 'APR', 'No application servers found'),
                        reasons: [
                            Uni.I18n.translate('appServers.empty.list.item1', 'APR', 'There are no application servers in the system')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addApplicationServer', 'APR', 'Add application server'),
                                itemId: 'empty-grid-add-app-server',
                                href: '#/administration/appservers/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'appservers-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});