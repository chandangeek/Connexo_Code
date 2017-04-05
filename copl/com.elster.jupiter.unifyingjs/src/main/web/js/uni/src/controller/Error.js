/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Uni.view.error.NotVisible',
        'Uni.view.error.Launch',
        'Uni.controller.history.Router',
        'Uni.controller.history.EventBus'
    ],

    config: {
        window: null
    },

    unhandledErrorMessages: [
        Uni.I18n.translate('error.communication.failure', 'UNI', 'Connexo has encountered a problem, try refreshing the page. If the problem persists, please contact your system administrator.')
        //
    ],

    routeConfig: {
        notfound: {
            title: Uni.I18n.translate('error.pageNotFound', 'UNI', 'Page not found'),
            route: 'error/notfound',
            controller: 'Uni.controller.Error',
            action: 'showPageNotFound'
        },
        notvisible: {
            title: Uni.I18n.translate('error.pageNotVisible', 'UNI', 'Page not visible'),
            route: 'error/notvisible',
            controller: 'Uni.controller.Error',
            action: 'showPageNotVisible'
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

        if (Ext.isObject(error) && Ext.isDefined(error.title)) {
            title = error.title;
        } else if (Ext.isObject(error)) {
            title = Uni.I18n.translate('error.requestFailedConnexoKnownError', 'UNI', 'Couldn\'t perform your action');
        } else {
            title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Your action can\'t be successfully executed');
        }
        if (Ext.isObject(error) && Ext.isDefined(error.msg)) {
            error = error.msg;
        }
        if (Ext.isObject(error) && Ext.isDefined(error.errCode)) {
            errCode = error.errCode;
        }

        me.showError(title, error, errCode);
    },

    handleRequestError: function (conn, response, options) {
        var me = this,
            title = Uni.I18n.translate('error.requestFailedConnexoKnownError', 'UNI', 'Couldn\'t perform your action'),
            message = response.responseText || response.statusText,
            decoded = Ext.decode(message, true),
            code;

        if (Ext.isDefined(decoded) && decoded !== null) {
            if (!Ext.isEmpty(decoded.message)) {
                message = decoded.message;
                code = decoded.errorCode;
            } else if (Ext.isDefined(decoded.errors) && Ext.isArray(decoded.errors)) {
                if (1 === decoded.errors.length) {
                    message = decoded.errors[0].msg;
                    code = decoded.errors[0].errCode
                } else if (1 < decoded.errors.length) {
                    message = '<ul>';
                    for (var i = 0; i < decoded.errors.length; i++) {
                        message += '<li>' + decoded.errors[i].msg + '</li><br\>';
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
                console.log('Error code: ' + decoded.errorCode);
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
                'Please check that server is available.'
            );
            code = 'CFT-1001'; // known code - to be extracted to a reference file
        }
        else {

            title = Uni.I18n.translate('error.communication.failureTitle', 'UNI', 'Your action took longer than expected');
            message = Uni.I18n.translate('error.' + message.replace(' ', '.'), 'UNI', message);
            code = code ? code : 'CFT-1000'; // known code - to be extracted to a reference file
        }

        switch (response.status) {
            case 0: // timeout.
                if (!(options.notHandleTimeout || (options.operation && options.operation.notHandleTimeout))) {
                    me.showError(title, message, code);
                }
                break;
            case 400: // Bad request.
                if (decoded && decoded.message) {
                    title = Uni.I18n.translate(
                        'error.requestFailed',
                        'UNI',
                        'Your action can\'t be successfully executed'
                    );
                    me.showError(title, decoded.message ? decoded.message : message, decoded.errorCode ? decoded.errorCode : code);
                }
                break;
            case 500: // Internal server error.
                title = Uni.I18n.translate(
                    'error.internalServerError',
                    'UNI',
                    'Internal Connexo error'
                );
                me.showError(title, decoded.message ? decoded.message : message, decoded.errorCode ? decoded.errorCode : code);
                break;
            case 503: // Service unavailable.
                title = Uni.I18n.translate(
                    'error.serviceUnavailable',
                    'UNI',
                    'Service Unavailable'
                );
                me.showError(title, decoded.message ? decoded.message : message, decoded.errorCode ? decoded.errorCode : code);
                break;
            case 404: // Not found.
                title = Uni.I18n.translate(
                    'error.requestFailed',
                    'UNI',
                    'Your action can\'t be successfully executed'
                );
                message = Uni.I18n.translate(
                    'error.internalServerErrorMessage',
                    'UNI',
                    'Connexo has encountered an error, please contact your system administrator.'
                );
                options.method !== 'HEAD' && me.showError(title, message, code);
                break;
            case 401: // Unauthorized.
                me.getApplication().fireEvent('sessionexpired');
                break;
            case 403: // Forbidden.
                title = Uni.I18n.translate(
                    'error.requestFailedConnexoKnownError', 'UNI', 'Couldn\'t perform your action'
                );
                message = Uni.I18n.translate(
                    'error.forbiddenAccess',
                    'UNI',
                    'Access denied.'
                );
                break;
            case 408:
                title = Uni.I18n.translate('general.timeOut', 'UNI', 'Time out');
                message = Uni.I18n.translate('general.timeOutMessage', 'UNI', 'Request processing took too long.');
                break;
            case 409:
                me.concurrentErrorHandler(options, decoded);
                break;
            case 418: // I'm a teapot.
            // Fallthrough.
            default:
                me.showError(title, message, code);
                break;
        }
    },

    concurrentErrorHandler: function (requestOptions, responseText) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            title = Ext.htmlEncode(responseText.message),
            message = responseText.error,
            errorCode = responseText.errorCode,
            buttons,
            repeatRequest = function () {
                requestOptions.jsonData.version = responseText.version;
                requestOptions.jsonData.parent = responseText.parent;
                Ext.Ajax.request(requestOptions);
            },
            refreshPage = function () {
                Ext.util.History.fireEvent('change', Ext.util.History.getToken());
            };

        if (requestOptions.isNotEdit || (requestOptions.operation && requestOptions.operation.isNotEdit)) {
            buttons = [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.refresh', 'UNI', 'Refresh'),
                    itemId: 'refresh-button',
                    ui: 'remove',
                    handler: function (button) {
                        refreshPage();
                        button.up('messagebox').close();
                    }
                }
            ];
            me.showError(title, message, errorCode, undefined, buttons, 'concurrent-use-error-msg');
        } else {
            if (requestOptions.method === 'PUT' || requestOptions.method === 'POST') {
                buttons = [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.tryAgain', 'UNI', 'Try again'),
                        itemId: 'try-again-button',
                        privileges: !(requestOptions.dontTryAgain || (requestOptions.operation && requestOptions.operation.dontTryAgain) || !responseText.version),
                        ui: 'remove',
                        handler: function (button) {
                            if (!Ext.isEmpty(responseText.version)) {
                                refreshPage();
                            } else {
                                me.showPageNotFound();
                            }
                            button.up('messagebox').close();
                        }
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                        ui: 'link',
                        itemId: 'concurrent-cancel',
                        handler: function (button) {
                            if (requestOptions.backUrl || requestOptions.operation && requestOptions.operation.backUrl) {
                                window.location.href = requestOptions.backUrl || requestOptions.operation.backUrl;
                            }
                            button.up('messagebox').close();
                        }
                    }
                ];
                me.showError(title, message, errorCode, undefined, buttons, 'concurrent-use-error-msg');
            } else if (requestOptions.method === 'DELETE') {
                if (!Ext.isEmpty(responseText.version)) {
                    buttons = [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.tryAgain', 'UNI', 'Try again'),
                            itemId: 'try-again-button',
                            ui: 'remove',
                            handler: function (button) {
                                repeatRequest();
                                button.up('messagebox').close();
                            }
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                            itemId: 'concurrent-cancel',
                            ui: 'link',
                            handler: function (button) {
                                refreshPage();
                                button.up('messagebox').close();
                            }
                        }
                    ];
                    me.showError(title, message, errorCode, undefined, buttons, 'concurrent-use-error-msg');
                } else {
                    if (requestOptions.operation && Ext.isFunction(requestOptions.operation.callback)) {
                        requestOptions.operation.callback.call();
                    } else if (!requestOptions.operation && Ext.isFunction(requestOptions.callback)) {
                        requestOptions.callback.call();
                    }
                    if (requestOptions.operation && Ext.isFunction(requestOptions.operation.success)) {
                        requestOptions.operation.success.call();
                    } else if (!requestOptions.operation && Ext.isFunction(requestOptions.success)) {
                        requestOptions.success.call();
                    }
                }
            }
        }
    },

    /**
     * Shows an error window with a title and a message to the user.
     *
     * @param {String} title Window title to show
     * @param {String} message Error message to show
     * @param {String} [config={}] Optional {@link Ext.window.MessageBox} configuration if tweaks are required
     */
    showError: function (title, message, errorCode, config, buttons, itemId) {
        config = config ? config : {};
        Ext.apply(config, {
            title: title,
            errCode: errorCode,
            msg: message,
            modal: false,
            ui: 'message-error',
            icon: 'icon-warning2',
            style: 'font-size: 34px;',
            minWidth: 300
        });

        var box = Ext.create('Ext.window.MessageBox', {
            itemId: itemId,
            buttons: buttons || [
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
            ],
            initComponent: function () {
                var me = this,
                    msgClass = Ext.getClass(me),
                    sLabel = Uni.I18n.translate('general.errorCode', 'UNI', 'Error Code'),
                    tm = new Ext.util.TextMetrics(),
                    separator = ':',
                    labelWidth = tm.getSize(sLabel).width + tm.getSize(separator).width;
                msgClass.prototype.initComponent.apply(me, arguments);
                me.down('displayfield').margin = '0px';
                me.down('displayfield').fieldStyle = 'min-height: 0px';

                var fieldErrorCode = new Ext.form.field.Display({
                    fieldLabel: sLabel,
                    value: errorCode,
                    labelStyle: 'margin: 1px',
                    fieldStyle: 'margin: 0px',
                    labelWidth: labelWidth,
                    labelAlign: 'left',
                    labelSeparator: separator,
                    labelPad: 0
                });
                me.promptContainer.insert(2, fieldErrorCode);

            }
        });
        box.show(config);
    },
    showPageNotFound: function () {
        var widget = Ext.widget('errorNotFound');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
    ,
    showPageNotVisible: function () {
        var widget = Ext.widget('errorNotVisible');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
    ,
    showErrorLaunch: function () {
        var widget = Ext.widget('errorLaunch');
        this.getApplication().fireEvent('changecontentevent', widget);
    }

})
;