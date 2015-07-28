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
            xtype: 'button',
            text: Uni.I18n.translate('estimationtasks.general.actions', 'EST', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'estimationtasks-history-action-menu'
            }
        }
    ],

    items: {
        xtype: 'estimationtasks-history-preview-form'
    }
});

