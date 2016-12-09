Ext.define('Imt.controller.History', {
    extend: 'Uni.controller.history.Converter',

    routeConfig: {
        usagepoints: {
            title: Uni.I18n.translate('general.label.usagepoints', 'IMT', 'Usage points'),
            route: 'usagepoints',
            disabled: true,
            items: {
                usagepointgroups: {
                    title: Uni.I18n.translate('general.usagePointGroups', 'IMT', 'Usage point groups'),
                    route: 'usagepointgroups',
                    controller: 'Imt.usagepointgroups.controller.UsagePointGroups',
                    privileges: Imt.privileges.UsagePointGroup.view,
                    action: 'showUsagePointGroups',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addUsagePointGroup', 'IMT', 'Add usage point group'),
                            route: 'add',
                            controller: 'Imt.usagepointgroups.controller.AddUsagePointGroupAction',
                            privileges: Imt.privileges.UsagePointGroup.view,
                            action: 'showWizard'
                        },
                        view: {
                            title: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                            route: '{usagePointGroupId}',
                            controller: 'Imt.usagepointgroups.controller.UsagePointGroups',
                            privileges: Imt.privileges.UsagePointGroup.view,
                            action: 'showUsagePointGroupDetailsView',
                            callback: function (route) {
                                this.getApplication().on('loadUsagePointGroup', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                edit: {
                                    title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                    route: 'edit',
                                    controller: 'Imt.usagepointgroups.controller.AddUsagePointGroupAction',
                                    privileges: Imt.privileges.UsagePointGroup.view,
                                    action: 'showWizard'
                                }
                            }
                        }
                    }
                },
                add: {
                    title: Uni.I18n.translate('general.label.usagepoint.add', 'IMT', 'Add usage point'),
                    route: 'add',
                    privileges: Imt.privileges.UsagePoint.admin,
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'showWizard'
                },
                view: {
                    title: Uni.I18n.translate('general.label.usagepoint.view', 'IMT', 'View usage point'),
                    route: '{usagePointId}',
                    privileges: Imt.privileges.UsagePoint.view,
                    controller: 'Imt.usagepointmanagement.controller.View',
                    action: 'showUsagePoint',
                    callback: function (route) {
                        this.getApplication().on('usagePointLoaded', function (record) {
                            route.setTitle(record.get('name'));
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
                             route: 'device/{deviceId}',
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
                        calendars: {
                            title: Uni.I18n.translate('general.label.calendars', 'IMT', 'Calendars'),
                            route: 'calendars',
                            controller: 'Imt.usagepointmanagement.controller.Calendars',
                            action: 'showCalendars',
                            items: {
                                addcalendar: {
                                    title: Uni.I18n.translate('general.label.addCalendar', 'IMT', 'Add calendar'),
                                    route: 'add',
                                    controller: 'Imt.usagepointmanagement.controller.Calendars',
                                    action: 'addCalendar',
                                    privileges: Imt.privileges.UsagePoint.adminCalendars
                                },
                                preview: {
                                    title: Uni.I18n.translate('general.label.previewCalendar', 'IMT', 'Preview calendar'),
                                    route: 'preview/{calendarId}',
                                    controller: 'Imt.usagepointmanagement.controller.Calendars',
                                    action: 'previewCalendar',
                                    callback: function (route) {
                                        this.getApplication().on('calendarLoaded', function (record) {
                                            route.setTitle(record.get('mRID'));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
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
                                    },
                                    items: {
                                        addregisterdata: {
                                            title: Uni.I18n.translate('general.label.addReading', 'IMT', 'Add reading'),
                                            route: 'add',
                                            controller: 'Imt.purpose.controller.RegisterData',
                                            action: 'showAddRegisterData',
                                            // privileges:
                                            // Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.view)
                                            // &&  Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view),
                                            // callback: function (route) {
                                            //     this.getApplication().on('output-loaded', function (output) {
                                            //         if (output) {
                                            //             route.setTitle(output.get('name'));
                                            //         }
                                            //         return true;
                                            //     }, {single: true});
                                            //     return this;
                                            // }
                                        },
                                        editregisterdata: {
                                            title: Uni.I18n.translate('general.label.editReading', 'IMT', 'Edit reading'),
                                            route: '{timestamp}/edit',
                                            controller: 'Imt.purpose.controller.RegisterData',
                                            action: 'showEditRegisterData',
                                        }
                                    }
                                }
                            }
                        }
           			}
           		},
           		device: {
                    title: Uni.I18n.translate('general.label.device.view', 'IMT', 'View device'),
                    route: 'device/{deviceId}',
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
                                },
                                estimation: {
                                    title: Uni.I18n.translate('usagepoint.estimation.estimationConfiguration', 'IMT', 'Estimation configuration'),
                                    route: 'estimation/:tab:',
                                    controller: 'Imt.metrologyconfiguration.controller.EstimationConfiguration',
                                    action: 'showEstimationConfiguration',
                                    privileges: Imt.privileges.MetrologyConfig.viewEstimation,
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('estimation.addRuleSet', 'IMT', 'Add estimation rule set'),
                                            route: 'add',
                                            controller: 'Imt.metrologyconfiguration.controller.EstimationConfiguration',
                                            action: 'showAddEstimationRuleSets',
                                            privileges: Imt.privileges.MetrologyConfig.adminEstimation
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
                }
            }
        },
        search: {
            title: Uni.I18n.translate('general.label.search', 'IMT', 'Search'),
            route: 'search',
            controller: 'Imt.controller.Search',
            action: 'showOverview',
            items: {
                bulkaction: {
                    title: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                    route: 'bulk',
                    controller: 'Imt.controller.SearchItemsBulkAction',
                //    privileges: Mdc.privileges.Device.administrateDeviceOrDeviceCommunication,
                    action: 'showBulkAction'
                }
            }
        }
    }
});
