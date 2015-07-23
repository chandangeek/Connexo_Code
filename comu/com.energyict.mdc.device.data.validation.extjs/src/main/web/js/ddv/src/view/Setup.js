Ext.define('Ddv.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ddv-validationoverview-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Ddv.view.Grid'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',            
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'ddv-validationoverview-grid',
                        itemId: 'grd-validationoverview',
                        hidden: me.isHidden()
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-suspects',
                        title: Uni.I18n.translate('validation.validationOverview.noDevicesWithSuspect', 'MDC', 'No devices with suspects found'),
                        reasons: [
                            Uni.I18n.translate('validation.validationOverview.empty.list.item1', 'MDC', 'Data has not been validated yet.'),
                            Uni.I18n.translate('validation.validationOverview.empty.list.item2', 'MDC', 'Data has been successfully validated.')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
