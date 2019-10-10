/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.controller.history.Webservices', {
    extend: 'Uni.controller.history.Converter',
    alias: 'widget.controller-history-webservice',
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
        var namespace = Uni.util.Application.getAppNamespace();
        var webserviceendpoints = {
            title: Uni.I18n.translate('webservices.webserviceEndpoints', 'WSS', 'Web service endpoints'),
            privileges: ['privilege.view.webservices', 'privilege.administrate.webservices'],
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
                    privileges: ['privilege.view.webservices','privilege.administrate.webservices'],
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
                        status: {
                            privileges: ['privilege.view.webservices', 'privilege.administrate.webservices'],
                            route: 'status',
                            title: Uni.I18n.translate('general.endpointStatusHistory', 'WSS', 'Endpoint status history'),
                            controller: 'Wss.controller.Webservices',
                            action: 'showEndpointStatusHistory'
                        },
                        history: {
                            privileges: ['privilege.administrate.webservices','privilege.view.webservices'],
                            route: 'history',
                            title: Uni.I18n.translate('general.history', 'WSS', 'History'),
                            controller: 'Wss.controller.Webservices',
                            action: 'showWebserviceHistory',
                            items: {
                                occurrence: {
                                    privileges: ['privilege.administrate.webservices', 'privilege.view.webservices'],
                                    route: '{occurenceId}',
                                    title: Uni.I18n.translate('general.logs', 'WSS', "Log '{0}'",Uni.DateTime.formatDateTimeShort(me.time), false),
                                    controller: 'Wss.controller.Webservices',
                                    action: 'showWebserviceEndPoint',
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
            title: Uni.I18n.translate('webservices.webserviceHistory', 'WSS', 'Web service endpoint history'),
            privileges: ['privilege.view.webservices', 'privilege.viewHistory.webservices', 'privilege.administrate.webservices'],
            route: 'webservicehistory',
            controller: 'Wss.controller.Webservices',
            action: 'showWebservicesHistoryOverview',
            items: {
                view: {
                    route: '{endpointId}',
                    action: 'showEndpointHistoryOverview',
                    controller: 'Wss.controller.Webservices',
                    privileges: ['privilege.viewHistory.webservices'],
                    callback: function (route) {
                                        var router = this;
                                        this.getApplication().on('occurenceload', function (name) {
                                            route.setTitle(name);
                                        }, { single: true });
                                        return this;
                                    },
                    items: {
                                occurrence: {
                                    privileges: ['privilege.viewHistory.webservices'],
                                    route: '{occurenceId}',
                                    title: Uni.I18n.translate('general.log', 'WSS', 'Log'),
                                    controller: 'Wss.controller.Webservices',
                                    action:  'showWebserviceEndPoint'
                                }
                            },
                    }
                }
            }

        var items = {
            webserviceendpoints: webserviceendpoints,
            webservicehistory: webservicehistory
        };

        if (namespace === 'SystemApp') {
            me.routeConfig.administration.items = items;
        } else {
            me.routeConfig.workspace.items = items;
        }

        me.callParent(arguments);
    }
});
