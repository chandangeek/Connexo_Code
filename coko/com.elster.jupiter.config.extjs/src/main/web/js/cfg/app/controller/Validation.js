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
        {ref: 'rulePreviewTitle',selector: 'rulePreview #rulePreviewTitle'} ,
        {ref: 'readingTypesArea',selector: 'rulePreview #readingTypesArea'} ,
        {ref: 'propertiesArea',selector: 'rulePreview #propertiesArea'} ,
        {ref: 'ruleForm',selector: 'rulePreview #ruleForm'} ,
        {ref: 'rulesetOverviewTitle',selector: 'ruleSetOverview #rulesetOverviewTitle'} ,
        {ref: 'ruleBrowseTitle',selector: 'validationruleBrowse #ruleBrowseTitle'} ,
        {ref: 'ruleSetDetailsLink',selector: 'ruleSetPreview #ruleSetDetailsLink'} ,
        {ref: 'rulesListContainer', selector: 'rulesContainer > #rulesListContainer'},

        {ref: 'ruleSetOverviewLink',selector: 'rulesContainer #ruleSetOverviewLink'} ,
        {ref: 'rulesLink',selector: 'rulesContainer #rulesLink'} ,

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

    showRules: function(id) {
        var me = this;
        var rulesWidget = Ext.create('Cfg.view.validation.RuleBrowse');
        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getById(id);
                var ruleSetName = selectedRuleSet.get("name");
                me.getRuleBrowseTitle().update('<h1>' + ruleSetName + ' - Rules</h1>');
                me.getValidationRulesStore().load({
                    params: {
                        id: id
                    }});

                var rulesContainerWidget = Ext.create('Cfg.view.validation.RulesContainer');
                Cfg.getApplication().getMainController().showContent(rulesContainerWidget);

                me.updateMenulinks(id);
;               me.highlightRulesButton();

                me.getRulesListContainer().add(rulesWidget);
                me.getRulesListContainer().doComponentLayout();

            }
        });
    },

    highlightRuleSetOverviewButton: function() {
        this.getRuleSetOverviewLink().setBorder(1);
        this.getRulesLink().setBorder(0)
    },

    highlightRulesButton: function() {
        this.getRuleSetOverviewLink().setBorder(0);
        this.getRulesLink().setBorder(1)
    },

    showRuleSetOverview: function(id) {
        var me = this;
        var ruleSetOverviewWidget = Ext.create('Cfg.view.validation.RuleSetOverview');

        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getById(id);
                var ruleSetName = selectedRuleSet.get("name");
                me.getRulesetOverviewTitle().update('<h1>' + ruleSetName + ' - Overview</h1>');
                me.getRulesetOverviewForm().loadRecord(selectedRuleSet);

                var rulesContainerWidget = Ext.create('Cfg.view.validation.RulesContainer');
                Cfg.getApplication().getMainController().showContent(rulesContainerWidget);

                me.updateMenulinks(id);
                me.highlightRuleSetOverviewButton();

                me.getRulesListContainer().add(ruleSetOverviewWidget);
                me.getRulesListContainer().doComponentLayout();

            }
        });
    },

    updateMenulinks: function(id) {
        /*this.getRuleSetOverviewLink().update(
            '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation/overview/' + id + '">Overview</a>');
        this.getRulesLink().update(
            '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation/rules/' + id + '">Rules</a>');  */
        this.getRuleSetOverviewLink().setHref('#/validation/overview/' + id);
        this.getRulesLink().setHref('#/validation/rules/' + id);

    },

    clearRulesListContainer: function () {
        var widget;
        while (widget = this.getRulesListContainer().items.first()) {
            this.getRulesListContainer().remove(widget,true);
        }
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
            this.getRuleSetDetailsLink().update('<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation/overview/' + selectedSets[0].getId() + '">View details</a>');
            this.getRulesetPreviewTitle().update('<h4>' + ruleSetName + '</h4>');
        } else {
            this.getRuleSetPreview().hide();
        }
    },

    previewValidationRule: function (grid, record) {
        var selectedRules = this.getRulesGrid().getSelectionModel().getSelection();
        if (selectedRules.length == 1) {
            var selectedRule = selectedRules[0];
            this.getRuleForm().loadRecord(selectedRule);
            this.getRulePreviewTitle().update('<h4>' + selectedRule.get("displayName") + '</h4>');
            this.addProperties(selectedRule);
            this.addReadingTypes(selectedRule);
            this.getRulePreview().show();
        } else {
            this.getRulePreview().hide();
        }
    },



    addProperties: function(selectedRule) {
        var properties = selectedRule.data.properties;
        this.getPropertiesArea().removeAll();
        for (var i = 0; i < properties.length; i++) {
            var property = properties[i];
            var propertyName = property.name;
            var propertyValue = property.value;
            var required = property.required;
            var label = propertyName + ':';
            if (required) {
                label = '* ' + label;
            }
            this.getPropertiesArea().add(
                {
                    xtype: 'displayfield',
                    fieldLabel: label,
                    value: propertyValue,
                    labelAlign: 'right',
                    labelWidth:	150
                }
            );

        }

    },

    addReadingTypes: function(selectedRule) {
        var readingTypes = selectedRule.data.readingTypes;
        this.getReadingTypesArea().removeAll();
        for (var i = 0; i < readingTypes.length; i++) {
            var readingType = readingTypes[i];
            var aliasName = readingType.aliasName;
            var mRID = readingType.mRID;
            this.getReadingTypesArea().add(
                {
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    defaults: {
                        flex: 1,
                        hideLabel: true
                    },
                    items: [
                        {
                            xtype: 'component',
                            html: mRID,
                            width: 250
                        },
                        {
                            xtype: 'component',
                            html: '<span style="color:grey"><i>' + aliasName + '</i></span>',
                            margin: '0 0 0 20'
                        }
                    ]
                }
            );
        }
    }



});
