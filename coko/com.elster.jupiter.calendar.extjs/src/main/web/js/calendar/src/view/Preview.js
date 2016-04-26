Ext.define('Cal.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.tou-preview',
    requires: [
        'Cal.view.PreviewForm',
        'Cal.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'CAL', 'Actions'),
                //privileges: Scs.privileges.ServiceCall.admin,
                iconCls: 'x-uni-action-iconD',
                itemId: 'touPreviewMenuButton',
                menu: {
                    xtype: 'tou-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'tou-preview-form',
            itemId: 'tou-grid-preview-form',
        };
        me.callParent(arguments);
    }

});