Ext.define('Dxp.view.tasks.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-tasks-history-preview',

    requires: [
        'Dxp.view.tasks.HistoryPreviewForm'
    ],

    items: {
        xtype: 'dxp-tasks-history-preview-form'
    }
});

