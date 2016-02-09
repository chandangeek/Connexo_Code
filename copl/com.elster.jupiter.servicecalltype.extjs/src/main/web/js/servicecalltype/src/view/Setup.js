Ext.define('Sct.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecalltypes-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Sct.view.Grid',
        'Sct.view.Preview',
        'Sct.view.ActionMenu'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.serviceCallTypes', 'SCT', 'Service call types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'servicecalltypes-grid',
                        itemId: 'grd-service-call-types',
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-service-calls',
                        title: Uni.I18n.translate('serviceCallTypes.empty.title', 'SCT', 'No service call types found'),
                        reasons: [
                            Uni.I18n.translate('serviceCallTypes.empty.list.item1', 'SCT', 'There are no service call types in the system')
                        ]
                    },
                    previewComponent: {
                        xtype: 'servicecalltypes-preview',
                        itemId: 'pnl-servicecalltypes-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});