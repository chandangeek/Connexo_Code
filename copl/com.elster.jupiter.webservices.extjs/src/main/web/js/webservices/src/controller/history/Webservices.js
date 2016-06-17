Ext.define('Wss.controller.history.Webservices', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'WSS', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                webserviceendpoints: {
                    title: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Webservice endpoints'),
                    //privileges: Cal.privileges.Calendar.admin,
                    route: 'webserviceendpoints',
                    controller: 'Wss.controller.Webservices',
                    action: 'showWebservicesOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('webservices.addWebServiceEndpoint', 'WSS', 'Add webservice endpoint'),
                            route: 'add',
                            controller: 'Wss.controller.Webservices',
                            action: 'showAddWebserviceEndPoint'
                        },
                        view: {
                            route: '{endpointId}',
                            title: Uni.I18n.translate('webservices.webserviceEndpointOverview', 'WSS', 'Webservice endpoint overview'),
                            //privileges: Apr.privileges.AppServer.view,
                            controller: 'Wss.controller.Webservices',
                            action: 'showEndpointOverview',
                            callback: function (route) {
                                this.getApplication().on('endpointload', function (name) {
                                    route.setTitle(Ext.String.htmlEncode(name));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                logs: {
                                    route: 'logs',
                                    title: Uni.I18n.translate('general.Logging', 'WSS', 'Logging'),
                                    //privileges: Apr.privileges.AppServer.view,
                                    controller: 'Wss.controller.Webservices',
                                    action: 'showLoggingPage',
                                    callback: function (route) {
                                        this.getApplication().on('endpointload', function (name) {
                                            route.setTitle(Ext.String.htmlEncode(name));
                                            return true;
                                        }, {single: true});
                                        return this;
                                    }
                                },
                                edit: {
                                    route: 'edit',
                                    title: Uni.I18n.translate('general.Edit', 'WSS', 'Edit'),
                                    //privileges: Apr.privileges.AppServer.view,
                                    controller: 'Wss.controller.Webservices',
                                    action: 'showEditPage',
                                    callback: function (route) {
                                        this.getApplication().on('endpointload', function (name) {
                                            route.setTitle(Ext.String.format(Uni.I18n.translate('general.EditEndpoint', 'WSS', 'Edit {0}'),Ext.String.htmlEncode(name)));
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
});
