Ext.define('Usr.controller.history.Group', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'roles',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;
        crossroads.addRoute('roles',function(){
            me.getApplication().getGroupController().showOverview();
        });
        crossroads.addRoute('roles/{id}/edit',function(id){
            me.getApplication().getGroupPrivilegesController().showEditOverview(id);
        });
        crossroads.addRoute('roles/create',function(){
            me.getApplication().getGroupPrivilegesController().showCreateOverview();
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens,token) {
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