Ext.define('Usr.controller.history.User', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'users',

    routeConfig: {
        users: {
            title: 'Users',
            route: 'users',
            controller: 'Usr.controller.User',
            items: {
                edit: {
                    title: 'Edit',
                    route: '{id}/edit',
                    controller: 'Usr.controller.UserGroups',
                    action: 'showEditOverview'
                },
                login: {
                    title: 'login',
                    route: 'login',
                    controller: 'Usr.controller.Login'
                }
            }
        }
    },

    init: function () {
        this.getController('Uni.controller.history.Router').addConfig(this.routeConfig);
    }
});