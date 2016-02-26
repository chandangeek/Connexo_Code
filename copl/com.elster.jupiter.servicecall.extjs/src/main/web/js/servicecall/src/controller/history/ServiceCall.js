Ext.define('Scs.controller.history.ServiceCall', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',

    routeConfig: {
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'SCS', 'Workspace'),
            route: 'workspace',
            disabled:true,
            items: {
                servicecalls: {
                    title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                    route: 'servicecalls',
                    controller: 'Scs.controller.ServiceCalls',
                    action: 'showServiceCalls',
                    items: {
                        overview: {
                            route: '{serviceCallIdDepth1}',
                           // privileges: Apr.privileges.AppServer.view,
                            title: Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'),
                            controller: 'Scs.controller.ServiceCalls',
                            action: 'showServiceCallOverview',
                            callback: function (route) {
                                this.getApplication().on('servicecallload', function (arguments) {
                                    route.setTitle(arguments[0] ? arguments[0] : Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                overview: {
                                    route: '{serviceCallIdDepth2}',
                                    // privileges: Apr.privileges.AppServer.view,
                                    title: Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'),
                                    controller: 'Scs.controller.ServiceCalls',
                                    action: 'showServiceCallOverview',
                                    callback: function (route) {
                                        this.getApplication().on('servicecallload', function (arguments) {
                                            route.setTitle(arguments[1] ? arguments[1] : Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    },
                                    items: {
                                        overview: {
                                            route: '{serviceCallIdDepth3}',
                                            // privileges: Apr.privileges.AppServer.view,
                                            title: Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'),
                                            controller: 'Scs.controller.ServiceCalls',
                                            action: 'showServiceCallOverview',
                                            callback: function (route) {
                                                this.getApplication().on('servicecallload', function (arguments) {
                                                    route.setTitle(arguments[2] ? arguments[2] : Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'));
                                                    return true;
                                                }, {single: true});
                                                return this;
                                            },
                                            items: {
                                                overview: {
                                                    route: '{serviceCallIdDepth4}',
                                                    // privileges: Apr.privileges.AppServer.view,
                                                    title: Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'),
                                                    controller: 'Scs.controller.ServiceCalls',
                                                    action: 'showServiceCallOverview',
                                                    callback: function (route) {
                                                        this.getApplication().on('servicecallload', function (arguments) {
                                                            route.setTitle(arguments[3] ? arguments[3] : Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'));
                                                            return true;
                                                        }, {single: true});
                                                        return this;
                                                    },
                                                    items: {
                                                        overview: {
                                                            route: '{serviceCallIdDepth5}',
                                                            // privileges: Apr.privileges.AppServer.view,
                                                            title: Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'),
                                                            controller: 'Scs.controller.ServiceCalls',
                                                            action: 'showServiceCallOverview',
                                                            callback: function (route) {
                                                                this.getApplication().on('servicecallload', function (arguments) {
                                                                    route.setTitle(arguments[4] ? arguments[4] : Uni.I18n.translate('general.serviceCallOverview', 'SCS', 'Service call overview'));
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
