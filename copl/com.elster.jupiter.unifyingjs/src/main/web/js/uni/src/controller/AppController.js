/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.controller.AppController
 */
Ext.define('Uni.controller.AppController', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.state.Manager',
        'Ext.state.LocalStorageProvider',
        'Uni.store.Apps'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        },
        {
            ref: 'logo',
            selector: 'viewport uni-nav-logo'
        }
    ],

    /**
     * @cfg {String} applicationTitle
     *
     * The title to be used across the application.
     */
    applicationTitle: 'Connexo',

    /**
     * @cfg {String} applicationKey
     *
     * The key to be used for licensing the application.
     */
    applicationKey: 'SYS',

    /**
     * @cfg {String} defaultToken
     *
     * The default history token the application needs to use.
     */
    defaultToken: '',

    /**
     * @cfg {Boolean} searchEnabled
     *
     * Whether the search button shows or not in the application header.
     * True by default.
     */
    searchEnabled: true,

    /**
     * @cfg {Boolean} [onlineHelpEnabled=true]
     *
     * Whether the help button shows or not in the application header.
     */
    onlineHelpEnabled: true,

    /**
     * @cfg {String[]} privileges
     * The privileges that allow user to access application.
     * Empty by default.
     */
    privileges: [],

    // <debug>
    /**
     * @cfg {Object[]} packages
     *
     * The packages that need to be loaded in by the application.
     *
     */
    packages: [],
    // </debug>

    init: function (app) {

        var me = this;
        me.initCrossroads();
        Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());

        me.getController('Uni.controller.Navigation');
        me.getApplication().fireEvent('onnavigationtitlechanged', me.applicationTitle);
        me.getApplication().fireEvent('onnavigationtogglesearch', me.searchEnabled);
        me.getApplication().fireEvent('onnavigationtogglehelp', me.onlineHelpEnabled);

        me.getController('Uni.controller.history.EventBus').setDefaultToken(me.defaultToken);
        me.getApplication().on('getapplicationtitleevent', me.getApplicationTitle, me);
        me.getApplication().on('changecontentevent', me.showContent, me);
        me.getApplication().on('sessionexpired', me.redirectToLogin, me);

        if (Uni.Auth.hasAnyPrivilege(me.privileges)) {
            me.checkLicenseStatus();
            me.loadControllers();
            me.showLicenseGraced();
        }

        me.callParent(arguments);
    },

    /**
     * Makes crossroads ignore state so that applications that don't use crossroads
     * have no influence on crossroads' behavior.
     */
    initCrossroads: function () {
        crossroads.ignoreState = true;
    },

    onLaunch: function () {
        var me = this,
            logo = me.getLogo();

        var applicationName = Uni.util.Application.getAppName();
        console.log("SET LOGO!!!");
        console.log("me.applicationTitle=",me.applicationTitle);
        console.log("APPLICATION NAME = ",applicationName);

        if(applicationName == 'MdmApp')
        {
            console.log("SET NAME FOR INSIGHT");
            applicationName = 'Insight'
        }

        var icon ="";
        var envTxt = "";
        console.log("applicationName=",applicationName);
        var applicationRecord = Uni.store.Apps.getApp(applicationName);
        console.log("applicationRecord =",applicationRecord);

        var color = null;//applicationRecord.get('systemIdentifierColor');
        var env = null;//applicationRecord.get('systemIdentifier');
        if (applicationRecord){
            color = applicationRecord.get('systemIdentifierColor');
            env = applicationRecord.get('systemIdentifier');
        }

        console.log("color=",color);
        console.log("env=",env);

        if (color && env ){
            icon = '<span class="icon-circle-small" style="display:inline-block; color:'+color+'; font-size:20px;"></span>';
            envTxt = '<span style="color:'+color+';">' + env + '</span>';
        }

        if (logo.rendered) {
            logo.setText(me.applicationTitle+icon+envTxt);
        } else {
            logo.text = me.applicationTitle+icon+envTxt;
        }
        me.callParent(arguments);
    },

    showContent: function (widget, config) {
        var panel = this.getContentPanel();

        Ext.suspendLayouts();
        panel.removeAll();

        if (Ext.isString(widget)) {
            widget = Ext.widget(widget, Ext.applyIf(config, {
                router: this.getController('Uni.controller.history.Router')
            }));
        }

        panel.add(widget);
        this.getController('Uni.controller.Navigation').initBreadcrumbs();
        Ext.resumeLayouts();

        panel.doComponentLayout();
    },

    checkLicenseStatus: function () {
        var me = this;

        if (typeof me.applicationKey !== 'undefined' && me.applicationKey !== 'SYS') {
            Ext.Ajax.request({
                url: '/api/apps/apps/status/' + me.applicationKey,
                method: 'GET',
                async: false,
                success: function (response) {
                    var data = Ext.JSON.decode(response.responseText);
                    me.licenseStatus = data.status;
                    if (me.licenseStatus === 'EXPIRED') {
                        me.controllers = [];
                        me.getController('Uni.controller.Navigation').searchEnabled = false;
                    }
                },
                failure: function (response) {
                    me.licenseStatus = 'NO_LICENSE';
                }
            });
        }
    },

    showLicenseGraced: function () {
        if (!isNaN(this.licenseStatus) && !Ext.state.Manager.get('licenseGraced')) {
            Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
            Ext.state.Manager.set('licenseGraced', 'Y');

            var config = {
                title: Uni.I18n.translate('error.license', 'UNI', 'License'),
                msg: Uni.I18n.translate('error.license.graced', 'UNI', 'The system is currently running on a license that has a grace period. You have {0} day(s) remaining.', this.licenseStatus),
                modal: false,
                ui: 'message-error',
                icon: Ext.MessageBox.ERROR
            };

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
        }
    },

    redirectToLogin: function () {
        window.location = '/apps/login/index.html?expired&page='
        + window.location.pathname
        + window.location.hash;
    },

    getApplicationTitle: function (callback) {
        if (Ext.isFunction(callback)) {
            callback(this.applicationTitle);
        }
    },

    loadControllers: function () {
        for (var i = 0; i < this.controllers.length; i++) {
            var controller = this.controllers[i];

            try {
                this.getController(controller);
            } catch (ex) {
                console.error('Could not load the \'' + controller + '\' controller.');
            }
        }
    }
});
