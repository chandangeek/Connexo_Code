Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    xtype: 'device-configuration-menu',

    deviceTypeId: null,
    deviceConfigurationId: null,
    toggle: null,

    initComponent: function () {
        this.callParent(arguments);

        this.add(
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.overview', 'MDC', 'Overview'),
                pressed: false,
                itemId: 'deviceConfigurationOverviewLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceConfiguration', 'privilege.view.deviceConfiguration']),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.registerTypes', 'MDC', 'Register configurations'),
                pressed: false,
                itemId: 'registerConfigsLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceConfiguration', 'privilege.view.deviceConfiguration']),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/registerconfigurations',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.loadProfiles', 'MDC', 'Load profiles'),
                pressed: false,
                itemId: 'loadProfilesLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceConfiguration', 'privilege.view.deviceConfiguration']),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles',
                hrefTarget: '_self'
            },
            {
                text: 'Logbook configuration',
                pressed: false,
                itemId: 'logbooksLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceConfiguration', 'privilege.view.deviceConfiguration']),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/logbookconfigurations',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.connectionMethods', 'MDC', 'Connection methods'),
                pressed: false,
                itemId: 'connectionMethodsLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/connectionmethods',
                hrefTarget: '_self'
            },
            {
                text: 'Security settings',
                pressed: false,
                itemId: 'securitySettingsLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.protocols', 'MDC', 'Protocol dialects'),
                pressed: false,
                itemId: 'protocolLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.protocol','privilege.view.protocol']),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/protocols',
                hrefTarget: '_self'
            },
            {
                text: 'Communication tasks',
                pressed: false,
                itemId: 'communicationTasksLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/comtaskenablements',
                hrefTarget: '_self'
            },
            {
                text: 'Validation rule sets',
                pressed: false,
                itemId: 'validationRuleSetsLink',
                hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration']),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/validationrulesets',
                hrefTarget: '_self'
            },
            {
                text: 'Commands',
                pressed: false,
                itemId: 'messagesLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/messages',
                hrefTarget: '_self'
            }
        );

        this.toggleMenuItem(this.toggle);
    }
});
