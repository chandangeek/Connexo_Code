Ext.define('Mdc.view.setup.device.DeviceMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'deviceMenu',

    // TODO See what impact removing the 'toggleById' function has.
//    toggleId: null,
    device: null,

    title: Uni.I18n.translate('devicemenu.title', 'MDC', 'Device'),

    initComponent: function () {
        var me = this,
            mRID = me.device.get('mRID');

        me.menuItems = [
            {
                text: mRID,
                itemId: 'deviceOverviewLink',
                href: '#/devices/' + mRID
            },
            {
                title: 'Data sources',
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                        itemId: 'loadProfilesLink',
                        href: '#/devices/' + mRID + '/loadprofiles',
                        showCondition: me.device.get('hasLoadProfiles')
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.channels', 'MDC', 'Channels'),
                        privileges:Mdc.privileges.Device.viewDevice,
                        itemId: 'channelsLink',
                        href: '#/devices/' + mRID + '/channels',
                        showCondition: me.device.get('hasLoadProfiles')
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.logbooks', 'MDC', 'Logbooks'),
                        itemId: 'logbooksLink',
                        href: '#/devices/' + mRID + '/logbooks',
                        showCondition: me.device.get('hasLogBooks')
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.events', 'MDC', 'Events'),
                        itemId: 'events',
                        href: '#/devices/' + mRID + '/events',
                        showCondition: me.device.get('hasLogBooks')
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.registerTypes', 'MDC', 'Registers'),
                        itemId: 'registersLink',
                        href: '#/devices/' + mRID + '/registers',
                        showCondition: me.device.get('hasRegisters')
                    }
                ]
            },
            {
                title: 'Communication',
                items: [
                    {
                        text: Uni.I18n.translate('deviceconfigurationmenu.generalAttributes', 'MDC', 'General attributes'),
                        itemId: 'deviceGeneralAttributesLink',
                        href: '#/devices/' + mRID + '/generalattributes'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.communicationPlanning', 'MDC', 'Communication planning'),
                        itemId: 'communicationSchedulesLink',
                        href: '#/devices/' + mRID + '/communicationplanning'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.communicationTasks', 'MDC', 'Communication tasks'),
                        itemId: 'communicationTasksLink',
                        href: '#/devices/' + mRID + '/communicationtasks',
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.connectionMethods', 'MDC', 'Connection methods'),
                        itemId: 'connectionMethodsLink',
                        href: '#/devices/' + mRID + '/connectionmethods'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.security', 'MDC', 'Security settings'),
                        itemId: 'securitySettingLink',
                        href: '#/devices/' + mRID + '/securitysettings'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocol dialects'),
                        itemId: 'protocolLink',
                        href: '#/devices/' + mRID + '/protocols'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.commands', 'MDC', 'Commands'),
                        itemId: 'deviceCommands',
                        href: '#/devices/' + mRID + '/commands'
                    },
                    {
                        text: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
                        itemId: 'topologyLink',
                        href: '#/devices/' + mRID + '/topology',
                        showCondition: me.device.get('gatewayType') === 'LAN'
                            || me.device.get('gatewayType') === 'HAN'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('device.readingQuality', 'MDC', 'Reading quality'),
                items: [
                    {
                        text: Uni.I18n.translate('devicemenu.dataValidation', 'MDC', 'Validation configuration'),
                        itemId: 'dataValidationLink',
                        privileges: Cfg.privileges.Validation.fineTuneValidation,
                        href: '#/devices/' + mRID + '/datavalidation',
                        showCondition: me.device.get('hasLogBooks')
                        || me.device.get('hasLoadProfiles')
                        || me.device.get('hasRegisters')
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.validationResults', 'MDC', 'Validation results'),
                        itemId: 'validationResultsLink',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration']),
                        href: '#/devices/' + mRID + '/validationresults/data',
                        showCondition: me.device.get('hasLogBooks')
                        || me.device.get('hasLoadProfiles')
                        || me.device.get('hasRegisters')
                    },
                    {
                        text: Uni.I18n.translate('general.dataEstimation', 'MDC', 'Data estimation'),
                        itemId: 'dataEstimationLink',
                        href: '#/devices/' + mRID + '/dataestimation',
                        showCondition: me.device.get('hasLogBooks')
                        || me.device.get('hasLoadProfiles')
                        || me.device.get('hasRegisters'),
                        privileges: Mdc.privileges.DeviceConfigurationEstimations.view
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    toggleByItemId: function (toggleId) {
        // TODO See what impact removing this function has.

//        var cls = this.selectedCls,
//            item = this.down('#' + toggleId);
//
//        if (item.hasCls(cls)) {
//            item.removeCls(cls);
//        } else {
//            item.addCls(cls);
//        }
    }

});