Ext.define('Mdc.dynamicprivileges.DeviceTypeCapability', {
    singleton: true,

    requires: [
        'Uni.DynamicPrivileges'
    ],

    supportsFileManagement: 'devicetype.supports.filemanagement'
});
