Ext.define('Usr.controller.history.Group', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'roles',

    routeConfig: {
        roles: {
            title: 'Roles',
            route: 'roles',
            controller: 'Usr.controller.Group',
            items: {
                edit: {
                    title: 'Edit',
                    route: '{id}/edit',
                    controller: 'Usr.controller.GroupPrivileges',
                    action: 'showEditOverview'
                },
                create: {
                    title: 'Create',
                    route: 'create',
                    controller: 'Usr.controller.GroupPrivileges',
                    action: 'showCreateOverview'
                }
            }
        }
    },

    init: function () {
        this.getController('Uni.controller.history.Router').addConfig(this.routeConfig);
    }
});