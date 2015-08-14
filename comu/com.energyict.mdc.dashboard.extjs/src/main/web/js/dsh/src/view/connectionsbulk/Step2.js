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
                    boxLabel: '<b>' + Uni.I18n.translate('general.runNow', 'DSH', 'Run now') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('connection.bulk.actionRadioGroup.RunNowDescription', 'DSH', 'The selected outbound connections and their associated tasks with state Pending, Failed, Retrying or Never completed will be queued. All non-outbound connections will be ignored.')
                    + '</span>',
                    inputValue: 'runNow',
                    checked: true
                },
                {
                    itemId: 'adjust-radio-btn',
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('connection.bulk.actionRadioGroup.adjustAttributesLabel', 'DSH', 'Adjust connection attributes') + '</b>',
                    afterSubTpl : '<span style="color: grey;padding: 0 0 0 19px;">'
                        + Ext.String.format(Uni.I18n.translate('connection.bulk.actionRadioGroup.adjustAttributesDescription', 'DSH', 'The requested connection attribute adjustments will be queued. The selected connections must have the same connection type. If needed, use the Connection type filter on the {0} screen to make your data set compliant.'),
                            '<a id="connections-link" href="#/workspace/connections/details">'
                            + Uni.I18n.translate('general.connections', 'DSH', 'Connections')
                            + '</a>')
                        + '</span>',
                    inputValue: 'adjustAttributes',
                    checked: false
                }
            ]
        }
    ]
});