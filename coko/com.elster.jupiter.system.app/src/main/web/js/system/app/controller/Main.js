/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
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

    packages: [
        {
            name: 'Cfg',
            controller: 'Cfg.controller.Main',
            path: '../../apps/cfg/app'
        },
        {
            name: 'Usr',
            controller: 'Usr.controller.Main',
            path: '../../apps/usr/app'
        }
    ],

    init: function () {
        // Makes crossroads ignore state so that applications that don't use crossroads have no influence on crossroads' behavior.
        crossroads.ignoreState = true;

        this.getController('Uni.controller.Navigation').applicationTitle = 'Connexo Pulse';
        this.getApplication().on('changecontentevent', this.showContent, this);
        this.loadDependencies();
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
