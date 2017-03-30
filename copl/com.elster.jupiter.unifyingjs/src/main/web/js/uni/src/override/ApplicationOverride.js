/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.ApplicationOverride', {
    override: 'Ext.app.Application',

    unifyingControllers: [
        'Uni.controller.Acknowledgements',
        'Uni.controller.Configuration',
        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.controller.Portal',
        'Uni.controller.Notifications'
    ],

    /**
     *
     */
    initControllers: function () {
        this.callParent(arguments);
        this.loadUnifyingControllers();
        this.loadPluginControllers();
    },

    loadUnifyingControllers: function () {
        var me = this;

        for (var i = 0, ln = me.unifyingControllers.length; i < ln; i++) {
            me.getController(me.unifyingControllers[i]);
        }
    },

    loadPluginControllers: function() {
        var me = this;
        Ldr.store.Pluggable.each(function (pluginScript) {
            Ext.require(pluginScript.get('mainController'),function(){
                me.getController(pluginScript.get('mainController'));
            });
        });
    }
});