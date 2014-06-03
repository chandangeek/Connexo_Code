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
                    title: 'Logbook Types',
                    route: 'logbooktypes',
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showLogbookTypes',
                    items: {
                        create: {
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.LogForm'
                        },
                        edit: {
                            title: 'Edit',
                            route: 'edit/{id}',
                            controller: 'Mdc.controller.setup.LogForm'
                        }
                    }
                },
                devicetypes: {
                    title: 'Device Types',
                    route: 'devicetypes',
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showDeviceTypes',
                    items: {
                        create: {
                            title: 'Add',
                            route: 'add',
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeCreateView'
                        },
                        view: {
                            callback: function(route) {
                                var ctrl = this.getController(route.controller);

                                ctrl.on('loadDeviceType', function(record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            route: '{id}',
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeDetailsView'
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeEditView'
                        },
                        logbooktypes: {
                            title: 'Logbook Types',
                            route: '{id}/logbooktypes',
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeLogbookTypesView',
                            items: {
                                add: {
                                    title: 'Add logbook type',
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    action: 'showAddLogbookTypesView'
                                }
                            }
                        },
                        loadprofiles: {
                            title: 'Load Profiles',
                            route: '{id}/loadprofiles',
                            controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                            action: 'showDeviceTypeLoadProfileTypesView',
                            items: {
                                add: {
                                    title: 'Add Load Profile',
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                    action: 'showDeviceTypeLoadProfileTypesAddView'
                                }
                            }
                        },
                        deviceconfigurations: {
                            title: 'Device Configurations',
                            route: '{deviceTypeId}/deviceconfigurations',
                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                            action: 'showDeviceConfigurations',
                            items: {
                                create: {
                                    title: 'Create',
                                    route: 'create',
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    action: 'showDeviceConfigurationCreateView'
                                },
                                edit: {
                                    title: 'Edit',
                                    route: '{id}/edit',
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    action: 'showDeviceConfigurationEditView'
                                },
                                view: {
                                    title: 'Details',
                                    route: '{deviceConfigurationId}',
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    action: 'showDeviceConfigurationDetailsView'
                                },
                                loadprofiles: {
                                    title: 'Load profiles',
                                    route: '{deviceConfigurationId}/loadprofiles',
                                    controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                    action: 'showDeviceConfigurationLoadProfilesView',
                                    items: {
                                        add: {
                                            title: 'Add Load Profile',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                            action: 'showDeviceConfigurationLoadProfilesAddView'
                                        },
                                        edit: {
                                            title: 'Add Load Profile',
                                            route: '{loadProfileConfigurationId}/edit',
                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                            action: 'showDeviceConfigurationLoadProfilesEditView'
                                        },
                                        view: {
                                            title: 'Load Profile',
                                            route: '{loadProfileConfigurationId}/channels',
                                            controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                            action: 'showDeviceConfigurationLoadProfilesConfigurationDetailsView',
                                            items: {
                                                add: {
                                                    title: 'Add Channel',
                                                    route: 'add',
                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsAddView'
                                                },
                                                edit: {
                                                    title: 'Edit Channel',
                                                    route: '{channelId}/edit',
                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsEditView'
                                                }
                                            }
                                        }
                                    }
                                },
                                logbookconfigurations: {
                                    title: 'Logbook configurations',
                                    route: '{deviceConfigurationId}/logbookconfigurations',
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    action: 'showDeviceConfigurationLogbooksView',
                                    items: {
                                        add: {
                                            title: 'Add Logbook configuration',
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            action: 'showAddDeviceConfigurationLogbooksView'
                                        },
                                        edit: {
                                            title: 'Edit',
                                            route: '{logbookConfigurationId}/edit',
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            action: 'showEditDeviceConfigurationLogbooksView'
                                        }
                                    }
                                },
                                //Register configuration routes
                                registerconfigurations: {
                                    title: 'Register configurations',
                                    route: '{deviceConfigurationId}/registerconfigurations',
                                    controller: 'Mdc.controller.setup.RegisterConfigs',
                                    action: 'showRegisterConfigs',
                                    items: {
                                        create: {
                                            title: 'Create',
                                            route: 'create',
                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                            action: 'showRegisterConfigurationCreateView'
                                        },
                                        edit: {
                                            title: 'Edit',
                                            route: '{registerConfigurationId}/edit',
                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                            action: 'showRegisterConfigurationEditView'
                                        }
                                    }
                                },
                                //Security settings routes
                                securitysettings: {
                                    title: 'Security settings',
                                    route: '{deviceConfigurationId}/securitysettings',
                                    controller: 'Mdc.controller.setup.SecuritySettings',
                                    action: 'showSecuritySettings',
                                    items: {
                                        create: {
                                            title: 'Create',
                                            route: 'create',
                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                            action: 'showSecuritySettingsCreateView'
                                        },
                                        edit: {
                                            title: 'Edit',
                                            route: '{securitySettingId}/edit',
                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                            action: 'showSecuritySettingsEditView'
                                        }
                                    }
                                },
                                //Communication tasks routes
                                comtaskenablements: {
                                    title: 'Communication tasks',
                                    route: '{deviceConfigurationId}/comtaskenablements',
                                    controller: 'Mdc.controller.setup.CommunicationTasks',
                                    action: 'showCommunicationTasks',
                                    items: {
                                        create: {
                                            title: 'Create',
                                            route: 'create',
                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                            action: 'showAddCommunicationTaskView'
                                        },
                                        edit: {
                                            title: 'Edit',
                                            route: '{comTaskEnablementId}/edit',
                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                            action: 'showEditCommunicationTaskView'
                                        }
                                    }
                                },
                                //connection methods routes
                                connectionmethods: {
                                    title: 'Connection methods',
                                    route: '{deviceConfigurationId}/connectionmethods',
                                    controller: 'Mdc.controller.setup.ConnectionMethods',
                                    action: 'showConnectionMethods',
                                    items: {
                                        addoutbound: {
                                            title: 'Add outbound',
                                            route: 'addoutbound',
                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                            action: 'showAddConnectionMethodView',
                                            params: {
                                                'type': 'Outbound'
                                            }
                                        },
                                        addinbound: {
                                            title: 'Add inbound',
                                            route: 'addinbound',
                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                            action: 'showAddConnectionMethodView',
                                            params: {
                                                'type': 'Inbound'
                                            }
                                        },
                                        edit: {
                                            title: 'Edit',
                                            route: '{connectionMethodId}/edit',
                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                            action: 'showConnectionMethodEditView'
                                        }
                                    }
                                },
                                //protocol dialects routes
                                protocols: {
                                    title: 'Protocol dialects',
                                    route: '{deviceConfigurationId}/protocols',
                                    controller: 'Mdc.controller.setup.ProtocolDialects',
                                    action: 'showProtocolDialectsView',
                                    items: {
                                        edit: {
                                            title: 'Edit',
                                            route: '{protocolDialectId}/edit',
                                            controller: 'Mdc.controller.setup.ProtocolDialects',
                                            action: 'showProtocolDialectsEditView'
                                        }
                                    }
                                }
                            }
                        },
                        registertypes: {
                            title: 'Register types',
                            route: '{id}/registertypes',
                            controller: 'Mdc.controller.setup.RegisterMappings',
                            action: 'showRegisterMappings',
                            items: {
                                add: {
                                    title: 'Add Register mapping',
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.RegisterMappings',
                                    action: 'addRegisterMappings'
                                }
                            }
                        }
                    }
                },
                loadprofiletypes: {
                    title: 'Load Profile Types',
                    route: 'loadprofiletypes',
                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                    action: 'showLoadProfileTypes',
                    items: {
                        create: {
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            action: 'showLoadProfileTypesCreateView',
                            items: {
                                addmeasurementtypes: {
                                    title: 'Add Measurement Types',
                                    route: 'addmeasurementtypes',
                                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                                    action: 'showMeasurementTypesAddView'
                                }
                            }
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            action: 'showLoadProfileTypesEditView',
                            items: {
                                addmeasurementtypes: {
                                    title: 'Add Measurement Types',
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
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showComServers',
                    items: {
                        create: {
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.ComServers',
                            action: 'showEditView'
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.ComServers',
                            action: 'showEditView'
                        }
                    }
                },
                devicecommunicationprotocols: {
                    title: 'Device Communication protocols',
                    route: 'devicecommunicationprotocols',
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showDeviceCommunicationProtocols',
                    items: {
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.DeviceCommunicationProtocols',
                            action: 'showDeviceCommunicationProtocolEditView'
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
                    action: 'showComPortPools',
                    items: {
                        create: {
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.ComPortPools',
                            action: 'showEditView'
                        },
                        view: {
                            title: 'View',
                            route: '{id}',
                            controller: 'Mdc.controller.setup.ComPortPools',
                            action: 'showEditView'
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
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeCreateView'
                        },
                        view: {
                            title: 'View',
                            route: '{id}',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeDetailsView'
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeEditView'
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
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            action: 'showRegisterGroupCreateView'
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            action: 'showRegisterGroupEditView'
                        }
                    }
                },
                communicationtasks: {
                    title: 'Communication tasks',
                    route: 'communicationtasks',
                    controller: 'Mdc.controller.setup.CommunicationTasksView',
                    action: 'showCommunicationTasksView',
                    items: {
                        create: {
                            title: 'Create communication task',
                            route: 'create',
                            controller: 'Mdc.controller.setup.CommunicationTasksCreateEdit',
                            action: 'showCommunicationTasksCreateEdit'
                        },
                        edit: {
                            title: 'Edit communication task',
                            route: '{id}',
                            controller: 'Mdc.controller.setup.CommunicationTasksCreateEdit',
                            action: 'showCommunicationTasksCreateEdit'
                        }
                    }
                },
                communicationschedules: {
                    title: 'Communication schedules',
                    route: 'communicationschedules',
                    controller: 'Mdc.controller.setup.CommunicationSchedules',
                    action: 'showCommunicationSchedules',
                    items: {
                        create: {
                            title: 'Create',
                            route: 'create',
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            action: 'showCommunicationSchedulesEditView'
                        },
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            action: 'showCommunicationSchedulesEditView'
                        }
                    }
                },
                search: {
                    title: 'Search',
                    route: 'searchitems',
                    controller: 'Mdc.controller.setup.SearchItems',
                    action: 'showSearchItems'
                }
            }
        },
        device: {
            title: 'Device',
            route: 'devices/{id}',
            controller: 'Mdc.controller.setup.Devices',
            action: 'showDeviceDetailsView'
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