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
        'validation.RuleSetOverview',
        'validation.AddRule',
        'validation.CreateRuleSet',
        'validation.RulePreviewContainer',
        'validation.RuleSetSubMenu',
        'validation.RuleOverview',
        'validation.RuleSubMenu'
    ],

    refs: [
        {ref: 'rulesetOverviewForm', selector: 'ruleSetOverview #rulesetOverviewForm'},
        {ref: 'rulesetPreviewTitle', selector: 'validation-ruleset-preview #rulesetPreviewTitle'},
        {ref: 'rulePreviewTitle', selector: 'validation-rule-preview #rulePreviewTitle'},
        {ref: 'readingTypesArea', selector: 'validation-rule-preview #readingTypesArea'},
        {ref: 'propertiesArea', selector: 'validation-rule-preview #propertiesArea'},
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
        {ref: 'removeReadingTypesButtonsContainer', selector: 'addRule #removeReadingTypesButtonsContainer'},
        {ref: 'validatorCombo', selector: 'addRule #validatorCombo'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'ruleBrowsePanel', selector: 'validationruleBrowse'},
        {ref: 'ruleSetBrowsePanel', selector: 'validationrulesetBrowse'},
        {ref: 'rulePreviewContainer', selector: 'rulePreviewContainer'},
        {ref: 'ruleOverview', selector: 'ruleOverview'}

    ],

    readingTypeIndex: 2,
    ruleId: null,
    ruleSetId: null,
    ruleModel: null,
    ruleSetModel: null,

    init: function () {
        this.control({
            '#validationrulesetList': {
                selectionchange: this.previewValidationRuleSet
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
                click: this.chooseRuleAction
            },
            '#ruleGridMenu': {
                show: this.onMenuShow
            },
            'addRule button[action=editRuleAction]': {
                click: this.createEditRule
            },
            '#validationrulesetList uni-actioncolumn': {
                menuclick: this.chooseRuleSetAction
            }
        });
    },

    onMenuShow: function (menu) {
        if (this.getRuleSetBrowsePanel()) {
            menu.down('#deactivate').setText(Uni.I18n.translate('general.view', 'CFG', 'View'));
            menu.down('#deactivate').action = 'view';
        } else {
            if (menu.record.get('active')) {
                menu.down('#deactivate').setText(Uni.I18n.translate('general.deactivate', 'CFG', 'Deactivate'));
            } else {
                menu.down('#deactivate').setText(Uni.I18n.translate('general.activate', 'CFG', 'Activate'));
            }
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
                    var messageText = Uni.I18n.translate('general.success', 'CFG', 'Operation completed succesfully'),
                        router = me.getController('Uni.controller.history.Router'),
                        params = router.routeparams;
                    params.ruleSetId = ruleSetId;
                    if (me.fromRulePreview) {
                        router.getRoute('administration/validation/rulesets/validationrules/ruleoverview').forward(params);
                    } else {
                        router.getRoute('administration/validation/rulesets/validationrules').forward(params);
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
        this.getPropertiesContainer().removeAll();
        var allPropertiesStore = this.getValidationPropertySpecsStore();
        allPropertiesStore.clearFilter();
        allPropertiesStore.filter('validator', field.value);
        for (var i = 0; i < allPropertiesStore.data.items.length; i++) {
            var property = allPropertiesStore.data.items[i];
            var label = property.data.name;
            if (property.data.name === 'NumberOfConsecutiveZerosAllowed') {
                label = 'Consecutive zeros';
            } else if (property.data.name === 'minimum') {
                label = 'Minimum';
            } else if (property.data.name === 'maximum') {
                label = 'Maximum';
            }
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
                        validateOnChange: false,
                        validateOnBlur: false,
                        itemId: itemIdValue,
                        labelWidth: 250
                    }
                );
            } else {
                this.getPropertiesContainer().add(
                    {
                        xtype: 'numberfield',
                        fieldLabel: label,
                        width: 400,
                        allowBlank: false,
                        /* validator: function (text) {
                         if (Ext.util.Format.trim(text).length == 0)
                         return 'This field is required';
                         else
                         return true;
                         },*/
                        required: true,
                        msgTarget: 'under',
                        labelAlign: 'right',
                        validateOnChange: false,
                        validateOnBlur: false,
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
                returnLink: '#/administration/validation/rulesets/validationrules/' + id
            }),
            editRulePanel = me.getAddRule();
        me.getApplication().fireEvent('changecontentevent', widget);
        editRulePanel.down('#addRuleTitle').setTitle(Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'));
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
                    var messageText = Uni.I18n.translate('general.success', 'CFG', 'Operation completed succesfully');
                    if (button.text === 'Save') {
                        me.getValidationRuleSetsStore().reload(
                            {
                                callback: function () {
                                    location.href = '#/administration/validation/';
                                }
                            }
                        );
                    } else {
                        var router = me.getController('Uni.controller.history.Router'),
                            params = router.routeparams;
                        params.ruleSetId = record.get('id');
                        router.getRoute('administration/validation/rulesets/validationrules').forward(params);
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
        var widget = Ext.widget('validationrulesetBrowse');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
    },

    showRules: function (id) {
        var me = this,
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        this.getValidationRulesStore().clearFilter();
        this.getValidationRulesStore().filter('ruleSetId', id);
        me.ruleSetId = id;
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getByInternalId(id),
                    rulesContainerWidget = Ext.widget('rulePreviewContainer', {ruleSetId: id});
                me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);
                rulesContainerWidget.down('#stepsMenu').setTitle(selectedRuleSet.get('name'));
                me.getApplication().fireEvent('loadRuleSet', selectedRuleSet);
            }
        });
    },

    showRuleSetOverview: function (id) {
        var me = this,
            ruleSetsStore = Ext.getStore('ValidationRuleSets') || Ext.create('Cfg.store.ValidationRuleSets'),
            rulesStore = me.getStore('Cfg.store.ValidationRules');
        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getByInternalId(id),
                    rulesContainerWidget = Ext.widget('ruleSetOverview', {ruleSetId: id});
                me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);
                rulesContainerWidget.setLoading();
                me.getRulesetOverviewForm().loadRecord(selectedRuleSet);
                rulesContainerWidget.down('#stepsMenu').setTitle(selectedRuleSet.get('name'));
                me.getApplication().fireEvent('loadRuleSet', selectedRuleSet);
                me.getRulesetOverviewForm().down('#inactiveRules').removeAll();
                me.getRulesetOverviewForm().down('#activeRules').removeAll();
                rulesStore.load({
                    params: {
                        id: id
                    },
                    callback: function (records) {
                        Ext.Array.each(records, function (item) {
                            if (!item.get('active')) {
                                me.getRulesetOverviewForm().down('#inactiveRules').add({
                                    xtype: 'displayfield',
                                    fieldLabel: '',
                                    value: item.get('rule_name')
                                })
                            } else {
                                me.getRulesetOverviewForm().down('#activeRules').add({
                                    xtype: 'displayfield',
                                    fieldLabel: '',
                                    value: item.get('rule_name')
                                })
                            }
                        });
                        rulesContainerWidget.setLoading(false);
                    }
                });
            }
        });
    },

    previewValidationRuleSet: function (grid, record) {
        var me = this,
            selection = this.getRuleSetsGrid().getSelectionModel().getSelection();
        this.ruleSetId = grid.view.getSelectionModel().getLastSelected().get('id');

        if (selection.length > 0) {
            this.getRuleSetPreview().hide();
            this.showRulesGrid();
        } else {
            if (me.getRuleSetBrowsePanel() && me.getRuleSetBrowsePanel().down('#validationruleBrowse')) {
                me.getRuleSetBrowsePanel().down('#validationruleBrowse').destroy();
            }
            if (me.getRulePreview()) {
                me.getRulePreview().destroy();
            }
            if (me.getRuleSetBrowsePanel() && me.getRuleSetBrowsePanel().down('#emptyRuleContainer')) {
                me.getRuleSetBrowsePanel().down('#ruleBrowseTitle').destroy();
                me.getRuleSetBrowsePanel().down('#emptyRuleContainer').destroy();
            }
        }
    },

    showRulesGrid: function () {
        var me = this,
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        me.getValidationRulesStore().clearFilter();
        me.getValidationRulesStore().filter('ruleSetId', me.ruleSetId);
        ruleSetsStore.load({
            params: {
                id: me.ruleSetId
            },
            callback: function (records) {
                var selectedRuleSet = ruleSetsStore.getByInternalId(me.ruleSetId);
                if (selectedRuleSet) {
                    var rulesCount = selectedRuleSet.get('numberOfRules'),
                        ruleSetName = selectedRuleSet.get('name');
                    me.ruleSetName = selectedRuleSet.get('name');

                    if (me.getRuleSetBrowsePanel() && me.getRuleSetBrowsePanel().down('#validationruleBrowse')) {
                        me.getRuleSetBrowsePanel().down('#validationruleBrowse').destroy();
                    }
                    if (me.getRulePreview()) {
                        me.getRulePreview().destroy();
                    }
                    var rulesPanel = Ext.create('Cfg.view.validation.RuleBrowse', {ruleSetId: me.ruleSetId});
                    if (me.getRuleSetBrowsePanel() && me.getRuleSetBrowsePanel().down('#emptyRuleContainer')) {
                        me.getRuleSetBrowsePanel().down('#ruleBrowseTitle').destroy();
                        me.getRuleSetBrowsePanel().down('#emptyRuleContainer').destroy();
                    }
                    if (rulesCount > 0) {
                        rulesPanel.down('#addRuleLink').hide();
                        rulesPanel.down('#ruleBulkAction').hide();
                        me.getRuleSetBrowsePanel().down('panel').add(rulesPanel);
                    } else {
                        me.createEmptyComponent();
                    }
                    me.getRuleSetBrowsePanel().down('panel').down('#ruleBrowseTitle').update('<h2>' + ruleSetName + '</h2>');
                }
            }
        });
    },

    createEmptyComponent: function () {
        var me = this;
        if (me.getRuleSetBrowsePanel()) {
            me.getRuleSetBrowsePanel().down('panel').add(
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('validation.rules', 'CFG', 'Rules') + '</h1>',
                    itemId: 'ruleBrowseTitle'
                },
                {
                    xtype: 'container',
                    itemId: 'emptyRuleContainer',
                    layout: {
                        type: 'hbox',
                        align: 'left'
                    },
                    minHeight: 20,
                    items: [
                        {
                            xtype: 'image',
                            margin: '0 10 0 0',
                            src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                            height: 20,
                            width: 20
                        },
                        {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'component',
                                    html: '<h4>' + Uni.I18n.translate('validation.empty.rules.title', 'CFG', 'No validation rules found') + '</h4><br>' +
                                        Uni.I18n.translate('validation.empty.rules.detail', 'CFG', 'There are no validation rules. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                        Uni.I18n.translate('validation.empty.rules.list.item1', 'CFG', 'No validation rules have been added yet.')
                                }
                            ]
                        }
                    ]
                }
            );
        }
    },

    previewValidationRule: function (grid, record) {
        var me = this,
            itemPanel = me.getRulePreviewContainer() || me.getRuleSetBrowsePanel(),
            itemForm = itemPanel.down('validation-rule-preview');
        me.ruleId = record.internalId;
        if (me.displayedItemId != record.id) {
            grid.view.clearHighlight();
        }
        me.displayedItemId = record.id;
        itemForm.loadRecord(record);
        if (!Ext.isEmpty(record.get('properties'))) {
            if (record.data.properties[0].name === 'maximum') {
                itemForm.down('#consField').hide();
                itemForm.down('#minField').show();
                itemForm.down('#maxField').show();
            } else {
                itemForm.down('#minField').hide();
                itemForm.down('#maxField').hide();
                itemForm.down('#consField').show();
            }
        } else {
            itemForm.down('#minField').hide();
            itemForm.down('#maxField').hide();
            itemForm.down('#consField').hide();
        }
        itemForm.setTitle(record.get('name'));
        if (me.getRuleSetBrowsePanel()) {
            me.getRulePreview().down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.view', 'CFG', 'View'));
            me.getRulePreview().down('validation-rule-action-menu').down('#deactivate').action = 'view';
            me.getRulePreview().down('validation-rule-action-menu').record = record;
        } else if (me.getRulePreviewContainer()) {
            itemForm.down('validation-rule-action-menu').record = record;
            if (!record.get('active')) {
                if (me.getRulePreview() && me.getRulePreview().down('validation-rule-action-menu')) {
                    me.getRulePreview().down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.activate', 'CFG', 'Activate'));
                } else {
                    itemForm.down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.activate', 'CFG', 'Activate'));
                }
            } else {
                if (me.getRulePreview() && me.getRulePreview().down('validation-rule-action-menu')) {
                    me.getRulePreview().down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.deactivate', 'CFG', 'Deactivate'));
                } else {
                    itemForm.down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.deactivate', 'CFG', 'Deactivate'));
                }
            }
        }
    },

    showEditRuleOverview: function (id) {
        var me = this,
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            widget,
            ruleSet,
            cancelLink,
            editRulePanel;
        if (me.fromRuleSet) {
            cancelLink = '#/administration/validation';
        } else if (me.fromRulePreview) {
            cancelLink = '#/administration/validation/rulesets/validationrules/' + id + '/ruleoverview/' + me.ruleId;
        } else {
            cancelLink = '#/administration/validation/rulesets/validationrules/' + id;
        }
        me.ruleSetId = id;
        widget = Ext.widget('addRule', {
            edit: true,
            returnLink: cancelLink
        });
        editRulePanel = me.getAddRule();
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
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            params = router.routeparams,
            active = false,
            record;
        record = menu.record || me.getRulesGrid().getSelectionModel().getLastSelected();
        params.ruleId = me.ruleId;
        params.ruleSetId = me.ruleSetId;
        this.getRuleSetBrowsePanel() ? me.fromRuleSet = true : me.fromRuleSet = false;
        this.getRuleOverview() ? me.fromRulePreview = true : me.fromRulePreview = false;
        if (menu.down('#deactivate').text === 'Deactivate') {
            active = true;
        }
        switch (item.action) {
            case 'view':
                router.getRoute('administration/validation/rulesets/validationrules/ruleoverview').forward(params);
                break;
            case 'deactivateRule':
                me.deactivateRule(record, active);
                break;
            case 'editRule':
                router.getRoute('administration/validation/rulesets/validationrules/edit').forward(params);
                break;
            case 'deleteRule':
                me.showDeleteConfirmation(record);
                break;
        }
    },

    deactivateRule: function (record, active) {
        var me = this,
            view = me.getRulePreviewContainer() || me.getRuleOverview(),
            grid = view.down('grid');
        if (record) {
            record.readingTypes().removeAll();
            Ext.Array.each(record.get('readingTypes'), function (item) {
                var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                readingTypeRecord.set('mRID', item.mRID);
                record.readingTypes().add(readingTypeRecord);
            });
        }
        if (active) {
            record.set('active', false);
        } else {
            record.set('active', true);
        }
        view.setLoading('Loading...');
        record.save({
            params: {
                id: me.ruleSetId
            },
            success: function (record, operation) {
                if (grid) {
                    grid.getView().refresh();
                    view.down('validation-rule-preview').loadRecord(record);
                    if (active) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.deactivateRuleSuccess.msg', 'CFG', 'Validation rule deactivated'));
                        if (me.getRulePreview() && me.getRulePreview().down('validation-rule-action-menu')) {
                            me.getRulePreview().down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.activate', 'CFG', 'Activate'));
                        } else {
                            view.down('validation-rule-preview').down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.activate', 'CFG', 'Activate'));
                        }
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.activateRuleSuccess.msg', 'CFG', 'Validation rule activated'));
                        if (me.getRulePreview() && me.getRulePreview().down('validation-rule-action-menu')) {
                            me.getRulePreview().down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.deactivate', 'CFG', 'Deactivate'));
                        } else {
                            view.down('validation-rule-preview').down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.deactivate', 'CFG', 'Deactivate'));
                        }
                    }
                } else {
                    var itemForm = view.down('#ruleOverviewForm');
                    itemForm.loadRecord(record);
                    if (active) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.deactivateRuleSuccess.msg', 'CFG', 'Validation rule deactivated'));
                        view.down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.activate', 'CFG', 'Activate'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.activateRuleSuccess.msg', 'CFG', 'Validation rule activated'));
                        view.down('validation-rule-action-menu').down('#deactivate').setText(Uni.I18n.translate('general.deactivate', 'CFG', 'Deactivate'));
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
                            ruleSetSelModel.select(ruleSet);
                            self.loadRulesStore(grid);
                        }
                    });
                } else if (self.getRuleOverview()) {
                    var router = self.getController('Uni.controller.history.Router'),
                        params = router.routeparams;
                    router.getRoute('administration/validation/rulesets/validationrules').forward(params);
                } else {
                    self.loadRulesStore(grid);
                }
                self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSuccess.msg', 'CFG', 'Validation rule removed'));
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },

    loadRulesStore: function (grid) {
        var self = this;
        grid.getStore().load({
            params: {
                id: self.ruleSetId
            },
            callback: function (records) {
                var gridView = grid.getView(),
                    selectionModel = gridView.getSelectionModel();
                selectionModel.select(0);
                grid.fireEvent('select', gridView, selectionModel.getLastSelected());
                if (self.getRuleSetBrowsePanel() && Ext.isEmpty(records)) {
                    if (self.getRuleSetBrowsePanel().down('#validationruleBrowse')) {
                        self.getRuleSetBrowsePanel().down('#validationruleBrowse').destroy();
                    }
                    if (self.getRulePreview()) {
                        self.getRulePreview().destroy();
                    }
                    self.createEmptyComponent();
                    self.getRuleSetBrowsePanel().down('panel').down('#ruleBrowseTitle').update('<h2>' + self.ruleSetName + '</h2>');
                }
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
            case 'viewRuleSet':
                router.getRoute('administration/validation/rulesets/overview').forward(params);
                break;
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
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSetSuccess.msg', 'CFG', 'Validation rule set removed'));
            }
        });
    },

    showRuleOverview: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            params = router.routeparams,
            rulesStore = me.getStore('Cfg.store.ValidationRules'),
            rulesContainerWidget = Ext.widget('ruleOverview',
                {
                    ruleSetId: id,
                    ruleId: params.ruleId
                }
            ),
            rule;
        me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);
        rulesContainerWidget.setLoading();
        rulesStore.load({
            params: {
                id: id
            },
            callback: function (records) {
                var itemForm = rulesContainerWidget.down('#ruleOverviewForm');
                Ext.Array.each(records, function (item) {
                    if (params.ruleId == item.get('id')) {
                        rule = item;
                    }
                });
                itemForm.loadRecord(rule);
                rulesContainerWidget.down('validation-rule-action-menu').record = rule;
                rulesContainerWidget.down('#stepsRuleMenu').setTitle(rule.get('name'));
                if (!Ext.isEmpty(rule.get('properties'))) {
                    if (rule.data.properties[0].name === 'maximum') {
                        itemForm.down('#consField').hide();
                        itemForm.down('#minField').show();
                        itemForm.down('#maxField').show();
                    } else {
                        itemForm.down('#minField').hide();
                        itemForm.down('#maxField').hide();
                        itemForm.down('#consField').show();
                    }
                } else {
                    itemForm.down('#minField').hide();
                    itemForm.down('#maxField').hide();
                    itemForm.down('#consField').hide();
                }
                me.getValidationRuleSetsStore().load({
                    callback: function () {
                        var ruleSet = this.getById(parseInt(id));
                        me.getApplication().fireEvent('loadRuleSet', ruleSet);
                        rulesContainerWidget.setLoading(false);
                    }
                });

            }
        });
    }
});
