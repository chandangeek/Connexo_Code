Ext.define('Cfg.controller.EventType', {
    extend: 'Ext.app.Controller',

    stores: [
        'EventTypes'
    ],

    views: [
        'eventtype.Browse'
    ],


    init: function () {
        this.initMenu();

        this.control({
            'eventtypeList button[action=save]': {
                click: this.saveEventTypes
            }
        });
    },

    saveEventTypes: function (button) {
        this.getEventTypesStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    },
    saveSuccess: function () {
        alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'EventTypes',
            href: Cfg.getApplication().getHistoryEventTypeController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('eventtypeBrowse');
        Cfg.getApplication().getMainController().showContent(widget);
    }

});
