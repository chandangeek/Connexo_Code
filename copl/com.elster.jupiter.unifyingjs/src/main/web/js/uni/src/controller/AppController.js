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
        }
    ],

    /**
     * @cfg {String} applicationTitle
     */
    applicationTitle: 'Connexo',

    /**
     * @cfg {Object[]} packages
     *
     * TODO Example code.
     */
    packages: [],

    init: function () {
        var me = this;

        // Makes crossroads ignore state so that applications that don't use crossroads have no influence on crossroads' behavior.
        crossroads.ignoreState = true;

        me.getController('Uni.controller.Navigation').applicationTitle = me.applicationTitle;
        // TODO Set the title logo.
        me.getApplication().on('changecontentevent', me.showContent, me);
        me.loadDependencies();

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
