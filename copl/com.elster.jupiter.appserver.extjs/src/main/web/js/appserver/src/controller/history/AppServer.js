/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.history.AppServer', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'APR', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                appservers: {
                    title: Uni.I18n.translate('general.applicationServers', 'APR', 'Application servers'),
                    route: 'appservers',
                    privileges: Apr.privileges.AppServer.view,
                    controller: 'Apr.controller.AppServers',
                    action: 'showAppServers',
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addApplicationServer', 'APR', 'Add application server'),
                            route: 'add',
                            privileges: Apr.privileges.AppServer.admin,
                            controller: 'Apr.controller.AppServers',
                            action: 'showAddEditAppServer',
                            items: {
                                addimportservices: {
                                    route: 'addimportservices',
                                    privileges: Apr.privileges.AppServer.admin,
                                    title: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showAddImportServices'
                                },
                                addmessageservices: {
                                    route: 'addmessageservices',
                                    privileges: Apr.privileges.AppServer.admin,
                                    title: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showAddMessageServiceView'
                                },
                                addwebserviceendpoints: {
                                    route: 'addwebserviceendpoints',
                                    privileges: Apr.privileges.AppServer.admin,
                                    title: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showAddWebserviceEndpointsView'
                                }
                            }
                        },
                        edit: {
                            route: '{appServerName}/edit',
                            privileges: Apr.privileges.AppServer.admin,
                            title: Uni.I18n.translate('general.edit', 'APR', 'Edit'),
                            controller: 'Apr.controller.AppServers',
                            action: 'showAddEditAppServer',
                            callback: function (route) {
                                this.getApplication().on('appserverload', function (name) {
                                    route.setTitle(Uni.I18n.translate('general.editx', 'APR', "Edit '{0}'",[name]));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                addimportservices: {
                                    route: 'addimportservices',
                                    privileges: Apr.privileges.AppServer.admin,
                                    title: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showAddImportServices'
                                },
                                addmessageservices: {
                                    route: 'addmessageservices',
                                    privileges: Apr.privileges.AppServer.admin,
                                    title: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showAddMessageServiceView'
                                },
                                addwebserviceendpoints: {
                                    route: 'addwebserviceendpoints',
                                    privileges: Apr.privileges.AppServer.admin,
                                    title: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showAddWebserviceEndpointsView'
                                }
                            }
                        },
                        overview: {
                            route: '{appServerName}',
                            privileges: Apr.privileges.AppServer.view,
                            title: Uni.I18n.translate('general.overview', 'APR', 'Overview'),
                            controller: 'Apr.controller.AppServers',
                            action: 'showAppServerOverview',
                            callback: function (route) {
                                this.getApplication().on('appserverload', function (name) {
                                    route.setTitle(Ext.String.htmlEncode(name));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                messageservices: {
                                    route: 'messageservices',
                                    privileges: Apr.privileges.AppServer.view,
                                    title: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showMessageServices',
                                    items: {
                                        addmessageservices: {
                                            route: 'addmessageservices',
                                            privileges: Apr.privileges.AppServer.admin,
                                            title: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                            controller: 'Apr.controller.AppServers',
                                            action: 'showAddMessageServiceViewFromDetails'
                                        }
                                    }
                                },
                                importservices: {
                                    route: 'importservices',
                                    privileges: Apr.privileges.AppServer.view,
                                    title: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showImportServices',
                                    items: {
                                        addimportservices: {
                                            route: 'addimportservices',
                                            privileges: Apr.privileges.AppServer.admin,
                                            title: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                            controller: 'Apr.controller.AppServers',
                                            action: 'returnToShowImportServicesIfRefresh'
                                        }
                                    }
                                },
                                webserviceendpoints: {
                                    route: 'webserviceendpoints',
                                    privileges: Apr.privileges.AppServer.view,
                                    title: Uni.I18n.translate('general.webserviceEndpoints', 'APR', 'Web service endpoints'),
                                    controller: 'Apr.controller.AppServers',
                                    action: 'showWebServiceEndpoints',
                                    items: {
                                        addwebserviceendpoints: {
                                            route: 'addwebserviceendpoints',
                                            privileges: Apr.privileges.AppServer.admin,
                                            title: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
                                            controller: 'Apr.controller.AppServers',
                                            action: 'returnToShowWebserviceEndpointsIfRefresh'
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                taskoverview: {
                    title: Uni.I18n.translate('general.taskOverview', 'APR', 'Task overview'),
                    route: 'taskoverview',
                    privileges: Apr.privileges.AppServer.taskOverview,
                    controller: 'Apr.controller.TaskOverview',
                    action: 'showTaskOverview'
                },
                messagequeues: {
                    title: Uni.I18n.translate('general.messageQueues', 'APR', 'Message queues'),
                    route: 'messagequeues',
                    privileges: Apr.privileges.AppServer.view,
                    controller: 'Apr.controller.MessageQueues',
                    action: 'showMessageQueues',
                    items: {
                        monitor: {
                            route: 'monitor',
                            privileges: Apr.privileges.AppServer.view,
                            title: Uni.I18n.translate('general.monitor', 'APR', 'Monitor'),
                            controller: 'Apr.controller.MessageQueues',
                            action: 'showMessageQueuesMonitor'
                        },
                        messagequeues: {
                            route: 'messagequeues',
                            privileges: Apr.privileges.AppServer.view,
                            title: Uni.I18n.translate('general.overview', 'APR', 'Overview'),
                            controller: 'Apr.controller.MessageQueues',
                            action: 'showMessageQueues'
                        }

                    }
                }
            }
        }
    }
});
