Ext.define('Fim.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-history-log-preview',
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'frm-history-log-preview',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200,
                labelAlign: 'right'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('importService.history.fileName', 'FIM', 'File name'),
                    name: 'fileName'
                },
                {
                    fieldLabel: Uni.I18n.translate('importService.log.runStartedOn', 'FIM', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('importService.history.status', 'FIM', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('importService.history.summary', 'FIM', 'Summary'),
                    name: 'summary'
                }
            ]
        };
        me.callParent(arguments);
    }
});
