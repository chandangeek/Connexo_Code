/**
 * @class Uni.controller.AppController
 */
Ext.define('Uni.controller.AppController', {
    extend: 'Ext.app.Controller',

    requires: [
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

            me.getController('Uni.controller.Navigation').applicationTitle = me.applicationTitle;
            me.getController('Uni.controller.Navigation').searchEnabled = me.searchEnabled;
            me.getController('Uni.controller.history.EventBus').setDefaultToken(me.defaultToken);
            me.getApplication().on('changecontentevent', me.showContent, me);
            me.getApplication().on('sessionexpired', me.redirectToLogin, me);

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
