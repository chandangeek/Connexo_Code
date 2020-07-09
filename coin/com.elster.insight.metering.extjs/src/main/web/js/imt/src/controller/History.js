/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                        transitions: {
                            title: Uni.I18n.translate('general.transition', 'IMT', 'Transition'),
                            route: 'transitions/{transitionId}',
                            controller: 'Imt.usagepointmanagement.controller.UsagePointTransitionExecute',
                            action: 'showExecuteTransition',
                            privileges: Imt.privileges.UsagePoint.view,
                            callback: function (route) {
                                this.getApplication().on('loadUsagePointTransition', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        attributes: {
                            title: Uni.I18n.translate('general.usagePointAttributes', 'IMT', 'Usage point attributes'),
                            route: 'attributes',
                            privileges: Imt.privileges.UsagePoint.view,
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
                        'change': {
                            title: Uni.I18n.translate('usagePointLifeCycle.change', 'IMT', 'Change usage point life cycle'),
                            route: 'change',
                            controller: 'Imt.usagepointmanagement.controller.ChangeUsagePointLifeCycle',
                            privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                            action: 'showChangeUsagePointLifeCycle'
                        },
                        servicecalls: {
                            title: Uni.I18n.translate('general.serviceCalls', 'IMT', 'Service calls'),
                            route: 'servicecalls',
                            controller: 'Imt.servicecalls.controller.ServiceCalls',
                            privileges: Scs.privileges.ServiceCall.view,
                            action: 'showServiceCalls',
                            items: {
                                history: {
                                    title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                                    route: 'history',
                                    privileges: Scs.privileges.ServiceCall.view,
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
                                    title: Uni.I18n.translate('general.linkMeters', 'IMT', 'Link meters'),
                                    route: 'activatemeters',
                                    controller: 'Imt.usagepointsetup.controller.MetrologyConfig',
                                    action: 'showActivateMeters',
                                    privileges: Imt.privileges.UsagePoint.admin
                                },
                                unlinkmeter: {
                                    title: Uni.I18n.translate('general.unlinkMeter', 'IMT', 'Unlink meter'),
                                    route: 'unlink/{meterName}',
                                    controller: 'Imt.usagepointmanagement.controller.MetrologyConfigurationDetails',
                                    action: 'unlinkMeter',
                                    privileges: Imt.privileges.UsagePoint.admin,
                                    callback: function (route) {
                                        this.getApplication().on('unlinkMeterPageLoaded', function (meterName) {
                                            route.setTitle(Uni.I18n.translate('general.tooltip.unlink.meter', 'IMT', "Unlink '{0}'", [meterName]));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
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
                            title: Uni.I18n.translate('usagePoint.metrologyConfiguration.link', 'IMT', 'Link metrology configuration'),
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'showWizard',
                            privileges: Imt.privileges.UsagePoint.admin,
                            dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.usagePointMetrologyConfigurationStore,
                            dynamicPrivilege: Imt.dynamicprivileges.UsagePoint.metrologyConfigurationDefine,
                            route: 'metrologyconfiguration/define'
                        },
                        purpose: {
                            title: Uni.I18n.translate('general.label.purpose', 'IMT', 'Purpose'),
                            route: 'purpose/{purposeId}',
                            controller: 'Imt.purpose.controller.Purpose',
                            action: 'showOutputs',
                            privileges:
                                Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.view)
                                && Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view),
                            callback: function (route) {
                                var me = this;
                                this.getApplication().on('purposes-loaded', function (purposes) {
                                    var purpose = _.find(purposes, function (p) {
                                        return p.getId() == me.arguments.purposeId
                                    });
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
                                        && Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view),
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
                                            action: 'showAddEditRegisterData',
                                            privileges: Imt.privileges.UsagePoint.admin
                                        },
                                        editregisterdata: {
                                            title: Uni.I18n.translate('general.label.editReading', 'IMT', 'Edit reading'),
                                            route: '{timestamp}/edit',
                                            controller: 'Imt.purpose.controller.RegisterData',
                                            action: 'showAddEditRegisterData',
                                            privileges: Imt.privileges.UsagePoint.admin
                                        },
                                        editvalidationrule: {
                                            title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                            route: '{ruleId}/editvalidationrule',
                                            controller: 'Imt.purpose.controller.Purpose',
                                            action: 'showEditValidationRuleWithAttributes',
                                            privileges: Imt.privileges.UsagePoint.administerValidationConfiguration,
                                            callback: function (route) {
                                                this.getApplication().on('rule-with-attributes-loaded', function (rule) {
                                                    if (rule) {
                                                        route.setTitle(Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.editAttributesFor', 'IMT', 'Edit attributes for'), rule.get('name')));
                                                    }
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            }
                                        },
                                        editestimationrule: {
                                            title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                            route: '{ruleId}/editestimationrule',
                                            controller: 'Imt.purpose.controller.Purpose',
                                            action: 'showEditEstimationRuleWithAttributes',
                                            privileges: Imt.privileges.UsagePoint.administerEstimationConfiguration,
                                            callback: function (route) {
                                                this.getApplication().on('rule-with-attributes-loaded', function (rule) {
                                                    if (rule) {
                                                        route.setTitle(Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.editAttributesFor', 'IMT', 'Edit attributes for'), rule.get('name')));
                                                    }
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            }
                                        },
                                        history: {
                                            title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                                            route: 'history',
                                            controller: 'Imt.purpose.controller.Readings',
                                            action: 'viewHistory',
                                            privileges: Imt.privileges.UsagePoint.admin
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
                    privileges: Imt.privileges.UsagePoint.view,
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
                            privileges: Imt.privileges.MetrologyConfig.admin,
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
                                    privileges: Imt.privileges.MetrologyConfig.admin,
                                    action: 'editMetrologyConfiguration'
                                },
                                manage: {
                                    title: Uni.I18n.translate('general.label.metrologyconfiguration.valruleset.manage', 'IMT', 'Link validation rule sets'),
                                    route: 'manage',
                                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                                    privileges: Imt.privileges.MetrologyConfig.admin,
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
                                    action: 'showEditUsagePointLifeCycle'
                                },
                                states: {
                                    title: Uni.I18n.translate('general.states', 'IMT', 'States'),
                                    route: 'states',
                                    controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                    action: 'showUsagePointLifeCycleStates',
                                    privileges: Imt.privileges.UsagePointLifeCycle.view,
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('usagePointLifeCycleStates.add', 'IMT', 'Add state'),
                                            route: 'add',
                                            controller: 'Imt.usagepointlifecyclestates.controller.UsagePointLifeCycleStates',
                                            action: 'showUsagePointLifeCycleStateEdit',
                                            privileges: Imt.privileges.UsagePointLifeCycle.configure,
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
                                            privileges: Imt.privileges.UsagePointLifeCycle.configure,
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
                                transitions: {
                                    title: Uni.I18n.translate('general.transitions', 'IMT', 'Transitions'),
                                    route: 'transitions',
                                    controller: 'Imt.usagepointlifecycletransitions.controller.UsagePointLifeCycleTransitions',
                                    action: 'showUsagePointLifeCycleTransitions',
                                    privileges: Imt.privileges.UsagePointLifeCycle.configure,
                                    items: {
                                        add: {
                                            title: Uni.I18n.translate('general.addTransition', 'IMT', 'Add transition'),
                                            route: 'add',
                                            controller: 'Imt.usagepointlifecycletransitions.controller.UsagePointLifeCycleTransitions',
                                            action: 'showAddUsagePointLifeCycleTransition',
                                            privileges: Imt.privileges.UsagePointLifeCycle.configure
                                        },
                                        edit: {
                                            title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                            route: '{transitionId}/edit',
                                            controller: 'Imt.usagepointlifecycletransitions.controller.UsagePointLifeCycleTransitions',
                                            action: 'showAddUsagePointLifeCycleTransition',
                                            privileges: Imt.privileges.UsagePointLifeCycle.configure,
                                            callback: function (route) {
                                                this.getApplication().on('usagePointLifeCycleTransitionEdit', function (record) {
                                                    route.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [record.get('name')]));
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
                'rulesets/overview/metrologyconfigurationpurposes': {
                    title: Uni.I18n.translate('general.metrologyConfigurationPurposes', 'IMT', 'Metrology configuration purposes'),
                    route: 'validation/rulesets/{ruleSetId}/metrologyconfigurationpurposes',
                    controller: 'Imt.rulesets.controller.ValidationRuleSetPurposes',
                    action: 'showMetrologyConfigurationPurposes',
                    privileges: Imt.privileges.MetrologyConfig.canViewValidation() || Imt.privileges.MetrologyConfig.canAdministrateValidation(),
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addMetrologyConfigurationPurposes', 'IMT', 'Add metrology configuration purposes'),
                            route: 'add',
                            controller: 'Imt.rulesets.controller.AddPurposesToValidationRuleSet',
                            action: 'showAddMetrologyConfigurationPurposes',
                            privileges: Imt.privileges.MetrologyConfig.adminValidation
                        }
                    }
                },
                'estimationrulesets/estimationruleset/metrologyconfigurationpurposes': {
                    title: Uni.I18n.translate('general.metrologyConfigurationPurposes', 'IMT', 'Metrology configuration purposes'),
                    route: 'estimationrulesets/{ruleSetId}/metrologyconfigurationpurposes',
                    controller: 'Imt.rulesets.controller.EstimationRuleSetPurposes',
                    action: 'showMetrologyConfigurationPurposes',
                    privileges: Imt.privileges.MetrologyConfig.canViewEstimation() || Imt.privileges.MetrologyConfig.canAdministrateEstimation(),
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addMetrologyConfigurationPurposes', 'IMT', 'Add metrology configuration purposes'),
                            route: 'add',
                            controller: 'Imt.rulesets.controller.AddPurposesToEstimationRuleSet',
                            action: 'showAddMetrologyConfigurationPurposes',
                            privileges: Imt.privileges.MetrologyConfig.adminEstimation
                        }
                    }
                },
                taskmanagement: {
                    title: Uni.I18n.translate('tsk.task.title', 'IMT', 'Tasks'),
                    route: 'taskmanagement',
                    controller: 'Imt.controller.TaskManagement',
                    action: 'showTaskManagement',
                    privileges: Imt.privileges.TaskManagement.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('tsk.general.addTask', 'IMT', 'Add task'),
                            route: 'add',
                            privileges: function () {
                                return Apr.TaskManagementApp.canAdministrate();
                            },
                            haveDependenciesLoaded: function () {
                                return Apr.TaskManagementApp.dependenciesLoaded();
                            },
                            controller: 'Imt.controller.TaskManagement',
                            action: 'showAddTask'
                        },
                        viewTaskManagement: {
                            title: '',
                            route: '{type}/{taskManagementId}',
                            controller: 'Imt.controller.TaskManagement',
                            action: 'viewTaskManagement'
                        },
                        view: {
                            title: '',
                            route: '{type}/{taskManagementId}/{taskId}',
                            privileges: function () {
                                var taskType = this.MdmApp.getApplication().controllers.map['Uni.controller.history.Router'].arguments.type,
                                    taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

                                if (taskManagement && taskManagement.controller && taskManagement.controller) {
                                    return taskManagement.controller.canView();
                                }
                                return false;
                            },
                            haveDependenciesLoaded: function () {
                                return Apr.TaskManagementApp.dependenciesLoaded();
                            },
                            controller: 'Imt.controller.TaskManagement',
                            action: 'viewTask',
                            callback: function (route) {
                                this.getApplication().on('loadTask', function (record) {
                                    route.setTitle(record.get ? record.get('name') : record);
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                edit: {
                                    title: '',
                                    route: 'edit',
                                    privileges: function () {
                                        var taskType = this.MdmApp.getApplication().controllers.map['Uni.controller.history.Router'].arguments.type,
                                            taskManagement = Apr.TaskManagementApp.getTaskManagementApps().get(taskType);

                                        if (taskManagement && taskManagement.controller && taskManagement.controller) {
                                            return taskManagement.controller.canEdit();
                                        }
                                        return false;
                                    },
                                    haveDependenciesLoaded: function () {
                                        return Apr.TaskManagementApp.dependenciesLoaded();
                                    },
                                    controller: 'Imt.controller.TaskManagement',
                                    action: 'editTask',
                                    callback: function (route) {
                                        this.getApplication().on('loadTask', function (name) {
                                            route.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", name, false));
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                },
                                history: {
                                    title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                                    route: 'history',
                                    privileges: Imt.privileges.TaskManagement.view,
                                    controller: 'Imt.controller.TaskManagement',
                                    action: 'viewHistoryTask',
                                    haveDependenciesLoaded: function () {
                                        return Apr.TaskManagementApp.dependenciesLoaded();
                                    },
                                    items: {
                                        occurrence: {
                                            title: '',
                                            route: '{occurrenceId}',
                                            controller: 'Imt.controller.TaskManagement',
                                            action: 'viewHistoryTaskLog',
                                            haveDependenciesLoaded: function () {
                                                return Apr.TaskManagementApp.dependenciesLoaded();
                                            },
                                            callback: function (route) {
                                                this.getApplication().on('viewHistoryTaskLog', function (name) {
                                                    route.setTitle(name);
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
                creationrules: {
                    title: Uni.I18n.translate('general.issueCreationRules', 'IMT', 'Issue creation rules'),
                    route: 'creationrules',
                    controller: 'Isu.controller.CreationRules',
                    privileges: Isu.privileges.Issue.adminCreateRule,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.issueCreationRules.add', 'IMT', 'Add issue creation rule'),
                            route: 'add',
                            controller: 'Isu.controller.CreationRuleEdit',
                            privileges: Isu.privileges.Issue.createRule,
                            action: 'showEdit',
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'IMT', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                },
                                addexclgroups: {
                                    title: Uni.I18n.translate('general.excludeDeviceGroups', 'IMT', 'Add device groups to exclude'),
                                    route: 'addexclgroups',
                                    controller: 'Isu.controller.CreationRuleGroupsEdit',
                                    action: 'showEdit'
                                }
                            }
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Isu.controller.CreationRuleEdit',
                            action: 'showEdit',
                            privileges: Isu.privileges.Issue.createRule,
                            callback: function (route) {
                                this.getApplication().on('issueCreationRuleEdit', function (record) {
                                    route.setTitle(Uni.I18n.translate('administration.issueCreationRules.title.editIssueCreationRule', 'IMT', "Edit '{0}'", record.get('name'), false));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                addaction: {
                                    title: Uni.I18n.translate('general.addAction', 'IMT', 'Add action'),
                                    route: 'addaction',
                                    controller: 'Isu.controller.CreationRuleActionEdit',
                                    action: 'showEdit'
                                },
                                addexclgroups: {
                                    title: Uni.I18n.translate('general.excludeDeviceGroups', 'IMT', 'Add device groups to exclude'),
                                    route: 'addexclgroups',
                                    controller: 'Isu.controller.CreationRuleGroupsEdit',
                                    action: 'showEdit'
                                }
                            }
                        }
                    }
                }
            }
        },
        search: {
            title: Uni.I18n.translate('general.label.search', 'IMT', 'Search'),
            route: 'search',
            controller: 'Imt.controller.Search',
            action: 'showOverview',
            privileges: Imt.privileges.UsagePoint.view,
            items: {
                bulkaction: {
                    title: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                    route: 'bulk',
                    controller: 'Imt.controller.SearchItemsBulkAction',
                    privileges: Imt.privileges.UsagePoint.view,
                    action: 'showBulkAction'
                }
            }
        },
        dashboard: {
            title: Uni.I18n.translate('general.label.dashboard', 'IMT', 'Dashboard'),
            route: 'dashboard',
            controller: 'Imt.dashboard.controller.OperatorDashboard',
            action: 'showOverview',
            privileges: Imt.privileges.UsagePoint.view,
            items: {
                selectfavoriteusagepointgroups: {
                    title: Uni.I18n.translate('general.selectFavoriteUsagePointGroups', 'IMT', 'Select favourite usage point groups'),
                    route: 'favoriteusagepointgroups',
                    controller: 'Imt.dashboard.controller.FavoriteUsagePointGroups',
                    privileges: Imt.privileges.UsagePointGroup.flag,
                    action: 'showFavoriteUsagePointGroups'
                }
            }
        },
        "workspace/dataquality": {
            title: Uni.I18n.translate('general.dataQuality', 'IMT', 'Data quality'),
            route: 'workspace/dataquality',
            controller: 'Imt.dataquality.controller.DataQuality',
            action: 'showDataQuality',
            privileges: Cfg.privileges.Validation.viewResultsOrAdministerDataQuality
        },
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'IMT', 'Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                issues: {
                    title: Uni.I18n.translate('workspace.issues.title', 'IMT', 'Issues'),
                    route: 'issues',
                    controller: 'Imt.issue.controller.IssuesOverview',
                    action: 'showOverview',
                    privileges: Isu.privileges.Issue.viewAdminDevice,
                    items: {
                        bulkaction: {
                            title: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                            route: 'bulkaction',
                            privileges: Isu.privileges.Issue.closeOrAssing,
                            controller: 'Isu.controller.BulkChangeIssues'
                        },
                        usagepointgroup: {
                            title: Uni.I18n.translate('general.addUsagePointGroup', 'IMT', 'Add usage point group'),
                            route: 'usagepointgroup',
                            controller: 'Imt.usagepointgroupsfromissues.controller.AddUsagePointGroupFromIssues',
                            privileges: Isu.privileges.Issue.closeOrAssing,
                            action: 'showUsagePointGroupWizard',
                        },
                        view: {
                            title: Uni.I18n.translate('general.issueDetails', 'IMT', 'Issue details'),
                            route: '{issueId}',
                            controller: 'Imt.datavalidation.controller.Detail',
                            action: 'showOverview',
                            privileges: Isu.privileges.Issue.viewAdminDevice,
                            callback: function (route) {
                                this.getApplication().on('issueLoad', function (record) {
                                    route.setTitle(record.get('title'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                action: {
                                    title: Uni.I18n.translate('general.action', 'IMT', 'Action'),
                                    route: 'action/{actionId}',
                                    controller: 'Imt.datavalidation.controller.ApplyAction',
                                    privileges: Isu.privileges.Issue.viewAdminDevice,
                                    callback: function (route) {
                                        this.getApplication().on('issueActionLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                setpriority: {
                                    title: Uni.I18n.translate('issue.setpriority', 'IMT', 'Set priority'),
                                    route: 'setpriority',
                                    controller: 'Imt.datavalidation.controller.SetPriority',
                                    action: 'setPriority',
                                    privileges: Isu.privileges.Issue.viewAdminDevice
                                }
                            }
                        }
                    }
                },
                insightprocesses: {
                    title: Uni.I18n.translate('general.insightprocesses', 'IMT', 'Processes'),
                    route: 'insightprocesses',
                    privileges: Bpm.privileges.BpmManagement.viewProcesses,
                    controller: 'Imt.processes.controller.WorkspaceProcesses',
                    action: 'showAllProcessesInsight'
                },
                issuesoverview: {
                    title: Uni.I18n.translate('workspace.issuesOverview', 'IMT', 'Issues overview'),
                    route: 'issuesoverview',
                    controller: 'Isu.controller.Overview',
                    action: 'showIssuesOverview',
                    privileges: Isu.privileges.Issue.viewAdminDevice
                },
                audit: {
                    title: Uni.I18n.translate('general.auditTrail', 'IMT', 'Audit trail'),
                    route: 'audit',
                    controller: 'Cfg.audit.controller.Audit',
                    action: 'showOverview',
                    privileges: Cfg.privileges.Audit.viewAuditLog
                }
            }
        },
    }
});
