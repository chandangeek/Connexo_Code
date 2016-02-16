Ext.define('Scs.view.ServiceCallPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.service-call-preview-container',
    router: null,

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
            Uni.I18n.translate('serviceCalls.empty.list.item1', 'SCS', 'There are no service calls in the system')
        ]
    },

    initComponent: function () {
        var me = this;
        //me.title =  me.title === 'none' ? '' : Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls');
         me.grid = {
            xtype: 'servicecalls-grid',
            itemId: 'grd-service-calls',
            router: me.router
        };

        me.previewComponent = {
            xtype: 'servicecalls-preview',
            itemId: 'pnl-servicecalls-preview'
        };


        me.callParent(arguments);
    }
});