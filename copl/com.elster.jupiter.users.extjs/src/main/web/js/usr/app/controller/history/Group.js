Ext.define('Usr.controller.history.Group', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'roles',

    init: function () {
        var me = this;

        crossroads.addRoute('roles', function () {
            me.getApplication().getController('Usr.controller.Group').showOverview();
        });
        crossroads.addRoute('roles/{id}/edit', function (id) {
            me.getApplication().getController('Usr.controller.GroupPrivileges').showEditOverview(id);
        });
        crossroads.addRoute('roles/create', function () {
            me.getApplication().getController('Usr.controller.GroupPrivileges').showCreateOverview();
        });

        this.callParent(arguments);
    }
});