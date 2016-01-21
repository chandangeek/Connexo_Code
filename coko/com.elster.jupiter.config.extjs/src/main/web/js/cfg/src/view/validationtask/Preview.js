Ext.define('Cfg.view.validationtask.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.cfg-tasks-preview',
    appName: null,

    requires: [
        'Cfg.view.validationtask.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'cfg-validation-tasks-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'cfg-tasks-preview-form',
            itemId: 'pnl-validation-task-preview-form',
            appName: me.appName
        };

        me.callParent(arguments);
    }
});
