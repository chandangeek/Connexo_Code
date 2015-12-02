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
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'createUsagePoint'
            	},
           		view: {
           			title: Uni.I18n.translate('general.label.usagepoint.view', 'IMT', 'View usage point'),
           			route: '{mRID}',
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
                        channels: {
                            title: Uni.I18n.translate('general.channels', 'IMT', 'Channels'),
                            route: 'channels',
                            controller: 'Imt.controller.setup.DeviceChannels',
//                            privileges: Imt.privileges.Device.viewDevice,
                            action: 'showOverview',
                            filter: 'Imt.model.filter.DeviceChannelsFilter',
//                            dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.deviceStateStore,
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('routing.channel', 'IMT', 'Channel'),
                                    route: '{channelId}',
                                    controller: 'Imt.controller.setup.DeviceChannelData',
//                                    privileges: Imt.privileges.Device.viewDeviceCommunication,
                                    action: 'showSpecifications',
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        editcustomattributes: {
                                            route: 'customattributes/{customAttributeSetId}/edit',
                                            controller: 'Imt.controller.setup.DeviceChannelData',
//                                            privileges: Imt.privileges.Device.administrateDeviceData,
                                            action: 'showEditChannelOfLoadProfileCustomAttributes',
                                            callback: function (route) {
                                                this.getApplication().on('channelOfLoadProfileCustomAttributes', function (record) {
                                                    route.setTitle(Uni.I18n.translate('deviceChannelOfLoadProfile.editCustomAttributes', 'IMT', "Edit '{0}'", [record.get('name')]));
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            }
                                        },
                                        customattributesversions: {
                                            title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                                            route: 'customattributes/{customAttributeSetId}/versions',
                                            controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                            privileges: Imt.privileges.Device.viewDeviceData,
                                            action: 'loadCustomAttributeVersions',
                                            callback: function (route) {
                                                this.getApplication().on('loadCustomAttributeSetOnChannel', function (record) {
                                                    route.setTitle(Uni.I18n.translate('deviceChannelOfLoadProfile.historyCustomAttributes', 'IMT', "'{0}' history", [record.get('name')]));
                                                    return true;
                                                }, {single: true});

                                                return this;
                                            },
                                            items: {
                                                edit: {
                                                    title: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                                                    route: '{versionId}/edit',
                                                    controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                                    privileges: Imt.privileges.Device.administrateDeviceData,
                                                    action: 'editCustomAttributeVersion',
                                                    callback: function (route) {
                                                        this.getApplication().on('loadCustomAttributeSetVersionOnChannel', function (record) {
                                                            route.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [record.get('period')]));
                                                            return true;
                                                        }, {single: true});

                                                        return this;
                                                    }
                                                },
                                                add: {
                                                    title: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                                    route: 'add',
                                                    controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                                    privileges: Imt.privileges.Device.administrateDeviceData,
                                                    action: 'addCustomAttributeVersion',
                                                    callback: function (route) {
                                                        this.getApplication().on('loadCustomAttributeSetOnChannelAdd', function (record) {
                                                            route.setTitle(Uni.I18n.translate('general.addxversion', 'IMT', "Add '{0}' version", [record.get('name')]));
                                                            return true;
                                                        }, {single: true});

                                                        return this;
                                                    }
                                                },
                                                clone: {
                                                    title: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                                                    route: '{versionId}/clone',
                                                    controller: 'Imt.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel',
//                                                    privileges: Imt.privileges.Device.administrateDeviceData,
                                                    action: 'cloneCustomAttributeVersion',
                                                    callback: function (route) {
                                                        this.getApplication().on('loadCustomAttributeSetVersionOnChannelClone', function (record) {
                                                            route.setTitle(Uni.I18n.translate('general.clonex', 'IMT', "Clone '{0}'", [record.get('period')]));
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
                                    controller: 'Imt.controller.setup.DeviceChannelData',
//                                    privileges: Imt.privileges.Device.viewDeviceCommunication,
                                    action: 'showData',
//                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.deviceStateStore,
                                    callback: function (route) {
                                        this.getApplication().on('channelOfLoadProfileOfDeviceLoad', function (record) {
                                            route.setTitle(record.get('name'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                channelvalidationblocks: {
                                    title: Uni.I18n.translate('routing.channelData', 'IMT', 'Channel data'),
                                    route: '{channelId}/validationblocks/{issueId}',
                                    controller: 'Imt.controller.setup.DeviceChannelData',
//                                    privileges: Imt.privileges.Device.viewDeviceCommunication,
//                                    dynamicPrivilegeStores: Imt.dynamicprivileges.Stores.deviceStateStore,
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
                                    title: Uni.I18n.translate('routing.channelValidation', 'IMT', 'Channel validation'),
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
           			    registers: {
                            title: Uni.I18n.translate('general.label.usagepoint.registers', 'IMT', 'Registers'),
                            route: 'registers',
                            controller: 'Imt.registerdata.controller.View',
                            action: 'showUsagePointRegisters',
                            items: {
                            	register: {
                                    title: Uni.I18n.translate('general.label.usagepoint.register', 'IMT', 'Register'),
                                    route: '{registerId}',
                                    controller: 'Imt.registerdata.controller.View',
                                    action: 'showUsagePointRegisterData',
                                    callback: function (route) {
                                        this.getApplication().on('registerDataLoaded', function (record) {
                                            route.setTitle(record.get('readingType').fullAliasName);
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                }                                    
                            }
           			    },
           			},
           		},
   				edit: {
                	title: Uni.I18n.translate('general.label.usagepoint.edit', 'IMT', 'Edit usage point'),
                    route: '{mRID}/edit',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'editUsagePoint'               					
   				},
                device: {
                    title: Uni.I18n.translate('general.label.device.view', 'IMT', 'View device'),
                    route: 'device/{mRID}',
                    controller: 'Imt.devicemanagement.controller.Device',
                    action: 'showDevice'
                },
                metrologyconfiguration: {
	              title: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
	              route: '{mRID}',
	              controller: 'Imt.usagepointmanagement.controller.View',
	              action: 'showMetrologyConfiguration',
                  items: {
                	  view: {
	                    route: 'metrologyconfiguration/{mcid}/view',
	                    controller: 'Imt.usagepointmanagement.controller.View',
	                    action: 'showMetrologyConfiguration',
	                    callback: function (route) {
	                        this.getApplication().on('metrologyConfigurationLoaded', function (record) {
	                            route.setTitle(record.get('name'));
	                            return true;
	                        }, {single: true});
	
	                        return this;
	                    },  
                	  },
                  }
                },
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
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.add', 'IMT', 'Add metrology configuration'),
                            route: 'add',
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'createMetrologyConfiguration'
                        },
                        view: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.view', 'IMT', 'View metrology configuration'),
                            route: '{mcid}/view',
                            controller: 'Imt.metrologyconfiguration.controller.View',
                            action: 'showMetrologyConfiguration',
                            callback: function (route) {
                                this.getApplication().on('metrologyConfigurationLoaded', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                        },
                        edit: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.edit', 'IMT', 'Edit metrology configuration'),
                            route: '{mcid}/edit',
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'editMetrologyConfiguration'                                
                        },
                        manage: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.valruleset.manage', 'IMT', 'Manage metrology configuration validation rule sets'),
                            route: '{mcid}/manage',
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'manageValidationRuleSets',
                            callback: function (route) {
                                this.getApplication().on('metrologyConfigurationValRuleSetLoaded', function (record) {
                                    route.setTitle('Manage metrology configuration validation rules sets');
                                    return true;
                                }, {single: true});

                                return this;
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
