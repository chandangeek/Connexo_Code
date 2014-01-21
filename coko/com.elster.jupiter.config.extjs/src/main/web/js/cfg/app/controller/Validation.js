Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules'
    ],

    models: [
        'ValidationRuleSet',
        'ValidationRule'
    ],

    views: [
        'validation.RuleSetBrowse',
        'validation.RuleSetList',
        'validation.RuleSetPreview',
        'validation.RuleBrowse',
        'validation.RuleList',
        'validation.RulePreview',
        'validation.RuleSetEdit',
        'validation.RulesContainer',
        'validation.RuleSetOverview'
    ],

    refs: [
        {ref: 'ruleSetForm',selector: 'ruleSetPreview #rulesetForm'} ,
        {ref: 'rulesetOverviewForm',selector: 'ruleSetOverview #rulesetOverviewForm'} ,
        {ref: 'ruleForm',selector: 'rulePreview #ruleForm'} ,
        {ref: 'rulesetPreviewTitle',selector: 'ruleSetPreview #rulesetPreviewTitle'} ,
        {ref: 'rulesetOverviewTitle',selector: 'ruleSetOverview #rulesetOverviewTitle'} ,
        {ref: 'ruleSetDetailsLink',selector: 'ruleSetPreview #ruleSetDetailsLink'} ,
        {ref: 'rulesListContainer', selector: 'rulesContainer > #rulesListContainer'},

        {ref: 'rulesContainer',selector: 'rulesContainer'} ,
        {ref: 'ruleSetsGrid',selector: 'validationrulesetList'},
        {ref: 'rulesGrid',selector: 'validationruleList'},
        {ref: 'ruleSetPreview',selector:'ruleSetPreview'},
        {ref: 'ruleSetOverview',selector:'ruleSetOverview'},
        {ref: 'rulePreview',selector:'rulePreview'},
        {ref: 'ruleSetDetails',selector: 'ruleSetPreview #ruleSetDetails'},
        {ref: 'ruleSetEdit',selector: 'validationrulesetEdit'},

        {ref: 'rulesListContainer', selector: 'rulesContainer > #rulesListContainer'}
    ],

    selectedRuleSet : null,

    init: function () {
        this.initMenu();

        this.control({
            '#validationrulesetList': {
                selectionchange: this.previewValidationRuleSet
            },
            '#validationruleList': {
                selectionchange: this.previewValidationRule
            },
            'rulesContainer button[action=showRulesAction]': {
                click: this.showRules
            },
            'rulesContainer button[action=showRulesetOverviewAction]': {
                click: this.showRuleSetOverview
            },


            'validationrulesetList button[action=newRuleset]': {
                click: this.newRuleSet
            },
            'ruleSetEdit button[action=saveRuleset]': {
                click: this.saveRuleset
            }
        });
    },

    saveRuleset: function() {
        var me = this;
        var win = button.up('window'),
        form = win.down('form'),

        record = form.getRecord(),
        values = form.getValues();
        if(!record){
            record = Ext.create(Cfg.model.ValidationRuleSet);
            record.set(values);
        } else {
            record.set(values);
        }

        record.save({
            success: function (record, operation) {
                record.commit();
                me.getValidationRuleSetsStore().reload(
                    {
                        callback: function(){
                            win.close();
                            me.showOverview();
                        }
                    });
            }
        });
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
    },

    showRules: function() {
        this.clearRulesListContainer();
        this.getRulesListContainer().add(Ext.create('Cfg.view.validation.RuleBrowse'));
        this.getRulesListContainer().doComponentLayout();
    },

    showRuleSetOverview: function() {
        var ruleSetOverviewWidget = Ext.create('Cfg.view.validation.RuleSetOverview');
        var ruleSetName = this.selectedRuleSet.get("name");
        this.getRulesetOverviewTitle().update('<h1>' + ruleSetName + ': Overview</h1>');
        this.getRulesetOverviewForm().loadRecord(this.selectedRuleSet);

        this.clearRulesListContainer();
        this.getRulesListContainer().add(ruleSetOverviewWidget);
        this.getRulesListContainer().doComponentLayout();

        //Cfg.getApplication().getMainController().showContent(rulesContainerWidget);
    },

    clearRulesListContainer: function () {
        var widget;
        while (widget = this.getRulesListContainer().items.first()) {
            this.getRulesListContainer().remove(widget,true);
        }
    },

    // set selected ruleset + get rules
    // fill container with ruleset overview
    showRulesContainer: function(id) {
        var me = this;
        var ruleSetOverviewWidget = Ext.create('Cfg.view.validation.RuleSetOverview');

        me.getValidationRuleSetsStore().load();
        me.getValidationRuleSetsStore().filter('id', id);
        me.selectedRuleSet = me.getValidationRuleSetsStore().first();
        me.getValidationRuleSetsStore().clearFilter();

        var ruleSetName = me.selectedRuleSet.get("name");
        this.getRulesetOverviewTitle().update('<h1>' + ruleSetName + ': Overview</h1>');
        this.getRulesetOverviewForm().loadRecord(me.selectedRuleSet);
        var ruleSetId = me.selectedRuleSet.get("id");
        me.getValidationRulesStore().load({
            params: {
                id: ruleSetId
            }});
        var rulesContainerWidget = Ext.widget('rulesContainer');

        this.clearRulesListContainer();
        this.getRulesListContainer().add(ruleSetOverviewWidget);
        this.getRulesListContainer().doComponentLayout();

        Cfg.getApplication().getMainController().showContent(rulesContainerWidget);
    },

    newRuleSet: function () {
        var view = Ext.widget('validationrulesetEdit');
        view.down('form').loadRecord(Ext.create('Cfg.model.ValidationRuleSet'));
    },


    previewValidationRuleSet: function (grid, record) {
        var selectedSets = this.getRuleSetsGrid().getSelectionModel().getSelection();
        if (selectedSets.length == 1) {
            this.getRuleSetForm().loadRecord(selectedSets[0]);
            var ruleSetName = this.getRuleSetForm().form.findField('name').getSubmitValue();
            this.getRuleSetPreview().show();
            this.getRuleSetDetailsLink().update('<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation/rulesforset/' + selectedSets[0].getId() + '">View details</a>');
            this.getRulesetPreviewTitle().update('<h4>' + ruleSetName + '</h4>');
        } else {
            this.getRuleSetPreview().hide();
        }
    },

    previewValidationRule: function (grid, record) {
        var selectedRules = this.getRulesGrid().getSelectionModel().getSelection();
        if (selectedRules.length == 1) {
            this.getRuleForm().loadRecord(selectedRules[0]);
            this.getRulePreview().show();
        } else {
            this.getRulePreview().hide();
        }
    }



});
