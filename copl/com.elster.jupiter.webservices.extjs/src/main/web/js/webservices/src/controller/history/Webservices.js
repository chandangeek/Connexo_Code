/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                    title: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Web service endpoints'),
                    privileges: Wss.privileges.Webservices.view,
                    route: 'webserviceendpoints',
                    controller: 'Wss.controller.Webservices',
                    action: 'showWebservicesOverview',
                    items: {
                        add: {
                            title: Uni.I18n.translate('webservices.addWebServiceEndpoint', 'WSS', 'Add web service endpoint'),
                            privileges: Wss.privileges.Webservices.admin,
                            route: 'add',
                            controller: 'Wss.controller.Webservices',
                            action: 'showAddWebserviceEndPoint'
                        },
                        view: {
                            route: '{endpointId}',
                            privileges: Wss.privileges.Webservices.view,
                            title: Uni.I18n.translate('webservices.webserviceEndpointOverview', 'WSS', 'Web service endpoint overview'),
                            controller: 'Wss.controller.Webservices',
                            action: 'showEndpointOverview',
                            callback: function (route) {
                                this.getApplication().on('endpointload', function (name) {
                                    route.setTitle(name);
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                logs: {
                                    privileges: Wss.privileges.Webservices.view,
                                    route: 'logs',
                                    title: Uni.I18n.translate('general.Logging', 'WSS', 'Logging'),
                                    controller: 'Wss.controller.Webservices',
                                    action: 'showLoggingPage'
                                },
                                edit: {
                                    route: 'edit',
                                    title: Uni.I18n.translate('general.Edit', 'WSS', 'Edit'),
                                    privileges: Wss.privileges.Webservices.admin,
                                    controller: 'Wss.controller.Webservices',
                                    action: 'showEditPage',
                                    callback: function (route) {
                                        this.getApplication().on('endpointload', function (name) {
                                            route.setTitle(Ext.String.format(Uni.I18n.translate('endPointAdd.editTitle', 'WSS', 'Edit \'{0}\''),name));
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
