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
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.registerTypes', 'MDC', 'Register configurations'),
                pressed: false,
                itemId: 'registerConfigsLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/registerconfigurations',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.loadProfiles', 'MDC', 'Load profiles'),
                pressed: false,
                itemId: 'loadProfilesLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles',
                hrefTarget: '_self'
            },
            {
                text: 'Logbook configuration',
                pressed: false,
                itemId: 'logbooksLink',
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
                text: 'Validation rules',
                pressed: false,
                itemId: 'validationRulesLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/validationrules',
                hrefTarget: '_self'
            }
        );

        this.toggleMenuItem(this.toggle);
    }
});
