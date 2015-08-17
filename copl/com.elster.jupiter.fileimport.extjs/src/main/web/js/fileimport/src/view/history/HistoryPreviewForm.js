Ext.define('Fim.view.history.HistoryPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.fim-history-preview-form',

    requires: [
        'Uni.form.field.Duration'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),
    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.history.fileName', 'FIM', 'File name'),
                name: 'fileName'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'FIM', 'Status'),
                name: 'status'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.history.startedOn', 'FIM', 'Started on'),
                name: 'startedOnDisplay'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.history.finishedOn', 'FIM', 'Finished on'),
                name: 'finishedOnDisplay'
            },
            {
                xtype: 'uni-form-field-duration',
                name: 'duration'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.history.summary', 'FIM', 'Summary'),
                name: 'summary'
            }
        ];
        me.callParent();
    }
});
