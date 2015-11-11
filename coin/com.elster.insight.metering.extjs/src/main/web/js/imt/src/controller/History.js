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
                            title: Uni.I18n.translate('general.label.usagepoint.channels', 'IMT', 'Channels'),
                            route: 'channels',
                            controller: 'Imt.channeldata.controller.View',
                            action: 'showUsagePointChannels',
                            items: {
                                channel: {
                                    title: Uni.I18n.translate('general.label.usagepoint.channel', 'IMT', 'Channel'),
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
           			    }
           			}
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
                }
            }
        },
        administration: {
            title: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
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
        }
    }
});
