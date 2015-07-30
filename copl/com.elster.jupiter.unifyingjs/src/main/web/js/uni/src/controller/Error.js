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
        'Ext.ux.window.Notification',
        'Uni.view.error.NotFound',
        'Uni.view.error.Launch'
    ],

    config: {
        window: null
    },

    routeConfig: {
        notfound: {
            title: Uni.I18n.translate('error.pageNotFound', 'UNI', 'Page not found'),
            route: 'error/notfound',
            controller: 'Uni.controller.Error',
            action: 'showPageNotFound'
        },
        launch: {
            title: Uni.I18n.translate('error.errorLaunch', 'UNI', 'Application error'),
            route: 'error/launch',
            controller: 'Uni.controller.Error',
            action: 'showErrorLaunch'
        }
    },

    refs: [
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        var me = this;

        Ext.Error.handle = function (error) {
            me.handleGenericError(error, me);
        };

        Ext.Ajax.on('requestexception', me.handleRequestError, me);

        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    },

    handleGenericError: function (error, scope) {
        //<debug>
        console.log(error);
        //</debug>

        var me = scope || this,
            title;

        if(Ext.isObject(error) && Ext.isDefined(error.title)) {
            title = error.title;
        } else {
            title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed');
        }
        if(Ext.isObject(error) && Ext.isDefined(error.msg)) {
            error = error.msg;
        }

        me.showError(title, error);
    },

    handleRequestError: function (conn, response, options) {
        var title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed'),
            message = response.responseText || response.statusText,
            decoded = Ext.decode(message, true);

        if (Ext.isDefined(decoded) && decoded !== null) {
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
        else {
            title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed');
            message = Uni.I18n.translate(message, 'UNI', message);
        }

        switch (response.status) {
            case 400: // Bad request.
                if (decoded && decoded.message) {
                    title = Uni.I18n.translate(
                        'error.requestFailed',
                        'UNI',
                        'Request failed'
                    );
                    this.showError(title, message);
                }
                break;
            case 500: // Internal server error.
                title = Uni.I18n.translate(
                    'error.internalServerError',
                    'UNI',
                    'Internal server error'
                );
                message = Uni.I18n.translate(
                    'error.internalServerErrorMessage',
                    'UNI',
                    'Please contact your system administrator.'
                );
                this.showError(title, message);
                break;
            case 404: // Not found.
                title = Uni.I18n.translate(
                    'error.requestFailed',
                    'UNI',
                    'Request failed'
                );
                message = Uni.I18n.translate(
                    'error.notFoundErrorMessage',
                    'UNI',
                    'Please contact your system administrator.'
                );
                options.method !== 'HEAD' && this.showError(title, message);
                break;
            case 401: // Unauthorized.
                this.getApplication().fireEvent('sessionexpired');
                break;
            case 403: // Forbidden.
                title = Uni.I18n.translate(
                    'error.requestFailed',
                    'UNI',
                    'Request failed'
                );
                message = Uni.I18n.translate(
                    'error.notFoundErrorMessage',
                    'UNI',
                    'Please contact your system administrator.'
                );
            case 418: // I'm a teapot.
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
            icon: 'icon-warning2',
            style: 'font-size: 34px;'
        });

        var box = Ext.create('Ext.window.MessageBox', {
            buttons: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.close', 'UNI', 'Close'),
                    action: 'close',
                    name: 'close',
                    ui: 'remove',
                    handler: function () {
                        box.close();
                    }
                }
            ]
        });

        box.show(config);
    },
    showPageNotFound: function () {
        var widget = Ext.widget('errorNotFound');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showErrorLaunch: function () {
        var widget = Ext.widget('errorLaunch');
        this.getApplication().fireEvent('changecontentevent', widget);
    }

});