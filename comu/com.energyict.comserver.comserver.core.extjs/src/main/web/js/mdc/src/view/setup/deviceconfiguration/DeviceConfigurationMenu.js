Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'device-configuration-menu',

    deviceTypeId: null,
    deviceConfigurationId: null,

    title: Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.overview', 'MDC', 'Overview'),
                itemId: 'deviceConfigurationOverviewLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.registerTypes', 'MDC', 'Register configurations'),
                itemId: 'registerConfigsLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/registerconfigurations'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.loadProfiles', 'MDC', 'Load profiles'),
                itemId: 'loadProfilesLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofiles'
            },
            {
                text: 'Logbook configurations',
                itemId: 'logbooksLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/logbookconfigurations'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.connectionMethods', 'MDC', 'Connection methods'),
                itemId: 'connectionMethodsLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/connectionmethods'
            },
            {
                text: 'Security settings',
                itemId: 'securitySettingsLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securitysettings'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.protocols', 'MDC', 'Protocol dialects'),
                itemId: 'protocolLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/protocols'
            },
            {
                text: 'Communication tasks',
                itemId: 'communicationTasksLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId+ '/comtaskenablements'
            },
            {
                text: 'Validation rule sets',
                itemId: 'validationRuleSetsLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration']),
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/validationrulesets'
            },
            {
                text: 'Commands',
                itemId: 'messagesLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/messages'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.generalAttributes', 'MDC', 'General attributes'),
                itemId: 'generalAttributesLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/generalattributes'
            }
        ];

        me.callParent(arguments);
    }
});
