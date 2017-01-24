Ext.define('Fim.view.history.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.fim-history-preview',
    requires: [
        'Fim.view.history.HistoryPreviewForm'
    ],

    items: {
        xtype: 'fim-history-preview-form'
    }
});

