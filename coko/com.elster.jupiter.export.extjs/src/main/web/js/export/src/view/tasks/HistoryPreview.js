Ext.define('Dxp.view.tasks.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.tasks-history-preview',

    requires: [
        'Dxp.view.tasks.HistoryPreviewForm',
        'Dxp.view.tasks.HistoryActionMenu'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DES', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'tasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'tasks-history-preview-form'//,
        //itemId: 'pnl-data-export-task-preview'
    }
});

