Ext.define('Cfg.view.validationtask.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.cfg-tasks-history-preview',

    requires: [
        'Cfg.view.validationtask.HistoryPreviewForm',
        'Cfg.view.validationtask.HistoryActionMenu'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'cfg-tasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'cfg-tasks-history-preview-form'
    }
});

