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
                    controller: 'Mdc.controller.setup.SetupOverview',
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    action: 'showLogbookTypes',
                    items: {
                        create: {
                            title: 'Add logbook type',
                            route: 'add',
                            controller: 'Mdc.controller.setup.LogbookTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            action: 'showLogbookTypeCreateView'
                        },
                        edit: {
                            title: 'Edit logbook type',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.LogbookTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                    controller: 'Mdc.controller.setup.SetupOverview',
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    action: 'showDeviceTypes',
                    items: {
                        create: {
                            title: 'Add device type',
                            route: 'add',
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            action: 'showDeviceTypeCreateView'
                        },
                        view: {
                            title: 'Overview',
                            route: '{deviceTypeId}',
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
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
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                    action: 'showDeviceTypeEditView'
                                },
                                logbooktypes: {
                                    title: 'Logbook types',
                                    route: 'logbooktypes',
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showDeviceTypeLogbookTypesView',
                                    items: {
                                        add: {
                                            title: 'Add logbook types',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.DeviceTypes',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                            action: 'showAddLogbookTypesView'
                                        }
                                    }
                                },
                                loadprofiles: {
                                    title: 'Load profiles',
                                    route: 'loadprofiles',
                                    controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showDeviceTypeLoadProfileTypesView',
                                    items: {
                                        add: {
                                            title: 'Add load profiles',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                            action: 'showDeviceTypeLoadProfileTypesAddView'
                                        }
                                    }
                                },
                                deviceconfigurations: {
                                    title: 'Device configurations',
                                    route: 'deviceconfigurations',
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showDeviceConfigurations',
                                    items: {
                                        create: {
                                            title: 'Add device configuration',
                                            route: 'create',
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                            action: 'showDeviceConfigurationCreateView'
                                        },
                                        view: {
                                            title: 'Device configuration',
                                            route: '{deviceConfigurationId}',
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
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
                                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                    action: 'showDeviceConfigurationEditView'
                                                },
                                                loadprofiles: {
                                                    title: 'Load profiles',
                                                    route: 'loadprofiles',
                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    action: 'showDeviceConfigurationLoadProfilesView',
                                                    items: {
                                                        add: {
                                                            title: 'Add load profile',
                                                            route: 'add',
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showDeviceConfigurationLoadProfilesAddView'
                                                        },
                                                        edit: {
                                                            title: 'Edit load profile',
                                                            route: '{loadProfileConfigurationId}/edit',
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
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
                                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsAddView'
                                                                },
                                                                edit: {
                                                                    title: 'Edit channel configuration',
                                                                    route: '{channelId}/edit',
                                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsEditView'
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                logbookconfigurations: {
                                                    title: 'Logbook configurations',
                                                    route: 'logbookconfigurations',
                                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    action: 'showDeviceConfigurationLogbooksView',
                                                    items: {
                                                        add: {
                                                            title: 'Add logbook configuration',
                                                            route: 'add',
                                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showAddDeviceConfigurationLogbooksView'
                                                        },
                                                        edit: {
                                                            title: 'Edit logbook configuration',
                                                            route: '{logbookConfigurationId}/edit',
                                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showEditDeviceConfigurationLogbooksView'
                                                        }
                                                    }
                                                },
                                                //Register configuration routes
                                                registerconfigurations: {
                                                    title: 'Register configurations',
                                                    route: 'registerconfigurations',
                                                    controller: 'Mdc.controller.setup.RegisterConfigs',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    action: 'showRegisterConfigs',
                                                    items: {
                                                        create: {
                                                            title: 'Add register configuration',
                                                            route: 'create',
                                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showRegisterConfigurationCreateView'
                                                        },
                                                        edit: {
                                                            title: 'Edit register configuration',
                                                            route: '{registerConfigurationId}/edit',
                                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showRegisterConfigurationEditView'
                                                        }
                                                    }
                                                },
                                                //Security settings routes
                                                securitysettings: {
                                                    title: 'Security settings',
                                                    route: 'securitysettings',
                                                    controller: 'Mdc.controller.setup.SecuritySettings',
                                                    privileges: ['privilege.administrate.deviceSecurity','privilege.view.deviceSecurity'],
                                                    action: 'showSecuritySettings',
                                                    items: {
                                                        create: {
                                                            title: 'Add security setting',
                                                            route: 'create',
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            privileges: ['privilege.administrate.deviceSecurity'],
                                                            action: 'showSecuritySettingsCreateView'
                                                        },
                                                        edit: {
                                                            title: 'Edit security setting',
                                                            route: '{securitySettingId}/edit',
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            privileges: ['privilege.administrate.deviceSecurity'],
                                                            action: 'showSecuritySettingsEditView'
                                                        },
                                                        executionLevels: {
                                                            title: 'Add privileges',
                                                            route: '{securitySettingId}/privileges/add',
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            privileges: ['privilege.administrate.deviceSecurity'],
                                                            action: 'showAddExecutionLevelsView'
                                                        }
                                                    }
                                                },
                                                //Communication tasks routes
                                                comtaskenablements: {
                                                    title: 'Communication task configurations',
                                                    route: 'comtaskenablements',
                                                    controller: 'Mdc.controller.setup.CommunicationTasks',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    action: 'showCommunicationTasks',
                                                    items: {
                                                        create: {
                                                            title: 'Add communication task configuration',
                                                            route: 'create',
                                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showAddCommunicationTaskView'
                                                        },
                                                        edit: {
                                                            title: 'Edit communication task configuration',
                                                            route: '{comTaskEnablementId}/edit',
                                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                                                    controller: 'Mdc.controller.setup.ConnectionMethods',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    action: 'showConnectionMethods',
                                                    items: {
                                                        addoutbound: {
                                                            title: 'Add outbound connection method',
                                                            route: 'addoutbound',
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showAddConnectionMethodView',
                                                            params: {
                                                                'type': 'Outbound'
                                                            }
                                                        },
                                                        addinbound: {
                                                            title: 'Add inbound connection method',
                                                            route: 'addinbound',
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
                                                            action: 'showAddConnectionMethodView',
                                                            params: {
                                                                'type': 'Inbound'
                                                            }
                                                        },
                                                        edit: {
                                                            title: 'Edit connection method',
                                                            route: '{connectionMethodId}/edit',
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                                                    controller: 'Mdc.controller.setup.ProtocolDialects',
                                                    privileges: ['privilege.administrate.protocol','privilege.view.protocol'],
                                                    action: 'showProtocolDialectsView',
                                                    items: {
                                                        edit: {
                                                            title: 'Edit protocol dialect',
                                                            route: '{protocolDialectId}/edit',
                                                            controller: 'Mdc.controller.setup.ProtocolDialects',
                                                            privileges: ['privilege.administrate.protocol'],
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
                                                    privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'],
                                                    action: 'showValidationRuleSetsOverview',
                                                    items: {
                                                        add: {
                                                            title: 'Add validation rule sets',
                                                            route: 'add',
                                                            controller: 'Mdc.controller.setup.ValidationRuleSets',
                                                            privileges: ['privilege.administrate.validationConfiguration'],
                                                            action: 'showAddValidationRuleSets'
                                                        }
                                                    }
                                                },
                                                //messages routes
                                                messages: {
                                                    title: 'Messages',
                                                    route: 'messages',
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
                                    controller: 'Mdc.controller.setup.RegisterMappings',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showRegisterMappings',
                                    items: {
                                        add: {
                                            title: 'Add register types',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.RegisterMappings',
                                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    action: 'showLoadProfileTypes',
                    items: {
                        create: {
                            title: 'Add profile type',
                            route: 'create',
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            action: 'showEdit',
                            items: {
                                addmeasurementtypes: {
                                    title: 'Add measurement types',
                                    route: 'addmeasurementtypes',
                                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                    action: 'showMeasurementTypesAddView'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit profile type',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                    action: 'showMeasurementTypesAddView'
                                }
                            }
                        }
                    }
                },
                comservers: {
                    title: 'Communication servers',
                    route: 'comservers',
                    controller: 'Mdc.controller.setup.SetupOverview',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    action: 'showComServers',
                    items: {
                        onlineadd: {
                            title: 'Add online communication server',
                            route: 'add/online',
                            controller: 'Mdc.controller.setup.ComServerEdit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            action: 'showOnlineAddView'
                        },
                        edit: {
                            title: 'Edit communication server',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.ComServerEdit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
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
                            controller: 'Mdc.controller.setup.ComServerOverview',
                            privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
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
                                    controller: 'Mdc.controller.setup.ComServerOverview',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    action: 'showOverview'
                                },
                                edit: {
                                    title: 'Edit',
                                    route: 'edit_',
                                    controller: 'Mdc.controller.setup.ComServerEdit',
                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                    action: 'showEditView'
                                },
                                comports: {
                                    title: 'Communication ports',
                                    route: 'comports',
                                    controller: 'Mdc.controller.setup.ComServerComPortsView',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    action: 'showView',
                                    items: {
                                        addInbound: {
                                            title: 'Add inbound communication port',
                                            route: 'add/inbound',
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                            action: 'showAddInbound'
                                        },
                                        addOutbound: {
                                            title: 'Add outbound communication port',
                                            route: 'add/outbound',
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
                                            action: 'showAddOutbound',
                                            items: {
                                                addComPortPool: {
                                                    title: 'Add communication port pool',
                                                    route: 'addPool',
                                                    controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                                    action: 'showAddComPortPool'
                                                }
                                            }
                                        },
                                        edit: {
                                            title: 'Edit communication port',
                                            route: '{direction}/{comPortId}/edit',
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
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
                                                    controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                                    privileges: ['privilege.administrate.communicationInfrastructure'],
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
                    controller: 'Mdc.controller.setup.SetupOverview',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    action: 'showDeviceCommunicationProtocols',
                    items: {
                        edit: {
                            title: 'Edit communication protocol',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.DeviceCommunicationProtocols',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
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
                    controller: 'Mdc.controller.setup.SetupOverview',
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    action: 'showComPortPools',
                    items: {
                        addinbound: {
                            title: Uni.I18n.translate('comPortPool.title.addInbound', 'MDC', 'Add inbound communication port pool'),
                            route: 'add/inbound',
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            action: 'showInboundAddView'
                        },
                        addoutbound: {
                            title: Uni.I18n.translate('comPortPool.title.addOutbound', 'MDC', 'Add outbound communication port pool'),
                            route: 'add/outbound',
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            action: 'showOutboundAddView'
                        },
                        edit: {
                            title: 'Edit communication port pool',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
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
                            controller: 'Mdc.controller.setup.ComPortPoolOverview',
                            privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
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
                                    controller: 'Mdc.controller.setup.ComPortPoolOverview',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    action: 'showOverview'
                                },
                                edit: {
                                    title: 'Edit',
                                    route: 'edit_',
                                    controller: 'Mdc.controller.setup.ComPortPoolEdit',
                                    privileges: ['privilege.administrate.communicationInfrastructure'],
                                    action: 'showEditView'
                                },
                                comports: {
                                    title: 'Communication ports',
                                    route: 'comports',
                                    controller: 'Mdc.controller.setup.ComPortPoolComPortsView',
                                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                                    action: 'showView',
                                    items: {
                                        add: {
                                            title: 'Add communication port',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.ComPortPoolComPortsView',
                                            privileges: ['privilege.administrate.communicationInfrastructure'],
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
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    action: 'showRegisterTypes',
                    items: {
                        create: {
                            title: 'Add register type',
                            route: 'add',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            action: 'showRegisterTypeCreateView'
                        },
                        edit: {
                            title: 'Edit register type',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                    action: 'showRegisterGroups',
                    items: {
                        create: {
                            title: Uni.I18n.translate('registerGroup.create', 'USM', 'Add register group'),
                            route: 'add',
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            privileges: ['privilege.administrate.deviceConfiguration'],
                            action: 'showRegisterGroupCreateView'
                        },
                        edit: {
                            title: Uni.I18n.translate('registerGroup.edit', 'USM', 'Edit register group'),
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            privileges: ['privilege.administrate.deviceConfiguration'],
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
                    privileges: ['privilege.administrate.communicationInfrastructure','privilege.view.communicationInfrastructure'],
                    action: 'showCommunicationTasksView',
                    items: {
                        create: {
                            title: 'Add communication task',
                            route: 'create',
                            controller: 'Mdc.controller.setup.Comtasks',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
                            action: 'showCommunicationTasksCreateEdit'
                        },
                        edit: {
                            title: 'Edit communication task',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.Comtasks',
                            privileges: ['privilege.administrate.communicationInfrastructure'],
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
                    controller: 'Mdc.controller.setup.CommunicationSchedules',
                    privileges: ['privilege.administrate.schedule','privilege.view.schedule'],
                    action: 'showCommunicationSchedules',
                    items: {
                        create: {
                            title: 'Add shared communication schedule',
                            route: 'create',
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            privileges: ['privilege.administrate.schedule'],
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
                    privileges: ['privilege.view.device', 'privilege.administrate.device'],
                    action: 'showDeviceGroups'
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
                                }
                            }
                        },
                        registers: {
                            title: 'Registers',
                            route: 'registers',
                            controller: 'Mdc.controller.setup.DeviceRegisterConfiguration',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            action: 'showDeviceRegisterConfigurationsView',
                            items: {
                                register: {
                                    route: '{registerId}',
                                    controller: 'Mdc.controller.setup.DeviceRegisterConfiguration',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showDeviceRegisterConfigurationDetailsView',
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
                                            controller: 'Mdc.controller.setup.DeviceRegisterData',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showDeviceRegisterDataView',
                                            filter: 'Mdc.model.RegisterDataFilter',
                                            items: {
                                                create: {
                                                    title: Uni.I18n.translate('device.registerData.addData', 'MDC', 'Add register data'),
                                                    route: 'create',
                                                    controller: 'Mdc.controller.setup.DeviceRegisterDataEdit',
                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                    action: 'showDeviceRegisterConfigurationDataAddView'
                                                },
                                                edit: {
                                                    title: Uni.I18n.translate('device.registerData.editData', 'MDC', 'Edit register data'),
                                                    route: '{timestamp}/edit',
                                                    controller: 'Mdc.controller.setup.DeviceRegisterDataEdit',
                                                    privileges: ['privilege.administrate.deviceConfiguration'],
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
                            privileges: ['privilege.view.validateDevice'],
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

                            }
                        },
                        loadprofiles: {
                            title: 'Load profiles',
                            route: 'loadprofiles',
                            controller: 'Mdc.controller.setup.DeviceLoadProfiles',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            action: 'showView',
                            items: {
                                loadprofile: {
                                    title: 'Load profile',
                                    route: '{loadProfileId}',
                                    controller: 'Mdc.controller.setup.DeviceLoadProfileOverview',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                    action: 'showOverview',
                                    callback: function (route) {
                                        this.getApplication().on('loadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        channels: {
                                            title: Uni.I18n.translate('routing.channels', 'MDC', 'Channels'),
                                            route: 'channels',
                                            controller: 'Mdc.controller.setup.DeviceLoadProfileChannels',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showOverview',
                                            items: {
                                                channel: {
                                                    title: Uni.I18n.translate('routing.channel', 'MDC', 'Channel'),
                                                    route: '{channelId}',
                                                    controller: 'Mdc.controller.setup.DeviceLoadProfileChannelOverview',
                                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                    action: 'showOverview',
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
                                                            controller: 'Mdc.controller.setup.DeviceLoadProfileChannelData',
                                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                            action: 'showGraphOverview',
                                                            filter: 'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter',
                                                            items: {
                                                                editreadings: {
                                                                    title: Uni.I18n.translate('routing.editReadings', 'MDC', 'Edit readings'),
                                                                    route: 'editreadings',
                                                                    controller: 'Mdc.controller.setup.DeviceLoadProfileChannelDataEditReadings',
                                                                    privileges: ['privilege.administrate.deviceConfiguration'],
                                                                    action: 'showOverview',
                                                                    filter: 'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'
                                                                }
                                                            }
                                                        },
                                                        tableData: {
                                                            title: Uni.I18n.translate('routing.channelData', 'MDC', 'Channel data'),
                                                            route: 'table',
                                                            controller: 'Mdc.controller.setup.DeviceLoadProfileChannelData',
                                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                                            action: 'showTableOverview',
                                                            filter: 'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'
                                                        },
                                                        validation: {
                                                            title: Uni.I18n.translate('routing.channelValidation', 'MDC', 'Channel validation'),
                                                            route: 'validation'
                                                        }

                                                    }
                                                }
                                            }
                                        },
                                        data: {
                                            title: 'Load profile data',
                                            route: 'graph',
                                            controller: 'Mdc.controller.setup.DeviceLoadProfileData',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showGraphOverview',
                                            filter: 'Mdc.model.LoadProfilesOfDeviceDataFilter'
                                        },
                                        tableData: {
                                            title: 'Load profile data',
                                            route: 'table',
                                            controller: 'Mdc.controller.setup.DeviceLoadProfileData',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showTableOverview',
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
                        logbooks: {
                            title: Uni.I18n.translate('router.logbooks', 'MDC', 'Logbooks'),
                            route: 'logbooks',
                            controller: 'Mdc.controller.setup.DeviceLogbooks',
                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                            action: 'showView',
                            items: {
                                logbook: {
                                    title: Uni.I18n.translate('router.logbook', 'MDC', 'Logbook'),
                                    route: '{logbookId}',
                                    controller: 'Mdc.controller.setup.DeviceLogbookOverview',
                                    privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
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
                                            controller: 'Mdc.controller.setup.DeviceLogbookOverview',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showOverview'
                                        },
                                        data: {
                                            title: Uni.I18n.translate('router.logbookData', 'MDC', 'Logbook data'),
                                            route: 'data',
                                            controller: 'Mdc.controller.setup.DeviceLogbookData',
                                            privileges: ['privilege.administrate.deviceConfiguration','privilege.view.deviceConfiguration'],
                                            action: 'showOverview',
                                            filter: 'Mdc.model.LogbookOfDeviceDataFilter'
                                        }
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
