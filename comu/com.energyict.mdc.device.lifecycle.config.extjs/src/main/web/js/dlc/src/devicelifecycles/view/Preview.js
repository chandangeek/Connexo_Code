Ext.define('Dlc.devicelifecycles.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycles-preview',

    requires: [
        'Dlc.devicelifecycles.view.PreviewForm',
        'Dlc.devicelifecycles.view.ActionMenu'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DLC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-life-cycles-action-menu',
                itemId: 'lifeCyclesActionMenu'
            }
        }
    ],

    items: {
        xtype: 'device-life-cycles-preview-form',
        itemId: 'device-life-cycles-preview-form'
    }
});
