Ext.define('Cfg.view.validationtask.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.tasks-history-preview',

    requires: [
        'Cfg.view.validationtask.HistoryPreviewForm',
        'Cfg.view.validationtask.HistoryActionMenu'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('validationTasks.general.actions', 'CFG', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'tasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'tasks-history-preview-form'
    }
});

