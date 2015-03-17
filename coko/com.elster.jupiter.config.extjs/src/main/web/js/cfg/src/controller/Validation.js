Ext.define('Cfg.controller.Validation', {
    extend: 'Ext.app.Controller',

    stores: [
        'ValidationRuleSets',
        'ValidationRules',
        'Validators',
        'UnitsOfMeasure',
        'Intervals',
        'TimeOfUse',
        'ReadingTypesForRule',
        'AdaptedReadingTypes',
        'Cfg.store.ReadingTypesToAddForRule'
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
        'validation.RulePreviewContainerPanel',
        'validation.AddReadingTypesToRuleSetup',
        'validation.AddReadingTypesBulk'
    ],

    refs: [
        {ref: 'rulesetOverviewForm', selector: 'ruleSetOverview #rulesetOverviewForm'},
        {ref: 'rulesetPreviewTitle', selector: 'validation-ruleset-preview #rulesetPreviewTitle'},
        {ref: 'rulePreviewTitle', selector: 'validation-rule-preview #rulePreviewTitle'},
        {ref: 'readingTypesArea', selector: 'validation-rule-preview #readingTypesArea'},
        {ref: 'readingTypesOverviewArea', selector: 'ruleOverview #readingTypesArea'},
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
        {ref: 'addReadingTypesGrid', selector: '#addRule #addReadingTypesGrid'},
        {ref: 'addRuleTitle', selector: '#addRule #addRuleTitle'},
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
        {ref: 'rulePreviewContainerPanel', selector: 'rule-preview-container-panel'},
        {ref: 'addReadingTypesSetup', selector: '#addReadingTypesToRuleSetup'}

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
            },
            '#addRuleForm #addReadingTypeButton': {
                click: this.showAddReadingGrid
            },
            '#addReadingTypesToRuleSetup #buttonsContainer button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            '#addReadingTypesToRuleSetup cfg-side-filter button[action=applyfilter]': {
                click: this.loadReadingTypes
            },
            '#addReadingTypesToRuleSetup #filterReadingTypes': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearAllFilters
            },
            '#addReadingTypesToRuleSetup cfg-side-filter button[action=clearfilter]': {
                click: this.clearAllCombos
            },

            '#addReadingTypesToRuleSetup #buttonsContainer button[name=add]': {
                click: this.addReadingTypesToGrid
            }
        });
    },

    showAddReadingGrid: function () {
        var router = this.getController('Uni.controller.history.Router'),
            addReadingTypesRoute = router.currentRoute + '/readingtypes';

        this.validationRuleRecord = this.formToModel();
        router.getRoute(addReadingTypesRoute).forward();
    },

    formToModel: function () {
        var form = this.getAddRule().down('#addRuleForm'),
            propertyForm = this.getAddRule().down('property-form'),
            grid = this.getAddRule().down('#readingTypesGridPanel'),
            record = form.getForm().getRecord(),
            readingTypes = [];


        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
        }
        record.beginEdit();
        record.set('implementation', form.down('#validatorCombo').getValue());
        record.set('name', form.down('#addRuleName').getValue());
        record.set('action', form.down('#dataQualityLevel').getValue().action);

        grid.getStore().each(function (rec) {
            readingTypes.push(rec.get('readingType'));
        });

        record.set('readingTypes', readingTypes);
        record.endEdit();
        return record;
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
            router = me.getController('Uni.controller.history.Router'),
            form = button.up('panel'),
            formErrorsPanel = form.down('[name=form-errors]'),
            arrReadingTypes = [],
            propertyForm = this.getAddRule().down('property-form'),
            record = me.formToModel();

        form.down('#addRuleName').clearInvalid();
        form.down('#validatorCombo').clearInvalid();
        form.down('#readingTypesErrorLabel').hide();
        form.down('#propertiesErrorLabel').hide();
        form.down('#readingTypesGridPanel').removeCls('error-border');

        formErrorsPanel.hide();

        record.set('ruleSet', {
            id: router.arguments.ruleSetId
        });

        if (button.action === 'editRuleAction') {
            record.readingTypes().removeAll();
        }

        Ext.each(record.get('readingTypes'), function (record) {
            var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
            readingTypeRecord.set('mRID', record.mRID);
            arrReadingTypes.push(readingTypeRecord);
        });

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord().properties();
        }

        record.readingTypes().add(arrReadingTypes);

        me.getAddRule().setLoading('Loading...');
        record.getProxy().setUrl(router.arguments.ruleSetId);
        record.save({
            success: function (record) {
                var messageText;
                if (button.action === 'editRuleAction') {
                    messageText = Uni.I18n.translate('validation.editRuleSuccess.msg', 'CFG', 'Validation rule saved');
                } else {
                    messageText = Uni.I18n.translate('validation.addRuleSuccess.msg', 'CFG', 'Validation rule added');
                }
                if (me.fromRulePreview) {
                    router.getRoute('administration/rulesets/overview/rules/overview').forward({ruleId: record.getId()});
                } else {
                    router.getRoute('administration/rulesets/overview/rules').forward();
                }

                me.getApplication().fireEvent('acknowledge', messageText);
            },
            failure: function (record, operation) {
                me.getAddRule().setLoading(false);
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    Ext.Array.each(json.errors, function (item) {
                        if (item.id.indexOf("name") !== -1) {
                            form.down('#addRuleName').setActiveError(item.msg);
                        }
                        if (item.id.indexOf("readingTypes") !== -1) {
                            form.down('#readingTypesGridPanel').addCls('error-border');
                            form.down('#readingTypesErrorLabel').setText(item.msg);
                            form.down('#readingTypesErrorLabel').show();
                        }
                        if (item.id.indexOf("properties") !== -1) {
                            form.down('#propertiesErrorLabel').setText(item.msg);
                            form.down('#propertiesErrorLabel').show();
                        }
                    });
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }
            }
        });
    },

    updateProperties: function (field, newValue) {
        var record = this.getValidatorsStore().getById(newValue),
            propertyForm = this.getAddRule().down('property-form');

        if (record && record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);

            propertyForm.on('afterlayout', function(){
                if (propertyForm.down('#minimumnumberfield')) {
                    propertyForm.down('#minimumnumberfield').hasNotValueSameAsDefaultMessage = true;
                    propertyForm.down('#maximumnumberfield').hasNotValueSameAsDefaultMessage = true;
                }
                propertyForm.un('afterlayout');
            })
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
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

    addReadingTypes: function () {
        if (!this.validationRuleRecord) {
            this.forwardToPreviousPage();
        } else {
            var me = this,
                widget = Ext.widget('AddReadingTypesToRuleSetup'),
                unitsOfMeasureStore = Ext.create('Cfg.store.UnitsOfMeasure'),
                timeOfUseStore = Ext.create('Cfg.store.TimeOfUse'),
                intervalsStore = Ext.create('Cfg.store.Intervals');

            me.getApplication().fireEvent('changecontentevent', widget);
            widget.down('#unitsOfMeasureCombo').bindStore(unitsOfMeasureStore);
            widget.down('#intervalsCombo').bindStore(intervalsStore);
            widget.down('#timeOfUseCombo').bindStore(timeOfUseStore);
            me.loadReadingTypes();
        }

    },


    clearAllCombos: function () {
        var widget = this.getAddReadingTypesSetup(),
            unitOfMeasureCombo = widget.down('#unitsOfMeasureCombo'),
            intervalsCombo = widget.down('#intervalsCombo'),
            timeOfUseCombo = widget.down('#timeOfUseCombo'),
            readingTypeNameText = widget.down('#readingTypeNameTextField');

        unitOfMeasureCombo.setValue(null);
        intervalsCombo.setValue(null);
        timeOfUseCombo.setValue(null);
        readingTypeNameText.setValue(null);
    },

    clearAllFilters: function () {
        this.clearAllCombos();
        this.loadReadingTypes();
    },

    removeFilter: function (key) {
        var widget = this.getAddReadingTypesSetup(),
            field;

        if (key === 'name') {
            field = widget.down('textfield[name=' + key + ']');
        } else {
            field = widget.down('combobox[name=' + key + ']');
        }

        field.setValue(null);
        this.loadReadingTypes();
    },

    addReadingTypesToGrid: function () {
        var me = this,
            widget = this.getAddReadingTypesSetup(),
            grid = widget.down('#addReadingTypesGrid'),
            selection = grid.getView().getSelectionModel().getSelection(),
            existedReadingTypes = me.validationRuleRecord.get('readingTypes');

        if (selection.length > 0) {
            Ext.each(selection, function (record) {
                existedReadingTypes.push(record.get('readingType'));
            });
        }

        me.validationRuleRecord.set('readingTypes', existedReadingTypes);

        me.forwardToPreviousPage();
    },

    forwardToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();

        router.getRoute(splittedPath.join('/')).forward();
    },

    loadReadingTypes: function () {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget = me.getAddReadingTypesSetup(),
            readingTypeStore = Ext.create('Cfg.store.ReadingTypesToAddForRule'),
            unitOfMeasureCombo = widget.down('#unitsOfMeasureCombo'),
            intervalsCombo = widget.down('#intervalsCombo'),
            timeOfUseCombo = widget.down('#timeOfUseCombo'),
            readingTypeNameText = widget.down('#readingTypeNameTextField'),
            unitOfMeasureComboValue = unitOfMeasureCombo.getValue(),
            filter = widget.down('#filterReadingTypes'),
            filterBtns = Ext.ComponentQuery.query('#filterReadingTypes tag-button'),
            bulkGridContainer = widget.down('#bulkReadingTypesContainer'),
            properties = [],
            unitOfMeasureRecord,
            intervalsRecord,
            previewContainer;

        bulkGridContainer.removeAll();

        Ext.each(filterBtns, function (btn) {
            btn.destroy();
        });

        if (unitOfMeasureComboValue) {
            unitOfMeasureRecord = unitOfMeasureCombo.findRecord(unitOfMeasureCombo.valueField, unitOfMeasureComboValue);
            properties.push({
                property: 'unitOfMeasure',
                value: unitOfMeasureRecord.get('unit')
            });
            properties.push({
                property: 'multiplier',
                value: unitOfMeasureRecord.get('multiplier')
            });
            filter.setFilter('unitOfMeasure', 'Unit of measure', unitOfMeasureRecord.get('name'), false);
        }

        if (!Ext.isEmpty(intervalsCombo.getValue())) {
            intervalsRecord = intervalsCombo.findRecord(intervalsCombo.valueField, intervalsCombo.getValue());
            properties.push({
                property: 'time',
                value: intervalsCombo.getValue()
            });
            filter.setFilter('time', 'Interval', intervalsRecord.get('name'), false);
        }

        if (readingTypeNameText.getValue()) {
            properties.push({
                property: 'name',
                value: readingTypeNameText.getValue()
            });
            filter.setFilter('name', 'Name', readingTypeNameText.getValue(), false);
        }

        if (!Ext.isEmpty(timeOfUseCombo.getValue())) {
            properties.push({
                property: 'tou',
                value: timeOfUseCombo.getValue()
            });
            filter.setFilter('tou', 'Time of use', timeOfUseCombo.getValue(), false);
        }

        if (me.validationRuleRecord) {
            var readingTypes = me.validationRuleRecord.get('readingTypes');
            if (Ext.isArray(readingTypes) && !Ext.isEmpty(readingTypes)) {
                var mRIDs = [];
                readingTypes.forEach(function (readingType) {
                    mRIDs.push(readingType.mRID.toLowerCase());

                });
                properties.push({
                    property: 'selectedReadings',
                    value:mRIDs
                });
            }
        }

        readingTypeStore.getProxy().setExtraParam('filter', Ext.encode(properties));
        readingTypeStore.loadData([], false);

        previewContainer = {
            xtype: 'preview-container',
            selectByDefault: false,
            grid: {
                itemId: 'addReadingTypesGrid',
                xtype: 'addReadingTypesBulk',
                store: readingTypeStore,
                height: 600,
                plugins: {
                    ptype: 'bufferedrenderer'
                }
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                margin: '0 0 20 0',
                title: Uni.I18n.translate('validation.readingType.empty.title', 'CFG', 'No reading types found.'),
                reasons: [
                    Uni.I18n.translate('validation.readingType.empty.list.item1', 'CFG', 'No reading types have been added yet.'),
                    Uni.I18n.translate('validation.readingType.empty.list.item2', 'CFG', 'No reading types comply to the filter.'),
                    Uni.I18n.translate('validation.readingType.empty.list.item3', 'CFG', 'All reading types have been already added to rule.')
                ]
            }
        };

        bulkGridContainer.add(previewContainer);

        //<debug>
        Ext.override(Ext.data.proxy.Ajax, {timeout: 120000});
        //</debug>

        readingTypeStore.on('load', function (store, records) {
            if (!records || !records.length) {
                widget.down('#buttonsContainer button[name=add]').setDisabled(true);
            }
        }, me, {
            single: true
        });

        readingTypeStore.load();
    },

    checkMridAlreadyAdded: function (array, record) {
        var isExist = false;
        Ext.each(array, function (addedRecord) {
            if (record.get('mRID') === addedRecord.mRID) {
                isExist = true;
            }
        });
        return isExist;
    },

    addRule: function (ruleSetId) {
        var me = this,
            widget = Ext.widget('addRule', {
                edit: false,
                returnLink: '#/administration/validation/rulesets/' + ruleSetId + '/rules'
            }),
            editRulePanel = me.getAddRule(),
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets'),
            readingTypesStore = me.getStore('ReadingTypesForRule'),
            model = Ext.create('Cfg.model.ValidationRule'),
            propertyForm,
            form;

        me.ruleId = null;

        me.getValidatorsStore().load({
            callback: function () {
                me.ruleSetId = ruleSetId;
                me.getApplication().fireEvent('changecontentevent', widget);
                readingTypesStore.removeAll();

                if (me.validationRuleRecord) {
                    me.modelToForm(null, me.validationRuleRecord, false);
                } else {
                    form = editRulePanel.down('#addRuleForm').getForm();
                    propertyForm = widget.down('property-form');
                    form.loadRecord(model);
                    propertyForm.loadRecord(model);
                }

                editRulePanel.down('#addRuleTitle').setTitle(Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'));
                me.ruleModel = null;

                ruleSetsStore.load({
                    params: {
                        id: ruleSetId
                    }
                });
            }
        });
    },

    createEditNewRuleSet: function (button) {
        var me = this,
            createEditRuleSetPanel = me.getCreateRuleSet(),
            form = button.up('form'),
            record,
            formErrorsPanel = form.down('[name=form-errors]');

        formErrorsPanel.hide();
        if (button.text === 'Save') {
            record = me.ruleSetModel;
        } else {
            record = Ext.create('Cfg.model.ValidationRuleSet');
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
        });
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
        this.validationRuleRecord = null;
    },

    showRules: function (id) {
        var me = this,
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        me.validationRuleRecord = null;
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
                rulesContainerWidget.down('#stepsMenu #ruleSetOverviewLink').setText(selectedRuleSet.get('name'));
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
                rulesContainerWidget.down('#stepsMenu #ruleSetOverviewLink').setText(ruleSet.get('name'));
                if (me.mdcIsActive) {
                    rulesContainerWidget.down('#deviceConfigLink').show();
                }

                me.getApplication().fireEvent('loadRuleSet', ruleSet);
                rulesContainerWidget.down('ruleset-action-menu').record = ruleSet;
            }
        });
    },

    previewValidationRuleSet: function (selectionModel, record) {
        if (record) {
            Ext.suspendLayouts();

            this.getRuleSetBrowsePreviewCt().removeAll(true);
            var rulesPreviewContainerPanel = Ext.widget('rule-preview-container-panel', {
                ruleSetId: record.getId(),
                title: record.get('name'),
                isSecondPagination: true
            });
            this.ruleSetId = record.getId();
            Ext.Array.each(Ext.ComponentQuery.query('#addRuleLink'), function (item) {
                item.hide();
            });
            this.getRuleSetBrowsePreviewCt().add(rulesPreviewContainerPanel);

            Ext.resumeLayouts(true);
        }
    },

    previewValidationRule: function (grid, record) {
        Ext.suspendLayouts();

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

        Ext.resumeLayouts();
    },

    showEditRuleOverview: function (id, ruleId) {
        var me = this,
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            widget,
            ruleSet,
            cancelLink;

        me.getValidatorsStore().load({
            callback: function () {
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

               ;
                me.getApplication().fireEvent('changecontentevent', widget);

                if (me.validationRuleRecord) {
                    me.modelToForm(null, me.validationRuleRecord, true);
                } else {
                    me.modelToForm(id, null, true);
                }

                ruleSetsStore.load({
                    callback: function () {
                        ruleSet = this.getById(parseInt(id));
                        me.getApplication().fireEvent('loadRuleSet', ruleSet);
                    }
                });
            }
        });
    },

    modelToForm: function (id, record, isEdit) {
        var me = this,
            rulesStore = me.getStore('Cfg.store.ValidationRules'),
            editRulePanel = me.getAddRule(),
            form = editRulePanel.down('#addRuleForm').getForm(),
            grid = editRulePanel.down('#readingTypesGridPanel'),
            validatorField = editRulePanel.down('#validatorCombo'),
            widget = me.getAddRule(),
            propertyForm = widget.down('property-form'),
            loadRecordToForm,
            rule;

        loadRecordToForm = function (rule) {
            if (isEdit) {
                editRulePanel.down('#addRuleTitle').setTitle(me.ruleTitle);
                validatorField.disable();
            }

            form.loadRecord(rule);

            grid.getStore().removeAll();

            Ext.each(rule.get('readingTypes'), function (readingType) {
                grid.getStore().add({readingType: readingType})
            });

            if (rule.properties().count() && rule.get('implementation') !== 'com.elster.jupiter.validators.impl.MissingValuesValidator') {
                propertyForm.loadRecord(rule);
                propertyForm.show();
            } else {
                propertyForm.hide();
            }
        };

        if (record) {
            loadRecordToForm(record);
        } else {
            editRulePanel.setLoading();
            rulesStore.load({
                params: {
                    id: id
                },
                callback: function () {
                    editRulePanel.setLoading(false);
                    rule = this.getById(me.ruleId);
                    if(!rule){
                        crossroads.parse("/error/notfound");
                        return;
                    }
                    me.ruleModel = rule;
                    me.ruleTitle = "Edit '" + rule.get('name') + "'"

                    me.getApplication().fireEvent('loadRule', rule);

                    loadRecordToForm(rule);
                }
            });
        }
    },

    chooseRuleAction: function (menu, item) {
        var me = this,
            record;

        record = menu.record || me.getRulesGrid().getSelectionModel().getLastSelected();
        me.getRuleSetBrowsePanel() ? me.fromRuleSet = true : me.fromRuleSet = false;
        me.getRuleOverview() ? me.fromRulePreview = true : me.fromRulePreview = false;

        switch (item.action) {
            case 'view':
                location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/rules/' + record.get('id');
                break;
            case 'activateRule':
                me.deactivateRule(record);
                break;
            case 'deactivateRule':
                me.deactivateRule(record);
                break;
            case 'editRule':
                location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/rules/' + record.get('id') + '/edit';
                break;
            case 'deleteRule':
                me.showDeleteConfirmation(record);
                break;
        }
    },

    deactivateRule: function (record, active) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            view = me.getRulePreviewContainer() || me.getRuleOverview() || me.getRulePreviewContainerPanel(),
            ruleSetWithRulesView = me.getRuleSetBrowsePanel(),
            grid = view.down('grid'),
            rule = record,
            isActive = record.get('active');

        if (record) {
            record.getProxy().setUrl(router.arguments.ruleSetId || record.get('ruleSetId'));
            record.readingTypes().removeAll();
            Ext.Array.each(record.get('readingTypes'), function (item) {
                var readingTypeRecord = Ext.create(Cfg.model.ReadingType);
                readingTypeRecord.set('mRID', item.mRID);
                record.readingTypes().add(readingTypeRecord);
            });
        }

        record.beginEdit();
        record.set('active', !isActive);
        record.endEdit(true);

        if (!ruleSetWithRulesView) {
            view.setLoading('Loading...');
        } else {
            ruleSetWithRulesView.setLoading();
        }

        record.save({
            params: {
                id: record.get('ruleSetId'),
                ruleId: record.get('id')
            },
            success: function (record, operation) {
                var ruleSet;
                if (ruleSetWithRulesView) {
                    ruleSet = ruleSetWithRulesView.down('#validationrulesetList').getSelectionModel().getLastSelected();
                    ruleSet.set('numberOfInactiveRules', isActive ? ruleSet.get('numberOfInactiveRules') + 1 : ruleSet.get('numberOfInactiveRules') - 1);
                    ruleSet.commit();
                }
                view.down('validation-rule-preview').loadRecord(record);
                if (isActive) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.deactivateRuleSuccess.msg', 'CFG', 'Validation rule deactivated'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.activateRuleSuccess.msg', 'CFG', 'Validation rule activated'));
                }
            },
            callback: function () {
                if (!ruleSetWithRulesView) {
                    view.setLoading(false);
                } else {
                    ruleSetWithRulesView.setLoading(false);
                }
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
            router = this.getController('Uni.controller.history.Router'),
            view = self.getRulePreviewContainer() || self.getRuleSetBrowsePanel() || self.getRuleOverview(),
            grid = view.down('#validationruleList'),
            gridRuleSet = view.down('#validationrulesetList');

        if (gridRuleSet) {
            var ruleSetSelModel = gridRuleSet.getSelectionModel(),
                ruleSet = ruleSetSelModel.getLastSelected();
        }

        view.setLoading('Removing...');

        rule.getProxy().setUrl(router.arguments.ruleSetId);
        rule.destroy({
            callback: function (records, operation) {
                if (operation.success) {
                    if (self.getRulePreviewContainer()) {
                        view.down('pagingtoolbartop').totalCount = 0;
                    } else if (self.getRuleSetBrowsePanel()) {
                        view.down('#rulesTopPagingToolbar').totalCount = 0;
                    }
                    if (self.getRuleSetBrowsePanel()) {
                        gridRuleSet.getStore().load({
                            callback: function () {
                                ruleSetSelModel.select(ruleSet);
                                self.getRuleSetBrowsePanel().setLoading(false);
                            }
                        });
                    } else if (self.getRuleOverview()) {
                        router.getRoute('administration/rulesets/overview/rules').forward();
                    } else {
                        grid.getStore().load({
                            params: {
                                id: rule.get('ruleSetId')
                            }
                        });
                    }
                    self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeRuleSuccess.msg', 'CFG', 'Validation rule removed'));
                }

                view.setLoading(false);
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
                location.href = '#/administration/validation/rulesets/' + record.get('id');
                break;
            case 'editRuleSet':
                location.href = '#/administration/validation/rulesets/' + record.get('id') + '/edit';
                break;
            case 'deleteRuleSet':
                me.checkIfRuleSetIsInUse(record);
                break;
        }
    },

    checkIfRuleSetIsInUse: function (ruleSet) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/val/validation/' + ruleSet.get('id') + '/usage',
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText);

                me.showDeleteRuleSetConfirmation(ruleSet, response.isInUse);
            }
        })
    },

    showDeleteRuleSetConfirmation: function (ruleSet, jsonIsInUse) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: me.getDeleteRuleSetConfirmationMsg(jsonIsInUse),
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

    getDeleteRuleSetConfirmationMsg: function (jsonIsInUse) {
        if (jsonIsInUse === "true") {
            return Uni.I18n.translate('validation.removeRuleSet.msgInUse', 'CFG', 'This validation rule set is in use. The validation rule set will no longer be available.')
        } else {
            return Uni.I18n.translate('validation.removeRuleSet.msg', 'CFG', 'This validation rule set will no longer be available.')
        }
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
                    if (grid.getStore().getCount() != 0) {
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
        Cfg.model.ValidationRuleSet.load(ruleSetId, {
            success: function (ruleSet) {
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
        var rulesStore = me.getValidationRulesStore();
        rulesStore.load({
            params: {
                id: ruleSetId
            },
            callback: function (records, operation, success) {
                var rule = rulesStore.getById(parseInt(ruleId));
                var itemForm = rulesContainerWidget.down('validation-rule-preview');
                itemForm.updateValidationRule(rule);
                itemForm.down('#rulePreviewActionsButton').destroy();
                itemForm.setTitle('');

                me.getApplication().fireEvent('loadRule', rule);
                rulesContainerWidget.down('validation-rule-action-menu').record = rule;
                rulesContainerWidget.down('#stepsRuleMenu #ruleSetOverviewLink').setText(rule.get('name'));
                rulesContainerWidget.setLoading(false);
            }
        });
    }
});
