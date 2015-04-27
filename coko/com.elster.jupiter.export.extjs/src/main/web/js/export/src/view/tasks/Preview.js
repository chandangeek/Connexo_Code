Ext.define('Dxp.view.tasks.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-tasks-preview',

    requires: [
        'Dxp.view.tasks.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DES', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'dxp-tasks-action-menu'
            }
        }
    ],

    items: {
        xtype: 'dxp-tasks-preview-form',
        itemId: 'pnl-data-export-task-preview-form'
    }
});
