/**
 * @class Uni.controller.AppController
 */
Ext.define('Uni.controller.AppController', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.license.LicenseStatus'
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
    applicationTitle: '',

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

    init: function () {

        var me = this;
        if (Uni.Auth.hasAnyPrivilege(me.privileges)){
            me.initCrossroads();

            me.control({
                'viewport': {
                    afterrender: me.showLicenseExpired
                }
            });

            me.getController('Uni.controller.Navigation').applicationTitle = me.applicationTitle;
            me.getController('Uni.controller.Navigation').searchEnabled = me.searchEnabled;
            me.getController('Uni.controller.history.EventBus').setDefaultToken(me.defaultToken);
            me.getApplication().on('changecontentevent', me.showContent, me);
            me.getApplication().on('sessionexpired', me.redirectToLogin, me);
            me.checkLicenseStatus();
            me.loadControllers();
            me.callParent(arguments);
        }
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

        if (logo.rendered) {
            logo.setText(me.applicationTitle);
        } else {
            logo.text = me.applicationTitle;
        }

        me.callParent(arguments);
    },

    showContent: function (widget) {
        this.getContentPanel().removeAll();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    checkLicenseStatus: function () {
        var me = this;
        if (me.applicationKey !== 'SYS'){
            Ext.Ajax.request({
                url: '/api/apps/apps/status/'+me.applicationKey,
                method: 'GET',
                async: false,
                success: function(response){
                    me.licenseStatus = response.responseText;
                    if (me.licenseStatus === 'EXPIRED') {
                        me.controllers = [];
                        me.searchEnabled = false;
                    }

                },
                failure: function(response) {
                    me.licenseStatus = 'NO_LICENSE';
                }
            });
        }
    },

    showLicenseExpired : function () {
        var showGracedMsg = false,
            message = Uni.I18n.translate('error.license.expired', 'UNI', 'License expired.');

        if (!isNaN(this.licenseStatus) && !Ext.state.Manager.get('licenseGraced')) {
            Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
            Ext.state.Manager.set('licenseGraced', 'Y');
            message = Uni.I18n.translate('error.license.graced', 'UNI', 'License graced. {0} day(s)', [this.licenseStatus]);
            showGracedMsg = true;
        }

        if (this.licenseStatus === 'EXPIRED' || showGracedMsg) {
            var config = {
                title: Uni.I18n.translate('error.license', 'UNI', 'License'),
                msg: message,
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
                        ui: 'action',
                        handler: function () {
                            box.close();
                        }
                    }
                ]
            });

            box.show(config);
        }

        if (this.licenseStatus === 'EXPIRED') {
            this.showContent(Ext.widget('LicenseStatus'));
        }

    },

    redirectToLogin: function () {
        window.location = '/apps/login/index.html?expired&page='
            + window.location.pathname
            + window.location.hash;
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
