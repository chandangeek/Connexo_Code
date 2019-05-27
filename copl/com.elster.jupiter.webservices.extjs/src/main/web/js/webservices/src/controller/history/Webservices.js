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
        },
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'WSS', 'Workspace'),
            route: 'workspace',
            disabled: true,
        }
    },

    init: function() {
        var me = this;
        var webserviceendpoints = {
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
                        }, { single: true });
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
                        history: {
                            privileges: Wss.privileges.Webservices.view,
                            route: 'history',
                            title: Uni.I18n.translate('general.history', 'WSS', 'History'),
                            controller: 'Wss.controller.Webservices',
                            action: 'showWebserviceHistory',
                            items: {
                                occurrence: {
                                    privileges: Wss.privileges.Webservices.view,
                                    route: '{occurenceId}',
                                    title: Uni.I18n.translate('general.log', 'WSS', 'Log'),
                                    controller: 'Wss.controller.Webservices',
                                    action: 'showWebserviceHistoryOccurrence',
                                }
                            },
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
        };

        var webservicehistory = {
            title: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service history'),
            privileges: Wss.privileges.Webservices.view,
            route: 'webservicehistory',
            controller: 'Wss.controller.Webservices',
            action: 'showWebservicesHistoryOverview',
        };

        var items = {
            webserviceendpoints: webserviceendpoints,
            webservicehistory: webservicehistory
        };

        me.routeConfig.administration.items = items;
        me.routeConfig.workspace.items = items;

        me.callParent(arguments);
    }
});
