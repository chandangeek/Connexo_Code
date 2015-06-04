Ext.define('Fim.view.history.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-history-preview',
    requires: [
        'Fim.view.history.HistoryPreviewForm',
        'Fim.view.history.HistoryActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'FIM', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'fim-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'fim-history-preview-form'
    }
});

