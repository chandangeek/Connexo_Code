/**
 * @class Uni.controller.Error
 *
 * General error controller that is responsible to log and show uncaught errors
 * that are not dealt with in a separate failure handle case.
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
        Ext.Ajax.on('requestexception', me.handleRequestError, me);
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
            decoded;

        try {
            decoded = Ext.decode(message);
        } catch (e) {
            // Ignore invalid JSON.
        }

        if (Ext.isDefined(decoded)) {
            if (!Ext.isEmpty(decoded.message)) {
                message = decoded.message;
            } else if (Ext.isDefined(decoded.errors) && Ext.isArray(decoded.errors)) {
                if (1 === decoded.errors.length) {
                    message = decoded.errors[0].msg;
                } else if (1 < decoded.errors.length) {
                    message = '<ul>';
                    for (var i = 0; i < decoded.errors.length; i++) {
                        message += '<li>' + decoded.errors[i].msg + '</li>';
                    }
                    message += '</ul>';
                } else {
                    message = Uni.I18n.translate(
                        'error.unknownErrorOccurred',
                        'UNI',
                        'An unknown error occurred.'
                    );
                }
            }

            //<debug>
            if (!Ext.isEmpty(decoded.error)) {
                console.log('Error code: ' + decoded.error);
            }
            //</debug>
        }

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

        switch (response.status) {
            case 400: // Bad request.
                // Do nothing.
                break;
            case 401: // Unauthorized.
            // Fallthrough.
            case 403: // Forbidden.
            // Fallthrough.
            case 404: // Not found.
            // Fallthrough.
            default:
                this.showError(title, message);
                break;
        }
    },

    /**
     * Shows an error window with a title and a message to the user.
     *
     * @param {String} title Window title to show
     * @param {String} message Error message to show
     * @param {String} [config={}] Optional {@link Ext.window.MessageBox} configuration if tweaks are required
     */
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