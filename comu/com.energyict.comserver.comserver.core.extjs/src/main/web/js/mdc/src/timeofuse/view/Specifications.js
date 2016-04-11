Ext.define('Mdc.timeofuse.view.Specifications', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tou-specifications-preview-panel',
    frame: false,
    requires: [
        'Mdc.timeofuse.view.SpecificationsForm',
        'Mdc.timeofuse.view.SpecificationsActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                //privileges: Scs.privileges.ServiceCall.admin,
                iconCls: 'x-uni-action-iconD',
                itemId: 'touSpecificationsButton',
                menu: {
                    xtype: 'tou-spec-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'tou-devicetype-specifications-form',
            itemId: 'tou-devicetype-specifications-form',
        };
        me.callParent(arguments);
    }

});