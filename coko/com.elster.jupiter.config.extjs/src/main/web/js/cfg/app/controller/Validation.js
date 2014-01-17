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
        {ref: 'rulesetOverviewTitle',selector: 'ruleSetPreview #rulesetOverviewTitle'} ,
        {ref: 'ruleSetDetailsLink',selector: 'ruleSetPreview #ruleSetDetailsLink'} ,
        {ref: 'rulesListContainer',selector: 'rulesContainer #rulesListContainer'} ,

        {ref: 'ruleSetsGrid',selector: 'validationrulesetList'},
        {ref: 'rulesGrid',selector: 'validationruleList'},
        {ref: 'ruleSetPreview',selector:'ruleSetPreview'},
        {ref: 'ruleSetOverview',selector:'ruleSetOverview'},
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

    showRulesContainer: function(id) {
        /*var selectedSets = this.getRuleSetsGrid().getSelectionModel().getSelection();
        var ruleSetId = selectedSets[0].getId();
        var me = this;
        me.getValidationRulesStore().load({
            params: {
                id: ruleSetId
            }});
        var widget = Ext.widget('rulesContainer');
        Cfg.getApplication().getMainController().showContent(widget);
        this.getRulesListContainer().add(Ext.create('Cfg.view.validation.RuleBrowse'));*/

        var ruleSetOverviewWidget = Ext.create('Cfg.view.validation.RuleSetOverview');
        var selectedSets = this.getRuleSetsGrid().getSelectionModel().getSelection();
        if (selectedSets.length == 1) {
            var ruleSet = selectedSets[0];
            var ruleSetId = ruleSet.get("id");
            var ruleSetName = ruleSet.get("name");
            var ruleSetDescription = ruleSet.get("description");
            alert(ruleSetId + ', ' + ruleSetName + ', ' + ruleSetDescription);
            this.getRulesetOverviewForm().loadRecord(selectedSets[0]);


            this.getRulesetOverviewForm().form.setValues({
                name: ruleSetName,
                description: ruleSetDescription,
                numberOfInactiveRules: 1,
                numberOfRules: 0
            });
            //var ruleSetName = this.getRulesetOverviewForm().form.findField('name').getSubmitValue();
            //this.getRulesetOverviewTitle().update('<h4>' + ruleSetName + ' - Overview</h4>');
        }
        var rulesContainerWidget = Ext.widget('rulesContainer');
        Cfg.getApplication().getMainController().showContent(rulesContainerWidget);
        this.getRulesListContainer().add(Ext.widget('ruleSetOverview'));
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
