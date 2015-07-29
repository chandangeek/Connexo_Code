Ext.define('Apr.controller.history.AppServer', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
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
                            action: 'showAddEditAppServer'
                        },
                        edit: {
                            route: '{appServerName}/edit',
                            privileges: Apr.privileges.AppServer.admin,
                            title: Uni.I18n.translate('general.edit', 'APR', 'Edit'),
                            controller: 'Apr.controller.AppServers',
                            action: 'showAddEditAppServer',
                            callback: function (route) {
                                this.getApplication().on('appserverload', function (name) {
                                    route.setTitle(Uni.I18n.translate('general.edit', 'APR', 'Edit') + " '" + name + "'");
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
