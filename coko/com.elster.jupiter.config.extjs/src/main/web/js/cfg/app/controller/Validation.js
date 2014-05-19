Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'ValidationPropertySpecs'
    ],

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
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
        'validation.RulesContainer',
        'validation.RuleSetOverview'
    ],

    refs: [
        {ref: 'ruleSetForm', selector: 'ruleSetPreview #rulesetForm'} ,
        {ref: 'rulesetOverviewForm', selector: 'ruleSetOverview #rulesetOverviewForm'} ,
        {ref: 'ruleForm', selector: 'rulePreview #ruleForm'} ,
        {ref: 'rulesetPreviewTitle', selector: 'ruleSetPreview #rulesetPreviewTitle'} ,
        {ref: 'rulePreviewTitle', selector: 'rulePreview #rulePreviewTitle'} ,
        {ref: 'readingTypesArea', selector: 'rulePreview #readingTypesArea'} ,
        {ref: 'propertiesArea', selector: 'rulePreview #propertiesArea'} ,
        {ref: 'ruleForm', selector: 'rulePreview #ruleForm'} ,
        {ref: 'rulesetOverviewTitle', selector: 'ruleSetOverview #rulesetOverviewTitle'} ,
        {ref: 'ruleBrowseTitle', selector: 'validationruleBrowse #ruleBrowseTitle'} ,
        {ref: 'ruleSetDetailsLink', selector: 'ruleSetPreview #ruleSetDetailsLink'} ,
        {ref: 'cancelAddRuleLink', selector: 'addRule #cancelAddRuleLink'} ,
        {ref: 'rulesListContainer', selector: 'rulesContainer > #rulesListContainer'},

        {ref: 'ruleSetOverviewLink', selector: 'rulesContainer #ruleSetOverviewLink'} ,
        {ref: 'rulesLink', selector: 'rulesContainer #rulesLink'} ,
        {ref: 'addRuleLink', selector: 'validationruleList #addRuleLink'} ,

        {ref: 'rulesContainer', selector: 'rulesContainer'} ,
        {ref: 'ruleSetsGrid', selector: 'validationrulesetList'},
        {ref: 'rulesGrid', selector: 'validationruleList'},
        {ref: 'ruleSetPreview', selector: 'ruleSetPreview'},
        {ref: 'ruleSetOverview', selector: 'ruleSetOverview'},
        {ref: 'rulePreview', selector: 'rulePreview'},
        {ref: 'ruleSetDetails', selector: 'ruleSetPreview #ruleSetDetails'},
        {ref: 'ruleSetEdit', selector: 'validationrulesetEdit'},

        {ref: 'stepsContainer', selector: 'rulesContainer > #stepsContainer'},
        {ref: 'newRuleSetForm', selector: 'createRuleSet > #newRuleSetForm'},

        {ref: 'createRuleSet', selector: 'createRuleSet'},
        {ref: 'addRule', selector: 'addRule'},

        {ref: 'rulesContainer', selector: 'rulesContainer'},
        {ref: 'stepsMenu', selector: 'rulesContainer > #stepsMenu'},

        {ref: 'readingValuesTextFieldsContainer', selector: 'addRule #readingValuesTextFieldsContainer'} ,
        {ref: 'propertiesContainer', selector: 'addRule #propertiesContainer'},
        {ref: 'removeReadingTypesButtonsContainer', selector: 'addRule #removeReadingTypesButtonsContainer'},
        {ref: 'validatorCombo', selector: 'addRule #validatorCombo'},

        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'}
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
        var urlPart = 'administration/validation/addRule/';
        var index = location.href.indexOf(urlPart);
        return parseInt(location.href.substring(index + urlPart.length));
    },


    createNewRule: function(button) {
        var me = this;
        var form = button.up('panel');
        //if (form.isValid()) {
            var ruleSetId = this.getRuleSetIdFromHref();
            var record = Ext.create(Cfg.model.ValidationRule);
            var values = form.getValues();

            var readingTypes = this.getReadingValuesTextFieldsContainer().items;
            var rule = values.implementation;
            var name = values.name;
            var properties = this.getPropertiesContainer().items;

            record.set('implementation', rule);
            record.set('name', name);

            for (var i = 0; i < readingTypes.items.length; i++) {
                var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                var readingType = readingTypes.items[i].items.items[0].value;
                readingTypeRecord.set('mRID',readingType );
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
                                location.href = '#administration/validation/rules/' + ruleSetId;
                            }
                        });
                },
                failure: function(record,operation){
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                    }
                }
                });
        //}
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
                        xtype: 'numberfield',
                        fieldLabel: label,
                        validator:function(text){
                            if(Ext.util.Format.trim(text).length==0)
                                return 'This field is required';
                            else
                                return true;
                        },
                        required: true,
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
                xtype: 'container',
                itemId: 'readingType' + me.readingTypeIndex,
                name: 'readingType' + me.readingTypeIndex,
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'textfield',
                        fieldLabel: '&nbsp',
                        labelAlign: 'right',
                        /*validator:function(text){
                            if(Ext.util.Format.trim(text).length==0)
                                return Uni.I18n.translate('validation.requiredField', 'CFG', 'This field is required');
                            else
                                return true;
                        },
                        required: true,    */
                        msgTarget: 'under',
                        labelWidth:	250,
                        maxLength: 80,
                        enforceMaxLength: true ,
                        width: 600
                    },
                    {
                        text: '-',
                        xtype: 'button',
                        action: 'removeReadingTypeAction',
                        pack: 'center',
                        margin:'0 0 5 5',
                        width: 30,
                        itemId: 'readingTypeRemoveButton'  + me.readingTypeIndex,
                        handler: function() {
                            me.getReadingValuesTextFieldsContainer().remove(Ext.ComponentQuery.query('#readingType'  + indexToRemove)[0]);
                        }
                    }
                ]
            }

        );
        me.readingTypeIndex = me.readingTypeIndex + 1;
    },

    addRule: function(id) {
        var me = this;
        var view = Ext.create('Cfg.view.validation.AddRule');
        this.getCancelAddRuleLink().update('<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#administration/validation/rules/' + id + '">' + Uni.I18n.translate('general.cancel', 'CFG', 'Cancel') + '</a>');
        this.getApplication().getController('Cfg.controller.Main').showContent(view);
        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getById(id);
                var ruleSetName = selectedRuleSet.get("name");
                me.createAddRuleBreadCrumbs(id, ruleSetName);
            }
        });
    },

    createAddRuleBreadCrumbs: function(ruleSetId, ruleSetName) {
        var me = this;

        var breadcrumbs = me.getBreadCrumbs();
        var breadcrumbRules = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Add rule'
        });

        var breadcrumbRuleSet = Ext.create('Uni.model.BreadcrumbItem', {
            text: ruleSetName,
            href: 'overview/' + ruleSetId
        });

        var breadcrumbRuleSets = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Validation rule sets',
            href: 'validation'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Administration',
            href: '#administration'
        });
        breadcrumbRuleSet.setChild(breadcrumbRules);
        breadcrumbRuleSets.setChild(breadcrumbRuleSet);
        breadcrumbParent.setChild(breadcrumbRuleSets);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);

    },

    createNewRuleSet: function(button) {
        var me = this;
        var form = Ext.ComponentQuery.query('#newRuleSetForm')[0].form;
        if (form.isValid()) {
            var record = record = Ext.create(Cfg.model.ValidationRuleSet);
            var values = form.getValues();
                record.set(values);
                record.save({
                    success: function (record, operation) {
                        //record.commit();
                        me.getValidationRuleSetsStore().reload(
                            {
                                callback: function(){
                                    location.href = '#administration/validation/rules/' + record.getId();
                                }
                            });
                    },
                    failure: function(record,operation){
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.getForm().markInvalid(json.errors);
                        }
                    }
            })
        }
    },

    initMenu: function () {

    },

    newRuleSet: function () {
        var view = Ext.create('Cfg.view.validation.CreateRuleSet');
        this.getApplication().getController('Cfg.controller.Main').showContent(view);
        this.createNewRuleSetBreadCrumbs();

    },

    createNewRuleSetBreadCrumbs: function() {
        var me = this;

        var breadcrumbs = me.getBreadCrumbs();

        var breadcrumbNewRuleSet = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Create rule set'
        });

        var breadcrumbRuleSets = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Validation rule sets',
            href: 'validation'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Administration',
            href: '#administration'
        });
        breadcrumbRuleSets.setChild(breadcrumbNewRuleSet);
        breadcrumbParent.setChild(breadcrumbRuleSets);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    showRuleSets: function () {
        this.initMenu();
        var widget = Ext.widget('validationrulesetBrowse');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
        this.createRuleSetsBreadCrumbs();
    },

    createRuleSetsBreadCrumbs: function() {
        var me = this;

        var breadcrumbs = me.getBreadCrumbs();

        var breadcrumbRulesets = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Validation rule sets'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Administration',
            href: '#administration'
        });
        breadcrumbParent.setChild(breadcrumbRulesets);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);

    },

    createRulesBreadCrumbs: function(ruleSetId, ruleSetName) {
        var me = this;

        var breadcrumbs = me.getBreadCrumbs();
        var breadcrumbRules = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Rules'
        });

        var breadcrumbRuleSet = Ext.create('Uni.model.BreadcrumbItem', {
            text: ruleSetName,
            href: 'overview/' + ruleSetId
        });

        var breadcrumbRuleSets = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Validation rule sets',
            href: 'validation'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Administration',
            href: '#administration'
        });
        breadcrumbRuleSet.setChild(breadcrumbRules);
        breadcrumbRuleSets.setChild(breadcrumbRuleSet);
        breadcrumbParent.setChild(breadcrumbRuleSets);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);

    },

    createRulesOverviewBreadCrumbs: function(ruleSetId, ruleSetName) {
        var me = this;

        var breadcrumbs = me.getBreadCrumbs();
        var breadcrumbRulesOverview = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Overview'
        });

        var breadcrumbRuleSet = Ext.create('Uni.model.BreadcrumbItem', {
            text: ruleSetName,
            href: 'overview/' + ruleSetId
        });

        var breadcrumbRuleSets = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Validation rule sets',
            href: 'validation'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Administration',
            href: '#administration'
        });
        breadcrumbRuleSet.setChild(breadcrumbRulesOverview);
        breadcrumbRuleSets.setChild(breadcrumbRuleSet);
        breadcrumbParent.setChild(breadcrumbRuleSets);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);

    },



    showRules: function(id) {
        var me = this;
        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        this.getValidationRulesStore().clearFilter();
        this.getValidationRulesStore().filter('ruleSetId', id);

        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getById(id);
                var ruleSetName = selectedRuleSet.get("name");

                if (Ext.ComponentQuery.query('#rulesContainer').length == 0) {
                    var rulesContainerWidget = Ext.create('Cfg.view.validation.RulesContainer', {ruleSetId: id});
                    me.getApplication().getController('Cfg.controller.Main').showContent(rulesContainerWidget);
                }

                me.getRuleBrowseTitle().update('<h1>' + ruleSetName + ' - Rules</h1>');
                me.goToMenuItem(1);
                me.createRulesBreadCrumbs(id, ruleSetName);
            }
        });
    },

    goToMenuItem: function(i) {
        Ext.ComponentQuery.query('#stepsContainer')[0].getLayout().setActiveItem(i);
    },


    showRuleSetOverview: function(id) {
        var me = this;

        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getById(id);
                var ruleSetName = selectedRuleSet.get("name");

                if (Ext.ComponentQuery.query('#rulesContainer').length == 0) {
                    var rulesContainerWidget = Ext.create('Cfg.view.validation.RulesContainer', {ruleSetId: id});
                    me.getApplication().getController('Cfg.controller.Main').showContent(rulesContainerWidget);
                }

                me.getRulesetOverviewTitle().update('<h1>' + ruleSetName + ' - Overview</h1>');
                me.getRulesetOverviewForm().loadRecord(selectedRuleSet);
                me.goToMenuItem(0);
                me.createRulesOverviewBreadCrumbs(id, ruleSetName);

            }
        });
    },

    previewValidationRuleSet: function (grid, record) {
        var selectedSets = this.getRuleSetsGrid().getSelectionModel().getSelection();
        if (selectedSets.length == 1) {
            this.getRuleSetForm().loadRecord(selectedSets[0]);
            var ruleSetName = this.getRuleSetForm().form.findField('name').getSubmitValue();
            this.getRuleSetPreview().getLayout().setActiveItem(1);
            //this.getRulesetPreviewTitle().update('<h4>' + ruleSetName + '</h4>');
            this.getRuleSetPreview().getHeader().setTitle(ruleSetName);
        } else {
            this.getRuleSetPreview().getLayout().setActiveItem(0);
        }
    },

    previewValidationRule: function (grid, record) {
        var selectedRules = this.getRulesGrid().getSelectionModel().getSelection();
        if (selectedRules.length == 1) {
            var selectedRule = selectedRules[0];
            this.getRuleForm().loadRecord(selectedRule);
            this.getRulePreview().getLayout().setActiveItem(1);
            //this.getRulePreviewTitle().update('<h4>' + selectedRule.get("name") + '</h4>');
            this.getRulePreview().getHeader().setTitle(selectedRule.get("name"));
            this.addProperties(selectedRule);
            this.addReadingTypes(selectedRule);
            this.getRulePreview().show();
        } else {
            this.getRulePreview().getLayout().setActiveItem(0);
        }
    },



    addProperties: function(selectedRule) {
        var properties = selectedRule.data.properties;
        this.getRuleForm()
        this.getPropertiesArea().removeAll();
        for (var i = 0; i < properties.length; i++) {
            var property = properties[i];
            var propertyName = property.name;
            var propertyValue = property.value;
            var required = property.required;
            var label = propertyName;
            if (!required) {
                label = label + ' (optional)';
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
            var fieldlabel = Uni.I18n.translate('validation.readingValues', 'CFG', 'Reading value(s)');
            if (i > 0) {
                fieldlabel = '&nbsp';
            }
            this.getReadingTypesArea().add(

                {
                            xtype: 'container',
                            layout: {
                                type: 'hbox'
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: fieldlabel,
                                    labelWidth:	250,
                                    width: 500,
                                    value: mRID
                                }, {
                                    xtype: 'component',
                                    width: 500,
                                    html: '<span style="color:grey"><i>' + aliasName + '</i></span>',
                                    margin: '5 0 0 10'
                                }
                            ]
                }

            );
        }
    }



});
