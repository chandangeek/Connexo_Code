Ext.define('Dlc.devicelifecycletransitions.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycle-transitions-preview',

    requires: [
        'Dlc.devicelifecycletransitions.view.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DLC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'transitions-action-menu',
                itemId: 'transitions-action-menu'
            }
        }
    ],

    items: {
        xtype: 'device-life-cycle-transitions-preview-form',
        itemId: 'device-life-cycle-transitions-preview-form'
    }
});
