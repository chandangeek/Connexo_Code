Ext.define('Dcs.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation'
    ],

    controllers: [
        'Dcs.controller.history.Schedule'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            menuItem = Ext.create('Uni.model.MenuItem', {
                text: 'Scheduling',
                href: me.getApplication().getController('Dcs.controller.history.Schedule').tokenizeShowOverview(),
                glyph: 'workspace'
            });

        Uni.store.MenuItems.add(menuItem);
        me.initDefaultHistoryToken();
    },

    initDefaultHistoryToken: function () {
        var controller = this.getController('Dcs.controller.history.Schedule'),
            eventBus = this.getController('Uni.controller.history.EventBus'),
            defaultToken = controller.tokenizeShowOverview();

        eventBus.setDefaultToken(defaultToken);
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
