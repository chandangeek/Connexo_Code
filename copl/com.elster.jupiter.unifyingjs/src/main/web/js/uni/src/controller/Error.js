Ext.define('Uni.controller.Error', {
    extend: 'Ext.app.Controller',

    views: [
        'error.Window'
    ],

    config: {
        window: null
    },

    init: function () {
        var me = this;

        Ext.Error.handle = me.handleGenericException;
        Ext.Ajax.on('requestexception', me.handleRequestException, this);
    },

    handleGenericException: function (ex) {
        // TODO
        console.log('General error');
        this.showException('TODO');
    },

    handleRequestException: function (conn, response, options) {
        // TODO
        console.log('Request exception');
        this.showException('TODO');
    },

    showException: function (ex) {
        if(this.getWindow() === undefined) {
            var window = Ext.widget('errorWindow');
            this.setWindow(window);
        }

        // TODO Visualize the exception.


        window.show();
    },

    showHttp404: function () {
        // TODO
    }
});