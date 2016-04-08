Ext.define('Mdc.timeofuse.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tou-preview-panel',
    frame: true,
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
                //privileges: Scs.privileges.ServiceCall.admin,
                iconCls: 'x-uni-action-iconD',
                itemId: 'touPreviewMenuButton',
                menu: {
                    xtype: 'tou-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'devicetype-tou-preview-form',
            itemId: 'devicetype-tou-preview-form',
        };
        me.callParent(arguments);
    }

});