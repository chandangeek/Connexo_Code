Ext.define('Mdc.keyfunctiontypes.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-key-function-types-setup',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.keyfunctiontypes.view.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    deviceTypeId: null,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: me.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.keyFunctionTypes', 'MDC', 'Key function types'),
            items: [
                {
                    xtype: 'key-function-types-preview-container',
                    itemId: 'key-function-types-preview-container',
                    deviceTypeId: me.deviceTypeId
                }
            ]
        };

        me.callParent(arguments);
    }
});