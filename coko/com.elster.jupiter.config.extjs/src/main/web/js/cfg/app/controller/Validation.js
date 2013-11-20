Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [

    ],

    views: [

    ],


    init: function () {
        this.initMenu();

        this.control({
            'valiationrulesetList button[action=save]': {
                click: this.saveRuleSets
            }
        });
    },

    saveRuleSets: function (button) {

    },
    saveSuccess: function () {
        alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Validation',
            href: Cfg.getApplication().getHistoryEventTypeController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {

    }

});
