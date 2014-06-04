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
        Ext.Ajax.on('requestexception', me.handleRequestError, this);
    },

    handleGenericError: function (error) {
        //<debug>
        console.log(error);
        //</debug>

        var title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed');
        this.showError(title, error);
    },

    handleRequestError: function (conn, response, options) {
        var title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed'),
            message = response.responseText || response.statusText,
            decoded = Ext.decode(message);

        if (!Ext.isEmpty(decoded.message)) {
            message = decoded.message;
        }

        //<debug>
        if (!Ext.isEmpty(decoded.error)) {
            console.log('Error code: ' + decoded.error);
        }
        //</debug>

        switch (response.status) {
            case 400:
                if (Ext.isEmpty(message)) {
                    title = Uni.I18n.translate(
                        'error.connectionProblemsTitle',
                        'UNI',
                        'Unexpected connection problems'
                    );

                    message = Uni.I18n.translate(
                        'error.connectionProblemsMessage',
                        'UNI',
                        'Unexpected connection problems. Please check that server is available.'
                    );
                }
                break;
            case 403:
            // Fallthrough.
            case 404:
            // Fallthrough.
            default:
                title = Uni.I18n.translate(
                    'error.unknownError',
                    'UNI',
                    'Unknown error'
                );
                break;
        }

        this.showError(title, message);
    },

    showError: function (title, message, config) {
        config = config ? config : {};
        Ext.apply(config, {
            title: title,
            msg: message,
            modal: false,
            ui: 'message-error',
            icon: Ext.MessageBox.ERROR
        });

        var box = Ext.create('Ext.window.MessageBox', {
            buttons: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.close', 'UNI', 'Close'),
                    action: 'close',
                    name: 'close',
                    ui: 'action',
                    margin: '0 0 0 44px',
                    handler: function () {
                        box.close();
                    }
                }
            ]
        });

        box.show(config);
    }
});