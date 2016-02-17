Ext.define('Scs.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.scs-log-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Scs.view.log.Grid',
        'Uni.view.container.PreviewContainer'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.logging', 'SCS', 'Logging'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'scs-log-grid',
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('servicecalls.log.empty.title', 'SCS', 'No logs found'),
                        reasons: [
                            Uni.I18n.translate('servicecalls.log.empty.list', 'SCS', 'There are no logs for this service call')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
