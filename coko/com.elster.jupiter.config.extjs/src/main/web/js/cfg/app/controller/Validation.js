Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'ValidationActions',
        'Validators',
        'ValidationRuleProperties',
        'ReadingTypesForRule',
        'AvailableReadingTypes'
    ],

    views: [
        'validation.Browse',
        'validation.Edit'
    ],

    refs: [
        {
            ref: 'rulesGrid',
            selector: 'validationrulesetEdit #validationruleList'
        } ,
        {
            ref: 'rulePropertiesGrid',
            selector: 'validationrulesetEdit #validationrulepropertiesList'
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
            } ,
            '#validationruleList' : {
                select: this.editValidationRuleProperties
            }
        });
    },

    editValidationRuleSet: function (grid, record) {
        var view = Ext.widget('validationrulesetEdit');
        view.down('form').loadRecord(record);
        var me = this;
        me.getValidationRulesStore().load({
            params: {
                id: record.data.id
            },
            callback: function () {
                if (me.getValidationRulesStore().getTotalCount() > 0) {
                    me.getRulesGrid().getSelectionModel().select(0);
                }
            }});
    },

    editValidationRuleProperties: function (grid, record) {
        this.getRulePropertiesGrid().reconfigure(record.properties());
        var me = this;
        me.getReadingTypesForRuleStore().load({
            params: {
                id: record.data.id
            }});

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
