Ext.define('Mdc.view.setup.deviceloadprofiles.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceLoadProfilesActionMenu',
    itemId: 'deviceLoadProfilesActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'viewSuspects',
                text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
                action: 'viewSuspects',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'validateNowLoadProfile',
                text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
                privileges: Cfg.privileges.Validation.validateManual,
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions,
                action: 'validateNow',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'editLoadProfile',
                text: Uni.I18n.translate('general.changeNextReadingBlockStart', 'MDC', 'Change next reading block start'),
                action: 'editLoadProfile',
                privileges: Mdc.privileges.Device.administrateDeviceData,
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});
