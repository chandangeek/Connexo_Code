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
        'validation.RuleSetEdit'
    ],

    refs: [
        {ref: 'ruleSetForm',selector: 'ruleSetPreview #rulesetForm'} ,
        {ref: 'ruleForm',selector: 'rulePreview #ruleForm'} ,
        {ref: 'rulesetPreviewTitle',selector: 'ruleSetPreview #rulesetPreviewTitle'} ,

        {ref: 'ruleSetsGrid',selector: 'validationrulesetList'},
        {ref: 'rulesGrid',selector: 'validationruleList'},
        {ref: 'ruleSetPreview',selector:'ruleSetPreview'},
        {ref: 'rulePreview',selector:'rulePreview'},
        {ref: 'ruleSetDetails',selector: 'ruleSetPreview #ruleSetDetails'},
        {ref: 'ruleSetEdit',selector: 'validationrulesetEdit'}
    ],

    init: function () {
        this.initMenu();

        this.control({
            '#validationrulesetList': {
                selectionchange: this.previewValidationRuleSet
            },
            '#validationruleList': {
                selectionchange: this.previewValidationRule
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
        var selectedSets = this.getRuleSetsGrid().getSelectionModel().getSelection();
        var ruleSetId = selectedSets[0].getId();
        var me = this;
        me.getValidationRulesStore().load({
            params: {
                id: ruleSetId
            }});
        var widget = Ext.widget('validationruleBrowse');
        Cfg.getApplication().getMainController().showContent(widget);
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
            //var ruleSetName = selectedSets[0].name();
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
