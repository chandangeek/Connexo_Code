Ext.define('Scs.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.scs-log-setup',
    requires: [
        'Uni.util.FormEmptyMessage',
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
                        xtype: 'scs-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('servicecalls.log.empty.list', 'SCS', 'There are no logs for this service call')
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
