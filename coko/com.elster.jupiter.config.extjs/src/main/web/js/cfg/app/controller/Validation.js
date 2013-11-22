Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'ValidationActions',
        'Validators'
    ],

    views: [
        'validation.Browse',
        'validation.Edit'
    ],

    refs: [
        {
            ref: 'rulesGrid',
            selector: 'validationrulesetEdit #validationruleList'
        }
    ],


    init: function () {
        this.initMenu();

        this.control({
            'valiationrulesetList button[action=save]': {
                click: this.saveRuleSets
            },
            '#validationrulesetList': {
                itemdblclick: this.editValidationRuleSet
            }
        });
    },

    editValidationRuleSet: function (grid, record) {
        var view = Ext.widget('validationrulesetEdit');
        view.down('form').loadRecord(record);
        this.getRulesGrid().reconfigure(record.rules());
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
            href: Cfg.getApplication().getHistoryValidationController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('validationrulesetBrowse');
        Cfg.getApplication().getMainController().showContent(widget);
    }

});
