Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'ValidationPropertySpecs'
    ],

    models: [
        'ValidationRuleSet',
        'ValidationRule',
        'ReadingType',
        'ValidationRuleProperty'
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
        {ref: 'addRuleLink',selector: 'validationruleList #addRuleLink'} ,

        {ref: 'rulesContainer',selector: 'rulesContainer'} ,
        {ref: 'ruleSetsGrid',selector: 'validationrulesetList'},
        {ref: 'rulesGrid',selector: 'validationruleList'},
        {ref: 'ruleSetPreview',selector:'ruleSetPreview'},
        {ref: 'ruleSetOverview',selector:'ruleSetOverview'},
        {ref: 'rulePreview',selector:'rulePreview'},
        {ref: 'ruleSetDetails',selector: 'ruleSetPreview #ruleSetDetails'},
        {ref: 'ruleSetEdit',selector: 'validationrulesetEdit'},

        {ref: 'rulesListContainer', selector: 'rulesContainer > #rulesListContainer'},
        {ref: 'newRuleSetForm', selector: 'createRuleSet > #newRuleSetForm'},

        {ref: 'createRuleSet',selector: 'createRuleSet'},
        {ref: 'addRule',selector: 'addRule'},

        {ref: 'readingValuesTextFieldsContainer',selector: 'addRule #readingValuesTextFieldsContainer'} ,
        {ref: 'propertiesContainer',selector: 'addRule #propertiesContainer'},
        {ref: 'removeReadingTypesButtonsContainer',selector: 'addRule #removeReadingTypesButtonsContainer'},
        {ref: 'validatorCombo',selector: 'addRule #validatorCombo'}


    ],

    readingTypeIndex: 2,


    init: function () {
        this.initMenu();

        this.control({
            '#validationrulesetList': {
                selectionchange: this.previewValidationRuleSet
            },
            '#validationruleList': {
                selectionchange: this.previewValidationRule
            },
            'createRuleSet button[action=createNewRuleSet]': {
                click: this.createNewRuleSet
            },
            'addRule button[action=createRuleAction]': {
            click: this.createNewRule
            },
            'addRule button[action=addRuleAction]': {
                click: this.addRule
            },
            'addRule button[action=addReadingTypeAction]': {
                click: this.addReadingType
            },
            'addRule combobox[itemId=validatorCombo]': {
                change: this.updateProperties
            }

        });
    },

    getRuleSetIdFromHref: function() {
        var urlPart = 'validation/addRule/';
        var index = location.href.indexOf(urlPart);
        return parseInt(location.href.substring(index + urlPart.length));
    },


    createNewRule: function(button) {
        var me = this;
        var form = button.up('panel');
        if (form.isValid()) {
            var ruleSetId = this.getRuleSetIdFromHref();
            var record = Ext.create(Cfg.model.ValidationRule);
            var values = form.getValues();

            var readingTypes = this.getReadingValuesTextFieldsContainer().items;
            var rule = values.implementation;
            var properties = this.getPropertiesContainer().items;

            record.set('implementation', rule);

            for (var i = 0; i < readingTypes.items.length; i++) {
                var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                readingTypeRecord.set('mRID', readingTypes.items[i].value);
                record.readingTypes().add(readingTypeRecord);
            }

            for (var i = 0; i < properties.items.length; i++) {
                var propertyRecord = Ext.create(Cfg.model.ValidationRuleProperty);
                propertyRecord.set('value', properties.items[i].value);
                propertyRecord.set('name', properties.items[i].itemId);
                record.properties().add(propertyRecord);

            }
            record.save({
                params: {
                    id: ruleSetId
                },
                success: function (record, operation) {
                    record.commit();
                    me.getValidationRuleSetsStore().reload(
                        {
                            callback: function(){
                                location.href = '#validation/rules/' + ruleSetId;
                            }
                        });
                }
            });
        }
    },

    updateProperties: function(field, oldValue, newValue) {
        this.getPropertiesContainer().removeAll();
        var allPropertiesStore = this.getValidationPropertySpecsStore();
        allPropertiesStore.clearFilter();
        allPropertiesStore.filter('validator', field.value);
        for (var i = 0; i < allPropertiesStore.data.items.length; i++) {
            var property = allPropertiesStore.data.items[i];
            var label = property.data.name;
            var itemIdValue = property.data.name;
            var optional =  property.data.optional;
            if (optional) {
                label = label + ' (optional)';
            }
            if (optional) {
                this.getPropertiesContainer().add(
                    {
                        xtype: 'textfield',
                        fieldLabel: label,
                        labelAlign: 'right',
                        itemId: itemIdValue,
                        labelWidth:	250
                    }
                );
            } else{
                this.getPropertiesContainer().add(
                    {
                        xtype: 'textfield',
                        fieldLabel: label,
                        allowBlank: false,  // requires a non-empty value
                        blankText: 'This is a required field',
                        msgTarget: 'under',
                        labelAlign: 'right',
                        itemId: itemIdValue,
                        labelWidth:	250
                    }
                );
            }

        }


    },

    addReadingType: function() {
        var me = this;
        var indexToRemove = me.readingTypeIndex;
        this.getReadingValuesTextFieldsContainer().add(
            {
                xtype: 'textfield',
                fieldLabel: '&nbsp',
                labelAlign: 'right',
                allowBlank: false,  // requires a non-empty value
                blankText: 'This is a required field',
                msgTarget: 'under',
                labelWidth:	250,
                itemId: 'readingTypeTextField' + me.readingTypeIndex
            }
        );
        this.getRemoveReadingTypesButtonsContainer().add(
            {
                text: '-',
                xtype: 'button',
                action: 'removeReadingTypeAction',
                pack: 'center',
                margin:'0 0 5 0',
                width: 30,
                itemId: 'readingTypeRemoveButton'  + me.readingTypeIndex,
                handler: function() {
                    me.getReadingValuesTextFieldsContainer().remove(Ext.ComponentQuery.query('#readingTypeTextField'  + indexToRemove)[0]);
                    me.getRemoveReadingTypesButtonsContainer().remove(Ext.ComponentQuery.query('#readingTypeRemoveButton'  + indexToRemove)[0]);
                }
            }
        );
        me.readingTypeIndex = me.readingTypeIndex + 1;
    },

    addRule: function(id) {
        var view = Ext.create('Cfg.view.validation.AddRule');
        Cfg.getApplication().getMainController().showContent(view);
    },

    createNewRuleSet: function(button) {
        var me = this;
        var form = button.up('panel');
        if (form.isValid()) {
            var record = record = Ext.create(Cfg.model.ValidationRuleSet);
            var values = form.getValues();
                record.set(values);
                record.save({
                    success: function (record, operation) {
                        record.commit();
                        me.getValidationRuleSetsStore().reload(
                            {
                                callback: function(){
                                    location.href = '#validation/rules/' + record.getId();
                                }
                            });
                    }
            })
        }
    },

    initMenu: function () {
        Uni.store.MenuItems.removeAll();
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Validation',
            href: Cfg.getApplication().getHistoryValidationController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    newRuleSet: function () {
        var view = Ext.create('Cfg.view.validation.CreateRuleSet');
        Cfg.getApplication().getMainController().showContent(view);
    },

    showOverview: function () {
        this.initMenu();
        var widget = Ext.widget('validationrulesetBrowse');
        Cfg.getApplication().getMainController().showContent(widget);
    },

    showRules: function(id) {
        var me = this;
        var rulesWidget = Ext.create('Cfg.view.validation.RuleBrowse');
        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        //this.getValidationRulesStore().clearFilter();
        //this.getValidationRulesStore().filter('ruleSetId', id);

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
                me.highlightRulesButton();

                me.getRulesListContainer().add(rulesWidget);
                me.getRulesListContainer().doComponentLayout();
                //me.getAddRuleLink().el.dom.href = '#/validation/addRule/';
                me.getAddRuleLink().setHref('#/validation/addRule/' + id);

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
            var label = propertyName;
            if (!required) {
                label = label + ' (optional):';
            }
            this.getPropertiesArea().add(
                {
                    xtype: 'displayfield',
                    fieldLabel: label,
                    value: propertyValue,
                    //labelAlign: 'right',
                    labelWidth:	250
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
