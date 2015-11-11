Ext.define('Imt.controller.History', {
    extend: 'Uni.controller.history.Converter',

    routeConfig: {
        usagepoints: {
            title: Uni.I18n.translate('general.label.usagepoints', 'INS', 'Usage points'),
            route: 'usagepoints',
            disabled: true,
            items: {
            	add: {
                	title: Uni.I18n.translate('general.label.usagepoint.add', 'INS', 'Add usage point'),
                    route: 'add',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'createUsagePoint'
            	},
           		view: {
           			title: Uni.I18n.translate('general.label.usagepoint.view', 'INS', 'View usage point'),
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
                            title: Uni.I18n.translate('general.label.usagepoint.channels', 'INS', 'Channels'),
                            route: 'channels',
                            controller: 'Imt.channeldata.controller.View',
                            action: 'showUsagePointChannels',
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('general.label.usagepoint.channel', 'INS', 'Channel'),
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
                            title: Uni.I18n.translate('general.label.usagepoint.registers', 'INS', 'Registers'),
                            route: 'registers',
                            controller: 'Imt.registerdata.controller.View',
                            action: 'showUsagePointRegisters',
                            items: {
                            	register: {
                                    title: Uni.I18n.translate('general.label.usagepoint.register', 'INS', 'Register'),
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
                	title: Uni.I18n.translate('general.label.usagepoint.edit', 'INS', 'Edit usage point'),
                    route: '{mRID}/edit',
                    controller: 'Imt.usagepointmanagement.controller.Edit',
                    action: 'editUsagePoint'               					
   				},
                device: {
                    title: Uni.I18n.translate('general.label.device.view', 'INS', 'View device'),
                    route: 'device/{mRID}',
                    controller: 'Imt.devicemanagement.controller.Device',
                    action: 'showDevice'
                }
            }
        },
        administration: {
            title: Uni.I18n.translate('general.label.metrologyconfiguration', 'INS', 'Metrology configuration'),
            route: 'administration',
            disabled: true,
            items: {
                metrologyconfiguration: {
                    title: Uni.I18n.translate('general.label.metrologyconfiguration.list', 'INS', 'Metrology configurations'),
                    route: 'metrologyconfiguration',
                    controller: 'Imt.metrologyconfiguration.controller.ViewList',
                    action: 'showMetrologyConfigurationList',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.add', 'INS', 'Add metrology configuration'),
                            route: 'add',
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'createMetrologyConfiguration'
                        },
                        view: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.view', 'INS', 'View metrology configuration'),
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
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.edit', 'INS', 'Edit metrology configuration'),
                            route: '{mcid}/edit',
                            controller: 'Imt.metrologyconfiguration.controller.Edit',
                            action: 'editMetrologyConfiguration'                                
                        },
//                        remove: {
//                            title: Uni.I18n.translate('general.label.metrologyconfiguration.remove', 'INS', 'Remove metrology configuration'),
//                            route: '{mcid}/remove',
//                            controller: 'Imt.metrologyconfiguration.controller.Edit',
//                            action: 'removeMetrologyConfiguration'                                  
//                        },
                        manage: {
                            title: Uni.I18n.translate('general.label.metrologyconfiguration.valruleset.manage', 'INS', 'Manage metrology configuration validation rule sets'),
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
        }
    }
});
