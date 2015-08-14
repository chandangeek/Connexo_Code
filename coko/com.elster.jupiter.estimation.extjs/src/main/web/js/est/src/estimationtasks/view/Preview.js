Ext.define('Est.estimationtasks.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.estimationtasks-preview',
    title: ' ',

    requires: [
        'Est.estimationtasks.view.DetailForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'EST', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'estimationtasks-action-menu'
            }
        }
    ],

    items: {
        xtype: 'estimationtasks-detail-form',
        itemId: 'estimationtasks-detail-form'
    }
});
