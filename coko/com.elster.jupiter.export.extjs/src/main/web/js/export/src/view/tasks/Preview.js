Ext.define('Dxp.view.tasks.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.tasks-preview',

    requires: [
        'Dxp.view.tasks.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DXP', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'tasks-action-menu'
            }
        }
    ],

    items: {
        xtype: 'tasks-preview-form'
    }
});
