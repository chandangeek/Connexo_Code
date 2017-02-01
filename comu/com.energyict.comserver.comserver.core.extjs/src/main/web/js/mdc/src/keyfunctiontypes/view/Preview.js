Ext.define('Mdc.keyfunctiontypes.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.key-function-types-preview',
    frame: true,

    requires: [
        'Mdc.keyfunctiontypes.view.PreviewForm',
        'Mdc.keyfunctiontypes.view.KeyFunctionTypesActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.admin,
                itemId: 'key-function-type-preview-button',
                menu: {
                    xtype: 'key-function-types-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'devicetype-key-function-types-preview-form',
            itemId: 'devicetype-key-function-types-preview-form'
        };
        me.callParent(arguments);
    }

});