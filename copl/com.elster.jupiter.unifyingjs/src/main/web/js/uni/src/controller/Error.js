/**
 * @class Uni.controller.Error
 */
Ext.define('Uni.controller.Error', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.error.Window',
        'Ext.ux.window.Notification'
    ],

    config: {
        window: null
    },

    refs: [
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        var me = this;

        Ext.Error.handle = me.handleGenericError;
//        Ext.Ajax.on('requestexception', me.handleRequestError, this);
    },

    handleGenericError: function (error) {
        //<debug>
        console.log(error);
        //</debug>

        this.showError(error);
    },

    handleRequestError: function (conn, response, options) {
        if(response.status!==400){
            var message = response.responseText || response.statusText;
            if(Ext.isEmpty(message)) {
                message = '<h2>ERROR</h2><br><h4>Unexpected connection problems. Please check that server is available.</h4>';
            }
            this.showError(message);
        } else {
            var response = Ext.decode(response.responseText);
            if(Ext.isEmpty(response.errors)){
                this.showError(response.message);
            }
        }
    },

    showError: function (error) {
        var me = this;
        Ext.create('widget.uxNotification', {
            position: 'tc',
            manager: '#contentPanel',
            cls: 'ux-notification-light',
            width: me.getContentPanel().getWidth()-20,
//            iconCls: 'ux-notification-icon-information',
            html: error,
            slideInDuration: 200,
            slideBackDuration: 200,
            autoCloseDelay: 7000,
            slideInAnimation: 'linear',
            slideBackAnimation: 'linear'
        }).show();
    },

    showHttp404: function () {
        // TODO
    }
});