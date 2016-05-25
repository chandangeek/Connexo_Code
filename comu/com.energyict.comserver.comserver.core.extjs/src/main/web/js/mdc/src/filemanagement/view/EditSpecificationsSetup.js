Ext.define('Mdc.fileManagement.view.EditSpecificationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.files-devicetype-edit-specs-setup',
    overflowY: true,
    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.filemanagement.view.EditSpecificationsForm'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('timeofuse.allowFileManagementQuestion', 'MDC', 'Allow file management?'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'tou-devicetype-edit-specs-form',
                        deviceTypeId: me.deviceTypeId
                    }
                ]
            }
        ];


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
        me.callParent(arguments);
    }
});
