Ext.define('Scs.controller.history.ServiceCall', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'SCS', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                servicecalls: {
                    title: Uni.I18n.translate('general.serviceCalls', 'SCS', 'Service calls'),
                    route: 'servicecalls',
                    controller: 'Scs.controller.ServiceCalls',
                    action: 'showServiceCalls',
                    items: {
                        overviewD1: {
                            route: '{serviceCallIdDepth1}',
                           // privileges: Apr.privileges.AppServer.view,
                            title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                            controller: 'Scs.controller.ServiceCalls',
                            action: 'showServiceCallOverview',
                            items: {
                                overviewD2: {
                                    route: '{serviceCallIdDepth2}',
                                    // privileges: Apr.privileges.AppServer.view,
                                    title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                                    controller: 'Scs.controller.ServiceCalls',
                                    action: 'showServiceCallOverview',
                                    items: {
                                        overviewD3: {
                                            route: '{serviceCallIdDepth3}',
                                            // privileges: Apr.privileges.AppServer.view,
                                            title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                                            controller: 'Scs.controller.ServiceCalls',
                                            action: 'showServiceCallOverview',
                                            items: {
                                                overviewD4: {
                                                    route: '{serviceCallIdDepth4}',
                                                    // privileges: Apr.privileges.AppServer.view,
                                                    title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                                                    controller: 'Scs.controller.ServiceCalls',
                                                    action: 'showServiceCallOverview',
                                                    items: {
                                                        overviewD5: {
                                                            route: '{serviceCallIdDepth5}',
                                                            // privileges: Apr.privileges.AppServer.view,
                                                            title: Uni.I18n.translate('general.overview', 'SCS', 'Overview'),
                                                            controller: 'Scs.controller.ServiceCalls',
                                                            action: 'showServiceCallOverview'
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
