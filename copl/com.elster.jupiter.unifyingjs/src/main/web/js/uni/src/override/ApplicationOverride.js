Ext.define('Uni.override.ApplicationOverride', {
    override: 'Ext.app.Application',

    unifyingControllers: [
        'Uni.controller.Breadcrumb',
        'Uni.controller.Error',
        'Uni.controller.Navigation'
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