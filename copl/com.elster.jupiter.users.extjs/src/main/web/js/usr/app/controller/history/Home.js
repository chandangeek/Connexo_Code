Ext.define('Usr.controller.history.Home', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'home',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;
        crossroads.addRoute('home',function(){
            me.getApplication().getHomeController().showOverview();
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens,token) {
        if (this.currentPath !== null) {
            this.previousPath = this.currentPath;
        }
        this.currentPath = token;
        crossroads.parse('home');
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.previousPath);
    }
});