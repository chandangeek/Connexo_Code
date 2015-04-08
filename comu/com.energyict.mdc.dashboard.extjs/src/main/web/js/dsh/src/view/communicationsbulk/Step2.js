Ext.define('Dsh.view.communicationsbulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communications-bulk-step2',
    items: [
        {
            xtype: 'radiogroup',
            itemId: 'communications-bulk-action-radiogroup',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '0 0 16 0'
            },
            items: [
                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('communication.bulk.actionRadioGroup.RunLabel', 'DSH', 'Run') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('communication.bulk.actionRadioGroup.RunDescription', 'DSH', 'The selected communications will be queued for the next scheduled run.')
                    + '</span>',
                    inputValue: 'run',
                    checked: true
                },
                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('communication.bulk.actionRadioGroup.RunNowLabel', 'DSH', 'Run now') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('communication.bulk.actionRadioGroup.RunNowDescription', 'DSH', 'The selected communications will be queued for an immediate run.')
                    + '</span>',
                    inputValue: 'runNow'
                }
            ]
        }
    ]
});