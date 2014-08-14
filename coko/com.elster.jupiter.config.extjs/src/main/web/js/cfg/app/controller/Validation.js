Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'ValidationPropertySpecs',
        'Validators'
    ],

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.model.Validator'
    ],

    models: [
        'ValidationRuleSet',
        'ValidationRule',
        'ReadingType',
        'ValidationRuleProperty',
        'Validator'
    ],

    views: [
        'validation.RuleSetBrowse',
        'validation.RuleSetList',
        'validation.RuleSetPreview',
        'validation.RuleList',
        'validation.RulePreview',
        'validation.RuleSetOverview',
        'validation.AddRule',
        'validation.CreateRuleSet',
        'validation.RulePreviewContainer',
        'validation.RuleSetSubMenu',
        'validation.RuleOverview',
        'validation.RuleSubMenu',
        'validation.RulePreviewContainerPanel'
    ],

    refs: [
        {ref: 'rulesetOverviewForm', selector: 'ruleSetOverview #rulesetOverviewForm'},
        {ref: 'rulesetPreviewTitle', selector: 'validation-ruleset-preview #rulesetPreviewTitle'},
        {ref: 'rulePreviewTitle', selector: 'validation-rule-preview #rulePreviewTitle'},
        {ref: 'readingTypesArea', selector: 'validation-rule-preview #readingTypesArea'},
        {ref: 'propertiesArea', selector: 'validation-rule-preview #propertiesArea'},
        {ref: 'readingTypesOverviewArea', selector: 'ruleOverview #readingTypesArea'},
        {ref: 'propertiesOverviewArea', selector: 'ruleOverview #propertiesArea'},
        {ref: 'rulesetOverviewTitle', selector: 'ruleSetOverview #rulesetOverviewTitle'},
        {ref: 'ruleBrowseTitle', selector: '#ruleBrowseTitle'},
        {ref: 'ruleSetDetailsLink', selector: 'validation-ruleset-preview #ruleSetDetailsLink'},
        {ref: 'cancelAddRuleLink', selector: 'addRule #cancelAddRuleLink'},
        {ref: 'addRuleLink', selector: 'validationruleList #addRuleLink'},
        {ref: 'ruleSetsGrid', selector: 'validationrulesetList'},
        {ref: 'rulesGrid', selector: 'validationruleList'},
        {ref: 'ruleSetPreview', selector: 'validation-ruleset-preview'},
        {ref: 'ruleSetOverview', selector: 'ruleSetOverview'},
        {ref: 'rulePreview', selector: 'validation-rule-preview'},
        {ref: 'ruleSetDetails', selector: 'validation-ruleset-preview #ruleSetDetails'},
        {ref: 'ruleSetEdit', selector: 'validationrulesetEdit'},
        {ref: 'newRuleSetForm', selector: 'createRuleSet > #newRuleSetForm'},
        {ref: 'createRuleSet', selector: 'createRuleSet'},
        {ref: 'addRule', selector: 'addRule'},
        {ref: 'readingValuesTextFieldsContainer', selector: 'addRule #readingValuesTextFieldsContainer'},
        {ref: 'propertiesContainer', selector: 'addRule #propertiesContainer'},
        {ref: 'propertyForm', selector: 'addRule #propertyForm'},
        {ref: 'removeReadingTypesButtonsContainer', selector: 'addRule #removeReadingTypesButtonsContainer'},
        {ref: 'validatorCombo', selector: 'addRule #validatorCombo'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'ruleSetBrowsePanel', selector: 'validationrulesetBrowse'},
        {ref: 'rulePreviewContainer', selector: 'rulePreviewContainer'},
        {ref: 'ruleOverview', selector: 'ruleOverview'},
        {ref: 'ruleSetBrowsePreviewCt', selector: '#ruleSetBrowsePreviewCt'},
        {ref: 'rulePreviewContainerPanel', selector: 'rule-preview-container-panel'}
    ],

    readingTypeIndex: 2,
    ruleId: null,
    ruleSetId: null,
    ruleModel: null,
    ruleSetModel: null,

    init: function () {
        this.control({
            '#validationrulesetList': {
                select: this.previewValidationRuleSet
            },
            '#validationruleList': {
                select: this.previewValidationRule
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
            'validation-rule-action-menu': {
                click: this.chooseRuleAction,
                show: this.onMenuShow
            },
            'addRule button[action=editRuleAction]': {
                click: this.createEditRule
            },
            'ruleSetOverview ruleset-action-menu': {
                click: this.chooseRuleSetAction
            },
            '#validationrulesetList uni-actioncolumn': {
                menuclick: this.chooseRuleSetAction
            }
        });
    },

    onMenuShow: function (menu) {
        if (menu.record.get('active')) {
            menu.down('#activate').hide();
            menu.down('#deactivate').show();
        } else {
            menu.down('#deactivate').hide();
            menu.down('#activate').show();
        }
    },

    getRuleSetIdFromHref: function () {
        var urlPart = 'administration/validation/addRule/';
        var index = location.href.indexOf(urlPart);
        return parseInt(location.href.substring(index + urlPart.length));
    },


    createEditRule: function (button) {
        var me = this,
            form = button.up('panel'),
            arrReadingTypes = [],
            formErrorsPanel = form.down('[name=form-errors]');

        var propertyForm = this.getAddRule().down('property-form');
        propertyForm.updateRecord();
        if (form.isValid()) {
            var ruleSetId = this.getRuleSetIdFromHref() || me.ruleSetId,
                record = me.ruleModel || Ext.create(Cfg.model.ValidationRule),
                values = form.getValues(),
                readingTypes = this.getReadingValuesTextFieldsContainer().items,
                rule = values.implementation,
                name = values.name;

            formErrorsPanel.hide();

            if (form.down('#validatorCombo').isDisabled()) {
                rule = form.down('#validatorCombo').value;
            }

            record .set('implementation', rule);
            record.set('name', name);
            record.set('ruleSet', {
                id: me.ruleSetId
            });

            if (button.action === 'editRuleAction') {
                record.readingTypes().removeAll();
            }
            var properties = propertyForm.getRecord().properties();

            for (var i = 0; i < readingTypes.items.length; i++) {
                var readingTypeMRID = readingTypes.items[i].items.items[0],
                    readingType = readingTypeMRID.value,
                    readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                readingTypeMRID.name = 'readingTypesInRule[' + i + '].readingTypeMRID';
                readingTypeRecord.set('mRID', readingType);
                arrReadingTypes.push(readingTypeRecord);
            }

            record.readingTypes().add(arrReadingTypes);
            record.propertiesStore = properties;

            me.getAddRule().setLoading('Loading...');
            record.save({
                params: {
                    id: ruleSetId
                },
                success: function (record, operation) {
                    var messageText;
                    if (button.action === 'editRuleAction') {
                        messageText = Uni.I18n.translate('validation.editRuleSuccess.msg', 'CFG', 'Validation rule saved');
                    } else {
                        messageText = Uni.I18n.translate('validation.addRuleSuccess.msg', 'CFG', 'Validation rule added');
                    }
                    if (me.fromRulePreview) {
                        location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/' + me.ruleId;
                    } else {
                        location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules';
                    }
                    me.getApplication().fireEvent('acknowledge', messageText);
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
        var store = this.getValidatorsStore();
        var found;
        for ( i = 0; i < store.data.items.length; i++){
            if ( store.data.items[i].data.implementation == field.value ){
                found = store.data.items[i];
                break;
            }
        }

        widget = this.getAddRule();

        var propertyForm = widget.down('property-form');
        if (found.properties().count()) {
            propertyForm.show();
            propertyForm.loadRecord(found);
        } else {
            propertyForm.hide();
        }
        widget.setLoading(false);
    },

    addReadingType: function () {
        var me = this;
        var indexToRemove = me.readingTypeIndex;

        var widget = this.getReadingValuesTextFieldsContainer().add(
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
                        msgTarget: 'under',
                        labelWidth: 260,
                        maskRe: /^($|\S.*$)/,
                        maxLength: 80,
                        validateOnChange: false,
                        validateOnBlur: false,
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
                        itemId: 'readingTypeRemoveButton' + me.readingTypeIndex,
                        handler: function () {
                            me.getReadingValuesTextFieldsContainer().remove(Ext.ComponentQuery.query('#readingType' + indexToRemove)[0]);
                        }
                    }
                ]
            }
        );

        me.readingTypeIndex = me.readingTypeIndex + 1;

        return widget;
    },

    addRule: function (ruleSetId) {
        var me = this,
            widget = Ext.widget('addRule', {
                edit: false,
                returnLink: '#/administration/validation/rulesets/' + ruleSetId + '/rules'
            }),
            editRulePanel = me.getAddRule(),
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        me.ruleSetId = ruleSetId;
        me.getApplication().fireEvent('changecontentevent', widget);
        editRulePanel.down('#addRuleTitle').setTitle(Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'));
        me.ruleModel = null;

        ruleSetsStore.load({
            params: {
                id: ruleSetId
            }
        });
    },

    createEditNewRuleSet: function (button) {
        var me = this,
            createEditRuleSetPanel = me.getCreateRuleSet(),
            form = button.up('form'),
            record,
            formErrorsPanel = form.down('[name=form-errors]');
        if (form.isValid()) {
            formErrorsPanel.hide();
            if (button.text === 'Save') {
                record = me.ruleSetModel;
            } else {
                record = Ext.create(Cfg.model.ValidationRuleSet);
            }
            var values = form.getValues();
            record.set(values);
            createEditRuleSetPanel.setLoading('Loading...');

            record.save({
                success: function (record, operation) {
                    var messageText;
                    if (button.text === 'Save') {
                        messageText = Uni.I18n.translate('validation.editRuleSetSuccess.msg', 'CFG', 'Validation rule set saved');
                        if (me.fromRuleSetOverview) {
                            location.href = '#/administration/validation/rulesets/' + record.get('id');
                        } else {
                            me.getValidationRuleSetsStore().reload(
                                {
                                    callback: function () {
                                        location.href = '#/administration/validation/rulesets';
                                    }
                                }
                            );
                        }
                    } else {
                        messageText = Uni.I18n.translate('validation.addRuleSetSuccess.msg', 'CFG', 'Validation rule set added');
                        location.href = '#/administration/validation/rulesets/' + record.get('id') + '/rules';
                    }
                    me.getApplication().fireEvent('acknowledge', messageText);
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

    createEditRuleSet: function (ruleSetId) {
        var me = this,
            cancelLink,
            widget;

        if (ruleSetId) {
            me.fromRuleSetOverview ? cancelLink = '#/administration/validation/rulesets/' + ruleSetId : cancelLink = '#/administration/validation/rulesets/';
            widget = Ext.widget('createRuleSet', {
                edit: true,
                returnLink: cancelLink
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            me.fillForm(widget, ruleSetId);
        } else {
            widget = Ext.widget('createRuleSet', {
                edit: false,
                returnLink: '#/administration/validation/rulesets/'
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
                view.down('#editRuleSetTitle').setTitle("Edit '" + ruleSet.get('name') + "'");
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
                me.ruleSetModel = ruleSet;
                form.loadRecord(ruleSet);
            }
        });
    },

    showRuleSets: function () {
        var widget = Ext.widget('validationrulesetBrowse');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
    },

    showRules: function (id) {
        var me = this,
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        me.ruleSetId = id;
        me.fromRulePreview = false;
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getByInternalId(id),
                    rulesContainerWidget = Ext.widget('rulePreviewContainer', {ruleSetId: id});
                me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);
                rulesContainerWidget.down('#stepsMenu').setTitle(selectedRuleSet.get('name'));
                if (me.mdcIsActive) {
                    rulesContainerWidget.down('#deviceConfigLink').show();
                }
                me.getApplication().fireEvent('loadRuleSet', selectedRuleSet);
            }
        });
    },

    showRuleSetOverview: function (id) {
        var me = this;

        Cfg.model.ValidationRuleSet.load(id, {
            success: function (ruleSet) {
                var rulesContainerWidget = Ext.widget('ruleSetOverview', {ruleSetId: id});
                me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);

                me.getRulesetOverviewForm().loadRecord(ruleSet);
                rulesContainerWidget.down('#stepsMenu').setTitle(ruleSet.get('name'));
                if (me.mdcIsActive) {
                    rulesContainerWidget.down('#deviceConfigLink').show();
                }

                me.getApplication().fireEvent('loadRuleSet', ruleSet);
                rulesContainerWidget.down('ruleset-action-menu').down('#viewRuleSet').hide();
                rulesContainerWidget.down('ruleset-action-menu').record = ruleSet;
            }
        });
    },

    previewValidationRuleSet: function (selectionModel, record) {
        this.getRuleSetBrowsePreviewCt().removeAll(true);
        var rulesPreviewContainerPanel = Ext.widget('rule-preview-container-panel', {
            ruleSetId: record.getId(),
            margin: '-20 0 0 0',
            title: '<h2>' + record.get('name') + '</h2>',
            isSecondPagination: true,
            height: 750
        });
        this.ruleSetId = record.getId();
        Ext.Array.each(Ext.ComponentQuery.query('#addRuleLink'), function (item) {
            item.hide();
        });
        this.getRuleSetBrowsePreviewCt().add(rulesPreviewContainerPanel);
    },

    previewValidationRule: function (grid, record) {
        var me = this,
            itemPanel = me.getRulePreviewContainer() || me.getRuleSetBrowsePanel(),
            itemForm = itemPanel.down('validation-rule-preview'),
            selectedRule;
        if (grid.view) {
            selectedRule = grid.view.getSelectionModel().getLastSelected();
        } else {
            selectedRule = record;
        }
        me.ruleId = selectedRule.internalId;

        itemForm.updateValidationRule(selectedRule);

        if (me.getRuleSetBrowsePanel()) {
            me.getRulePreview().down('validation-rule-action-menu').record = record;
        } else if (me.getRulePreviewContainer()) {
            itemForm.down('validation-rule-action-menu').record = record;
        }
    },

    showEditRuleOverview: function (id, ruleId) {
        var me = this,
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            widget,
            ruleSet,
            cancelLink,
            editRulePanel;

        if (me.fromRuleSet) {
            cancelLink = '#/administration/validation/rulesets';
        } else if (me.fromRulePreview) {
            cancelLink = '#/administration/validation/rulesets/' + id + '/rules/' + me.ruleId;
        } else {
            cancelLink = '#/administration/validation/rulesets/' + id + '/rules';
        }

        me.ruleSetId = parseInt(id);
        me.ruleId = parseInt(ruleId);

        widget = Ext.widget('addRule', {
            edit: true,
            returnLink: cancelLink
        });
        editRulePanel = me.getAddRule();
        me.getApplication().fireEvent('changecontentevent', widget);
        ruleSetsStore.load({
            callback: function () {
                ruleSet = this.getById(parseInt(id));
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
        me.modelToForm(editRulePanel, id);
    },

    modelToForm: function (editRulePanel, id) {
        var me = this,
            rulesStore = me.getStore('Cfg.store.ValidationRules'),
            form = editRulePanel.down('#addRuleForm').getForm(),
            readingTypeField = editRulePanel.down('#readingType1'),
            validatorField = editRulePanel.down('#validatorCombo'),
            nameField = editRulePanel.down('#addRuleName'),
            propField,
            rule;

        editRulePanel.setLoading();
        rulesStore.load({
            params: {
                id: id
            },
            callback: function () {
                editRulePanel.setLoading(false);
                rule = this.getById(me.ruleId);
                me.ruleModel = rule;

                me.getApplication().fireEvent('loadRule', rule);
                editRulePanel.down('#addRuleTitle').setTitle("Edit '" + rule.get('name') + "'");

                validatorField.setValue(rule.get('implementation'));

                if (rule.data.active) {
                    validatorField.disable();
                }

                readingTypeField.setValue(rule.get('readingTypes')[0].mRID);
                nameField.setValue(rule.get('name'));

                var ruleReadingTypes = rule.get('readingTypes');
                for (var i = 1; i < ruleReadingTypes.length; i++) {
                    var readingType = ruleReadingTypes[i],
                        field = me.addReadingType();
                    field.down('textfield').setValue(readingType.mRID);
                }

                widget = me.getAddRule();
                var propertyForm = widget.down('property-form');
                var j = rule.properties().count();
                if (rule.properties().count()) {
                    propertyForm.loadRecord(rule);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }

                widget.setLoading(false);
            }
        });
    },

    chooseRuleAction: function (menu, item) {
        var me = this,
            record;
        record = menu.record || me.getRulesGrid().getSelectionModel().getLastSelected();
        this.getRuleSetBrowsePanel() ? me.fromRuleSet = true : me.fromRuleSet = false;
        this.getRuleOverview() ? me.fromRulePreview = true : me.fromRulePreview = false;
        switch (item.action) {
            case 'view':
                location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/' + me.ruleId;
                break;
            case 'activateRule':
                me.deactivateRule(record);
                break;
            case 'deactivateRule':
                me.deactivateRule(record);
                break;
            case 'editRule':
                location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/' + me.ruleId + '/edit';
                break;
            case 'deleteRule':
                me.showDeleteConfirmation(record);
                break;
        }
    },

    deactivateRule: function (record, active) {
        var me = this,
            view = me.getRulePreviewContainer() || me.getRuleOverview() || me.getRulePreviewContainerPanel(),
            grid = view.down('grid'),
            isActive = record.get('active');
        if (record) {
            record.readingTypes().removeAll();
            Ext.Array.each(record.get('readingTypes'), function (item) {
                var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                readingTypeRecord.set('mRID', item.mRID);
                record.readingTypes().add(readingTypeRecord);
            });
        }
        record.set('active', !isActive);
        view.setLoading('Loading...');
        record.save({
            params: {
                id: me.ruleSetId
            },
            success: function (record, operation) {
                if (grid) {
                    grid.getView().refresh();
                    view.down('validation-rule-preview').loadRecord(record);
                    if (isActive) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.deactivateRuleSuccess.msg', 'CFG', 'Validation rule deactivated'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.activateRuleSuccess.msg', 'CFG', 'Validation rule activated'));
                    }
                } else {
                    var itemForm = view.down('validation-rule-preview');
                    itemForm.loadRecord(record);
                    if (isActive) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.deactivateRuleSuccess.msg', 'CFG', 'Validation rule deactivated'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.activateRuleSuccess.msg', 'CFG', 'Validation rule activated'));
                    }
                }
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },

    showDeleteConfirmation: function (rule) {
        var self = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('validation.removeRule.msg', 'CFG', 'This validation rule will no longer be available on the validation rule set.'),
            title: Ext.String.format(Uni.I18n.translate('validation.removeRule.title', 'CFG', "Remove '{0}'?"), rule.get('name')),
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
            view = self.getRulePreviewContainer() || self.getRuleSetBrowsePanel() || self.getRuleOverview(),
            grid = view.down('#validationruleList'),
            gridRuleSet = view.down('#validationrulesetList');
        if (gridRuleSet) {
            var ruleSetSelModel = gridRuleSet.getSelectionModel(),
                ruleSet = ruleSetSelModel.getLastSelected();
        }
        view.setLoading('Removing...');
        Ext.Ajax.request({
            url: '/api/val/validation/rules/' + self.ruleSetId + '?id=' + self.ruleId,
            method: 'DELETE',
            success: function () {
                if (self.getRulePreviewContainer()) {
                    view.down('pagingtoolbartop').totalCount = 0;
                } else if (self.getRuleSetBrowsePanel()) {
                    view.down('#rulesTopPagingToolbar').totalCount = 0;
                }
                if (self.getRuleSetBrowsePanel()) {
                    gridRuleSet.getStore().load({
                        callback: function () {
                            Ext.Function.defer(function () {
                                ruleSetSelModel.select(ruleSet);
                                self.getRuleSetBrowsePanel().setLoading(false);
                            }, 5000);
                        }
                    });
                } else if (self.getRuleOverview()) {
                    location.href = '#/administration/validation/rulesets/' + self.ruleSetId + '/rules';
                } else {
                    grid.getStore().load({
                        params: {
                            id: self.ruleSetId
                        }
                    });
                }
                self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSuccess.msg', 'CFG', 'Validation rule removed'));
            },
            callback: function () {
                view.setLoading(false);
                if (self.getRuleSetBrowsePanel()) {
                    self.getRuleSetBrowsePanel().setLoading();
                }
            }
        });
    },

    chooseRuleSetAction: function (menu, item) {
        var me = this,
            record;
        record = menu.record || me.getRuleSetsGrid().getSelectionModel().getLastSelected();
        me.getRuleSetOverview() ? me.fromRuleSetOverview = true : me.fromRuleSetOverview = false;
        switch (item.action) {
            case 'viewRuleSet':
                location.href = '#/administration/validation/rulesets/' + me.ruleSetId;
                break;
            case 'editRuleSet':
                location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/edit';
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
            title: Ext.String.format(Uni.I18n.translate('validation.removeRule.title', 'CFG', "Remove '{0}'?"), ruleSet.get('name')),
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
            view = me.getRuleSetBrowsePanel() || me.getRuleSetOverview(),
            grid = view.down('grid');
        view.setLoading('Removing...');
        ruleSet.destroy({
            callback: function () {
                view.setLoading(false);
                if (me.getRuleSetOverview()) {
                    location.href = '#/administration/validation/rulesets';
                } else {
                    view.down('pagingtoolbartop').totalCount = 0;
                    grid.getStore().load({
                            callback: function () {
                                var gridView = grid.getView(),
                                    selectionModel = gridView.getSelectionModel();
                                selectionModel.select(0);
                                grid.fireEvent('select', gridView, selectionModel.getLastSelected());
                            }
                        }
                    );
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSetSuccess.msg', 'CFG', 'Validation rule set removed'));
            }
        });
    },

    showRuleOverview: function (ruleSetId, ruleId) {
        var me = this,
            rulesContainerWidget = Ext.widget('ruleOverview',
                {
                    ruleSetId: ruleSetId,
                    ruleId: ruleId
                }
            );

        me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);
        rulesContainerWidget.setLoading(true);

        // TODO This is not performant whatsoever. Refactor when there is a REST call that filters on rule id.

        Ext.Ajax.request({
            url: '/api/val/validation/rules/' + ruleSetId,
            params: {},
            disableCaching: false,
            method: 'GET',
            success: function (response) {
                var rules = Ext.JSON.decode(response.responseText).rules,
                    itemForm = rulesContainerWidget.down('validation-rule-preview'),
                    rule = {};

                Ext.Array.each(rules, function (ruleJson) {
                    if (parseInt(ruleId) === ruleJson.id) {
                        rule = Ext.create('Cfg.model.ValidationRule', ruleJson);
                        return;
                    }
                });

                itemForm.updateValidationRule(rule);
                itemForm.down('#rulePreviewActionsButton').destroy();
                itemForm.setTitle('');

                me.getApplication().fireEvent('loadRule', rule);
                rulesContainerWidget.down('validation-rule-action-menu').record = rule;
                rulesContainerWidget.down('validation-rule-action-menu').down('#view').hide();
                rulesContainerWidget.down('#stepsRuleMenu').setTitle(rule.get('name'));

                Cfg.model.ValidationRuleSet.load(ruleSetId, {
                    success: function (ruleSet) {
                        me.getApplication().fireEvent('loadRuleSet', ruleSet);
                        rulesContainerWidget.setLoading(false);
                    }
                });
            }
        });
    }
});
