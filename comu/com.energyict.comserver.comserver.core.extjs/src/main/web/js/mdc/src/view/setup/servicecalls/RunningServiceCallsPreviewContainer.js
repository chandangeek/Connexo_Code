Ext.define('Mdc.view.setup.servicecalls.RunningServiceCallsPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.running-service-call-preview-container',
    store: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Scs.view.Grid'
    ],

    emptyComponent:  {
        xtype: 'no-items-found-panel',
        itemId: 'no-service-calls',
        title: Uni.I18n.translate('serviceCalls.empty.title', 'MDC', 'No service calls found'),
        reasons: [
            Uni.I18n.translate('serviceCalls.empty.list.item1', 'MDC', 'No service calls have been defined yet.'),
            Uni.I18n.translate('serviceCalls.empty.list.item2', 'MDC', 'Service calls exist, but you do not have permission to view them.'),
            Uni.I18n.translate('serviceCalls.empty.list.item3', 'MDC', 'No running service calls have been found.')
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'servicecalls-grid',
            store: me.store
        };


        me.callParent(arguments);
    }
});