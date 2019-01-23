/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Mdc.dynamicprivileges.DeviceState',
        'Mdc.dynamicprivileges.Stores',
        'Uni.property.controller.Registry',
        'Mdc.property.UsagePoint',
        'Mdc.dynamicprivileges.DeviceTypeCapability',
        'Mdc.privileges.RegisteredDevicesKpi',
        'Mdc.privileges.CrlRequest',
        'Apr.controller.TaskManagement',
        'Apr.controller.TaskManagementGeneralTask',
        'Mdc.zones.controller.Zones'
    ],

    controllers: [
        'Mdc.controller.history.Setup',
        'Mdc.controller.setup.AddLogbookConfigurations',
        'Mdc.controller.setup.AddLogbookTypes',
        'Mdc.controller.setup.CommunicationSchedules',
        'Mdc.controller.setup.CommunicationTasks',
        'Mdc.controller.setup.ComPortPoolComPortsView',
        'Mdc.controller.setup.ComPortPoolEdit',
        'Mdc.controller.setup.ComPortPoolOverview',
        'Mdc.controller.setup.ComPortPools',
        'Mdc.controller.setup.ComServerComPortsEdit',
        'Mdc.controller.setup.ComServerComPortsView',
        'Mdc.controller.setup.ComServerEdit',
        'Mdc.controller.setup.ComServerOverview',
        'Mdc.controller.setup.ComServersView',
        'Mdc.controller.setup.Comtasks',
        'Mdc.controller.setup.ConnectionMethods',
        'Mdc.controller.setup.DeviceCommunicationProtocols',
        'Mdc.controller.setup.DeviceCommunicationPlanning',
        'Mdc.controller.setup.DeviceCommunicationTasks',
        'Mdc.controller.setup.DeviceConfigurationLogbooks',
        'Mdc.controller.setup.DeviceConfigurations',
        'Mdc.controller.setup.DeviceConnectionMethods',
        'Mdc.controller.setup.DeviceConnectionHistory',
        'Mdc.controller.setup.DeviceDataValidation',
        'Mdc.controller.setup.DeviceGroups',
        'Mdc.controller.setup.DeviceChannelData',
        'Mdc.controller.setup.DeviceChannels',
        'Mdc.controller.setup.DeviceLoadProfileData',
        'Mdc.controller.setup.DeviceLoadProfileOverview',
        'Mdc.controller.setup.DeviceLoadProfiles',
        'Mdc.controller.setup.DeviceEvents',
        'Mdc.controller.setup.DeviceLogbookOverview',
        'Mdc.controller.setup.DeviceLogbooks',
        'Mdc.controller.setup.DeviceProtocolDialects',
        'Mdc.controller.setup.DeviceRegisterConfiguration',
        'Mdc.controller.setup.DeviceRegisterData',
        'Mdc.controller.setup.DeviceRegisterHistoryData',
        'Mdc.controller.setup.DeviceRegisterDataEdit',
        'Mdc.controller.setup.Devices',
        'Mdc.controller.setup.DeviceSecuritySettings',
        'Mdc.controller.setup.DeviceTopology',
        'Mdc.controller.setup.DeviceTypeLogbooks',
        'Mdc.controller.setup.DeviceTypes',
        'Mdc.controller.setup.GeneralAttributes',
        'Mdc.controller.setup.EditLogbookConfiguration',
        'Mdc.controller.setup.LicensedProtocol',
        'Mdc.controller.setup.LoadProfileConfigurationDetails',
        'Mdc.controller.setup.LoadProfileConfigurations',
        'Mdc.controller.setup.LoadProfileTypes',
        'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
        'Mdc.controller.setup.LogbookTypes',
        'Mdc.controller.setup.Properties',
        'Mdc.controller.setup.PropertiesView',
        'Mdc.controller.setup.ProtocolDialects',
        'Mdc.controller.setup.RegisterConfigs',
        'Mdc.controller.setup.RegisterGroups',
        'Mdc.controller.setup.RegisterMappings',
        'Mdc.controller.setup.RegisterTypes',
        'Mdc.controller.setup.RuleDeviceConfigurations',
        'Mdc.controller.setup.SearchItemsBulkAction',
        'Mdc.controller.setup.SecuritySettings',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ValidationRuleSets',
        'Mdc.controller.setup.DeviceCommands',
        'Mdc.controller.setup.Messages',
        'Mdc.controller.setup.DeviceCommunicationTaskHistory',
        'Mdc.controller.setup.DeviceConnectionHistory',
        'Mdc.controller.setup.DeviceGeneralAttributes',
        'Mdc.controller.setup.AddDeviceGroupAction',
        'Mdc.controller.setup.DeviceRegisterTab',
        'Mdc.controller.setup.DeviceLogBookTab',
        'Mdc.controller.setup.DeviceLoadProfileTab',
        'Mdc.controller.setup.DeviceLogbookData',
        'Mdc.controller.setup.DataCollectionKpi',
        'Mdc.controller.setup.DeviceValidationResults',
        'Mdc.deviceconfigurationestimationrules.controller.RuleSets',
        'Mdc.deviceconfigurationestimationrules.controller.AddRuleSets',
        'Mdc.controller.setup.EstimationDeviceConfigurations',
        'Mdc.controller.setup.DeviceDataEstimation',
        'Mdc.controller.setup.DeviceValidationResults',
        'Mdc.controller.setup.DeviceHistory',
        'Mdc.controller.setup.ChangeDeviceLifeCycle',
        'Mdc.controller.setup.DeviceTransitionExecute',
        'Mdc.controller.setup.DeviceAttributes',
        'Mdc.controller.setup.DataLoggerSlaves',
        'Mdc.usagepointmanagement.controller.UsagePoint',
        'Mdc.usagepointmanagement.controller.ViewChannelsList',
        'Mdc.usagepointmanagement.controller.ViewRegistersList',
        'Mdc.controller.setup.DeviceConflictingMapping',
        'Mdc.devicetypecustomattributes.controller.AttributeSets',
        'Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersions',
        'Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnDevice',
        'Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnRegister',
        'Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
        'Mdc.controller.setup.MonitorProcesses',
        'Mdc.controller.Search',
        'Mdc.timeofuse.controller.TimeOfUse',
        'Mdc.controller.setup.ServiceCalls',
        'Mdc.timeofuseondevice.controller.TimeOfUse',
        'Mdc.filemanagement.controller.FileManagement',
        'Mdc.metrologyconfiguration.controller.ListView',
        'Mdc.metrologyconfiguration.controller.AddView',
        'Mdc.usagepointmanagement.controller.UsagePointHistory',
        'Mdc.usagepointmanagement.controller.ViewChannelDataAndReadingQualities',
        'Mdc.usagepointmanagement.controller.ViewRegisterDataAndReadingQualities',
        'Mdc.controller.setup.CommandLimitationRules',
        'Mdc.securityaccessors.controller.SecurityAccessors',
        'Mdc.securityaccessors.controller.DeviceSecurityAccessors',
        'Mdc.controller.setup.DeviceRegisterValidation',
        'Mdc.commands.controller.Commands',
        'Mdc.controller.setup.IssueAlarmDetail',
        'Mdc.networkvisualiser.controller.NetworkVisualiser',
        'Mdc.registereddevices.controller.RegisteredDevices',
        'Mdc.controller.setup.TaskManagementDataCollectionKpi',
        'Mdc.controller.setup.TaskManagementRegisteredDevices',
        'Apr.controller.CustomTask',
        'Mdc.crlrequest.controller.TaskManagementCrlRequest',
        'Apr.controller.CustomTask',
        'Mdc.controller.setup.TaskManagement',
        'Mdc.zones.controller.Zones',
        'Mdc.controller.setup.DeviceZones',
        'Mdc.processes.controller.ProcessesController',
        'Mdc.processes.controller.ProcBulkActions'
    ],

    stores: [
        'Mdc.store.TimeUnits',
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.store.LoadProfilesOfDevice',
        'Mdc.store.DeviceStatePrivileges',
        'Mdc.store.DeviceCommandPrivileges',
        'Mdc.store.DeviceTypeCapabilities',
        'Mdc.store.Zones'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this;
            //historian = me.getController('Mdc.controller.history.Setup'); // Forces route registration.

        me.getController('Apr.controller.CustomTask');
        me.getController('Mdc.controller.setup.TaskManagementDataCollectionKpi');
        me.getController('Mdc.controller.setup.TaskManagementRegisteredDevices');
        me.getController('Mdc.crlrequest.controller.TaskManagementCrlRequest');
        Uni.property.controller.Registry.addProperty('USAGEPOINT', 'Mdc.property.UsagePoint');
        if (Mdc.privileges.Device.canViewDevices()) {
            var devicesMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.devices', 'MDC', 'Devices'),
                href: '#/devices',
                glyph: 'devices',
                portal: 'devices',
                index: 20
            });

            Uni.store.MenuItems.add(devicesMenuItem);
        }

        if (Mdc.privileges.DeviceGroup.canView()) {
            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                portal: 'devices',
                route: 'devices',
                items: [
                    {
                        text: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device groups'),
                        href: '#/devices/devicegroups',
                        route: 'devicegroups'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                portalItem
            );
        }

        if (Mdc.privileges.DeviceType.canView() || Mdc.privileges.MasterData.canView() ||
            Mdc.privileges.Communication.canView() || Mdc.privileges.CommunicationSchedule.canView()) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
                href: me.getApplication().getController('Mdc.controller.history.Setup').tokenizeShowOverview(),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });

            Uni.store.MenuItems.add(menuItem);

            var deviceManagementItem = null;
            if (Mdc.privileges.DeviceType.canView() || Mdc.privileges.MasterData.canView()) {
                deviceManagementItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.deviceManagement', 'MDC', 'Device management'),
                    portal: 'administration',
                    route: 'devicemanagement',
                    items: [
                        {
                            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
                            href: '#/administration/devicetypes',
                            itemId: 'lnk-device-types',
                            privileges: Mdc.privileges.DeviceType.view,
                            route: 'devicetypes'
                        },
                        {
                            text: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                            href: '#/administration/registertypes',
                            itemId: 'lnk-register-types',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'registertypes'
                        },
                        {
                            text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
                            href: '#/administration/registergroups',
                            itemId: 'lnk-register-groups',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'registergroups'
                        },
                        {
                            text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                            href: '#/administration/logbooktypes',
                            itemId: 'lnk-logbook-types',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'logbooktypes'
                        },
                        {
                            text: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                            href: '#/administration/loadprofiletypes',
                            itemId: 'lnk-load-profile-types',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'loadprofiletypes'
                        }
                    ]
                });
            }

            var deviceCommunicationItem = null;
            if (Mdc.privileges.Communication.canView() || Mdc.privileges.CommunicationSchedule.canView() || Mdc.privileges.DataCollectionKpi.canView()) {
                deviceCommunicationItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.deviceCommunication', 'MDC', 'Device communication'),
                    portal: 'administration',
                    route: 'devicecommunication',
                    items: [
                        {
                            text: Uni.I18n.translate('general.comServers', 'MDC', 'Communication servers'),
                            href: '#/administration/comservers',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'comservers'
                        },
                        {
                            text: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                            href: '#/administration/comportpools',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'comportpools'
                        },
                        {
                            text: Uni.I18n.translate('general.deviceComProtocols', 'MDC', 'Communication protocols'),
                            href: '#/administration/devicecommunicationprotocols',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'devicecommunicationprotocols'
                        },
                        {
                            text: Uni.I18n.translate('general.comSchedules', 'MDC', 'Shared communication schedules'),
                            privileges: Mdc.privileges.CommunicationSchedule.view,
                            href: '#/administration/communicationschedules',
                            route: 'communicationschedules'
                        },
                        {
                            text: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                            href: '#/administration/communicationtasks',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'communicationtasks'
                        },
                        {
                            text: Uni.I18n.translate('general.commandLimitationRules', 'MDC', 'Command limitation rules'),
                            href: '#/administration/commandrules',
                            privileges: Mdc.privileges.CommandLimitationRules.view,
                            route: 'commandrules'
                        }
                    ]
                });

                Uni.store.PortalItems.add(
                    Ext.create('Uni.model.PortalItem', {
                        title: Uni.I18n.translate('general.KPIs', 'MDC', 'KPIs'),
                        portal: 'administration',
                        items: [
                            {
                                text: Uni.I18n.translate('general.dataCollectionKpis', 'MDC', 'Data collection KPIs'),
                                href: '#/administration/datacollectionkpis',
                                privileges: Mdc.privileges.DataCollectionKpi.view,
                                route: 'datacollectionkpis'
                            }
                        ]
                    })
                );

            }
            if (deviceCommunicationItem !== null) {
                Uni.store.PortalItems.add(deviceCommunicationItem);
            }
            if (deviceManagementItem !== null) {
                Uni.store.PortalItems.add(deviceManagementItem);
            }

            if (Mdc.privileges.Device.canAddDevice()) {
                var portalItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.device.lifecycle.management', 'MDC', 'Device life cycle management'),
                    portal: 'devices',
                    route: 'devices',
                    items: [
                        {
                            text: Uni.I18n.translate('deviceAdd.title', 'MDC', 'Add device'),
                            itemId: 'lnk-add-device',
                            href: '#/devices/add',
                            route: 'add'
                        }
                    ]
                });

                Uni.store.PortalItems.add(portalItem);
            }
        }

        if (Mdc.privileges.MetrologyConfiguration.full()) {
            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.usagePointManagement', 'MDC', 'Usage point management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.metrologyConfigurations', 'MDC', 'Metrology configurations'),
                        itemId: 'lnk-metrology-configurations',
                        href: '#/administration/metrologyconfiguration',
                        route: 'add'
                    }
                ]
            }));
        }

        if(Mdc.privileges.UsagePoint.canAdmin()){
            var usagePointsMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.usagePoints', 'MDC', 'Usage points'),
                href: '#/usagepoints',
                glyph: 'usagepoints',
                portal: 'usagepoints',
                index: 20
            });
            Uni.store.MenuItems.add(usagePointsMenuItem);

            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.usagePointManagement', 'MDC', 'Usage point management'),
                portal: 'usagepoints',
                route: 'usagepoints',
                items: [
                    {
                        text: Uni.I18n.translate('usagePointAdd.title', 'MDC', 'Add usage point'),
                        itemId: 'lnk-add-usagepoints',
                        href: '#/usagepoints/add',
                        route: 'add'
                    }
                ]
            });
            Uni.store.PortalItems.add(portalItem);
        }

        if (Mdc.privileges.Device.canViewCommands()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'MDC', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

            Uni.store.PortalItems.add(
                Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.dataCommunication', 'MDC', 'Data communication'),
                    portal: 'workspace',
                    route: 'commands',
                    items: [
                        {
                            text: Uni.I18n.translate('title.commands', 'MDC', 'Commands'),
                            itemId: 'mdc-workspace-commands-link',
                            href: '#/workspace/commands'
                        }
                    ]
                })
            );
        }

        if (Uni.Auth.checkPrivileges(Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication)) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'MDC', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

            Uni.store.PortalItems.add(
                Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.dataCommunication', 'MDC', 'Data communication'),
                    portal: 'workspace',
                    route: 'regdevices',
                    items: [
                        {
                            text: Uni.I18n.translate('title.registeredDevices', 'MDC', 'Registered devices'),
                            itemId: 'mdc-workspace-registered-devices-link',
                            href: '#/workspace/regdevices'
                        }
                    ]
                })
            );
        }

        if (Mdc.privileges.RegisteredDevicesKpi.canView()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            }));

            Uni.store.PortalItems.add(
                Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.KPIs', 'MDC', 'KPIs'),
                    portal: 'administration',
                    route: 'regdeviceskpis',
                    items: [
                        {
                            text: Uni.I18n.translate('title.registeredDevicesKPIs', 'MDC', 'Registered devices KPIs'),
                            itemId: 'mdc-workspace-registered-devices-link',
                            privileges: Mdc.privileges.RegisteredDevicesKpi.view,
                            href: '#/administration/regdeviceskpis'
                        }
                    ]
                })
            );
        }

        if (Mdc.privileges.SecurityAccessor.canView()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            }));

            Uni.store.PortalItems.add(Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.security', 'MDC', 'Security'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.securityAccessors', 'MDC', 'Security accessors'),
                        itemId: 'lnk-metrology-configurations',
                        href: '#/administration/securityaccessors',
                        route: 'securityaccessors'
                    }
                ]
            }));
        }

        me.addTaskManagement();

        if (Bpm.privileges.BpmManagement.canViewProcesses()){
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                        text: Uni.I18n.translate('general.workspace', 'MDC', 'Workspace'),
                        glyph: 'workspace',
                        portal: 'workspace',
                        index: 30
                    }));
            Uni.store.PortalItems.add(
                Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.allprocesses', 'MDC', 'Processes'),
                portal: 'workspace',
                route: 'multisenseprocesses',
                items: [
                    {
                        text: Uni.I18n.translate('general.allprocesses', 'MDC', 'Processes'),
                        itemId: 'mdc-workspace-all-processes',
                        privileges: Bpm.privileges.BpmManagement.viewProcesses,
                        href: '#/workspace/multisenseprocesses',
                        route: 'multisenseprocesses'
                    }
                ]
                })
            );
        }

        if (Cfg.privileges.Validation.canViewZones()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
                glyph: 'settings',
                portal: 'administration',
                index: 10
            }));

            Uni.store.PortalItems.add(
                Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.zoneManagement', 'MDC', 'Zone management'),
                    portal: 'administration',
                    route: 'zones',
                    items: [
                        {
                            text: Uni.I18n.translate('title.zones', 'MDC', 'Zones'),
                            itemId: 'mdc-administration-zones-link',
                            href: '#/administration/zones'
                        }
                    ]
                })
            );
        }
    },

    addTaskManagement: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskManagement = null;

        if (Mdc.privileges.TaskManagement.canView()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            }));

            taskManagement = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.taskManagement', 'MDC', 'Task management'),
                portal: 'administration',
                route: 'taskmanagement',
                items: [
                    {
                        text: Uni.I18n.translate('general.taskmanagement.tasks', 'MDC', 'Tasks'),
                        href: router.getRoute('administration/taskmanagement').buildUrl({}, {application: Uni.util.Application.getAppName()}),
                        route: 'taskmanagement',
                        itemId: 'taskmanagement'
                    }
                ]
            });
            Uni.store.PortalItems.add(taskManagement);
        }
    }

});
