Ext.define('Wss.view.LoggingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.wss-logging-setup',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Wss.view.LoggingGrid',
        'Uni.view.container.PreviewContainer'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.logging', 'WSS', 'Logging'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'wss-logging-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('webservices.log.empty.list', 'WSS', 'There are no logs for this webservice endpoint')
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
