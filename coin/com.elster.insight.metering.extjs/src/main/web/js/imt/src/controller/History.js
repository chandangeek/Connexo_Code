Ext.define('Imt.controller.History', {
    extend: 'Uni.controller.history.Converter',

    routeConfig: {
        usagepoints: {
            title: Uni.I18n.translate('general.label.usagepoints', 'IMT', 'Usage points'),
            route: 'usagepoints',
            disabled: true,
            items: {
            	add: {
                	title: Uni.I18n.translate('general.label.usagepoint.add', 'IMT', 'Add usage point'),
                    route: 'add',
                    privileges: Imt.privileges.UsagePoint.admin,
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'showWizard'
            	},
           		view: {
           			title: Uni.I18n.translate('general.label.usagepoint.view', 'IMT', 'View usage point'),
           			route: '{mRID}',
                    privileges: Imt.privileges.UsagePoint.view,
           			controller: 'Imt.usagepointmanagement.controller.View',
           			action: 'showUsagePoint',
           			callback: function (route) {
                        this.getApplication().on('usagePointLoaded', function (record) {
                            route.setTitle(record.get('mRID'));
                            return true;
                        }, {single: true});

                        return this;
                    },
           			items: {
                        //edit: {
                        //    title: Uni.I18n.translate('general.label.usagepoint.edit', 'IMT', 'Edit'),
                        //    route: 'edit',
                        //    controller: 'Imt.usagepointmanagement.controller.Edit',
                        //    action: 'editUsagePoint'
                        //},
                        attributes: {
                            title: Uni.I18n.translate('general.usagePointAttributes', 'IMT', 'Usage point attributes'),
                            route: 'attributes',
                            controller: 'Imt.usagepointmanagement.controller.Attributes',
                            action: 'showUsagePointAttributes'
                        },
                        history: {
                            title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                            route: 'history',
                            privileges: Imt.privileges.UsagePoint.view,
                            controller: 'Imt.usagepointhistory.controller.History',
                            action: 'showHistory',
                            items: {
                                customattributesversionsedit: {
                                    title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                    route: 'customattributes/{customAttributeSetId}/versions/{versionId}/edit',
                                    privileges: Imt.privileges.UsagePoint.admin,
                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStore,
                                    dynamicPrivilege: Imt.dynamicprivileges.UsagePoint.viable,
                                    controller: 'Imt.usagepointhistory.controller.CasVersionEdit',
                                    action: 'editCasVersion',
                                    callback: function (route) {
                                        this.getApplication().on('loadCasVersionOnUsagePoint', function (record) {
                                            route.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [record.get('period')], false));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                },
                                customattributesversionsadd: {
                                    title: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                    route: 'customattributes/{customAttributeSetId}/versions/add',
                                    privileges: Imt.privileges.UsagePoint.hasFullAdministrateTimeSlicedCps(),
                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStore,
                                    dynamicPrivilege: Imt.dynamicprivileges.UsagePoint.viable,
                                    controller: 'Imt.usagepointhistory.controller.CasVersionEdit',
                                    action: 'editCasVersion',
                                    callback: function (route) {
                                        this.getApplication().on('loadCasOnUsagePointAdd', function (record) {
                                            route.setTitle(Uni.I18n.translate('general.addxversion', 'IMT', "Add '{0}' version", [record.get('name')], false));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                },
                                customattributesversionsclone: {
                                    title: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                                    route: 'customattributes/{customAttributeSetId}/versions/{versionId}/clone',
                                    privileges: Imt.privileges.UsagePoint.hasFullAdministrateTimeSlicedCps(),
                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStore,
                                    dynamicPrivilege: Imt.dynamicprivileges.UsagePoint.viable,
                                    controller: 'Imt.usagepointhistory.controller.CasVersionEdit',
                                    action: 'cloneCustomAttributeVersion',
                                    callback: function (route) {
                                        this.getApplication().on('loadCustomAttributeSetVersionOnUsagePointClone', function (record) {
                                            route.setTitle(Uni.I18n.translate('general.clonex', 'IMT', "Clone '{0}'", [record.get('period')], false));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                }
                            }
                        },
                        'processes': {
                            title: Uni.I18n.translate('processes.title', 'IMT', 'Processes'),
                            route: 'processes',
                            controller: 'Imt.processes.controller.MonitorProcesses',
                            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                            action: 'showUsagePointProcesses'
                        },
                        'processesrunning': {
                            title: Uni.I18n.translate('processes.title', 'IMT', 'Processes'),
                            route: 'running',
                            controller: 'Imt.processes.controller.MonitorProcesses',
                            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                            action: 'showUsagePointProcesses'
                        },
                        'processeshistory': {
                            title: Uni.I18n.translate('processes.title', 'IMT', 'Processes'),
                            route: 'processes/history',
                            controller: 'Imt.processes.controller.MonitorProcesses',
                            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                            filter: 'Bpm.monitorprocesses.model.HistoryProcessesFilter',
                            action: 'showUsagePointProcesses'
                        },
                        'processstart': {
                            title: Uni.I18n.translate('processes.title', 'IMT', 'Processes'),
                            route: 'processes/start',
                            controller: 'Imt.processes.controller.MonitorProcesses',
                            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                            action: 'showUsagePointStartProcess'
                        },
                        servicecalls: {
                            title: Uni.I18n.translate('general.serviceCalls', 'IMT', 'Service calls'),
                            route: 'servicecalls',
                            controller: 'Imt.servicecalls.controller.ServiceCalls',
                            privileges: Imt.privileges.UsagePoint.view,
                            action: 'showServiceCalls',
                            items: {
                                history: {
                                    title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                                    route: 'history',
                                    privileges: Imt.privileges.UsagePoint.view,
                                    controller: 'Imt.servicecalls.controller.ServiceCalls',
                                    action: 'showServiceCallHistory'
                                }
                            }
                        },
                        channels: {
                            title: Uni.I18n.translate('general.channels', 'IMT', 'Channels'),
                            route: 'channels',
                            controller: 'Imt.channeldata.controller.Channels',
//                            privileges: Imt.privileges.UsagePoint.view,
                            action: 'showOverview',
                            filter: 'Imt.channeldata.model.ChannelsFilter',
//                            dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStateStore,
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('routing.channel', 'IMT', 'Channel'),
                                    route: '{channelId}',
                                    controller: 'Imt.channeldata.controller.ChannelData',
//                                    privileges: Imt.privileges.UsagePoint.view,
                                    action: 'showSpecifications',
                                    callback: function (route) {
                                        this.getApplication().on('channelLoaded', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        editcustomattributes: {
                                            route: 'customattributes/{customAttributeSetId}/edit',
                                            controller: 'Imt.channeldata.controller.ChannelData',
//                                            privileges: Imt.privileges.UsagePoint.admin,
                                            action: 'showEditChannelCustomAttributes',
                                            callback: function (route) {
                                                this.getApplication().on('channelCustomAttributesLoaded', function (record) {
                                                    route.setTitle(Uni.I18n.translate('channels.editCustomAttributes', 'IMT', "Edit '{0}'", record.get('name')));
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            }
                                        },
                                        customattributesversions: {
                                            title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                                            route: 'customattributes/{customAttributeSetId}/versions',
                                            controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                            privileges: Imt.privileges.UsagePoint.view,
                                            action: 'loadCustomAttributeVersions',
                                            callback: function (route) {
                                                this.getApplication().on('loadCustomAttributeSetOnChannel', function (record) {
                                                    route.setTitle(Uni.I18n.translate('channels.historyCustomAttributes', 'IMT', "'{0}' history", record.get('name')));
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            },
                                            items: {
                                                edit: {
                                                    title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                                    route: '{versionId}/edit',
                                                    controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                                    privileges: Imt.privileges.UsagePoint.admin,
                                                    action: 'editCustomAttributeVersion',
                                                    callback: function (route) {
                                                        this.getApplication().on('loadCustomAttributeSetVersionOnChannel', function (record) {
                                                            route.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", record.get('period')));
                                                            return true;
                                                        }, {single: true});

                                                        return this;
                                                    }
                                                },
                                                add: {
                                                    title: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                                    route: 'add',
                                                    controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                                    privileges: Imt.privileges.UsagePoint.admin,
                                                    action: 'addCustomAttributeVersion',
                                                    callback: function (route) {
                                                        this.getApplication().on('loadCustomAttributeSetOnChannelAdd', function (record) {
                                                            route.setTitle(Uni.I18n.translate('general.addxversion', 'IMT', "Add '{0}' version", record.get('name')));
                                                            return true;
                                                        }, {single: true});

                                                        return this;
                                                    }
                                                },
                                                clone: {
                                                    title: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                                                    route: '{versionId}/clone',
                                                    controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                                    privileges: Imt.privileges.UsagePoint.admin,
                                                    action: 'cloneCustomAttributeVersion',
                                                    callback: function (route) {
                                                        this.getApplication().on('loadCustomAttributeSetVersionOnChannelClone', function (record) {
                                                            route.setTitle(Uni.I18n.translate('general.clonex', 'IMT', "Clone '{0}'", record.get('period')));
                                                            return true;
                                                        }, {single: true});

                                                        return this;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                channeldata: {
                                    title: Uni.I18n.translate('routing.channelData', 'IMT', 'Channel data'),
                                    route: '{channelId}/data',
                                    controller: 'Imt.channeldata.controller.ChannelData',
//                                    privileges: Imt.privileges.UsagePoint.view,
                                    action: 'showData',
//                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStateStore,
                                    callback: function (route) {
                                        this.getApplication().on('channelLoaded', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                channelvalidationblocks: {
                                    title: Uni.I18n.translate('routing.channelData', 'IMT', 'Channel data'),
                                    route: '{channelId}/validationblocks/{issueId}',
                                    controller: 'Imt.channeldata.controller.ChannelData',
//                                    privileges: Imt.privileges.UsagePoint.view,
//                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStateStore,
                                    action: 'showValidationBlocks',
                                    callback: function (route) {
                                        this.getApplication().on('channelLoaded', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                channelvalidation: {
                                    title: Uni.I18n.translate('routing.channelValidation', 'IMT', 'Channel validation'),
                                    route: '{channelId}/validation',
                                    callback: function (route) {
                                        this.getApplication().on('channelLoaded', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                }
                            }
                        },
           			    registers: {
                            title: Uni.I18n.translate('general.label.usagepoint.registers', 'IMT', 'Registers'),
                            route: 'registers',
                            controller: 'Imt.registerdata.controller.View',
                            action: 'showUsagePointRegisters',
                            items: {
                            	register: {
                                    title: Uni.I18n.translate('general.label.usagepoint.register', 'IMT', 'Register'),
                                    route: '{registerId}',
                                    controller: 'Imt.registerdata.controller.ViewData',
                                    action: 'showRegisterSpecifications', //'showUsagePointRegisterData',
                                    callback: function (route) {
                                        this.getApplication().on('registerDataLoaded', function (record) {
                                            route.setTitle(record.get('readingType').fullAliasName);
                                            return true;
                                        }, {single: true});

                                        return this;
                                    },
                                    items: {
                                        create: {
                                            title: Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'),
                                            route: 'add',
                                            controller: 'Imt.registerdata.controller.EditData',
                                 //           privileges: Mdc.privileges.Device.administrateDeviceData,
                                            action: 'showRegisterDataAddView',
                                 //           dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                 //           dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('registerdata.editReading', 'IMT', 'Edit reading'),
                                            route: '{timestamp}/edit',
                                            controller: 'Imt.registerdata.controller.EditData',
                                  //          privileges: Mdc.privileges.Device.administrateDeviceData,
                                            action: 'showRegisterDataEditView',
                                  //          dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                                  //          dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                        }
                                    }
                                },
                                registerdata: {
                                    title: Uni.I18n.translate('routing.registerData', 'IMT', 'Register data'),
                                    route: '{registerId}/data',
                                    controller: 'Imt.registerdata.controller.ViewData',
//                                    privileges: Imt.privileges.UsagePoint.view,
                                    action: 'showUsagePointRegisterData',
//                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointStateStore,
                                    callback: function (route) {
                                        this.getApplication().on('registerdataLoaded', function (record) {
                                            route.setTitle(record.get('readingType').fullAliasName);
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                            }
           			    },
           			    datavalidation: {
                            title: Uni.I18n.translate('general.label.usagepoint.validation.configuration', 'IMT', 'Validation configuration'),
                            route: 'datavalidation',
                            controller: 'Imt.validation.controller.UsagePointDataValidation',
                            action: 'showUsagePointDataValidationMainView',
           			    },
                        device: {
                             title: Uni.I18n.translate('general.label.device.view', 'IMT', 'View device'),
                             route: 'device/{deviceMRID}',
                             controller: 'Imt.devicemanagement.controller.Device',
                             action: 'showDevice',
                             callback: function (route) {
                                 this.getApplication().on('deviceloaded', function (record) {
                                     route.setTitle('Device ' + record.get('name'));
                                     return true;
                                 }, {single: true});       
                                 return this;
                             } 
                        },
                        metrologyconfiguration: {
                           title: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                           route: 'metrologyconfiguration/{mcid}',
                           controller: 'Imt.usagepointmanagement.controller.View',
                           action: 'showMetrologyConfiguration',
                           callback: function (route) {
                               this.getApplication().on('metrologyConfigurationLoaded', function (record) {
                                   route.setTitle(record.get('name'));
                                   return true;
                               }, {single: true});       
                               return this;
                           }  
                        },
                        purpose: {
                            title: Uni.I18n.translate('general.label.purpose', 'IMT', 'Purpose'),
                            route: 'purpose/{purposeId}',
                            controller: 'Imt.purpose.controller.Purpose',
                            action: 'showOutputs',
                            callback: function (route) {
                                var me = this;
                                this.getApplication().on('purposesLoaded', function (purposes) {
                                    var purpose = _.find(purposes, function(p){return p.getId() == me.arguments.purposeId});
                                    if (purpose) {
                                        route.setTitle(purpose.get('name'));
                                    }
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                output: {
                                    title: Uni.I18n.translate('general.label.output', 'IMT', 'Output'),
                                    route: 'output/{outputId}',
                                    controller: 'Imt.purpose.controller.Purpose',
                                    action: 'showOutputDefaultTab',
                                    callback: function (route) {
                                        //var me = this;
                                        //this.getApplication().on('purposesLoaded', function (purposes) {
                                        //    var purpose = _.find(purposes, function(p){return p.getId() == me.arguments.purposeId});
                                        //    if (purpose) {
                                        //        route.setTitle(purpose.get('name'));
                                        //    }
                                        //    return true;
                                        //}, {single: true});
                                        //return this;
                                    }
                                }
                            }
                        }
           			}
           		},
           		device: {
                    title: Uni.I18n.translate('general.label.device.view', 'IMT', 'View device'),
                    route: 'device/{deviceMRID}',
                    controller: 'Imt.devicemanagement.controller.Device',
                    action: 'showDevice',
                    callback: function (route) {
                        this.getApplication().on('deviceloaded', function (record) {
                            route.setTitle('Device ' + record.get('name'));
                            return true;
                        }, {single: true});       
                        return this;
                    } 
               }
            }
        },
        administration: {
            title: Uni.I18n.translate('general.label.administration', 'IMT', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                metrologyconfiguration: {
                    title: Uni.I18n.translate('general.label.metrologyconfiguration.list', 'IMT', 'Metrology configurations'),
                    route: 'metrologyconfiguration',
                    controller: 'Imt.metrologyconfiguration.controller.ViewList',
                    action: 'showMetrologyConfigurationList',
                    privileges: Imt.privileges.MetrologyConfig.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.add', 'IMT', 'Add metrology configuration'),
                            route: 'add',
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'createMetrologyConfiguration'
                        },
                        view: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.view', 'IMT', 'View metrology configuration'),
                            route: '{mcid}',
                            controller: 'Imt.metrologyconfiguration.controller.View',
                            action: 'showMetrologyConfiguration',
                            privileges: Imt.privileges.MetrologyConfig.view,
                            callback: function (route) {
                                this.getApplication().on('metrologyConfigurationLoaded', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.label.metrologyconfiguration.edit', 'IMT', 'Edit'),
                                    route: 'edit',
                                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                                    action: 'editMetrologyConfiguration'
                                },
                                manage: {
                                    title: Uni.I18n.translate('general.label.metrologyconfiguration.valruleset.manage', 'IMT', 'Link validation rule sets'),
                                    route: 'manage',
                                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                                    action: 'manageValidationRuleSets'
                                },
                                customAttributeSets: {
                                    title: Uni.I18n.translate('metrologyconfiguration.label.CAS', 'IMT', 'Custom attribute sets'),
                                    route: 'custom-attribute-sets',
                                    controller: 'Imt.metrologyconfiguration.controller.View',
                                    action: 'showCustomAttributeSets',
                                    privileges: Imt.privileges.MetrologyConfig.view,
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('metrologyconfiguration.label.CAS.add', 'IMT', 'Add custom attribute sets'),
                                            route: 'add',
                                            controller: 'Imt.metrologyconfiguration.controller.View',
                                            action: 'showAddCustomAttributeSets',
                                            privileges: Imt.privileges.MetrologyConfig.admin
                                        }
                                    }
                                },
                                associatedvalidationrulesets: {
                                    title: Uni.I18n.translate('general.label.metrologyconfiguration.valruleset.associated', 'IMT', 'Linked validation rule sets'),
                                    route: 'associatedvalidationrulesets',
                                    controller: 'Imt.metrologyconfiguration.controller.ValidationRuleSets',
                                    action: 'showValidationRuleSetsOverview',
	                                items: {
	                                	addruleset: {
	                                        title: Uni.I18n.translate('general.label.metrologyconfiguration.edit', 'IMT', 'Add validation rule set'),
	                                        route: 'addruleset',
	                                        controller: 'Imt.metrologyconfiguration.controller.ValidationRuleSets',
	                                        action: 'showAddValidationRuleSets'
	                                    },
	                                }
                                }
                            }
                        }
                    }
                },
                servicecategories: {
                    title: Uni.I18n.translate('general.serviceCategories', 'IMT', 'Service categories'),
                    route: 'servicecategories',
                    controller: 'Imt.servicecategories.controller.ServiceCategories',
                    action: 'showOverview',
                    privileges: Imt.privileges.ServiceCategory.view
                }
            }
        },
        search: {
            title: Uni.I18n.translate('general.label.search','IMT','Search'),
            route: 'search',
            controller: 'Imt.controller.Search',
            action: 'showOverview'
        }
    }
});
