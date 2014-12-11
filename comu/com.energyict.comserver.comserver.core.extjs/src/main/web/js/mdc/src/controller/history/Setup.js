Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        administration: {
            title: 'Administration',
            route: 'administration',
            disabled: true,
            items: {
                logbooktypes: {
                    title: 'Logbook types',
                    route: 'logbooktypes',
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showLogbookTypes',
                    items: {
                        create: {
                            title: 'Add logbook type',
                            route: 'add',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            controller: 'Mdc.controller.setup.LogbookTypes',
                            action: 'showLogbookTypeCreateView'
                        },
                        edit: {
                            title: 'Edit logbook type',
                            route: '{id}/edit',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            controller: 'Mdc.controller.setup.LogbookTypes',
                            action: 'showLogbookTypeEditView',
                            callback: function (route) {
                                this.getApplication().on('loadLogbookType', function (record) {
                                    route.setTitle('Edit ' + record.get('name') + '');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                devicetypes: {
                    title: 'Device types',
                    route: 'devicetypes',
                    privileges: ['privilege.administrate.deviceConfiguration', 'privilege.view.deviceConfiguration'],
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showDeviceTypes',
                    items: {
                        create: {
                            title: 'Add device type',
                            route: 'add',
                            privileges: [['privilege.administrate.deviceConfiguration'],['privilege.administrate.protocol','privilege.view.protocol']],
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeCreateView'
                        },
                        view: {
                            title: 'Overview',
                            route: '{deviceTypeId}',
                            privileges: ['privilege.administrate.deviceConfiguration', 'privilege.view.deviceConfiguration'],
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeDetailsView',
                            callback: function (route) {
                                this.getApplication().on('loadDeviceType', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                edit: {
                                    title: 'Edit',
                                    route: 'edit',
                                    privileges: [['privilege.administrate.deviceConfiguration'],['privilege.administrate.protocol','privilege.view.protocol']],
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    action: 'showDeviceTypeEditView'
                                },
                                logbooktypes: {
                                    title: 'Logbook types',
                                    route: 'logbooktypes',
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    action: 'showDeviceTypeLogbookTypesView',
                                    items: {
                                        add: {
                                            title: 'Add logbook types',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.DeviceTypes',
                                            action: 'showAddLogbookTypesView'
                                        }
                                    }
                                },
                                loadprofiles: {
                                    title: 'Load profiles',
                                    route: 'loadprofiles',
                                    controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                    action: 'showDeviceTypeLoadProfileTypesView',
                                    items: {
                                        add: {
                                            title: 'Add load profiles',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                            action: 'showDeviceTypeLoadProfileTypesAddView'
                                        }
                                    }
                                },
                                deviceconfigurations: {
                                    title: 'Device configurations',
                                    route: 'deviceconfigurations',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    action: 'showDeviceConfigurations',
                                    items: {
                                        create: {
                                            title: 'Add device configuration',
                                            route: 'create',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            action: 'showDeviceConfigurationCreateView'
                                        },
                                        view: {
                                            title: 'Device configuration',
                                            route: '{deviceConfigurationId}',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            action: 'showDeviceConfigurationDetailsView',
                                            callback: function (route) {
                                                this.getApplication().on('loadDeviceConfiguration', function (record) {
                                                    route.setTitle(record.get('name'));
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            },
                                            items: {
                                                edit: {
                                                    title: 'Edit device configuration',
                                                    route: 'edit',
                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                    action: 'showDeviceConfigurationEditView'
                                                },
                                                loadprofiles: {
                                                    title: 'Load profiles',
                                                    route: 'loadprofiles',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                    action: 'showDeviceConfigurationLoadProfilesView',
                                                    items: {
                                                        add: {
                                                            title: 'Add load profile',
                                                            route: 'add',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                            action: 'showDeviceConfigurationLoadProfilesAddView'
                                                        },
                                                        edit: {
                                                            title: 'Edit load profile',
                                                            route: '{loadProfileConfigurationId}/edit',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                            action: 'showDeviceConfigurationLoadProfilesEditView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadLoadProfile', function (record) {
                                                                    route.setTitle('Edit \'' + record.name + '\'');
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        },
                                                        channels: {
                                                            title: 'Load profile',
                                                            route: '{loadProfileConfigurationId}/channels',
                                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                            action: 'showDeviceConfigurationLoadProfilesConfigurationDetailsView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadLoadProfile', function (record) {
                                                                    route.setTitle(record.name);
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            },
                                                            items: {
                                                                add: {
                                                                    title: 'Add channel configuration',
                                                                    route: 'add',
                                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsAddView'
                                                                },
                                                                edit: {
                                                                    title: 'Edit channel configuration',
                                                                    route: '{channelId}/edit',
                                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsEditView'
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                logbookconfigurations: {
                                                    title: 'Logbook configurations',
                                                    route: 'logbookconfigurations',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                    action: 'showDeviceConfigurationLogbooksView',
                                                    items: {
                                                        add: {
                                                            title: 'Add logbook configuration',
                                                            route: 'add',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                            action: 'showAddDeviceConfigurationLogbooksView'
                                                        },
                                                        edit: {
                                                            title: 'Edit logbook configuration',
                                                            route: '{logbookConfigurationId}/edit',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                            action: 'showEditDeviceConfigurationLogbooksView'
                                                        }
                                                    }
                                                },
                                                //Register configuration routes
                                                registerconfigurations: {
                                                    title: 'Register configurations',
                                                    route: 'registerconfigurations',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    controller: 'Mdc.controller.setup.RegisterConfigs',
                                                    action: 'showRegisterConfigs',
                                                    items: {
                                                        create: {
                                                            title: 'Add register configuration',
                                                            route: 'create',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                                            action: 'showRegisterConfigurationCreateView'
                                                        },
                                                        edit: {
                                                            title: 'Edit register configuration',
                                                            route: '{registerConfigurationId}/edit',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                                            action: 'showRegisterConfigurationEditView'
                                                        }
                                                    }
                                                },
                                                //Security settings routes
                                                securitysettings: {
                                                    title: 'Security settings',
                                                    route: 'securitysettings',
                                                    privileges: ['privilege.administrate.deviceSecurity','privilege.view.deviceSecurity'],
                                                    controller: 'Mdc.controller.setup.SecuritySettings',
                                                    action: 'showSecuritySettings',
                                                    items: {
                                                        create: {
                                                            title: 'Add security setting',
                                                            route: 'create',
                                                            privileges: ['privilege.administrate.deviceSecurity'],
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            action: 'showSecuritySettingsCreateView'
                                                        },
                                                        edit: {
                                                            title: 'Edit security setting',
                                                            route: '{securitySettingId}/edit',
                                                            privileges: ['privilege.administrate.deviceSecurity'],
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            action: 'showSecuritySettingsEditView'
                                                        },
                                                        executionLevels: {
                                                            title: 'Add privileges',
                                                            route: '{securitySettingId}/privileges/add',
                                                            privileges: ['privilege.administrate.deviceSecurity'],
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            action: 'showAddExecutionLevelsView'
                                                        }
                                                    }
                                                },
                                                //Communication tasks routes
                                                comtaskenablements: {
                                                    title: 'Communication task configurations',
                                                    route: 'comtaskenablements',
                                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                                    controller: 'Mdc.controller.setup.CommunicationTasks',
                                                    action: 'showCommunicationTasks',
                                                    items: {
                                                        create: {
                                                            title: 'Add communication task configuration',
                                                            route: 'create',
                                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                                            action: 'showAddCommunicationTaskView'
                                                        },
                                                        edit: {
                                                            title: 'Edit communication task configuration',
                                                            route: '{comTaskEnablementId}/edit',
                                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                                            action: 'showEditCommunicationTaskView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadCommunicationTaskModel', function (record) {
                                                                    route.setTitle('Edit \'' + record.get('comTask').name + '\'');
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        }
                                                    }
                                                },
                                                //connection methods routes
                                                connectionmethods: {
                                                    title: 'Connection methods',
                                                    route: 'connectionmethods',
                                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                                    controller: 'Mdc.controller.setup.ConnectionMethods',
                                                    action: 'showConnectionMethods',
                                                    items: {
                                                        addoutbound: {
                                                            title: 'Add outbound connection method',
                                                            route: 'addoutbound',
                                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            action: 'showAddConnectionMethodView',
                                                            params: {
                                                                'type': 'Outbound'
                                                            }
                                                        },
                                                        addinbound: {
                                                            title: 'Add inbound connection method',
                                                            route: 'addinbound',
                                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            action: 'showAddConnectionMethodView',
                                                            params: {
                                                                'type': 'Inbound'
                                                            }
                                                        },
                                                        edit: {
                                                            title: 'Edit connection method',
                                                            route: '{connectionMethodId}/edit',
                                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            action: 'showConnectionMethodEditView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadConnectionMethod', function (record) {
                                                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        }
                                                    }
                                                },
                                                //protocol dialects routes
                                                protocols: {
                                                    title: 'Protocol dialects',
                                                    route: 'protocols',
                                                    privileges: ['privilege.administrate.protocol','privilege.view.protocol'],
                                                    controller: 'Mdc.controller.setup.ProtocolDialects',
                                                    action: 'showProtocolDialectsView',
                                                    items: {
                                                        edit: {
                                                            title: 'Edit protocol dialect',
                                                            route: '{protocolDialectId}/edit',
                                                            privileges: ['privilege.administrate.protocol'],
                                                            controller: 'Mdc.controller.setup.ProtocolDialects',
                                                            action: 'showProtocolDialectsEditView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadProtocolDialect', function (record) {
                                                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        }
                                                    }
                                                },
                                                // Validation rule sets
                                                validationrulesets: {
                                                    title: 'Validation rule sets',
                                                    route: 'validationrulesets',
                                                    controller: 'Mdc.controller.setup.ValidationRuleSets',
                                                    privileges:['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration'],
                                                    action: 'showValidationRuleSetsOverview',
                                                    items: {
                                                        add: {
                                                            title: 'Add validation rule sets',
                                                            route: 'add',
                                                            controller: 'Mdc.controller.setup.ValidationRuleSets',
                                                            action: 'showAddValidationRuleSets'
                                                        }
                                                    }
                                                },
                                                //messages routes
                                                messages: {
                                                    title: 'Commands',
                                                    route: 'messages',
                                                    privileges: ['privilege.administrate.deviceCommand','privilege.view.deviceCommand'],
                                                    controller: 'Mdc.controller.setup.Messages',
                                                    action: 'showMessagesOverview'
                                                }
                                            }
                                        }
                                    }
                                },
                                registertypes: {
                                    title: 'Register types',
                                    route: 'registertypes',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    controller: 'Mdc.controller.setup.RegisterMappings',
                                    action: 'showRegisterMappings',
                                    items: {
                                        add: {
                                            title: 'Add register types',
                                            route: 'add',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                            controller: 'Mdc.controller.setup.RegisterMappings',
                                            action: 'addRegisterMappings'
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                loadprofiletypes: {
                    title: 'Load profile types',
                    route: 'loadprofiletypes',
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                    action: 'showLoadProfileTypes',
                    items: {
                        create: {
                            title: 'Add profile type',
                            route: 'create',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            action: 'showEdit',
                            items: {
                                addmeasurementtypes: {
                                    title: 'Add measurement types',
                                    route: 'addmeasurementtypes',
                                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                                    action: 'showMeasurementTypesAddView'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit profile type',
                            route: '{id}/edit',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            action: 'showEdit',
                            callback: function (route) {
                                this.getApplication().on('loadProfileType', function (record) {
                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                addmeasurementtypes: {
                                    title: 'Add measurement types',
                                    route: 'addmeasurementtypes',
                                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                                    action: 'showMeasurementTypesAddView'
                                }
                            }
                        }
                    }
                },
                comservers: {
                    title: 'Communication servers',
                    route: 'comservers',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showComServers',
                    items: {
                        onlineadd: {
                            title: 'Add online communication server',
                            route: 'add/online',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComServerEdit',
                            action: 'showOnlineAddView'
                        },
                        edit: {
                            title: 'Edit communication server',
                            route: '{id}/edit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComServerEdit',
                            action: 'showEditView',
                            callback: function (route) {
                                this.getApplication().on('loadComServer', function (record) {
                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        detail: {
                            title: 'Overview',
                            route: '{id}',
                            privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComServerOverview',
                            action: 'showOverview',
                            redirect: 'administration/comservers/detail/overview',
                            callback: function (route) {
                                this.getApplication().on('comServerOverviewLoad', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                overview: {
                                    title: 'Overview',
                                    route: 'overview',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    controller: 'Mdc.controller.setup.ComServerOverview',
                                    action: 'showOverview'
                                },
                                edit: {
                                    title: 'Edit',
                                    route: 'edit_',
                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                    controller: 'Mdc.controller.setup.ComServerEdit',
                                    action: 'showEditView'
                                },
                                comports: {
                                    title: 'Communication ports',
                                    route: 'comports',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    controller: 'Mdc.controller.setup.ComServerComPortsView',
                                    action: 'showView',
                                    items: {
                                        addInbound: {
                                            title: 'Add inbound communication port',
                                            route: 'add/inbound',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            action: 'showAddInbound'
                                        },
                                        addOutbound: {
                                            title: 'Add outbound communication port',
                                            route: 'add/outbound',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            action: 'showAddOutbound',
                                            items: {
                                                addComPortPool: {
                                                    title: 'Add communication port pool',
                                                    route: 'addPool',
                                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                                    controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                                    action: 'showAddComPortPool'
                                                }
                                            }
                                        },
                                        edit: {
                                            title: 'Edit communication port',
                                            route: '{direction}/{comPortId}/edit',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            action: 'showEditView',
                                            callback: function (route) {
                                                this.getApplication().on('loadComPortOnComServer', function (name) {
                                                    route.setTitle('Edit \'' + name + '\'');
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            },
                                            items: {
                                                addComPortPool: {
                                                    title: 'Add communication port pool',
                                                    route: 'addPool',
                                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                                    controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                                    action: 'showAddComPortPool'
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                devicecommunicationprotocols: {
                    title: 'Communication protocols',
                    route: 'devicecommunicationprotocols',
                    privileges: ['privilege.administrate.protocol','privilege.view.protocol'],
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showDeviceCommunicationProtocols',
                    items: {
                        edit: {
                            title: 'Edit communication protocol',
                            route: '{id}/edit',
                            privileges: ['privilege.administrate.protocol'],
                            controller: 'Mdc.controller.setup.DeviceCommunicationProtocols',
                            action: 'showDeviceCommunicationProtocolEditView',
                            callback: function (route) {
                                this.getApplication().on('loadDeviceCommunicationProtocol', function (record) {
                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                licensedprotocols: {
                    title: 'Licensed protocols',
                    route: 'licensedprotocols',
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showLicensedProtocols'
                },
                comportpools: {
                    title: 'Communication port pools',
                    route: 'comportpools',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showComPortPools',
                    items: {
                        addinbound: {
                            title: Uni.I18n.translate('comPortPool.title.addInbound', 'MDC', 'Add inbound communication port pool'),
                            route: 'add/inbound',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            action: 'showInboundAddView'
                        },
                        addoutbound: {
                            title: Uni.I18n.translate('comPortPool.title.addOutbound', 'MDC', 'Add outbound communication port pool'),
                            route: 'add/outbound',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            action: 'showOutboundAddView'
                        },
                        edit: {
                            title: 'Edit communication port pool',
                            route: '{id}/edit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            action: 'showEditView',
                            callback: function (route) {
                                this.getApplication().on('loadComPortPool', function (record) {
                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        detail: {
                            title: 'Detail',
                            route: '{id}',
                            privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                            controller: 'Mdc.controller.setup.ComPortPoolOverview',
                            action: 'showOverview',
                            redirect: 'administration/comportpools/detail/overview',
                            callback: function (route) {
                                this.getApplication().on('comPortPoolOverviewLoad', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                overview: {
                                    title: 'Overview',
                                    route: 'overview',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    controller: 'Mdc.controller.setup.ComPortPoolOverview',
                                    action: 'showOverview'
                                },
                                edit: {
                                    title: 'Edit',
                                    route: 'edit_',
                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                    controller: 'Mdc.controller.setup.ComPortPoolEdit',
                                    action: 'showEditView'
                                },
                                comports: {
                                    title: 'Communication ports',
                                    route: 'comports',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    controller: 'Mdc.controller.setup.ComPortPoolComPortsView',
                                    action: 'showView',
                                    items: {
                                        add: {
                                            title: 'Add communication port',
                                            route: 'add',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                            controller: 'Mdc.controller.setup.ComPortPoolComPortsView',
                                            action: 'showAddComPortView'
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                registertypes: {
                    title: 'Register types',
                    route: 'registertypes',
                    controller: 'Mdc.controller.setup.RegisterTypes',
                    action: 'showRegisterTypes',
                    items: {
                        create: {
                            title: 'Add register type',
                            route: 'add',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeCreateView'
                        },
                        edit: {
                            title: 'Edit register type',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeEditView',
                            callback: function (route) {
                                this.getApplication().on('loadRegisterType', function (record) {
                                    route.setTitle('Edit ' + record.get('name') + '');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                registergroups: {
                    title: 'Register groups',
                    route: 'registergroups',
                    controller: 'Mdc.controller.setup.RegisterGroups',
                    action: 'showRegisterGroups',
                    items: {
                        create: {
                            title: Uni.I18n.translate('registerGroup.create', 'USM', 'Add register group'),
                            route: 'add',
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            action: 'showRegisterGroupCreateView'
                        },
                        edit: {
                            title: Uni.I18n.translate('registerGroup.edit', 'USM', 'Edit register group'),
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            action: 'showRegisterGroupEditView',
                            callback: function (route) {
                                this.getApplication().on('loadRegisterGroup', function (record) {
                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                communicationtasks: {
                    title: 'Communication tasks',
                    route: 'communicationtasks',
                    controller: 'Mdc.controller.setup.Comtasks',
                    action: 'showCommunicationTasksView',
                    items: {
                        create: {
                            title: 'Add communication task',
                            route: 'create',
                            controller: 'Mdc.controller.setup.Comtasks',
                            action: 'showCommunicationTasksCreateEdit'
                        },
                        edit: {
                            title: 'Edit communication task',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.Comtasks',
                            action: 'showCommunicationTasksCreateEdit',
                            callback: function (route) {
                                this.getApplication().on('loadCommunicationTask', function (record) {
                                    route.setTitle(('general.edit', 'MDC', 'Edit') + ' \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                communicationschedules: {
                    title: 'Shared communication schedules',
                    route: 'communicationschedules',
                    privileges: ['privilege.administrate.schedule','privilege.view.schedule'],
                    controller: 'Mdc.controller.setup.CommunicationSchedules',
                    action: 'showCommunicationSchedules',
                    items: {
                        create: {
                            title: 'Add shared communication schedule',
                            route: 'create',
                            privileges: ['privilege.administrate.schedule'],
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            action: 'showCommunicationSchedulesEditView'
                        },
                        edit: {
                            title: 'Edit shared communication schedule',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            privileges: ['privilege.administrate.schedule'],
                            action: 'showCommunicationSchedulesEditView',
                            callback: function (route) {
                                this.getApplication().on('loadCommunicationSchedule', function (record) {
                                    route.setTitle('Edit \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                }
            }
        },
        devices: {
            title: 'Devices',
            route: 'devices',
            disabled: true,
            items: {
                devicegroups: {
                    title: Uni.I18n.translate('deviceGroups.title', 'MDC', 'Device groups'),
                    route: 'devicegroups',
                    controller: 'Mdc.controller.setup.DeviceGroups',
                    privileges: ['privilege.administrate.deviceGroup','privilege.administrate.deviceOfEnumeratedGroup','privilege.view.deviceGroupDetail'],
                    action: 'showDeviceGroups',
                    items: {
                        add: {
                            title: 'Add device group',
                            route: 'add',
                            controller: 'Mdc.controller.setup.AddDeviceGroupAction',
                            privileges: ['privilege.administrate.deviceGroup'],
                            action: 'showAddDeviceGroupAction',
                            filter: 'Mdc.model.DeviceFilter'
                        }
                    }
                },
                add: {
                    title: Uni.I18n.translate('deviceAdd.title', 'MDC', 'Add device'),
                    route: 'add',
                    controller: 'Mdc.controller.setup.Devices',
                    privileges: ['privilege.create.inventoryManagement'],
                    action: 'showAddDevice'
                },
                device: {
                    title: 'Device',
                    route: '{mRID}',
                    controller: 'Mdc.controller.setup.Devices',
                    privileges: ['privilege.view.device', 'privilege.administrate.device'],
                    action: 'showDeviceDetailsView',
                    callback: function (route) {
                        this.getApplication().on('loadDevice', function (record) {
                            route.setTitle(record.get('mRID'));
                            return true;
                        }, {single: true});

                        return this;
                    },
                    items: {
                        commands: {
                            title: 'Commands',
                            route: 'commands',
                            controller: 'Mdc.controller.setup.DeviceCommands',
                            privileges: ['privilege.administrate.device', 'privilege.view.device'],
                            action: 'showOverview',
                            items: {
                                add: {
                                    title: 'Add command',
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.DeviceCommands',
                                    privileges: ['privilege.administrate.device', 'privilege.view.device'],
                                    action: 'showAddOverview'
                                }
                            }
                        },
                        //protocol dialects routes
                        protocols: {
                            title: 'Protocol dialects',
                            route: 'protocols',
                            controller: 'Mdc.controller.setup.DeviceProtocolDialects',
                            privileges: ['privilege.administrate.protocol', 'privilege.view.protocol'],
                            action: 'showProtocolDialectsView',
                            items: {
                                edit: {
                                    title: 'Edit protocol dialect',
                                    route: '{protocolDialectId}/edit',
                                    controller: 'Mdc.controller.setup.DeviceProtocolDialects',
                                    privileges: ['privilege.administrate.protocol'],
                                    action: 'showProtocolDialectsEditView',
                                    callback: function (route) {
                                        this.getApplication().on('loadDeviceProtocolDialect', function (record) {
                                            route.setTitle('Edit \'' + record.get('name') + '\'');
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                }
                            }
                        },

                        topology: {
                            title: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
                            route: 'topology',
                            controller: 'Mdc.controller.setup.DeviceTopology',
                            privileges: ['privilege.view.device', 'privilege.administrate.device'],
                            filter: 'Mdc.model.TopologyFilter',
                            action: 'showTopologyView'
                        },

                        connectionmethods: {
                            title: 'Connection methods',
                            route: 'connectionmethods',
                            controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            action: 'showDeviceConnectionMethods',
                            items: {
                                addoutbound: {
                                    title: 'Add outbound',
                                    route: 'addoutbound',
                                    controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                    action: 'showAddDeviceConnectionMethodView',
                                    params: {
                                        'type': 'Outbound'
                                    }
                                },
                                addinbound: {
                                    title: 'Add inbound',
                                    route: 'addinbound',
                                    controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                                    action: 'showAddDeviceConnectionMethodView',
                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                    params: {
                                        'type': 'Inbound'
                                    }
                                },
                                edit: {
                                    title: 'Edit connection method',
                                    route: '{connectionMethodId}/edit',
                                    controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                    action: 'showDeviceConnectionMethodEditView',
                                    callback: function (route) {
                                        this.getApplication().on('loadConnectionMethod', function (record) {
                                            route.setTitle('Edit \'' + record.get('name') + '\'');
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                history: {
                                    title: 'Show connection history',
                                    route: '{connectionMethodId}/history',
                                    controller: 'Mdc.controller.setup.DeviceConnectionHistory',
                                    action: 'showDeviceConnectionMethodHistory',
                                    callback: function (route) {
                                        this.getApplication().on('loadConnectionMethod', function (record) {
                                            route.setTitle('History of \''+ record.get('name') + '\'');
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                       viewlog: {
                                           title: 'Connection log',
                                           route: '{historyId}/viewlog',
                                           controller: 'Mdc.controller.setup.DeviceConnectionHistory',
                                           action: 'showDeviceConnectionMethodHistoryLog',
                                           filter: 'Mdc.model.ConnectionLogFilter'
                                       }
                                    }
                                }
                            }
                        },
                        registers: {
                            title: 'Registers',
                            route: 'registers',
                            controller: 'Mdc.controller.setup.DeviceRegisterConfiguration',
                            privileges: ['privilege.administrate.device','privilege.view.device'],
                            action: 'showDeviceRegisterConfigurationsView',
                            items: {
                                register: {
                                    route: '{registerId}',
                                    controller: 'Mdc.controller.setup.DeviceRegisterTab',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'initTabDeviceRegisterConfigurationDetailsView',
                                    callback: function (route) {
                                        this.getApplication().on('loadRegisterConfiguration', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    },
                                    items: {
                                        data: {
                                            title: 'Register data',
                                            route: 'data',
                                            controller: 'Mdc.controller.setup.DeviceRegisterTab',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'initTabShowDeviceRegisterDataView',
                                            filter: 'Mdc.model.RegisterDataFilter',
                                            items: {
                                                create: {
                                                    title: Uni.I18n.translate('device.registerData.addReading', 'MDC', 'Add reading'),
                                                    route: 'create',
                                                    controller: 'Mdc.controller.setup.DeviceRegisterDataEdit',
                                                    privileges: ['privilege.administrate.device'],
                                                    action: 'showDeviceRegisterConfigurationDataAddView'
                                                },
                                                edit: {
                                                    title: Uni.I18n.translate('device.registerData.editReading', 'MDC', 'Edit reading'),
                                                    route: '{timestamp}/edit',
                                                    controller: 'Mdc.controller.setup.DeviceRegisterDataEdit',
                                                    privileges: ['privilege.administrate.device'],
                                                    action: 'showDeviceRegisterConfigurationDataEditView'
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        datavalidation: {
                            title: 'Data validation',
                            route: 'datavalidation',
                            controller: 'Mdc.controller.setup.DeviceDataValidation',
                            privileges: ['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration'],
                            action: 'showDeviceDataValidationMainView'
                        },
                        communicationschedules: {
                            title: 'Communication planning',
                            route: 'communicationplanning',
                            controller: 'Mdc.controller.setup.DeviceCommunicationSchedules',
                            privileges: ['privilege.view.scheduleDevice'],
                            action: 'showDeviceCommunicationScheduleView',
                            items: {

                                add: {
                                    title: 'Add shared communication schedules',
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.DeviceCommunicationSchedules',
                                    privileges: ['privilege.administrate.schedule'],
                                    action: 'addSharedCommunicationSchedule'
                                }

                            }
                        },
                        communicationtasks: {
                            title: 'Communication tasks',
                            route: 'communicationtasks',
                            controller: 'Mdc.controller.setup.DeviceCommunicationTasks',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            action: 'showDeviceCommunicationTasksView',
                            items: {
                                history: {
                                    title: 'Show communication history',
                                    route: '{comTaskId}/history',
                                    controller: 'Mdc.controller.setup.DeviceCommunicationTaskHistory',
                                    action: 'showDeviceCommunicationTaskHistory',
                                    callback: function (route) {
                                        this.getApplication().on('loadCommunicationTask', function (record) {
                                            route.setTitle(('general.history', 'MDC', 'History of') + ' \'' + record.get('name') + '\'');
                                            return true;
                                        }, {single: true});

                                        return this;
                                    },
                                    items: {
                                        viewlog: {
                                            title: 'Communication log',
                                            route: '{historyId}/viewlog',
                                            controller: 'Mdc.controller.setup.DeviceCommunicationTaskHistory',
                                            action: 'showDeviceCommunicationTaskHistoryLog',
                                            filter: 'Mdc.model.DeviceComTaskLogFilter'
                                        }
                                    }
                                }
                            }
                        },
                        loadprofiles: {
                            title: 'Load profiles',
                            route: 'loadprofiles',
                            controller: 'Mdc.controller.setup.DeviceLoadProfiles',
                            privileges: ['privilege.administrate.device','privilege.view.device'],
                            action: 'showView',
                            items: {
                                loadprofile: {
                                    title: 'Load profile',
                                    route: '{loadProfileId}',
                                    controller: 'Mdc.controller.setup.DeviceLoadProfileTab',
                                    privileges: ['privilege.administrate.device','privilege.view.device'],
                                    action: 'initTabDeviceLoadProfileDetailsView',
                                    callback: function (route) {
                                        this.getApplication().on('loadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        data: {
                                            title: 'Load profile data',
                                            route: 'graph',
                                            controller: 'Mdc.controller.setup.DeviceLoadProfileTab',
                                            privileges: ['privilege.administrate.device','privilege.view.device'],
                                            action: 'initTabLoadProfileGraphView',
                                            filter: 'Mdc.model.LoadProfilesOfDeviceDataFilter'
                                        },
                                        tableData: {
                                            title: 'Load profile data',
                                            route: 'table',
                                            controller: 'Mdc.controller.setup.DeviceLoadProfileTab',
                                            privileges: ['privilege.administrate.device','privilege.view.device'],
                                            action: 'initTabLoadProfileDataView',
                                            filter: 'Mdc.model.LoadProfilesOfDeviceDataFilter'
                                        },
                                        validation: {
                                            title: 'Load profile validation',
                                            route: 'validation'
                                        }
                                    }
                                }
                            }
                        },
                        channels: {
                            title: Uni.I18n.translate('routing.channels', 'MDC', 'Channels'),
                            route: 'channels',
                            controller: 'Mdc.controller.setup.DeviceLoadProfileChannels',
                            privileges: ['privilege.administrate.device','privilege.view.device'],
                            action: 'showOverview',
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('routing.channel', 'MDC', 'Channel'),
                                    route: '{channelId}',
                                    controller: 'Mdc.controller.setup.DeviceChannelTab',
                                    privileges: ['privilege.administrate.device','privilege.view.device'],
                                    action: 'initTabDeviceChannelDetailsView',
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        data: {
                                            title: Uni.I18n.translate('routing.channelData', 'MDC', 'Channel data'),
                                            route: 'graph',
                                            controller: 'Mdc.controller.setup.DeviceChannelTab',
                                            privileges: ['privilege.administrate.device','privilege.view.device'],
                                            action: 'initTabChannelGraphView',
                                            filter: 'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'
                                        },
                                        tableData: {
                                            title: Uni.I18n.translate('routing.channelData', 'MDC', 'Channel data'),
                                            route: 'table',
                                            controller: 'Mdc.controller.setup.DeviceChannelTab',
                                            privileges: ['privilege.administrate.device','privilege.view.device'],
                                            action: 'initTabChannelDataView',
                                            filter: 'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter',
                                            items: {
                                                editreadings: {
                                                    title: Uni.I18n.translate('routing.editReadings', 'MDC', 'Edit readings'),
                                                    route: 'editreadings',
                                                    controller: 'Mdc.controller.setup.DeviceLoadProfileChannelDataEditReadings',
                                                    privileges: ['privilege.administrate.device'],
                                                    action: 'showOverview',
                                                    filter: 'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'
                                                }
                                            }
                                        },
                                        validation: {
                                            title: Uni.I18n.translate('routing.channelValidation', 'MDC', 'Channel validation'),
                                            route: 'validation'
                                        }
                                    }
                                }
                            }
                        },
                        logbooks: {
                            title: Uni.I18n.translate('router.logbooks', 'MDC', 'Logbooks'),
                            route: 'logbooks',
                            controller: 'Mdc.controller.setup.DeviceLogbooks',
                            privileges: ['privilege.administrate.device','privilege.view.device'],
                            action: 'showView',
                            items: {
                                logbook: {
                                    title: Uni.I18n.translate('router.logbook', 'MDC', 'Logbook'),
                                    route: '{logbookId}',
                                    controller: 'Mdc.controller.setup.DeviceLogbookOverview',
                                    privileges: ['privilege.administrate.device','privilege.view.device'],
                                    action: 'showOverview',
                                    redirect: 'devices/device/logbooks/logbook/overview',
                                    callback: function (route) {
                                        this.getApplication().on('logbookOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;

                                    },
                                    items: {
                                        overview: {
                                            title: Uni.I18n.translate('router.overview', 'MDC', 'Overview'),
                                            route: 'overview',
                                            controller: 'Mdc.controller.setup.DeviceLogBookTab',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showOverview'
                                        },
                                        data: {
                                            title: Uni.I18n.translate('router.logbookData', 'MDC', 'Logbook data'),
                                            route: 'data',
                                            controller: 'Mdc.controller.setup.DeviceLogBookTab',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showData',
                                            filter: 'Mdc.model.LogbookOfDeviceDataFilter'
                                        }
                                    }
                                }
                            }
                        },
                        events: {
                            title: Uni.I18n.translate('router.events', 'MDC', 'Events'),
                            route: 'events',
                            controller: 'Mdc.controller.setup.DeviceEvents',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            action: 'showOverview',
                            filter: 'Mdc.model.LogbookOfDeviceDataFilter'
                        },
                        securitysettings: {
                            title: 'Security settings',
                            route: 'securitysettings',
                            controller: 'Mdc.controller.setup.DeviceSecuritySettings',
                            action: 'showDeviceSecuritySettings',
                            items: {
                                edit: {
                                    title: 'Edit security setting',
                                    route: '{securitySettingId}/edit',
                                    controller: 'Mdc.controller.setup.DeviceSecuritySettings',
                                    action: 'showDeviceSecuritySettingEditView',
                                    callback: function (route) {
                                        this.getApplication().on('loadDeviceSecuritySetting', function (record) {
                                            route.setTitle('Edit \'' + record.get('name') + '\'');
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        search: {
            title: 'Search',
            route: 'search',
            controller: 'Mdc.controller.setup.SearchItems',
            privileges: ['privilege.view.device', 'privilege.administrate.device'],
            action: 'showSearchItems',
            items: {
                bulkAction: {
                    title: 'Bulk action',
                    route: 'bulk',
                    controller: 'Mdc.controller.setup.SearchItemsBulkAction',
                    privileges: ['privilege.administrate.device'],
                    action: 'showBulkAction'
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    },

    tokenizeBrowse: function (item, id) {
        if (id === undefined) {
            return this.tokenize([this.rootToken, item]);
        } else {
            return this.tokenize([this.rootToken, item, id]);
        }
    },

    tokenizeAddComserver: function () {
        return this.tokenize([this.rootToken, 'comservers', 'create']);
    },

    tokenizeAddDeviceCommunicationProtocol: function () {
        return this.tokenize([this.rootToken, 'devicecommunicationprotocols', 'create']);
    },

    tokenizeAddComPortPool: function () {
        return this.tokenize([this.rootToken, 'comportpools', 'create']);
    }
});
