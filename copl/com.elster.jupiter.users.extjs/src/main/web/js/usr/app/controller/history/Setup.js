Ext.define('Usr.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;
        crossroads.addRoute('setup',function(){
            me.getApplication().getController('Usr.controller.Home').showOverview();
        });

        //Device type routes
        crossroads.addRoute('setup/users',function(){
            me.getApplication().getController('Usr.controller.User').showOverview();
        });
        crossroads.addRoute('setup/groups',function(){
            me.getApplication().getController('Usr.controller.Group').showOverview();
        });
        /*crossroads.addRoute('setup/devicetypes/{id}',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeDetailsView(id);
        });
        crossroads.addRoute('setup/devicetypes/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeEditView(id);
        });*/

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