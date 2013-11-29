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
        'ValidationRule'
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
            'validationrulesetEdit button[action=save]': {
                click: this.saveRuleSet
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

        /*var me = this;
        me.getValidationPropertySpecsForRuleStore().load({
            params: {
                id: record.data.id
            },
            callback: function () {
                alert('available properties loaded for selectd rule');
                me.getAvailablePropertySpecsCombo().bindStore(me.getValidationPropertySpecsForRuleStore());
        }});  */
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
