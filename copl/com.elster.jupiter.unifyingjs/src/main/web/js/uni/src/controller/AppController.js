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
     * @cfg {Object[]} packages
     *
     * The packages that need to be loaded in by the application.
     *
     * @example
     *     {
     *         name: 'Cfg',
     *         controller: 'Cfg.controller.Main',
     *         path: '../../apps/cfg/app'
     *     },
     *     {
     *         name: 'Mdc',
     *         controller: 'Mdc.controller.Main',
     *         path: '../../apps/mdc/app'
     *     }
     */
    packages: [],

    init: function () {
        var me = this;

        me.initCrossroads();

        me.getController('Uni.controller.Navigation').applicationTitle = me.applicationTitle;
        me.getApplication().on('changecontentevent', me.showContent, me);

        me.loadDependencies();
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

    loadDependencies: function () {
        for (var i = 0; i < this.packages.length; i++) {
            var pkg = this.packages[i];

            // <debug>
            Ext.Loader.setPath(pkg.name, pkg.path);
            // </debug>

            try {
                this.getController(pkg.controller);
            } catch (ex) {
                console.log('Could not load the \'' + pkg.name + '\' bundle.');
            }
        }
    }
});
