Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'device-configuration-menu',

    deviceTypeId: null,
    deviceConfigurationId: null,

    title: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                itemId: 'deviceConfigurationOverviewLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId
            },
            {
                title: Uni.I18n.translate('deviceconfiguration.dataSources', 'MDC', 'Data sources'),
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('loadProfileConfigurations.title', 'MDC', 'Load profile configurations'),
                        itemId: 'loadProfilesLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofiles'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.logbookConfigurations', 'MDC', 'Logbook configurations'),
                        itemId: 'logbooksLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/logbookconfigurations'
                    },
                    {
                        text: Uni.I18n.translate('deviceconfigurationmenu.registerTypes', 'MDC', 'Register configurations'),
                        itemId: 'registerConfigsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/registerconfigurations'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicemenu.communication', 'MDC', 'Communication'),
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                        itemId: 'generalAttributesLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/generalattributes'
                    },
                    {
                        text: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                        itemId: 'communicationTasksLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId+ '/comtaskenablements'
                    },
                    {
                        text: Uni.I18n.translate('general.connectionMethods', 'MDC', 'Connection methods'),
                        itemId: 'connectionMethodsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/connectionmethods'
                    },
                    {
                        text: Uni.I18n.translate('devicesecuritysetting.securitySettings', 'MDC', 'Security settings'),
                        itemId: 'securitySettingsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securitysettings'
                    },
                    {
                        text: Uni.I18n.translate('deviceconfigurationmenu.protocols', 'MDC', 'Protocol dialects'),
                        itemId: 'protocolLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/protocols'
                    },
                    {
                        text: Uni.I18n.translate('comtask.messages', 'MDC', 'Commands'),
                        itemId: 'messagesLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/messages'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicemenu.readingQuality', 'MDC', 'Reading quality'),
                privileges: Cfg.privileges.Validation.fineTuneOnDeviceConfiguration,
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('device.dataValidation.rulesSetGrid.title', 'MDC', 'Validation rule sets'),
                        itemId: 'validationRuleSetsLink',
                        privileges: Cfg.privileges.Validation.fineTuneOnDeviceConfiguration,
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/validationrulesets'
                    },
                    {
                        text: Uni.I18n.translate('general.estimationRuleSets', 'MDC', 'Estimation rule sets'),
                        itemId: 'estimationRuleSetsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/estimationrulesets',
                        privileges : Mdc.privileges.DeviceConfigurationEstimations.view
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
