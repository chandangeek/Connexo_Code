Ext.define('Dsh.view.connectionsbulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.connections-bulk-step2',
    items: [
        {
            xtype: 'radiogroup',
            itemId: 'connections-bulk-action-radiogroup',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '0 0 16 0'
            },
            items: [
                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('connection.bulk.actionRadioGroup.RunNowLabel', 'DSH', 'Run now') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('connection.bulk.actionRadioGroup.RunNowDescription', 'DSH', 'The selected outbound connections and their associated tasks with state Pending, Failed, Retrying or Never completed will be queued. All non-outbound connections will be ignored.')
                    + '</span>',
                    inputValue: 'runNow',
                    checked: true
                }
            ]
        }
    ]
});