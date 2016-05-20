Ext.define('Mdc.timeofuse.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tou-preview-panel',
    frame: true,
    timeOfUseAllowed: null,

    requires: [
        'Mdc.timeofuse.view.PreviewForm',
        'Mdc.timeofuse.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                privileges: Mdc.privileges.DeviceType.view,
                iconCls: 'x-uni-action-iconD',
                itemId: 'touPreviewMenuButton',
                hidden: !me.timeOfUseAllowed,
                menu: {
                    xtype: 'tou-devicetype-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'devicetype-tou-preview-form',
            itemId: 'devicetype-tou-preview-form'
        };
        me.callParent(arguments);
    }

});