Ext.define('Sct.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.servicecalltypes-preview',
    requires: [
        'Sct.view.PreviewForm',
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'SCT', 'Actions'),
              //  privileges: Apr.privileges.AppServer.admin,
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'sct-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'servicecalltypes-preview-form',
        };
        me.callParent(arguments);
    }

});