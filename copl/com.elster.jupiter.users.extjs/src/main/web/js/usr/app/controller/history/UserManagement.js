Ext.define('Usr.controller.history.UserManagement', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'usermanagement',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;
        crossroads.addRoute('usermanagement',function(){
            me.getApplication().getController('Usr.controller.Home').showOverview();
        });

        crossroads.addRoute('usermanagement/users', function () {
            me.getApplication().getController('Usr.controller.User').showOverview();
        });
        crossroads.addRoute('usermanagement/users/{id}/edit', function (id) {
            me.getApplication().getController('Usr.controller.UserEdit').showEditOverview(id);
        });

        crossroads.addRoute('usermanagement/roles',function(){
            me.getApplication().getController('Usr.controller.Group').showOverview();
        });
        crossroads.addRoute('usermanagement/roles/{id}/edit',function(id){
            me.getApplication().getController('Usr.controller.GroupEdit').showEditOverview(id);
        });
        crossroads.addRoute('usermanagement/roles/create',function(){
            me.getApplication().getController('Usr.controller.GroupEdit').showCreateOverview();
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens, token) {
        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }
        if (this.currentPath !== null) {
            this.previousPath = this.currentPath;
        }
        this.currentPath = token;
        crossroads.parse(token);
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.previousPath);
    }
});