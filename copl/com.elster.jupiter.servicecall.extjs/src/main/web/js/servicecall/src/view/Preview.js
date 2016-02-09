Ext.define('Scs.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.servicecalls-preview',
    requires: [
        'Scs.view.PreviewForm',
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'SCS', 'Actions'),
              //  privileges: Apr.privileges.AppServer.admin,
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'scs-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'servicecalls-preview-form',
        };
        me.callParent(arguments);
    }

});