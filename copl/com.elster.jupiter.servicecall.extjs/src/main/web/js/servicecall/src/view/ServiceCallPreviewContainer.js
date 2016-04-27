Ext.define('Scs.view.ServiceCallPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.service-call-preview-container',
    router: null,
    store: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Scs.view.Grid',
        'Scs.view.Preview',
        'Scs.view.ActionMenu',
        'Scs.view.ServiceCallFilter'
    ],

    emptyComponent:  {
        xtype: 'no-items-found-panel',
        itemId: 'no-service-calls',
        title: Uni.I18n.translate('serviceCalls.empty.title', 'SCS', 'No service calls found'),
        reasons: [
            Uni.I18n.translate('serviceCalls.empty.list.item1', 'SCS', 'No service calls have been defined yet.'),
            Uni.I18n.translate('serviceCalls.empty.list.item4', 'SCS', 'The filter is too narrow and did not find any service calls in the system.'),
            Uni.I18n.translate('serviceCalls.empty.list.item2', 'SCS', "You don't have permission to see (all) service calls.")
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'servicecalls-grid',
            itemId: 'grd-service-calls',
            menuItemId: 'service-calls-overview-scs-menu',
            defaultPageSize: 200,
            router: me.router,
            store: me.store
        };

        me.previewComponent = {
            xtype: 'servicecalls-preview',
            itemId: 'pnl-servicecalls-preview',
            router: me.router
        };


        me.callParent(arguments);
    }
});