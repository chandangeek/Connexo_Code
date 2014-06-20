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
        'validation.RuleSetOverview',
        'validation.AddRule',
        'validation.CreateRuleSet'
    ],

    refs: [
        {ref: 'rulesetOverviewForm', selector: 'ruleSetOverview #rulesetOverviewForm'},
        {ref: 'ruleForm', selector: 'validation-rule-preview #ruleForm'},
        {ref: 'rulesetPreviewTitle', selector: 'validation-ruleset-preview #rulesetPreviewTitle'},
        {ref: 'rulePreviewTitle', selector: 'validation-rule-preview #rulePreviewTitle'},
        {ref: 'readingTypesArea', selector: 'validation-rule-preview #readingTypesArea'},
        {ref: 'propertiesArea', selector: 'validation-rule-preview #propertiesArea'},
        {ref: 'ruleForm', selector: 'validation-rule-preview #ruleForm'},
        {ref: 'rulesetOverviewTitle', selector: 'ruleSetOverview #rulesetOverviewTitle'},
        {ref: 'ruleBrowseTitle', selector: 'validationruleBrowse #ruleBrowseTitle'},
        {ref: 'ruleSetDetailsLink', selector: 'validation-ruleset-preview #ruleSetDetailsLink'},
        {ref: 'cancelAddRuleLink', selector: 'addRule #cancelAddRuleLink'},
        {ref: 'rulesListContainer', selector: 'rulesContainer > #rulesListContainer'},

        {ref: 'ruleSetOverviewLink', selector: 'rulesContainer #ruleSetOverviewLink'},
        {ref: 'rulesLink', selector: 'rulesContainer #rulesLink'},
        {ref: 'addRuleLink', selector: 'validationruleList #addRuleLink'},

        {ref: 'rulesContainer', selector: 'rulesContainer'},
        {ref: 'ruleSetsGrid', selector: 'validationrulesetList'},
        {ref: 'rulesGrid', selector: 'validationruleList'},
        {ref: 'ruleSetPreview', selector: 'validation-ruleset-preview'},
        {ref: 'ruleSetOverview', selector: 'ruleSetOverview'},
        {ref: 'rulePreview', selector: 'validation-rule-preview'},
        {ref: 'ruleSetDetails', selector: 'validation-ruleset-preview #ruleSetDetails'},
        {ref: 'ruleSetEdit', selector: 'validationrulesetEdit'},

        {ref: 'stepsContainer', selector: 'rulesContainer > #stepsContainer'},
        {ref: 'newRuleSetForm', selector: 'createRuleSet > #newRuleSetForm'},

        {ref: 'createRuleSet', selector: 'createRuleSet'},
        {ref: 'addRule', selector: 'addRule'},

        {ref: 'rulesContainer', selector: 'rulesContainer'},
        {ref: 'stepsMenu', selector: 'rulesContainer > #stepsMenu'},

        {ref: 'readingValuesTextFieldsContainer', selector: 'addRule #readingValuesTextFieldsContainer'},
        {ref: 'propertiesContainer', selector: 'addRule #propertiesContainer'},
        {ref: 'removeReadingTypesButtonsContainer', selector: 'addRule #removeReadingTypesButtonsContainer'},
        {ref: 'validatorCombo', selector: 'addRule #validatorCombo'},

        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'ruleBrowsePanel', selector: 'validationruleBrowse'},
        {ref: 'ruleSetBrowsePanel', selector: 'validationrulesetBrowse'}
    ],

    readingTypeIndex: 2,
    ruleId: null,
    ruleSetId: null,
    ruleModel: null,
    ruleSetModel: null,

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
                click: this.createEditNewRuleSet
            },
            'createRuleSet button[action=editNewRuleSet]': {
                click: this.createEditNewRuleSet
            },
            'addRule button[action=createRuleAction]': {
                click: this.createEditRule
            },
            'addRule button[action=addRuleAction]': {
                click: this.addRule
            },
            'addRule button[action=addReadingTypeAction]': {
                click: this.addReadingType
            },
            'addRule combobox[itemId=validatorCombo]': {
                change: this.updateProperties
            },
            'rule-action-menu': {
                click: this.chooseRuleAction
            },
            'ruleset-action-menu': {
                click: this.chooseRuleSetAction
            },
            '#validationruleList uni-actioncolumn': {
                menuclick: this.chooseRuleAction
            },
            'addRule button[action=editRuleAction]': {
                click: this.createEditRule
            },
            '#validationrulesetList uni-actioncolumn': {
                menuclick: this.chooseRuleSetAction
            }
        });
    },

    getRuleSetIdFromHref: function () {
        var urlPart = 'administration/validation/addRule/';
        var index = location.href.indexOf(urlPart);
        return parseInt(location.href.substring(index + urlPart.length));
    },


    createEditRule: function (button) {
        var me = this,
            form = button.up('panel'),
            formErrorsPanel = form.down('[name=form-errors]');
        if (form.isValid()) {
            formErrorsPanel.hide();

            var ruleSetId = this.getRuleSetIdFromHref() || me.ruleSetId,
                record = me.ruleModel || Ext.create(Cfg.model.ValidationRule),
                values = form.getValues(),
                readingTypes = this.getReadingValuesTextFieldsContainer().items,
                rule = values.implementation,
                name = values.name,
                properties = this.getPropertiesContainer().items;

            if (form.down('#validatorCombo').isDisabled()) {
                rule = form.down('#validatorCombo').value;
            }

            record.set('implementation', rule);
            record.set('name', name);

            if (button.action === 'editRuleAction') {
                record.readingTypes().removeAll();
                record.properties().removeAll();
            }

            for (var i = 0; i < readingTypes.items.length; i++) {
                var readingType = readingTypes.items[i].items.items[0].value;
                var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                readingTypeRecord.set('mRID', readingType);
                record.readingTypes().add(readingTypeRecord);
            }

            for (var i = 0; i < properties.items.length; i++) {
                var propertyRecord = Ext.create(Cfg.model.ValidationRuleProperty);
                propertyRecord.set('value', properties.items[i].value);
                propertyRecord.set('name', properties.items[i].itemId);
                record.properties().add(propertyRecord);
            }

            me.getAddRule().setLoading('Loading...');

            record.save({
                params: {
                    id: ruleSetId
                },
                success: function (record, operation) {
                    var messageText = Uni.I18n.translate('general.success', 'CFG', 'Operation completed succesfully');
                    record.commit();
                    me.getValidationRuleSetsStore().reload(
                        {
                            callback: function () {
                                location.href = '#administration/validation/rules/' + ruleSetId;
                                me.getApplication().fireEvent('acknowledge', messageText);
                            }
                        });
                },
                failure: function (record, operation) {
                    me.getAddRule().setLoading(false);
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    updateProperties: function (field, oldValue, newValue) {
        this.getPropertiesContainer().removeAll();
        var allPropertiesStore = this.getValidationPropertySpecsStore();
        allPropertiesStore.clearFilter();
        allPropertiesStore.filter('validator', field.value);
        for (var i = 0; i < allPropertiesStore.data.items.length; i++) {
            var property = allPropertiesStore.data.items[i];
            var label = property.data.name;
            var itemIdValue = property.data.name;
            var optional = property.data.optional;
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
                        labelWidth: 250
                    }
                );
            } else {
                this.getPropertiesContainer().add(
                    {
                        xtype: 'numberfield',
                        fieldLabel: label,
                        validator: function (text) {
                            if (Ext.util.Format.trim(text).length == 0)
                                return 'This field is required';
                            else
                                return true;
                        },
                        required: true,
                        msgTarget: 'under',
                        labelAlign: 'right',
                        itemId: itemIdValue,
                        labelWidth: 250
                    }
                );
            }
        }
    },

    addReadingType: function () {
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
                        name: 'readingType',
                        /*validator:function(text){
                         if(Ext.util.Format.trim(text).length==0)
                         return Uni.I18n.translate('validation.requiredField', 'CFG', 'This field is required');
                         else
                         return true;
                         },
                         required: true,    */
                        msgTarget: 'under',
                        labelWidth: 250,
                        maskRe: /^($|\S.*$)/,
                        maxLength: 80,
                        allowBlank: false,
                        enforceMaxLength: true,
                        width: 600
                    },
                    {
                        text: '-',
                        xtype: 'button',
                        action: 'removeReadingTypeAction',
                        pack: 'center',
                        margin: '0 0 5 5',
                        width: 30,
                        itemId: 'readingTypeRemoveButton' + me.readingTypeIndex,
                        handler: function () {
                            me.getReadingValuesTextFieldsContainer().remove(Ext.ComponentQuery.query('#readingType' + indexToRemove)[0]);
                        }
                    }
                ]
            }
        );
        me.readingTypeIndex = me.readingTypeIndex + 1;
    },

    addRule: function (id) {
        var me = this,
            widget = Ext.widget('addRule', {
                edit: false,
                returnLink: '#/administration/validation/rules/' + id
            }),
            editRulePanel = me.getAddRule();
        me.getApplication().fireEvent('changecontentevent', widget);
        editRulePanel.down('#addRuleTitle').setTitle(Uni.I18n.translate('validation.addRule', 'CFG', 'Add rule'));
        me.ruleModel = null;
        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        ruleSetsStore.load({
            params: {
                id: id
            }
        });
    },

    createEditNewRuleSet: function (button) {
        var me = this,
            createEditRuleSetPanel = me.getCreateRuleSet(),
            form = button.up('form'),
            formErrorsPanel = form.down('[name=form-errors]');

        if (form.isValid()) {
            formErrorsPanel.hide();
            var record = me.ruleSetModel || Ext.create(Cfg.model.ValidationRuleSet);
            var values = form.getValues();
            record.set(values);
            createEditRuleSetPanel.setLoading('Loading...');
            record.save({
                success: function (record, operation) {
                    var messageText = Uni.I18n.translate('general.success', 'CFG', 'Operation completed succesfully');
                    me.getValidationRuleSetsStore().reload(
                        {
                            callback: function () {
                                location.href = '#/administration/validation/';
                                me.getApplication().fireEvent('acknowledge', messageText);
                            }
                        }
                    );
                },
                failure: function (record, operation) {
                    createEditRuleSetPanel.setLoading(false);
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            })
        } else {
            formErrorsPanel.show();
        }
    },

    initMenu: function () {

    },

    createEditRuleSet: function (ruleSetId) {
        var me = this,
            widget;
        if (ruleSetId) {
            widget = Ext.widget('createRuleSet', {
                edit: true,
                returnLink: '#/administration/validation'
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            widget.down('#editRuleSetTitle').setTitle(Uni.I18n.translate('validation.editRuleSet', 'CFG', 'Edit validation rule set'));
            me.fillForm(widget, ruleSetId);
        } else {
            widget = Ext.widget('createRuleSet', {
                edit: false,
                returnLink: '#/administration/validation/'
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            me.ruleSetModel = null;
            widget.down('#editRuleSetTitle').setTitle(Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'));
        }
    },

    fillForm: function (view, ruleSetId) {
        var me = this,
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            formPanel = view.down('#newRuleSetForm'),
            form = formPanel.getForm(),
            ruleSet;
        view.setLoading('Loading...');
        ruleSetsStore.load({
            callback: function () {
                view.setLoading(false);
                ruleSet = this.getById(parseInt(ruleSetId));
                me.ruleSetModel = ruleSet;
                form.loadRecord(ruleSet);
            }
        });
    },

    showRuleSets: function () {
        this.initMenu();
        var widget = Ext.widget('validationrulesetBrowse');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
    },

    showRules: function (id) {
        var me = this;
        var ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        this.getValidationRulesStore().clearFilter();
        this.getValidationRulesStore().filter('ruleSetId', id);
        me.ruleSetId = id;

        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getByInternalId(id);
                var ruleSetName = selectedRuleSet.get("name");

                if (Ext.ComponentQuery.query('#rulesContainer').length == 0) {
                    var rulesContainerWidget = Ext.create('Cfg.view.validation.RulesContainer', {ruleSetId: id});
                    me.getApplication().getController('Cfg.controller.Main').showContent(rulesContainerWidget);
                }

                me.getRuleBrowseTitle().update('<h1>' + ruleSetName + ' - Rules</h1>');
                me.goToMenuItem(1);
            }
        });
    },

    goToMenuItem: function (i) {
        Ext.ComponentQuery.query('#stepsContainer')[0].getLayout().setActiveItem(i);
    },

    showRuleSetOverview: function (id) {
        var me = this;

        var ruleSetsStore = Ext.getStore('ValidationRuleSets') || Ext.create('Cfg.store.ValidationRuleSets');
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getByInternalId(id);
                var ruleSetName = selectedRuleSet.get("name");

                if (Ext.ComponentQuery.query('#rulesContainer').length == 0) {
                    var rulesContainerWidget = Ext.create('Cfg.view.validation.RulesContainer', {ruleSetId: id});
                    me.getApplication().getController('Cfg.controller.Main').showContent(rulesContainerWidget);
                }

                me.getRulesetOverviewTitle().update('<h1>' + ruleSetName + ' - Overview</h1>');
                me.getRulesetOverviewForm().loadRecord(selectedRuleSet);
                me.goToMenuItem(0);
            }
        });
    },

    previewValidationRuleSet: function (grid, record) {
        var selection = this.getRuleSetsGrid().getSelectionModel().getSelection();
        this.ruleSetId = grid.view.getSelectionModel().getLastSelected().get('id');

        if (selection.length > 0) {
            this.getRuleSetPreview().updateValidationRuleSet(selection[0]);
        }
    },

    previewValidationRule: function (grid, record) {
        var selectedRules = this.getRulesGrid().getSelectionModel().getSelection();
        if (selectedRules.length == 1) {
            var selectedRule = selectedRules[0];
            this.ruleId = selectedRule.internalId;
            this.getRuleForm().loadRecord(selectedRule);
            this.getRulePreview().getLayout().setActiveItem(1);
            this.getRulePreview().setTitle(selectedRule.get("name"));
            this.addProperties(selectedRule);
            this.addReadingTypes(selectedRule);
            this.getRulePreview().show();
        } else {
            this.getRulePreview().getLayout().setActiveItem(0);
        }
    },

    addProperties: function (selectedRule) {
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
                    labelWidth: 250
                }
            );

        }

    },

    addReadingTypes: function (selectedRule) {
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
                            labelWidth: 250,
                            width: 500,
                            value: mRID
                        },
                        {
                            xtype: 'component',
                            width: 500,
                            html: '<span style="color:grey"><i>' + aliasName + '</i></span>',
                            margin: '5 0 0 10'
                        }
                    ]
                }
            );
        }
    },

    showEditRuleOverview: function (id) {
        var me = this,
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            widget = Ext.widget('addRule', {
                edit: true,
                returnLink: '#/administration/validation/rules/' + id
            }),
            ruleSet,
            editRulePanel = me.getAddRule();
        me.ruleSetId = id;
        me.getApplication().fireEvent('changecontentevent', widget);
        editRulePanel.down('#addRuleTitle').setTitle(Uni.I18n.translate('validation.editRule', 'CFG', 'Edit validation rule'));
        ruleSetsStore.load({
            callback: function () {
                ruleSet = this.getById(parseInt(id));
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
        me.modelToForm(editRulePanel, id);
    },

    modelToForm: function (editRulePanel, id) {
        var self = this,
            rulesStore = self.getStore('Cfg.store.ValidationRules'),
            form = editRulePanel.down('#addRuleForm').getForm(),
            readingTypeField = editRulePanel.down('#readingType1'),
            validatorField = editRulePanel.down('#validatorCombo'),
            propField,
            readingTypeFields,
            rule,
            fieldItemId,
            countReadingTypes;
        editRulePanel.setLoading('Loading...');
        rulesStore.load({
            params: {
                id: id
            },
            callback: function () {
                editRulePanel.setLoading(false);
                rule = this.getById(self.ruleId);
                self.ruleModel = rule;
                countReadingTypes = rule.get('readingTypes').length;
                form.loadRecord(rule);
                if (rule.data.active) {
                    validatorField.disable();
                }
                for (var i = 0; i < countReadingTypes - 1; i++) {
                    self.addReadingType();
                }
                readingTypeField.setValue(rule.get('readingTypes')[0].mRID);
                readingTypeFields = Ext.ComponentQuery.query('addRule textfield[name=readingType]');
                Ext.Array.each(rule.get('readingTypes'), function (readType) {
                    Ext.Array.each(readingTypeFields, function (field) {
                        field.setValue(readType.mRID);
                    });
                });
                Ext.Array.each(rule.get('properties'), function (item) {
                    fieldItemId = '#' + item.name;
                    propField = editRulePanel.down(fieldItemId).setValue(item.value);
                });
            }
        });
    },

    chooseRuleAction: function (menu, item) {
        var self = this,
            router = this.getController('Uni.controller.history.Router'),
            params = router.routeparams,
            record;
        record = menu.record || self.getRulesGrid().getSelectionModel().getLastSelected();
        params.ruleId = self.ruleId;
        switch (item.action) {
            case 'editRule':
                router.getRoute('administration/validation/rules/edit').forward(params);
                break;
            case 'deleteRule':
                self.showDeleteConfirmation(record);
                break;
        }
    },

    showDeleteConfirmation: function (rule) {
        var self = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('validation.removeRule.msg', 'CFG', 'This validation rule will no longer be available on the validation rule set.'),
            title: Ext.String.format(Uni.I18n.translate('validation.removeRule.title', 'CFG', 'Remove {0}?'), rule.get('name')),
            config: {
                rule: rule
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        self.deleteRule(rule);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var self = this,
            view = self.getRulesContainer(),
            grid = view.down('grid');
        view.setLoading('Removing...');
        Ext.Ajax.request({
            url: '/api/val/validation/rules/' + self.ruleSetId + '?id=' + self.ruleId,
            method: 'DELETE',
            success: function () {
                grid.getStore().load({
                        params: {
                            id: self.ruleSetId
                        },
                        callback: function () {
                            var gridView = grid.getView(),
                                selectionModel = gridView.getSelectionModel();
                            view.down('pagingtoolbartop').totalCount = 0;
                            selectionModel.select(0);
                            grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                        }
                    }
                );
                self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSuccess.msg', 'CFG', 'Validation rule removed'));
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },

    chooseRuleSetAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            params = router.routeparams,
            record;
        record = menu.record || me.getRuleSetsGrid().getSelectionModel().getLastSelected();
        params.ruleSetId = me.ruleSetId;
        switch (item.action) {
            case 'editRuleSet':
                router.getRoute('administration/validation/editset').forward(params);
                break;
            case 'deleteRuleSet':
                me.showDeleteRuleSetConfirmation(record);
                break;
        }
    },

    showDeleteRuleSetConfirmation: function (ruleSet) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('validation.removeRuleSet.msg', 'CFG', 'This validation rule set will no longer be available.'),
            title: Ext.String.format(Uni.I18n.translate('validation.removeRule.title', 'CFG', 'Remove {0}?'), ruleSet.get('name')),
            config: {
                rule: ruleSet
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.deleteRuleSet(ruleSet);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteRuleSet: function (ruleSet) {
        var me = this,
            view = me.getRuleSetBrowsePanel(),
            grid = view.down('grid');

        view.setLoading('Removing...');
        ruleSet.destroy({
            callback: function () {
                view.setLoading(false);
                grid.getStore().load({
                        callback: function () {
                            var gridView = grid.getView(),
                                selectionModel = gridView.getSelectionModel();
                            view.down('pagingtoolbartop').totalCount = 0;
                            selectionModel.select(0);
                            grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                        }
                    }
                );
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSetSuccess.msg', 'CFG', 'Validation rule set removed'));
            }
        });
    }
});
