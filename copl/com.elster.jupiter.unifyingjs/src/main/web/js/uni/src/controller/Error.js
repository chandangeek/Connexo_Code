/**
 * @class Uni.controller.Error
 */
Ext.define('Uni.controller.Error', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.error.Window'
    ],

    config: {
        window: null
    },

    init: function () {
        var me = this;

        Ext.Error.handle = me.handleGenericError;
        Ext.Ajax.on('requestexception', me.handleRequestError, this);
    },

    handleGenericError: function (error) {
        //<debug>
        console.log(error);
        //</debug>

        this.showError(error);
    },

    handleRequestError: function (conn, response, options) {
        var message = response.responseText || response.statusText;
        this.showError(message);
    },

    showError: function (error) {
        var window = this.getWindow();

        if (window === null) {
            window = Ext.widget('errorWindow');
            this.setWindow(window);
        }

        window.setErrorMessage(error);
        window.show();
    },

    showHttp404: function () {
        // TODO
    }
});