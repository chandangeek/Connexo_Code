Ext.define('Est.estimationtasks.view.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.estimationtasks-history-preview',
    title: ' ',

    requires: [
        'Est.estimationtasks.view.HistoryPreviewForm',
        'Est.estimationtasks.view.HistoryActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'estimationtasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'estimationtasks-history-preview-form'
    }
});

