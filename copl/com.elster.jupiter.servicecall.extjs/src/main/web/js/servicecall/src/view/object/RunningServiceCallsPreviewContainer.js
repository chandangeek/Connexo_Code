Ext.define('Scs.view.object.RunningServiceCallsPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.running-service-call-preview-container',
    store: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Scs.view.Grid'
    ],

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-service-calls',
        title: Uni.I18n.translate('serviceCalls.empty.title', 'SCS', 'No service calls found'),
        reasons: [
            Uni.I18n.translate('serviceCalls.empty.list.item1', 'SCS', 'No service calls have been defined yet.'),
            Uni.I18n.translate('serviceCalls.empty.list.item3', 'SCS', 'No running service calls have been found.'),
            Uni.I18n.translate('serviceCalls.empty.list.item2', 'SCS', 'You don’t have permission to see (all) service calls.')
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'servicecalls-grid',
            store: me.store,
            menuItemId: 'object-service-calls-action-menu',
            itemId: 'running-service-calls-grid',
            cancelAllHidden: false,
            usesExactCount: true
        };


        me.callParent(arguments);
    }
});