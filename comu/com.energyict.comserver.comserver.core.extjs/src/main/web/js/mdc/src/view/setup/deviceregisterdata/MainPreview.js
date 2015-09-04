Ext.define('Mdc.view.setup.deviceregisterdata.MainPreview', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Mdc.view.setup.deviceregisterdata.ActionMenu',
        'Uni.form.field.EditedDisplay'
    ],

    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Mdc.privileges.Device.administrateDeviceData,
            iconCls: 'x-uni-action-iconD',
            itemId: 'gridPreviewActionMenu',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
            menu: {
                xtype: 'deviceregisterdataactionmenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
    }
});


