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
                            route: 'metrologyconfiguration',
                            controller: 'Imt.usagepointmanagement.controller.MetrologyConfigurationDetails',
                            action: 'showUsagePointMetrologyConfiguration',
                            privileges: Imt.privileges.UsagePoint.view,
                            items: {
                                activatemeters: {
                                    title: Uni.I18n.translate('general.label.editMeters', 'IMT', 'Edit meters'),
                                    route: 'activatemeters',
                                    controller: 'Imt.usagepointsetup.controller.MetrologyConfig',
                                    action: 'showActivateMeters',
                                    privileges: Imt.privileges.UsagePoint.admin
                                }
                            }
                        },
                        definemetrology: {
                            title: Uni.I18n.translate('general.label.definemetrologyconfiguration', 'IMT', 'Define metrology configuration'),
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'showWizard',
                            privileges: Imt.privileges.UsagePoint.admin,
                            route: 'metrologyconfiguration/define'
                        },
                        purpose: {
                            title: Uni.I18n.translate('general.label.purpose', 'IMT', 'Purpose'),
                            route: 'purpose/{purposeId}',
                            controller: 'Imt.purpose.controller.Purpose',
                            action: 'showOutputs',
                            privileges:
                                Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.view)
                            &&  Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view),
                            callback: function (route) {
                                var me = this;
                                this.getApplication().on('purposes-loaded', function (purposes) {
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
                                    route: 'output/{outputId}/:tab:',
                                    controller: 'Imt.purpose.controller.Purpose',
                                    action: 'showOutputDefaultTab',
                                    privileges:
                                        Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.view)
                                    &&  Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view),
                                    callback: function (route) {
                                        this.getApplication().on('output-loaded', function (output) {
                                            if (output) {
                                                route.setTitle(output.get('name'));
                                            }
                                            return true;
                                        }, {single: true});
                                        return this;
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
                                validation: {
                                    title: Uni.I18n.translate('usagepoint.dataValidation.validationConfiguration', 'IMT', 'Validation configuration'),
                                    route: 'validation/:tab:',
                                    controller: 'Imt.metrologyconfiguration.controller.ValidationConfiguration',
                                    action: 'showValidationConfiguration',
                                    privileges: Imt.privileges.MetrologyConfig.viewValidation,
	                                items: {
	                                	add: {
	                                        title: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
	                                        route: 'add',
	                                        controller: 'Imt.metrologyconfiguration.controller.ValidationConfiguration',
	                                        action: 'showAddValidationRuleSets',
                                            privileges: Imt.privileges.MetrologyConfig.adminValidation
	                                    }
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
                },
                usagepointlifecycles: {
                    title: Uni.I18n.translate('general.usagePointLifeCycles', 'IMT', 'Usage point life cycles'),
                    route: 'usagepointlifecycles',
                    controller: 'Imt.usagepointlifecycle.controller.UsagePointLifeCycles',
                    privileges: Imt.privileges.UsagePointLifeCycle.view,
                    action: 'showUsagePointLifeCycles',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addUsagePointLifeCycle', 'IMT', 'Add usage point life cycle'),
                            route: 'add',
                            controller: 'Imt.usagepointlifecycle.controller.UsagePointLifeCycles',
                            privileges: Imt.privileges.UsagePointLifeCycle.configure,
                            action: 'showAddUsagePointLifeCycle'
                        },
                        clone: {
                            title: Uni.I18n.translate('general.cloneUsagePointLifeCycle', 'IMT', 'Clone usage point life cycle'),
                            route: '{usagePointLifeCycleId}/clone',
                            controller: 'Imt.usagepointlifecycle.controller.UsagePointLifeCycles',
                            privileges: Imt.privileges.UsagePointLifeCycle.configure,
                            action: 'showCloneUsagePointLifeCycle',
                            callback: function (route) {
                                this.getApplication().on('usagepointlifecyclecloneload', function (recordName) {
                                    route.setTitle(Uni.I18n.translate('usagePointLifeCycles.clone.title', 'IMT', "Clone '{0}'", recordName, false));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        usagepointlifecycle: {
                            route: '{usagePointLifeCycleId}',
                            controller: 'Imt.usagepointlifecycle.controller.UsagePointLifeCycles',
                            privileges: Imt.privileges.UsagePointLifeCycle.view,
                            action: 'showUsagePointLifeCycleOverview',
                            callback: function (route) {
                                this.getApplication().on('usagepointlifecycleload', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                    route: 'edit',
                                    controller: 'Imt.usagepointlifecycle.controller.UsagePointLifeCycles',
                                    privileges: Imt.privileges.UsagePointLifeCycle.configure,
                                    action: 'showEditUsagePointLifeCycle',
                                    callback: function (route) {
                                        this.getApplication().on('usagePointLifeCycleEdit', function (record) {
                                            route.setTitle(Uni.I18n.translate('usagePointLifeCycles.edit.title', 'IMT', "Edit '{0}'", record.get('name'), false));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                states: {
                                    title: Uni.I18n.translate('general.states', 'IMT', 'States'),
                                    route: 'states',
                                    controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                    action: 'showUsagePointLifeCycleStates',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('usagePointLifeCycleStates.add', 'IMT', 'Add state'),
                                            route: 'add',
                                            controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                            action: 'showUsagePointLifeCycleStateEdit',
                                            items: {
                                                addEntryProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
                                                    route: 'entryprocesses',
                                                    controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                                    action: 'showAvailableEntryTransitionProcesses'
                                                },
                                                addExitProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
                                                    route: 'exitprocesses',
                                                    controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                                    action: 'showAvailableExitTransitionProcesses'
                                                }
                                            }
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('usagePointLifeCycleStates.edit', 'IMT', 'Edit state'),
                                            route: '{id}/edit',
                                            controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                            action: 'showUsagePointLifeCycleStateEdit',
                                            callback: function (route) {
                                                this.getApplication().on('loadlifecyclestate', function (record) {
                                                    route.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [record.get('name')], false));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            },
                                            items: {
                                                addEntryProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
                                                    route: 'entryprocesses',
                                                    controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                                    action: 'showAvailableEntryTransitionProcesses'
                                                },
                                                addExitProcesses: {
                                                    title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
                                                    route: 'exitprocesses',
                                                    controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                                    action: 'showAvailableExitTransitionProcesses'
                                                }
                                            }
                                        }
                                    }
                                },
                                /*transitions: {
                                    title: Uni.I18n.translate('general.transitions', 'IMT', 'Transitions'),
                                    route: 'transitions',
                                    controller: 'Imt.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                    action: 'showDeviceLifeCycleTransitions',
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.addTransition', 'IMT', 'Add transition'),
                                            route: 'add',
                                            controller: 'Imt.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                            action: 'showAddDeviceLifeCycleTransition'
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                            route: '{transitionId}/edit',
                                            controller: 'Imt.devicelifecycletransitions.controller.DeviceLifeCycleTransitions',
                                            action: 'showAddDeviceLifeCycleTransition',
                                            callback: function (route) {
                                                this.getApplication().on('deviceLifeCycleTransitionEdit', function (record) {
                                                    route.setTitle(Uni.I18n.translate('deviceLifeCycles.edit.title', 'IMT', "Edit '{0}'", [record.get('name')]));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            }
                                        }
                                    }
                                }*/
                            }
                        }
                    }
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
