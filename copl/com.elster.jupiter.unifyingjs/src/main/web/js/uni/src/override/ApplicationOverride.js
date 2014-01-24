/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.ApplicationOverride', {
    override: 'Ext.app.Application',

    unifyingControllers: [
        'Uni.controller.Configuration',
        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.controller.Notifications',
        'Uni.controller.Search'
    ],

    /**
     *
     */
    initControllers: function () {
        this.callParent(arguments);
        this.loadUnifyingControllers();
    },

    loadUnifyingControllers: function () {
        var me = this;

        for (var i = 0, ln = me.unifyingControllers.length; i < ln; i++) {
            me.getController(me.unifyingControllers[i]);
        }
    }

});