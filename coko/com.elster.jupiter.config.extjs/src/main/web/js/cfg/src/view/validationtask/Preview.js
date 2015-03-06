Ext.define('Cfg.view.validationtask.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.tasks-preview',

    requires: [
        'Cfg.view.validationtask.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('dataValidationTasks.general.actions', 'CFG', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'tasks-action-menu'
            }
        }
    ],

    items: {
        xtype: 'tasks-preview-form',
        itemId: 'pnl-data-validation-task-preview-form'
    }
});
