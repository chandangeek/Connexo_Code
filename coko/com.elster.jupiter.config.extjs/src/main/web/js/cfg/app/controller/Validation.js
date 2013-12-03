Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'ValidationActions',
        'Validators',
        'ValidationRuleProperties',
        'ReadingTypes',
        'ValidationPropertySpecsForRule'
    ],

    models: [
        'ReadingType',
        'ValidationRule',
        'ValidationRuleProperty'
    ],

    views: [
        'validation.Browse',
        'validation.Edit'
    ],

    refs: [
        {
            ref: 'availablePropertySpecsCombo',
            selector: 'validationrulesetEdit #availablepropertyspecscombo'
        },
        {
            ref: 'rulesGrid',
            selector: 'validationrulesetEdit #validationruleList'
        } ,
        {
            ref: 'ruleSetsGrid',
            selector: 'validationrulesetList #validationrulesetList'
        } ,
        {
            ref: 'rulePropertiesGrid',
            selector: 'validationrulesetEdit #validationrulepropertiesList'
        },
        {
            ref: 'availableReadingTypesGrid',
            selector: 'validationrulesetEdit #availableReadingTypes'
        },
        {
            ref: 'activeReadingTypesGrid',
            selector: 'validationrulesetEdit #activeReadingTypes'
        }
    ],


    init: function () {
        this.initMenu();

        this.control({
            'validationrulesetList button[action=removeRuleSet]': {
                click: this.removeRuleSet
            },
            'validationrulesetList button[action=addRuleSet]': {
                click: this.addRuleSet
            },
            'validationrulesetEdit button[action=save]': {
                click: this.saveRuleSet
            },
            'validationrulesetEdit button[action=removeRule]': {
                click: this.removeRule
            },
            'validationrulesetEdit button[action=addRule]': {
                click: this.addRule
            },
            'validationrulesetEdit button[action=removeRuleProperty]': {
                click: this.removeRuleProperty
            },
            'validationrulesetEdit button[action=addRuleProperty]': {
                click: this.addRuleProperty
            },
            '#validationrulesetList': {
                itemdblclick: this.editValidationRuleSet
            },
            '#validationruleList' : {
                select: this.editValidationRuleProperties
            },
            '#readingTypesActions button[action=activate]': {
                click: this.activateReadingType
            },
            '#readingTypesActions button[action=deactivate]': {
                click: this.deactivateReadingType
            },
            '#readingTypesActions button[action=reset]': {
                click: this.resetReadingTypes
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
        this.getValidationPropertySpecsForRuleStore().clearFilter();
        this.getValidationPropertySpecsForRuleStore().filter('validator', record.data.implementation);
        this.resetReadingTypesForRecord(record);
        this.getRulePropertiesGrid().reconfigure(record.properties());
    },

    removeRuleSet:function (button) {
        var sm = this.getRuleSetsGrid().getSelectionModel();
        this.getValidationRuleSetsStore().remove(sm.getSelection());
        sm.select(0);
    },

    addRuleSet:function (button) {
        var record = Ext.ModelManager.create({
            name: null,
            description: null
        }, 'Cfg.model.ValidationRuleSet');
        /*var index =  this.getValidationRuleSetsStore().getTotalCount();
        this.getValidationRuleSetsStore().insert(index, r);
        this.getRuleSetsGrid().getView().focusRow(index);
        this.getRuleSetsGrid().getSelectionModel().select(index);    */
        var view = Ext.widget('validationrulesetEdit');
        view.down('form').loadRecord(record);
        /*var me = this;
        me.getValidationRulesStore().load({
            params: {
                id: record.data.id
            },
            callback: function () {
                if (me.getValidationRulesStore().getTotalCount() > 0) {
                    me.getRulesGrid().getSelectionModel().select(0);
                }
            }});      */
    },

    removeRule:function (button) {
        var sm = this.getRulesGrid().getSelectionModel();
        this.getValidationRulesStore().remove(sm.getSelection());
        sm.select(0);
    },

    addRule:function (button) {
        var r = Ext.ModelManager.create({
            active: false,
            action: null,
            implementation: null
        }, 'Cfg.model.ValidationRule');
        var index =  this.getValidationRulesStore().getTotalCount();
        this.getValidationRulesStore().insert(index, r);
        this.getRulesGrid().getView().focusRow(index);
        this.getRulesGrid().getSelectionModel().select(index);
    },

    removeRuleProperty:function (button) {
        var sm = this.getRulePropertiesGrid().getSelectionModel();
        this.getValidationRulePropertiesStore().remove(sm.getSelection());
        sm.select(0);
    },

    addRuleProperty:function (button) {
        var r = Ext.ModelManager.create({
            name: null,
            value: null
        }, 'Cfg.model.ValidationRuleProperty');
        var index =  this.getValidationRulePropertiesStore().getTotalCount();
        this.getValidationRulePropertiesStore().insert(index, r);
        this.getRulePropertiesGrid().getView().focusRow(index);
        this.getRulePropertiesGrid().getSelectionModel().select(index);
    },

    saveRuleSet: function (button) {
       var win = button.up('window'),
            form = win.down('form'),
           ruleSetRecord = form.getRecord();
            values = form.getValues(),
            myRules = this.getRulesGrid.store.data.items;
        ruleSetRecord.set(values);
        ruleSetRecord.rules().removeAll();
        ruleSetRecord.rules().add(myRules);

        if (this.getRulesGrid().getSelectionModel().hasSelection()) {
            var ruleRecord = this.getRulesGrid().getSelectionModel().getSelection()[0];
            activeReadingTypes = this.getActiveReadingTypesGrid().store.data.items;
            ruleProperties = this.getRulePropertiesGrid.store.data.items;
            ruleRecord.properties().removeAll();
            ruleRecord.properties().add(ruleProperties);
            ruleRecord.readingTypes.removeAll();
            ruleRecord.readingTypes().add(activeReadingTypes);
            ruleRecord.save();
            ruleRecord.commit();
        }

        win.close();
        ruleSetRecord.save();
        ruleSetRecord.commit();
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
    },
    activateReadingType: function (button) {
        var selection = this.getAvailableReadingTypesGrid().getSelectionModel().getSelection();

        this.getAvailableReadingTypesGrid().store.remove(selection);
        this.getActiveReadingTypesGrid().store.add(selection);
    },
    deactivateReadingType: function (button) {
        var selection = this.getActiveReadingTypesGrid().getSelectionModel().getSelection();

        this.getActiveReadingTypesGrid().store.remove(selection);
        this.getAvailableReadingTypesGrid().store.add(selection);
    },
    resetReadingTypes: function (button) {
        if (this.getRulesGrid().getSelectionModel().hasSelection()) {
            var record = this.getRulesGrid().getSelectionModel().getSelection()[0];
            this.resetReadingTypesForRecord(record);
        }
    },
    resetReadingTypesForRecord: function (record) {
        var readingTypes = this.getReadingTypesStore(),
        currentReadingTypes = record.readingTypes().data.items;

        var availableReadingTypes = readingTypes.data.items,
            availableStore = this.getAvailableReadingTypesGrid().store;

        availableStore.removeAll();
        availableStore.add(availableReadingTypes);
        // Removes all active reading types for that rule.
        availableStore.remove(currentReadingTypes);

        var activeReadingTypes = [],
            activeStore = this.getActiveReadingTypesGrid().store;

        activeStore.removeAll();
        if (currentReadingTypes.length > 0) {
            // Find and add records that are currently active.
            for (var i = 0; i < currentReadingTypes.length; i++) {
                var readingTypeMRID = currentReadingTypes[i].data.mRID;
                var result = readingTypes.getById(readingTypeMRID);
                activeReadingTypes.push(result);
            }
            activeStore.add(activeReadingTypes);
        }
    }

});
