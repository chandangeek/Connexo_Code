Ext.define('Usr.controller.history.User', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'users',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;
        crossroads.addRoute('users',function(){
            me.getApplication().getUserController().showOverview();
        });
        crossroads.addRoute('users/{id}/edit',function(id){
            me.getApplication().getUserGroupsController().showEditOverview(id);
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