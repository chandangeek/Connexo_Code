Ext.define('InsightApp.controller.History', {
    extend: 'Uni.controller.history.Converter',

    routeConfig: {
        usagepoints: {
            title: Uni.I18n.translate('general.usagepoints', 'INS', 'Usage Points'),
            route: 'usagepoints',
            disabled: true,
            items: {
            	add: {
                	title: Uni.I18n.translate('general.usagePointAdd', 'INS', 'Add Usage Point'),
                    route: 'add',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'createUsagePoint'
            	},
           		view: {
           			title: Uni.I18n.translate('general.usagePointView', 'INS', 'View Usage Point'),
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
                            title: Uni.I18n.translate('general.usagePointChannels', 'INS', 'Channels'),
                            route: 'channels',
                            controller: 'Imt.channeldata.controller.View',
                            action: 'showUsagePointChannels',
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('general.usagePointChannel', 'INS', 'Channel'),
                                    route: '{channelId}',
                                    controller: 'Imt.channeldata.controller.View',
                                    action: 'showUsagePointChannelData',
                                    callback: function (route) {
                                        this.getApplication().on('channelDataLoaded', function (record) {
                                            route.setTitle(record.get('readingType').fullAliasName);
                                            return true;
                                        }, {single: true});

                                        return this;
                                    }
                                }
                            }
           			    },
           			    registers: {
                            title: Uni.I18n.translate('general.usagePointRegisters', 'INS', 'Registers'),
                            route: 'registers',
                            controller: 'Imt.registerdata.controller.View',
                            action: 'showUsagePointRegisters',
                            items: {
                            	register: {
                                    title: Uni.I18n.translate('general.usagePointRegister', 'INS', 'Register'),
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
           			    }
           			}
           		},
   				edit: {
                	title: Uni.I18n.translate('general.usagePointEdit', 'INS', 'Edit Usage Point'),
                    route: '{mRID}/edit',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'editUsagePoint'               					
   				},
                device: {
                    title: Uni.I18n.translate('general.device.view', 'INS', 'View Device'),
                    route: 'device/{mRID}',
                    controller: 'Imt.devicemanagement.controller.Device',
                    action: 'showDevice'
                }
            }
        },
        metrologyconfiguration: {
            title: Uni.I18n.translate('general.metrologyConfiguration', 'INS', 'Metrology Configuration'),
            route: 'metrologyconfiguration',
            disabled: true,
            items: {
            	add: {
                	title: Uni.I18n.translate('general.metrologyConfigurationAdd', 'INS', 'Add Metrology Configuration'),
                    route: 'add',
                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                    action: 'createMetrologyConfiguration'
            	},
            	overview: {
                	title: Uni.I18n.translate('general.metrologyConfigurationAdd', 'INS', 'List Metrology Configuration'),
                    route: 'overview',
                    controller: 'Imt.metrologyconfiguration.controller.ViewList',
                    action: 'showMetrologyConfigurationList'
            	},
           		view: {
           			title: Uni.I18n.translate('general.metrologyConfigurationView', 'INS', 'View Metrology Configuration'),
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
                	title: Uni.I18n.translate('general.metrologyConfigurationEdit', 'INS', 'Edit Metrology Configuration'),
                    route: '{mcid}/edit',
                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                    action: 'editMetrologyConfiguration'               					
   				},
   				remove: {
                	title: Uni.I18n.translate('general.metrologyConfigurationEdit', 'INS', 'Edit Metrology Configuration'),
                    route: '{mcid}/delete',
                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                    action: 'deleteMetrologyConfiguration'               					
   				},
   				manage: {
                	title: Uni.I18n.translate('general.metrologyConfigurationRuleSetEdit', 'INS', 'Manage Validation Rule Sets for Metrology Configuration'),
                    route: '{mcid}/manage',
                    controller: 'Imt.metrologyconfiguration.controller.Edit',
                    action: 'manageValidationRuleSets',
           			callback: function (route) {
                        this.getApplication().on('metrologyConfigurationValRuleSetLoaded', function (record) {
                            route.setTitle('Manage metrology configuration validation rules sets');
                            return true;
                        }, {single: true});

                        return this;
                    },
   				},
            }
        },
    }
});
