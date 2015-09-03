Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        usagepoints: {
            title: Uni.I18n.translate('general.usagePoints', 'MDC', 'Usage points'),
            route: 'usagepoints',
            items: {
                usagepoint: {
                    title: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
                    route: '{usagePointMRID}',
                    controller: 'Mdc.usagepointmanagement.controller.UsagePoint',
                    action: 'showUsagePoint',
                    callback: function (route) {
                        this.getApplication().on('usagePointLoaded', function (record) {
                            route.setTitle(record.get('mRID'));
                            return true;
                        }, {single: true});

                        return this;
                    }
                }
            }
        },
        administration: {
            title: Uni.I18n.translate('general.Administration', 'MDC', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                logbooktypes: {
                    title:  Uni.I18n.translate('general.logBookTypes', 'MDC', 'Logbook types'),
                    route: 'logbooktypes',
                    privileges: Mdc.privileges.MasterData.view,
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showLogbookTypes',
                    items: {
                        create: {
                            title: Uni.I18n.translate('general.addLogbookType', 'MDC', 'Add logbook type'),
                            route: 'add',
                            privileges: Mdc.privileges.MasterData.admin,
                            controller: 'Mdc.controller.setup.LogbookTypes',
                            action: 'showLogbookTypeCreateView'
                        },
                        edit: {
                            title: Uni.I18n.translate('general.editLogbookType','MDC','Edit logbook type'),
                            route: '{id}/edit',
                            privileges: Mdc.privileges.MasterData.admin,
                            controller: 'Mdc.controller.setup.LogbookTypes',
                            action: 'showLogbookTypeEditView',
                            callback: function (route) {
                                this.getApplication().on('loadLogbookType', function (record) {
                                    route.setTitle('Edit ' + " '" + record.get('name') + "'");
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                devicetypes: {
                    title: Uni.I18n.translate('general.deviceTypes','MDC','Device types'),
                    route: 'devicetypes',
                    privileges: Mdc.privileges.DeviceType.view,
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showDeviceTypes',
                    items: {
                        create: {
                            title: Uni.I18n.translate('general.addDeviceType','MDC','Add device type'),
                            route: 'add',
                            privileges: Mdc.privileges.DeviceType.admin,
                            controller: 'Mdc.controller.setup.DeviceTypes',
                            action: 'showDeviceTypeCreateView'
                        },
                        view: {
                            title: Uni.I18n.translate('general.Overview','MDC','Overview'),
                            route: '{deviceTypeId}',
                            privileges: Mdc.privileges.DeviceType.view,
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
                                change: {
                                    title: Uni.I18n.translate('deviceLifeCycle.change', 'MDC', 'Change device life cycle'),
                                    route: 'change',
                                    controller: 'Mdc.controller.setup.ChangeDeviceLifeCycle',
                                    action: 'showChangeDeviceLifeCycle'
                                },
                                edit: {
                                    title: Uni.I18n.translate('general.edit','MDC','Edit'),
                                    route: 'edit',
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    action: 'showDeviceTypeEditView'
                                },
                                logbooktypes: {
                                    title: Uni.I18n.translate('general.logbookTypes','MDC','Logbook types'),
                                    route: 'logbooktypes',
                                    privileges: Mdc.privileges.DeviceType.view,
                                    controller: 'Mdc.controller.setup.DeviceTypes',
                                    action: 'showDeviceTypeLogbookTypesView',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.addLogbookTypes','MDC','Add logbook types'),
                                            route: 'add',
                                            privileges: Mdc.privileges.DeviceType.admin,
                                            controller: 'Mdc.controller.setup.DeviceTypes',
                                            action: 'showAddLogbookTypesView'
                                        }
                                    }
                                },
                                loadprofiles: {
                                    title: Uni.I18n.translate('general.loadProfileTypes','MDC','Load profile types'),
                                    route: 'loadprofiles',
                                    privileges: Mdc.privileges.DeviceType.view,
                                    controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                    action: 'showDeviceTypeLoadProfileTypesView',
                                    items: {
                                        add: {
                                            title: 'Add load profiles',
                                            route: 'add',
                                            privileges: Mdc.privileges.DeviceType.admin,
                                            controller: 'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
                                            action: 'showDeviceTypeLoadProfileTypesAddView'
                                        }
                                    }
                                },
                                deviceconfigurations: {
                                    title: Uni.I18n.translate('general.deviceConfigurations','MDC','Device configurations'),
                                    route: 'deviceconfigurations',
                                    privileges: Mdc.privileges.DeviceType.view,
                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                    action: 'showDeviceConfigurations',
                                    items: {
                                        create: {
                                            title: Uni.I18n.translate('general.addDeviceConfiguration','MDC','Add device configuration'),
                                            route: 'add',
                                            privileges: Mdc.privileges.DeviceType.admin,
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            action: 'showDeviceConfigurationCreateView'
                                        },
                                        view: {
                                            title: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                                            route: '{deviceConfigurationId}',
                                            privileges: Mdc.privileges.DeviceType.view,
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
                                                    title: Uni.I18n.translate('genral.edit','MDC','Edit'),
                                                    route: 'edit',
                                                    privileges: Mdc.privileges.DeviceType.admin,
                                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                    action: 'showDeviceConfigurationEditView'
                                                },
                                                generalattributes: {
                                                    title: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                                                    route: 'generalattributes',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.GeneralAttributes',
                                                    action: 'showGeneralAttributesView',
                                                    items: {
                                                        edit: {
                                                            title: Uni.I18n.translate('deviceconfiguration.generalAttributes.edit', 'MDC', 'Edit general attributes'),
                                                            route: 'edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.GeneralAttributes',
                                                            action: 'showEditGeneralAttributesView'
                                                        }
                                                    }
                                                },

                                                loadprofiles: {
                                                    title: Uni.I18n.translate('loadProfileConfigurations.title', 'MDC', 'Load profile configurations'),
                                                    route: 'loadprofiles',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                    action: 'showDeviceConfigurationLoadProfilesView',
                                                    items: {
                                                        add: {
                                                            title: Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration'),
                                                            route: 'add',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                            action: 'showDeviceConfigurationLoadProfilesAddView'
                                                        },
                                                        edit: {
                                                            title: Uni.I18n.translate('loadProfileConfigurations.edit', 'MDC', 'Edit load profile configuration'),
                                                            route: '{loadProfileConfigurationId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.LoadProfileConfigurations',
                                                            action: 'showDeviceConfigurationLoadProfilesEditView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadLoadProfile', function (record) {
                                                                    route.setTitle('Edit' + " '" + record.name + "'");
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        },
                                                        channels: {
                                                            title: Uni.I18n.translate('general.loadProfileConfiguration', 'MDC', 'Load profile configuration'),
                                                            route: '{loadProfileConfigurationId}/channels',
                                                            privileges: Mdc.privileges.DeviceType.view,
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
                                                                    title: Uni.I18n.translate('general.addChannelConfiguration','MDC','Add channel configuration'),
                                                                    route: 'add',
                                                                    privileges: Mdc.privileges.DeviceType.admin,
                                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsAddView'
                                                                },
                                                                edit: {
                                                                    title: Uni.I18n.translate('general.editChannelConfiguration','MDC','Edit channel configuration'),
                                                                    route: '{channelId}/edit',
                                                                    privileges: Mdc.privileges.DeviceType.admin,
                                                                    controller: 'Mdc.controller.setup.LoadProfileConfigurationDetails',
                                                                    action: 'showDeviceConfigurationLoadProfilesConfigurationChannelsEditView'
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                logbookconfigurations: {
                                                    title: Uni.I18n.translate('general.logbookConfigurations','MDC','Logbook configurations'),
                                                    route: 'logbookconfigurations',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                    action: 'showDeviceConfigurationLogbooksView',
                                                    items: {
                                                        add: {
                                                            title: Uni.I18n.translate('general.addLogbookConfiguration','MDC','Add logbook configuration'),
                                                            route: 'add',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                            action: 'showAddDeviceConfigurationLogbooksView'
                                                        },
                                                        edit: {
                                                            title: Uni.I18n.translate('general.editLogbookConfiguration','MDC','Edit logbook configuration'),
                                                            route: '{logbookConfigurationId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                                            action: 'showEditDeviceConfigurationLogbooksView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadLogbooksConfiguration', function (record) {
                                                                    route.setTitle('Edit' + " '" + record.getValue() + "'");
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        }
                                                    }
                                                },
                                                //Register configuration routes
                                                registerconfigurations: {
                                                    title: Uni.I18n.translate('general.registerConfigurations','MDC','Register configurations'),
                                                    route: 'registerconfigurations',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.RegisterConfigs',
                                                    action: 'showRegisterConfigs',
                                                    items: {
                                                        create: {
                                                            title: Uni.I18n.translate('general.addRegisterConfiguration','MDC','Add register configuration'),
                                                            route: 'add',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                                            action: 'showRegisterConfigurationCreateView'
                                                        },
                                                        edit: {
                                                            title: Uni.I18n.translate('general.editRegisterConfiguration','MDC','Edit register configuration'),
                                                            route: '{registerConfigurationId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.RegisterConfigs',
                                                            action: 'showRegisterConfigurationEditView',
                                                            callback: function (route) {
                                                                this.getApplication().on('loadRegisterConfiguration', function (record) {
                                                                    route.setTitle('Edit' + " '" + record.get('readingType').fullAliasName + "'");
                                                                    return true;
                                                                }, {single: true});

                                                                return this;
                                                            }
                                                        }
                                                    }
                                                },
                                                //Security settings routes
                                                securitysettings: {
                                                    title: Uni.I18n.translate('securitySetting.title', 'MDC', 'Security settings'),
                                                    route: 'securitysettings',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.SecuritySettings',
                                                    action: 'showSecuritySettings',
                                                    items: {
                                                        create: {
                                                            title: Uni.I18n.translate('securitySetting.addSecuritySetting', 'MDC', 'Add security setting'),
                                                            route: 'add',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            action: 'showSecuritySettingsCreateView'
                                                        },
                                                        edit: {
                                                            title: Uni.I18n.translate('securitySetting.edit', 'MDC', 'Edit security setting'),
                                                            route: '{securitySettingId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            action: 'showSecuritySettingsEditView'
                                                        },
                                                        executionLevels: {
                                                            title: Uni.I18n.translate('executionlevels.addExecutionLevels', 'MDC', 'Add privileges'),
                                                            route: '{securitySettingId}/privileges/add',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.SecuritySettings',
                                                            action: 'showAddExecutionLevelsView'
                                                        }
                                                    }
                                                },
                                                //Communication tasks routes
                                                comtaskenablements: {
                                                    title: Uni.I18n.translate('general.communicationTaskConfigurations','MDC','Communication task configurations'),
                                                    route: 'comtaskenablements',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.CommunicationTasks',
                                                    action: 'showCommunicationTasks',
                                                    items: {
                                                        create: {
                                                            title: Uni.I18n.translate('addCommunicationTaskConfiguration','MDC','Add communication task configuration'),
                                                            route: 'add',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.CommunicationTasks',
                                                            action: 'showAddCommunicationTaskView'
                                                        },
                                                        edit: {
                                                            title: Uni.I18n.translate('general.EditCommunicationTaskConfiguration','MDC','Edit communication task configuration'),
                                                            route: '{comTaskEnablementId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
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
                                                    title: Uni.I18n.translate('general.connectionMethods','MDC','Connection methods'),
                                                    route: 'connectionmethods',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.ConnectionMethods',
                                                    action: 'showConnectionMethods',
                                                    items: {
                                                        addoutbound: {
                                                            title: Uni.I18n.translate('general.addOutboundConnectionMethod','MDC','Add outbound connection method'),
                                                            route: 'addoutbound',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            action: 'showAddConnectionMethodView',
                                                            params: {
                                                                'type': 'Outbound'
                                                            }
                                                        },
                                                        addinbound: {
                                                            title: Uni.I18n.translate('general.addInboundConnectionMethod','MDC','Add inbound connection method'),
                                                            route: 'addinbound',
                                                            privileges: Mdc.privileges.DeviceType.admin,
                                                            controller: 'Mdc.controller.setup.ConnectionMethods',
                                                            action: 'showAddConnectionMethodView',
                                                            params: {
                                                                'type': 'Inbound'
                                                            }
                                                        },
                                                        edit: {
                                                            title: Uni.I18n.translate('general.editConnectionMethod','MDC','Edit connection method'),
                                                            route: '{connectionMethodId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
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
                                                    title: Uni.I18n.translate('general.protocolDialects','MDC','Protocol dialects'),
                                                    route: 'protocols',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.ProtocolDialects',
                                                    action: 'showProtocolDialectsView',
                                                    items: {
                                                        edit: {
                                                            title: Uni.I18n.translate('general.editProtocolDialect','MDC','Edit protocol dialect'),
                                                            route: '{protocolDialectId}/edit',
                                                            privileges: Mdc.privileges.DeviceType.admin,
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
                                                    title: Uni.I18n.translate('general.validationRulesSets','MDC','Validation rule sets'),
                                                    route: 'validationrulesets',
                                                    controller: 'Mdc.controller.setup.ValidationRuleSets',
                                                    privileges : Cfg.privileges.Validation.fineTuneOnDeviceConfiguration,
                                                    action: 'showValidationRuleSetsOverview',
                                                    items: {
                                                        add: {
                                                            title: Uni.I18n.translate('general.addValidationRuleSets','MDC','Add validation rule sets'),
                                                            route: 'add',
                                                            controller: 'Mdc.controller.setup.ValidationRuleSets',
                                                            privileges : Cfg.privileges.Validation.deviceConfiguration,
                                                            action: 'showAddValidationRuleSets'
                                                        }
                                                    }
                                                },
                                                //Estimation rule sets
                                                estimationrulesets: {
                                                    title: Uni.I18n.translate('general.estimationRuleSets', 'MDC', 'Estimation rule sets'),
                                                    route: 'estimationrulesets',
                                                    controller: 'Mdc.deviceconfigurationestimationrules.controller.RuleSets',
                                                    privileges : Mdc.privileges.DeviceConfigurationEstimations.view,
                                                    action: 'showEstimationRuleSets',
                                                    items: {
                                                        add: {
                                                            title: Uni.I18n.translate('estimationRuleSet.add', 'MDC', 'Add estimation rule sets'),
                                                            route: 'add',
                                                            controller: 'Mdc.deviceconfigurationestimationrules.controller.AddRuleSets',
                                                            privileges : Mdc.privileges.DeviceConfigurationEstimations.view,
                                                            action: 'showAddEstimationRuleSetsView'
                                                        }
                                                    }
                                                },
                                                //messages routes
                                                messages: {
                                                    title: Uni.I18n.translate('general.commands','MDC','Commands'),
                                                    route: 'messages',
                                                    privileges: Mdc.privileges.DeviceType.view,
                                                    controller: 'Mdc.controller.setup.Messages',
                                                    action: 'showMessagesOverview'
                                                }
                                            }
                                        },
                                        clone: {
                                            title: Uni.I18n.translate('general.clone','MDC','Clone'),
                                            route: '{deviceConfigurationId}/clone',
                                            privileges: Mdc.privileges.DeviceType.admin,
                                            controller: 'Mdc.controller.setup.DeviceConfigurations',
                                            action: 'showDeviceConfigurationCloneView',
                                            callback: function (route) {
                                                this.getApplication().on('loadDeviceConfiguration', function (record) {
                                                    route.setTitle(Uni.I18n.translate('cloneDeviceConfiguration.title',
                                                        'MDC', "Clone device configuration '{0}'", [record.get('name')]));
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            }
                                        }
                                    }
                                },
                                registertypes: {
                                    title: Uni.I18n.translate('general.registerTypes','MDC','Register types'),
                                    route: 'registertypes',
                                    privileges: Mdc.privileges.DeviceType.view,
                                    controller: 'Mdc.controller.setup.RegisterMappings',
                                    action: 'showRegisterMappings',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.addRegisterTypes','MDC','Add register types'),
                                            route: 'add',
                                            privileges: Mdc.privileges.DeviceType.admin,
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
                    title: Uni.I18n.translate('general.loadProfileTypes','MDC', 'Load profile types'),
                    route: 'loadprofiletypes',
                    privileges: Mdc.privileges.MasterData.view,
                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                    action: 'showLoadProfileTypes',
                    items: {
                        create: {
                            title: Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'),
                            route: 'add',
                            privileges: Mdc.privileges.MasterData.admin,
                            controller: 'Mdc.controller.setup.LoadProfileTypes',
                            action: 'showEdit',
                            items: {
                                addregistertypes: {
                                    title: Uni.I18n.translate('general.addRegisterTypes','MDC','Add register types'),
                                    route: 'addregistertypes',
                                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                                    action: 'showRegisterTypesAddView'
                                }
                            }
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit','MDC','Edit'),
                            route: '{id}/edit',
                            privileges: Mdc.privileges.MasterData.admin,
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
                                addregistertypes: {
                                    title: Uni.I18n.translate('general.addRegisterTypes','MDC','Add register types'),
                                    route: 'addregistertypes',
                                    controller: 'Mdc.controller.setup.LoadProfileTypes',
                                    action: 'showRegisterTypesAddView'
                                }
                            }
                        }
                    }
                },
                comservers: {
                    title: Uni.I18n.translate('general.communicationServers','MDC','Communication servers'),
                    route: 'comservers',
                    privileges:Mdc.privileges.Communication.view,
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showComServers',
                    items: {
                        onlineadd: {
                            title: Uni.I18n.translate('general.addOnlineCommunicationServer','MDC','Add online communication server'),
                            route: 'add/online',
                            privileges:Mdc.privileges.Communication.admin,
                            controller: 'Mdc.controller.setup.ComServerEdit',
                            action: 'showOnlineAddView'
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit','MDC','Edit'),
                            route: '{id}/edit',
                            privileges:Mdc.privileges.Communication.admin,
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
                            title: Uni.I18n.translate('general.overview','MDC','Overview'),
                            route: '{id}',
                            privileges:Mdc.privileges.Communication.view,
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
                                    title: Uni.I18n.translate('general.overview','MDC','Overview'),
                                    route: 'overview',
                                    privileges:Mdc.privileges.Communication.view,
                                    controller: 'Mdc.controller.setup.ComServerOverview',
                                    action: 'showOverview'
                                },
                                edit: {
                                    title: Uni.I18n.translate('general.edit','MDC','Edit'),
                                    route: 'edit_',
                                    privileges:Mdc.privileges.Communication.admin,
                                    controller: 'Mdc.controller.setup.ComServerEdit',
                                    action: 'showEditView'
                                },
                                comports: {
                                    title: Uni.I18n.translate('general.communicationPorts','MDC','Communication ports'),
                                    route: 'comports',
                                    privileges:Mdc.privileges.Communication.view,
                                    controller: 'Mdc.controller.setup.ComServerComPortsView',
                                    action: 'showView',
                                    items: {
                                        addInbound: {
                                            title: Uni.I18n.translate('general.addInboundPort','MDC','Add inbound communication port'),
                                            route: 'add/inbound',
                                            privileges:Mdc.privileges.Communication.admin,
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            action: 'showAddInbound'
                                        },
                                        addOutbound: {
                                            title: Uni.I18n.translate('general.addOutboundPort','MDC','Add outbound communication port'),
                                            route: 'add/outbound',
                                            privileges:Mdc.privileges.Communication.admin,
                                            controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                            action: 'showAddOutbound',
                                            items: {
                                                addComPortPool: {
                                                    title: Uni.I18n.translate('general.addCommunicationPortPool','MDC','Add communication port pool'),
                                                    route: 'addPool',
                                                    privileges:Mdc.privileges.Communication.admin,
                                                    controller: 'Mdc.controller.setup.ComServerComPortsEdit',
                                                    action: 'showAddComPortPool'
                                                }
                                            }
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('general.editCommunicationPort','MDC','Edit communication port'),
                                            route: '{direction}/{comPortId}/edit',
                                            privileges:Mdc.privileges.Communication.admin,
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
                                                    title: Uni.I18n.translate('general.addCommunicationPortPool','MDC','Add communication port pool'),
                                                    route: 'addPool',
                                                    privileges:Mdc.privileges.Communication.admin,
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
                    title: Uni.I18n.translate('general.communicationProtocols','MDC','Communication protocols'),
                    route: 'devicecommunicationprotocols',
                    privileges:Mdc.privileges.Communication.view,
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showDeviceCommunicationProtocols',
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.editCommunicationProtocol','MDC', 'Edit communication protocol'),
                            route: '{id}/edit',
                            privileges:Mdc.privileges.Communication.admin,
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
                    title: Uni.I18n.translate('general.licensedProtocols','MDC','Licensed protocols'),
                    route: 'licensedprotocols',
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showLicensedProtocols'
                },
                comportpools: {
                    title: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                    route: 'comportpools',
                    privileges:Mdc.privileges.Communication.view,
                    controller: 'Mdc.controller.setup.SetupOverview',
                    action: 'showComPortPools',
                    items: {
                        addinbound: {
                            title: Uni.I18n.translate('comPortPool.title.addInbound', 'MDC', 'Add inbound communication port pool'),
                            route: 'add/inbound',
                            privileges:Mdc.privileges.Communication.admin,
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            action: 'showInboundAddView'
                        },
                        addoutbound: {
                            title: Uni.I18n.translate('comPortPool.title.addOutbound', 'MDC', 'Add outbound communication port pool'),
                            route: 'add/outbound',
                            privileges:Mdc.privileges.Communication.admin,
                            controller: 'Mdc.controller.setup.ComPortPoolEdit',
                            action: 'showOutboundAddView'
                        },
                        edit: {
                            title: Uni.I18n.translate('general.editCommunicationPortPool','MDC','Edit communication port pool'),
                            route: '{id}/edit',
                            privileges: Mdc.privileges.Communication.admin,
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
                            title: Uni.I18n.translate('general.detail','MDC','Detail'),
                            route: '{id}',
                            privileges: Mdc.privileges.Communication.view,
                            controller: 'Mdc.controller.setup.ComPortPoolOverview',
                            action: 'showOverview',
                            callback: function (route) {
                                this.getApplication().on('comPortPoolOverviewLoad', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit','MDC','Edit'),
                                    route: 'edit_',
                                    privileges: Mdc.privileges.Communication.admin,
                                    controller: 'Mdc.controller.setup.ComPortPoolEdit',
                                    action: 'showEditView'
                                },
                                comports: {
                                    title: Uni.I18n.translate('general.communicationPorts','MDC','Communication ports'),
                                    route: 'comports',
                                    privileges: Mdc.privileges.Communication.view,
                                    controller: 'Mdc.controller.setup.ComPortPoolComPortsView',
                                    action: 'showView',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.addCommunicationPort','MDC','Add communication port'),
                                            route: 'add',
                                            privileges: Mdc.privileges.Communication.admin,
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
                    title: Uni.I18n.translate('general.registerTypes','MDC','Register types'),
                    route: 'registertypes',
                    privileges: Mdc.privileges.MasterData.view,
                    controller: 'Mdc.controller.setup.RegisterTypes',
                    action: 'showRegisterTypes',
                    items: {
                        create: {
                            title: Uni.I18n.translate('general.addRegisterType','MDC','Add register type'),
                            route: 'add',
                            privileges: Mdc.privileges.MasterData.admin,
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeCreateView'
                        },
                        edit: {
                            title: Uni.I18n.translate('general.editRegisterType','MDC','Edit register type'),
                            route: '{id}/edit',
                            privileges: Mdc.privileges.MasterData.admin,
                            controller: 'Mdc.controller.setup.RegisterTypes',
                            action: 'showRegisterTypeEditView',
                            callback: function (route) {
                                this.getApplication().on('loadRegisterType', function (record) {
                                    route.setTitle('Edit \'' + record.get('readingType').fullAliasName + '\'');
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }
                    }
                },
                registergroups: {
                    title: Uni.I18n.translate('general.registerGroups','MDC','Register groups'),
                    route: 'registergroups',
                    privileges: Mdc.privileges.MasterData.view,
                    controller: 'Mdc.controller.setup.RegisterGroups',
                    action: 'showRegisterGroups',
                    items: {
                        create: {
                            title: Uni.I18n.translate('registerGroup.add', 'MDC', 'Add register group'),
                            route: 'add',
                            privileges: Mdc.privileges.MasterData.admin,
                            controller: 'Mdc.controller.setup.RegisterGroups',
                            action: 'showRegisterGroupCreateView'
                        },
                        edit: {
                            title: Uni.I18n.translate('registerGroup.edit', 'MDC', 'Edit register group'),
                            route: '{id}/edit',
                            privileges: Mdc.privileges.MasterData.admin,
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
                    title: Uni.I18n.translate('general.communicationTasks','MDC','Communication tasks'),
                    route: 'communicationtasks',
                    privileges: Mdc.privileges.Communication.view,
                    controller: 'Mdc.controller.setup.Comtasks',
                    action: 'showCommunicationTasksView',
                    items: {
                        create: {
                            title: Uni.I18n.translate('general.addCommunicationTask','MDC','Add communication task'),
                            route: 'add',
                            privileges: Mdc.privileges.Communication.admin,
                            controller: 'Mdc.controller.setup.Comtasks',
                            action: 'showCommunicationTasksCreateEdit'
                        },
                        edit: {
                            title: Uni.I18n.translate('general.editCommunicationTask','MDC','Edit communication task'),
                            route: '{id}/edit',
                            privileges: Mdc.privileges.Communication.admin,
                            controller: 'Mdc.controller.setup.Comtasks',
                            action: 'showCommunicationTasksCreateEdit',
                            callback: function (route) {
                                this.getApplication().on('loadCommunicationTask', function (record) {
                                    route.setTitle('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]);
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                },
                datacollectionkpis: {
                    title: Uni.I18n.translate('general.dataCollectionKpis', 'MDC', 'Data collection KPIs'),
                    route: 'datacollectionkpis',
                    controller: 'Mdc.controller.setup.DataCollectionKpi',
                    action: 'showDataCollectionKpiView',
                    items: {
                        add: {
                            title: Uni.I18n.translate('datacollectionkpis.add', 'MDC', 'Add data collection KPI'),
                            route: 'add',
                            controller: 'Mdc.controller.setup.DataCollectionKpi',
                            action: 'showDataCollectionKpiEditView'
                        },
                        edit: {
                            title: Uni.I18n.translate('datacollectionkpis.editDataCollectionKpi', 'MDC', 'Edit data collection KPI'),
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.DataCollectionKpi',
                            action: 'showDataCollectionKpiEditView',
                            callback: function (route) {
                                this.getApplication().on('loadDataCollectionKpi', function (title) {
                                    route.setTitle(title);
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        }
                    }
                },
                communicationschedules: {
                    title: Uni.I18n.translate('general.sharedCommunicationSchedules','MDC','Shared communication schedules'),
                    route: 'communicationschedules',
                    privileges: Mdc.privileges.CommunicationSchedule.view,
                    controller: 'Mdc.controller.setup.CommunicationSchedules',
                    action: 'showCommunicationSchedules',
                    items: {
                        create: {
                            title: Uni.I18n.translate('general.addSharedCommunicationSchedule','MDC', 'Add shared communication schedule'),
                            route: 'add',
                            privileges: Mdc.privileges.CommunicationSchedule.admin,
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            action: 'showCommunicationSchedulesEditView'
                        },
                        edit: {
                            title:  Uni.I18n.translate('general.editSharedCommunicationSchedule','MDC', 'Edit shared communication schedule'),
                            route: '{id}/edit',
                            controller: 'Mdc.controller.setup.CommunicationSchedules',
                            privileges: Mdc.privileges.CommunicationSchedule.admin,
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
            title: Uni.I18n.translate('general.devices','MDC','Devices'),
            route: 'devices',
            disabled: true,
            items: {
                devicegroups: {
                    title: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device groups'),
                    route: 'devicegroups',
                    controller: 'Mdc.controller.setup.DeviceGroups',
                    privileges: Mdc.privileges.DeviceGroup.view,
                    action: 'showDeviceGroups',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addDeviceGroup','MDC','Add device group'),
                            route: 'add',
                            controller: 'Mdc.controller.setup.AddDeviceGroupAction',
                            privileges: Mdc.privileges.DeviceGroup.view,
                            action: 'showAddDeviceGroupAction'
                        },
                        view: {
                            title: Uni.I18n.translate('general.overview','MDC','Overview'),
                            route: '{deviceGroupId}',
                            controller: 'Mdc.controller.setup.DeviceGroups',
                            privileges: Mdc.privileges.DeviceGroup.view,
                            action: 'showDevicegroupDetailsView',
                            callback: function (route) {
                                this.getApplication().on('loadDeviceGroup', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                    route: 'edit',
                                    controller: 'Mdc.controller.setup.AddDeviceGroupAction',
                                    privileges: Mdc.privileges.DeviceGroup.view,
                                    action: 'showEditDeviceGroup',
                                    filter: 'Mdc.model.DeviceFilter'
                                }
                            }
                        }
                    }
                },
                add: {
                    title: Uni.I18n.translate('deviceAdd.title', 'MDC', 'Add device'),
                    route: 'add',
                    controller: 'Mdc.controller.setup.Devices',
                    privileges : Mdc.privileges.Device.addDevice,
                    action: 'showAddDevice'
                },
                device: {
                    title: Uni.I18n.translate('general.device','MDC','Device'),
                    route: '{mRID}',
                    controller: 'Mdc.controller.setup.Devices',
                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                    action: 'showDeviceDetailsView',
                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                    callback: function (route) {
                        this.getApplication().on('loadDevice', function (record) {
                            route.setTitle(record.get('mRID'));
                            return true;
                        }, {single: true});

                        return this;
                    },
                    items: {

                        history: {
                            title: Uni.I18n.translate('general.history', 'MDC', 'History'),
                            route: 'history',
                            controller: 'Mdc.controller.setup.DeviceHistory',
                            action: 'showDeviceHistory'
                        },
                        transitions: {
                            title: Uni.I18n.translate('general.transition', 'MDC', 'Transition'),
                            route: 'transitions/{transitionId}',
                            controller: 'Mdc.controller.setup.DeviceTransitionExecute',
                            action: 'showExecuteTransition',
                            callback: function (route) {
                                this.getApplication().on('loadDeviceTransition', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        commands: {
                            title: Uni.I18n.translate('general.commands','MDC','Commands'),
                            route: 'commands',
                            controller: 'Mdc.controller.setup.DeviceCommands',
                            privileges: Ext.Array.merge(Mdc.privileges.Device.deviceOperator, Mdc.privileges.DeviceCommands.executeCommands),
                            action: 'showOverview',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.all,
                            items: {
                                add: {
                                    title: Uni.I18n.translate('general.addCommand','MDC','Add command'),
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.DeviceCommands',
                                    privileges: Mdc.privileges.DeviceCommands.executeCommands,
                                    action: 'showAddOverview',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.all,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.allDeviceCommandPrivileges
                                }
                            }
                        },
                        //protocol dialects routes
                        protocols: {
                            title: Uni.I18n.translate('general.protocolDialects','MDC','Protocol dialects'),
                            route: 'protocols',
                            controller: 'Mdc.controller.setup.DeviceProtocolDialects',
                            privileges: Mdc.privileges.Device.deviceOperator,
                            action: 'showProtocolDialectsView',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.editProtocolDialect','MDC','Edit protocol dialect'),
                                    route: '{protocolDialectId}/edit',
                                    controller: 'Mdc.controller.setup.DeviceProtocolDialects',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    action: 'showProtocolDialectsEditView',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.protocolDialectsActions,
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
                            privileges: Mdc.privileges.Device.deviceOperator,
                            filter: 'Mdc.model.TopologyFilter',
                            action: 'showTopologyView'
                        },
                        generalattributes: {
                            title: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                            route: 'generalattributes',
                            controller: 'Mdc.controller.setup.DeviceGeneralAttributes',
                            privileges: Mdc.privileges.Device.deviceOperator,
                            action: 'showGeneralAttributesView',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('deviceconfiguration.generalAttributes.edit', 'MDC', 'Edit general attributes'),
                                    route: 'edit',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    controller: 'Mdc.controller.setup.DeviceGeneralAttributes',
                                    action: 'showEditGeneralAttributesView',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.generalAttributesActions
                                }
                            }
                        },
                        attributes: {
                            title: Uni.I18n.translate('devicemenu.deviceAttributes', 'MDC', 'Device attributes'),
                            route: 'attributes',
                            controller: 'Mdc.controller.setup.DeviceAttributes',
                            privileges: Mdc.privileges.Device.viewOrAdministrateDeviceData,
                            action: 'showDeviceAttributesView',
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                    route: 'edit',
                                    privileges: Mdc.privileges.Device.editDeviceAttributes,
                                    controller: 'Mdc.controller.setup.DeviceAttributes',
                                    action: 'showEditDeviceAttributesView'
                                }
                            }
                        },
                        connectionmethods: {
                            title: Uni.I18n.translate('general.connectionMethods','MDC','Connection methods'),
                            route: 'connectionmethods',
                            controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                            privileges: Mdc.privileges.Device.deviceOperator,
                            action: 'showDeviceConnectionMethods',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                addoutbound: {
                                    title: Uni.I18n.translate('general.addOutbound','MDC','Add outbound'),
                                    route: 'addoutbound',
                                    controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    action: 'showAddDeviceConnectionMethodView',
                                    params: {
                                        'type': 'Outbound'
                                    },
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
                                },
                                addinbound: {
                                    title: Uni.I18n.translate('general.addInbound','MDC','Add inbound'),
                                    route: 'addinbound',
                                    controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                                    action: 'showAddDeviceConnectionMethodView',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    params: {
                                        'type': 'Inbound'
                                    },
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
                                },
                                edit: {
                                    title: Uni.I18n.translate('general.editConnectionMethod','MDC','Edit connection method'),
                                    route: '{connectionMethodId}/edit',
                                    controller: 'Mdc.controller.setup.DeviceConnectionMethods',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    action: 'showDeviceConnectionMethodEditView',
                                    callback: function (route) {
                                        this.getApplication().on('loadConnectionMethod', function (record) {
                                            route.setTitle('Edit \'' + record.get('name') + '\'');
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions
                                },
                                history: {
                                    title: Uni.I18n.translate('general.showConnectionHistory','MDC','Show connection history'),
                                    route: '{connectionMethodId}/history',
                                    controller: 'Mdc.controller.setup.DeviceConnectionHistory',
                                    privileges: Mdc.privileges.Device.deviceOperator,
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
                                           title: Uni.I18n.translate('general.connectionLog','MDC','Connection log'),
                                           route: '{historyId}/viewlog',
                                           controller: 'Mdc.controller.setup.DeviceConnectionHistory',
                                           privileges: Mdc.privileges.Device.deviceOperator,
                                           action: 'showDeviceConnectionMethodHistoryLog',
                                           filter: 'Mdc.model.ConnectionLogFilter'
                                       }
                                    }
                                }
                            }
                        },
                        registers: {
                            title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                            route: 'registers',
                            controller: 'Mdc.controller.setup.DeviceRegisterConfiguration',
                            privileges: Mdc.privileges.Device.viewDeviceCommunication,
                            action: 'showDeviceRegisterConfigurationsView',
                            items: {
                                register: {
                                    route: '{registerId}',
                                    controller: 'Mdc.controller.setup.DeviceRegisterTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'initTabDeviceRegisterConfigurationDetailsView',
                                    callback: function (route) {
                                        this.getApplication().on('loadRegisterConfiguration', function (record) {
                                            route.setTitle(record.get('readingType').fullAliasName);
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                },
                                registerdata: {
                                    title: Uni.I18n.translate('general.registerData','MDC','Register data'),
                                    route: '{registerId}/data',
                                    controller: 'Mdc.controller.setup.DeviceRegisterTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'initTabShowDeviceRegisterDataView',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    callback: function (route) {
                                        this.getApplication().on('loadRegisterConfiguration', function (record) {
                                            route.setTitle(record.get('readingType').fullAliasName);
                                            return true;
                                        }, {single: true});

                                        return this;
                                    },
                                    items: {
                                        create: {
                                            title: Uni.I18n.translate('general.addReading', 'MDC', 'Add reading'),
                                            route: 'add',
                                            controller: 'Mdc.controller.setup.DeviceRegisterDataEdit',
                                            privileges: Mdc.privileges.Device.administrateDeviceData,
                                            action: 'showDeviceRegisterConfigurationDataAddView',
                                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('device.registerData.editReading', 'MDC', 'Edit reading'),
                                            route: '{timestamp}/edit',
                                            controller: 'Mdc.controller.setup.DeviceRegisterDataEdit',
                                            privileges: Mdc.privileges.Device.administrateDeviceData,
                                            action: 'showDeviceRegisterConfigurationDataEditView',
                                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                        }
                                    }
                                }

                            }
                        },
                        datavalidation: {
                            title: Uni.I18n.translate('general.validationConfiguration','MDC','Validation configuration'),
                            route: 'datavalidation',
                            controller: 'Mdc.controller.setup.DeviceDataValidation',
                            privileges : Cfg.privileges.Validation.fineTuneOnDevice,
                            action: 'showDeviceDataValidationMainView',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore
                        },
						validationresultsconfiguration: {
                            title: Uni.I18n.translate('general.validationResults','MDC','Validation results'),
                            route: 'validationresults/configuration',
                            controller: 'Mdc.controller.setup.DeviceValidationResults',
                            privileges: ['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDevice'],
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            action: 'showDeviceValidationResultsMainView',
							filter: 'Mdc.model.ValidationResultsDataFilter',
							params: {
								'activeTab': 0
                                    }
                        },
						validationresultsdata: {
                            title: Uni.I18n.translate('general.validationResults','MDC','Validation results'),
                            route: 'validationresults/data',
                            controller: 'Mdc.controller.setup.DeviceValidationResults',
                            privileges: ['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDevice'],
                            action: 'showDeviceValidationResultsMainView',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
							filter: 'Mdc.model.ValidationResultsDataFilter',
							params: {
								'activeTab': 1	
								}
                        },
						validationresultsdataruleset: {
                            title: Uni.I18n.translate('general.validationResults','MDC','Validation results'),
                            route: 'validationresults/data/ruleset/{ruleSetId}',
                            controller: 'Mdc.controller.setup.DeviceValidationResults',
                            privileges: ['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDevice'],
                            action: 'showDeviceValidationResultsMainView',
							filter: 'Mdc.model.ValidationResultsDataFilter',
							params: {
								'activeTab': 1	
								}
                        },
						validationresultsdataversion: {
                            title: Uni.I18n.translate('general.validationResults','MDC','Validation results'),
                            route: 'validationresults/data/ruleset/{ruleSetId}/version/{ruleSetVersionId}',
                            controller: 'Mdc.controller.setup.DeviceValidationResults',
                            privileges: ['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDevice'],
                            action: 'showDeviceValidationResultsMainView',
							filter: 'Mdc.model.ValidationResultsDataFilter',
							params: {
								'activeTab': 1	
								}
                        },
						validationresultsdatarule: {
                            title: Uni.I18n.translate('general.validationResults','MDC','Validation results'),
                            route: 'validationresults/data/ruleset/{ruleSetId}/version/{ruleSetVersionId}/rule/{ruleId}',
                            controller: 'Mdc.controller.setup.DeviceValidationResults',
                            privileges: ['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration.onDevice'],
                            action: 'showDeviceValidationResultsMainView',
                            filter: 'Mdc.model.ValidationResultsDataFilter',
                            params: {
                                'activeTab': 1
                            }
                        },
                        dataestimation: {
                            title: Uni.I18n.translate('general.dataEstimation', 'MDC', 'Data estimation'),
                            route: 'dataestimation',
                            controller: 'Mdc.controller.setup.DeviceDataEstimation',
                            action: 'showDeviceDataEstimationMainView',
                            privileges: Mdc.privileges.DeviceConfigurationEstimations.view,
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore
                        },
                        communicationschedules: {
                            title: Uni.I18n.translate('general.communicationPlanning','MDC','Communication planning'),
                            route: 'communicationplanning',
                            controller: 'Mdc.controller.setup.DeviceCommunicationSchedules',
                            privileges: Mdc.privileges.Device.deviceOperator,
                            action: 'showDeviceCommunicationScheduleView',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationPlanningPages,
                            items: {

                                add: {
                                    title: Uni.I18n.translate('general.addSharedCommunicationSchedules','MDC','Add shared communication schedules'),
                                    route: 'add',
                                    controller: 'Mdc.controller.setup.DeviceCommunicationSchedules',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    action: 'addSharedCommunicationSchedule',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationPlanningPages
                                }

                            }
                        },
                        communicationtasks: {
                            title: Uni.I18n.translate('general.communicationTasks','MDC','Communication tasks'),
                            route: 'communicationtasks',
                            controller: 'Mdc.controller.setup.DeviceCommunicationTasks',
                            privileges: Mdc.privileges.Device.deviceOperator,
                            action: 'showDeviceCommunicationTasksView',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                history: {
                                    title: Uni.I18n.translate('general.showCommunicationHistory','MDC', 'Show communication history'),
                                    route: '{comTaskId}/history',
                                    controller: 'Mdc.controller.setup.DeviceCommunicationTaskHistory',
                                    privileges: Mdc.privileges.Device.deviceOperator,
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
                                            title: Uni.I18n.translate('general.communicationLog','MDC','Communication log'),
                                            route: '{historyId}/viewlog',
                                            controller: 'Mdc.controller.setup.DeviceCommunicationTaskHistory',
                                            privileges: Mdc.privileges.Device.deviceOperator,
                                            action: 'showDeviceCommunicationTaskHistoryLog',
                                            filter: 'Mdc.model.DeviceComTaskLogFilter'
                                        }
                                    }
                                }
                            }
                        },
                        loadprofiles: {
                            title: Uni.I18n.translate('general.loadProfiles','MDC','Load profiles'),
                            route: 'loadprofiles',
                            controller: 'Mdc.controller.setup.DeviceLoadProfiles',
                            privileges: Mdc.privileges.Device.viewDeviceCommunication,
                            action: 'showView',
                            items: {
                                loadprofile: {
                                    title: Uni.I18n.translate('general.loadProfile','MDC','Load profile'),
                                    route: '{loadProfileId}',
                                    controller: 'Mdc.controller.setup.DeviceLoadProfileTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'initTabDeviceLoadProfileDetailsView',
                                    callback: function (route) {
                                        this.getApplication().on('loadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                loadprofiledata: {
                                    title: Uni.I18n.translate('general.loadProfileData','MDC','Load profile data'),
                                    route: '{loadProfileId}/graph',
                                    controller: 'Mdc.controller.setup.DeviceLoadProfileTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'initTabLoadProfileGraphView',
                                    callback: function (route) {
                                        this.getApplication().on('loadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                loadprofiletableData: {
                                    title: Uni.I18n.translate('general.loadProfileData','MDC','Load profile data'),
                                    route: '{loadProfileId}/table',
                                    controller: 'Mdc.controller.setup.DeviceLoadProfileTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'initTabLoadProfileDataView',
                                    callback: function (route) {
                                        this.getApplication().on('loadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                loadprofilevalidation: {
                                    title: Uni.I18n.translate('general.loadPrfileValidation','MDC','Load profile validation'),
                                    route: '{loadProfileId}/validation',
                                    callback: function (route) {
                                        this.getApplication().on('loadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                }
                            }
                        },
                        channels: {
                            title: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                            route: 'channels',
                            controller: 'Mdc.controller.setup.DeviceChannels',
                            privileges: Mdc.privileges.Device.viewDevice,
                            action: 'showOverview',
                            filter: 'Mdc.model.filter.DeviceChannelsFilter',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('routing.channel', 'MDC', 'Channel'),
                                    route: '{channelId}',
                                    controller: 'Mdc.controller.setup.DeviceChannelData',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'showSpecifications',
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                channeldata: {
                                    title: Uni.I18n.translate('routing.channelData', 'MDC', 'Channel data'),
                                    route: '{channelId}/data',
                                    controller: 'Mdc.controller.setup.DeviceChannelData',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'showData',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                channelvalidationblocks: {
                                    title: Uni.I18n.translate('routing.channelData', 'MDC', 'Channel data'),
                                    route: '{channelId}/validationblocks/{issueId}',
                                    controller: 'Mdc.controller.setup.DeviceChannelData',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    action: 'showValidationBlocks',
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                channelvalidation: {
                                    title: Uni.I18n.translate('routing.channelValidation', 'MDC', 'Channel validation'),
                                    route: '{channelId}/validation',
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                }
                            }
                        },
                        logbooks: {
                            title: Uni.I18n.translate('general.logbooks', 'MDC', 'Logbooks'),
                            route: 'logbooks',
                            controller: 'Mdc.controller.setup.DeviceLogbooks',
                            privileges: Mdc.privileges.Device.viewDeviceCommunication,
                            action: 'showView',
                            items: {
                                logbook: {
                                    title: Uni.I18n.translate('general.logbook', 'MDC', 'Logbook'),
                                    route: '{logbookId}',
                                    controller: 'Mdc.controller.setup.DeviceLogbookOverview',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'showOverview',
                                    redirect: 'devices/device/logbooks/logbookoverview',
                                    callback: function (route) {
                                        this.getApplication().on('logbookOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                logbookoverview: {
                                    title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                                    route: '{logbookId}/overview',
                                    controller: 'Mdc.controller.setup.DeviceLogBookTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'showOverview',
                                    callback: function (route) {
                                        this.getApplication().on('logbookOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                logbookdata: {
                                    title: Uni.I18n.translate('router.logbookData', 'MDC', 'Logbook data'),
                                    route: '{logbookId}/data',
                                    controller: 'Mdc.controller.setup.DeviceLogBookTab',
                                    privileges: Mdc.privileges.Device.viewDeviceCommunication,
                                    action: 'showData',
                                    filter: 'Mdc.model.LogbookOfDeviceDataFilter',
                                    callback: function (route) {
                                        this.getApplication().on('logbookOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                }
                            }
                        },
                        events: {
                            title: Uni.I18n.translate('general.events', 'MDC', 'Events'),
                            route: 'events',
                            controller: 'Mdc.controller.setup.DeviceEvents',
                            privileges: Mdc.privileges.Device.viewDeviceCommunication,
                            action: 'showOverview',
                            filter: 'Mdc.model.LogbookOfDeviceDataFilter'
                        },
                        securitysettings: {
                            title: Uni.I18n.translate('securitySetting.title', 'MDC', 'Security settings'),
                            route: 'securitysettings',
                            controller: 'Mdc.controller.setup.DeviceSecuritySettings',
                            privileges: Ext.Array.merge(Mdc.privileges.Device.deviceOperator, Mdc.privileges.DeviceSecurity.viewOrEditLevels),
                            action: 'showDeviceSecuritySettings',
                            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('securitySetting.edit', 'MDC', 'Edit security setting'),
                                    route: '{securitySettingId}/edit',
                                    controller: 'Mdc.controller.setup.DeviceSecuritySettings',
                                    privileges: Mdc.privileges.DeviceSecurity.viewOrEditLevels,
                                    action: 'showDeviceSecuritySettingEditView',
                                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.securitySettingsActions,
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
            title: Uni.I18n.translate('general.search','MDC','Search'),
            route: 'search',
            controller: 'Mdc.controller.setup.SearchItems',
            privileges: Mdc.privileges.Device.viewDeviceCommunication,
            action: 'showSearchItems',
            items: {
                bulkAction: {
                    title: Uni.I18n.translate('general.bulkAction','MDC','Bulk action'),
                    route: 'bulk',
                    controller: 'Mdc.controller.setup.SearchItemsBulkAction',
                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
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
        return this.tokenize([this.rootToken, 'comservers', 'add']);
    },

    tokenizeAddDeviceCommunicationProtocol: function () {
        return this.tokenize([this.rootToken, 'devicecommunicationprotocols', 'add']);
    },

    tokenizeAddComPortPool: function () {
        return this.tokenize([this.rootToken, 'comportpools', 'add']);
    }
});
