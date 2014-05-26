Ext.define('Usr.controller.history.User', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'users',

    init: function () {
        var me = this;

        crossroads.addRoute('users', function () {
            me.getApplication().getController('Usr.controller.User').showOverview();
        });
        crossroads.addRoute('users/{id}/edit', function (id) {
            me.getApplication().getController('Usr.controller.UserGroups').showEditOverview(id);
        });
        crossroads.addRoute('users/login', function () {
            me.getApplication().getController('Usr.controller.Login').showOverview();
        });

        this.callParent(arguments);
    }
});