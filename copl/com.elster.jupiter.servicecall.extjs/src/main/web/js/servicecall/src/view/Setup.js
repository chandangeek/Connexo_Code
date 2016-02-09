Ext.define('Scs.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecalls-setup',
    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Scs.view.Grid',
        'Scs.view.Preview',
        'Scs.view.ActionMenu'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'servicecalls-grid',
                        itemId: 'grd-service-calls',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-service-calls',
                        title: Uni.I18n.translate('serviceCalls.empty.title', 'SCS', 'No service calls found'),
                        reasons: [
                            Uni.I18n.translate('serviceCalls.empty.list.item1', 'SCS', 'There are no service calls in the system')
                        ]
                    },
                    previewComponent: {
                        xtype: 'servicecalls-preview',
                        itemId: 'pnl-servicecalls-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});