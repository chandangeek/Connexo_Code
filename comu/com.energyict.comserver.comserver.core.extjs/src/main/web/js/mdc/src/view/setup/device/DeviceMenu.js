Ext.define('Mdc.view.setup.device.DeviceMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'deviceMenu',

    // TODO See what impact removing the 'toggleById' function has.
//    toggleId: null,
    device: null,

    title: Uni.I18n.translate('devicemenu.title', 'MDC', 'Device'),

    initComponent: function () {
        var me = this,
            mRID = me.device.get('mRID'),
            menu = {
                xtype: 'menu',
                items: [
                    {
                        text: mRID,
                        itemId: 'deviceOverviewLink',
                        href: '#/devices/' + encodeURIComponent(mRID)
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.deviceAttributes', 'MDC', 'Device attributes'),
                        privileges: Mdc.privileges.Device.viewOrAdministrateDeviceData,
                        itemId: 'device-attributes-link',
                        href: '#/devices/' + encodeURIComponent(mRID) + '/attributes'
                    },
                    {
                        text: Uni.I18n.translate('general.history', 'MDC', 'History'),
                        itemId: 'device-history-link',
                        href: '#/devices/' + encodeURIComponent(mRID) + '/history'
                    }
                ]
            };

        if ( !Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger') ) {
            menu.items.push(
                {
                    text: Uni.I18n.translate('devicemenu.dataLoggerSlaves', 'MDC', 'Data logger slaves'),
                    itemId: 'device-dataLoggerSlaves-link',
                    href: '#/devices/' + encodeURIComponent(mRID) + '/dataloggerslaves',
                    privileges: Mdc.privileges.Device.viewDevice
                }
            );
        }

        menu.items.push(
            {
                text: Uni.I18n.translate('devicemenu.processes', 'MDC', 'Processes'),
                privileges: Mdc.privileges.Device.deviceProcesses,
                itemId: 'device-processes-link',
                href: '#/devices/' + encodeURIComponent(mRID) + '/processes'
            },
            {
                text: Uni.I18n.translate('devicemenu.serviceCalls', 'MDC', 'Service calls'),
                privileges: Mdc.privileges.Device.viewDevice,
                itemId: 'device-servicecalls-link',
                href: '#/devices/' + encodeURIComponent(mRID) + '/servicecalls'
            }
        );

        me.menuItems = [];
        me.menuItems.push(menu);

        if (me.device.get('hasLoadProfiles') || me.device.get('hasRegisters') || me.device.get('hasLogBooks')) {
            me.menuItems.push(
                {
                    title: Uni.I18n.translate('device.dataSources', 'MDC', 'Data sources'),
                    xtype: 'menu',
                    items: [
                        {
                            text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                            itemId: 'loadProfilesLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/loadprofiles',
                            showCondition: me.device.get('hasLoadProfiles')
                        },
                        {
                            text: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                            privileges: Mdc.privileges.Device.viewDevice,
                            itemId: 'channelsLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/channels',
                            showCondition: me.device.get('hasLoadProfiles')
                        },
                        {
                            text: Uni.I18n.translate('general.logbooks', 'MDC', 'Logbooks'),
                            itemId: 'logbooksLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/logbooks',
                            showCondition: me.device.get('hasLogBooks')
                        },
                        {
                            text: Uni.I18n.translate('general.events', 'MDC', 'Events'),
                            itemId: 'events',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/events',
                            showCondition: me.device.get('hasLogBooks')
                        },
                        {
                            text: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                            itemId: 'registersLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/registers',
                            showCondition: me.device.get('hasRegisters')
                        }
                    ]
                }
            );
        }

        if (me.device.get('hasLoadProfiles') || me.device.get('hasRegisters')) {
            me.menuItems.push(
                {
                    title: Uni.I18n.translate('device.readingQuality', 'MDC', 'Reading quality'),
                    items: [
                        {
                            text: Uni.I18n.translate('devicemenu.dataValidation', 'MDC', 'Validation configuration'),
                            itemId: 'dataValidationLink',
                            privileges: Cfg.privileges.Validation.fineTuneValidation,
                            href: '#/devices/' + encodeURIComponent(mRID) + '/datavalidation'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.validationResults', 'MDC', 'Validation results'),
                            itemId: 'validationResultsLink',
                            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration']),
                            href: '#/devices/' + encodeURIComponent(mRID) + '/validationresults/data'
                        },
                        {
                            text: Uni.I18n.translate('general.dataEstimation', 'MDC', 'Data estimation'),
                            itemId: 'dataEstimationLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/dataestimation',
                            privileges: Mdc.privileges.DeviceConfigurationEstimations.view
                        }
                    ]
                }
            );
        }

        if (!me.device.get('isDataLoggerSlave')) {
            me.menuItems.push(
                {
                    title: Uni.I18n.translate('device.communication', 'MDC', 'Communication'),
                    items: [
                        {
                            text: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                            itemId: 'deviceGeneralAttributesLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/generalattributes'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.communicationPlanning', 'MDC', 'Communication planning'),
                            itemId: 'communicationSchedulesLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/communicationplanning',
                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationPlanningPages
                        },
                        {
                            text: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                            itemId: 'communicationTasksLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/communicationtasks'
                        },
                        {
                            text: Uni.I18n.translate('general.connectionMethods', 'MDC', 'Connection methods'),
                            itemId: 'connectionMethodsLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/connectionmethods'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.security', 'MDC', 'Security settings'),
                            itemId: 'securitySettingLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/securitysettings'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocol dialects'),
                            itemId: 'protocolLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/protocols'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.commands', 'MDC', 'Commands'),
                            itemId: 'deviceCommands',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/commands'
                        },
                        {
                            text: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
                            itemId: 'topologyLink',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/topology',
                            showCondition: me.device.get('gatewayType') === 'LAN' || me.device.get('gatewayType') === 'HAN'
                        }
                    ]
                },
                {
                    title: Uni.I18n.translate('general.configuration', 'MDC', 'Configuration'),
                    items: [
                        {
                            text: Uni.I18n.translate('general.firmware', 'MDC', 'Firmware'),
                            itemId: 'device-firmware-link-menu',
                            href: '#/devices/' + encodeURIComponent(mRID) + '/firmware'
                        },
                        {
                            text: Uni.I18n.translate('general.timeOfUse', 'MDC', 'Time of use'),
                            itemId: 'deviceTimeOfUseLink',
                            privileges: Mdc.privileges.Device.viewDevice,
                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.timeOfUseAllowed,
                            href: '#/devices/' + encodeURIComponent(mRID) + '/timeofuse'
                        }
                    ]
                }
            );
        }

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