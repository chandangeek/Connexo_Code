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
        'Cfg.store.ReadingTypesToAddForRule',
        'ValidationRuleSetVersions'
    ],

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.model.Validator',
        'Cfg.privileges.Validation'
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
        'validation.AddReadingTypesBulk',
        'validation.VersionsContainer',
        'validation.AddVersion',
        'validation.VersionOverview',
        'validation.VersionRulePreviewContainer'
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
		{ref: 'versionsPreviewContainerPanel', selector: 'versions-preview-container-panel'},		
        {ref: 'rulePreviewContainer', selector: 'rulePreviewContainer'},
        {ref: 'ruleOverview', selector: 'ruleOverview'},
        {ref: 'ruleSetBrowsePreviewCt', selector: '#ruleSetBrowsePreviewCt'},
        {ref: 'versionValidationRulesBrowsePreviewCt', selector: '#versionValidationRulesBrowsePreviewCt'},
        {ref: 'rulePreviewContainerPanel', selector: 'rule-preview-container-panel'},
        {ref: 'addReadingTypesSetup', selector: '#addReadingTypesToRuleSetup'},
        {ref: 'versionsContainer', selector: 'versionsContainer'},
        {ref: 'addVersion', selector: 'addVersion'},
        {ref: 'versionOverview', selector: 'versionOverview'},
        {ref: 'versionRulePreviewContainer', selector: 'versionRulePreviewContainer'},
        {ref: 'versionsGrid', selector: 'versionsList'}


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
            '#versionsList': {
                select: this.previewVersionValidationRule
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
            '#addReadingTypesToRuleSetup cfg-side-filter button[action=clearfilter]': {
                click: this.clearAllCombos
            },
            '#addReadingTypesToRuleSetup #buttonsContainer button[name=add]': {
                click: this.addReadingTypesToGrid
            },
            'addReadingTypesBulk': {
                selectionchange: this.setAddReadingTypesAddBtnState
            },
            'ruleSetSubMenu': {
                beforerender: this.onRuleSetMenuBeforeRender
            },
            'addVersion button[action=editVersionAction]': {
                click: this.createEditVersion
            },
            'addVersion button[action=createVersionAction]': {
                click: this.createEditVersion
            },
            'versions-action-menu': {
                click: this.chooseRuleSetVersionAction,
                show: this.onVersionMenuShow
            },
            'version-action-menu': {
                click: this.chooseRuleSetVersionAction,
                show: this.onVersionMenuShow
            }

        });
    },

    onRuleSetMenuBeforeRender: function (menu) {
        this.getApplication().fireEvent('validationrulesetmenurender', menu);
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

        record.set('ruleSetVersion', {
            id: router.arguments.versionId
        });

        //if (button.action === 'editRuleAction') {
        record.readingTypes().removeAll();
        //}

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

        me.getAddRule().setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));
        record.getProxy().setUrl(router.arguments.ruleSetId, router.arguments.versionId);
        record.save({
            success: function (record) {
                var messageText;
                if (button.action === 'editRuleAction') {
                    messageText = Uni.I18n.translate('validation.editRuleSuccess.msg', 'CFG', 'Validation rule saved');
                } else {
                    messageText = Uni.I18n.translate('validation.addRuleSuccess.msg', 'CFG', 'Validation rule added');
                }
                if (me.fromRulePreview) {
                    router.getRoute('administration/rulesets/overview/versions/overview/rules').forward({ruleSetId: router.arguments.ruleSetId, versionId: router.arguments.versionId});
                } else {
                    router.getRoute('administration/rulesets/overview/versions').forward({ruleSetId: router.arguments.ruleSetId});
                }

                me.getApplication().fireEvent('acknowledge', messageText);
                me.validationRuleRecord = null;
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
                    me.validationRuleRecord = null;
                }
            }
        });
    },

    updateProperties: function (field, newValue) {
        var record = this.getValidatorsStore().getById(newValue),
            propertyForm = this.getAddRule().down('property-form');

        if (record && record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);

            propertyForm.on('afterlayout', function () {
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
                widget = Ext.widget('AddReadingTypesToRuleSetup');
                unitsOfMeasureStore = me.getStore('UnitsOfMeasure'),

            me.getApplication().fireEvent('changecontentevent', widget);
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
            intervalsCombo = widget.down('#intervalsCombo'),
            timeOfUseCombo = widget.down('#timeOfUseCombo'),
            filterBtns = Ext.ComponentQuery.query('#filterReadingTypes tag-button');

        Ext.each(filterBtns, function (btn) {
            btn.destroy();
        });


        if (me.validationRuleRecord) {
            var readingTypes = me.validationRuleRecord.get('readingTypes');
            if (Ext.isArray(readingTypes) && !Ext.isEmpty(readingTypes)) {
                var mRIDs = [];
                readingTypes.forEach(function (readingType) {
                    mRIDs.push(readingType.mRID.toLowerCase());

                });
                viewport.down('readingTypesToAddForRule').setSelectedReadings(mRIDs);
            }
        }

        var readingTypeStore = Ext.ComponentQuery.query('#addReadingTypesGrid')[0].store;
        readingTypeStore.on('beforeLoad',function(){
            me.getAddReadingTypesSetup().down('#buttonsContainer button[name=add]').setDisabled(true);
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

    addRule: function (ruleSetId, versionId) {
        var me = this,
            widget = Ext.widget('addRule', {
                edit: false,
                returnLink: '#/administration/validation/rulesets/' + ruleSetId + '/versions'
            }),
            editRulePanel = me.getAddRule(),
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets'),
            readingTypesStore = me.getStore('ReadingTypesForRule'),
            model = Ext.create('Cfg.model.ValidationRule'),
            propertyForm,
            form;

		//refresh breadcrumb 
		Cfg.model.ValidationRuleSet.load(ruleSetId, {
            success: function (ruleSet) {
				me.getApplication().fireEvent('loadRuleSet', ruleSet);
				
				var versionStore = me.getValidationRuleSetVersionsStore();
				versionStore.load({
					params: {
						ruleSetId: ruleSetId,
						versionId: versionId
					},
					callback: function (records, operation, success) {
						var version = versionStore.getById(parseInt(versionId));
						me.getApplication().fireEvent('loadVersion', version);
					}
				});		
			}
		});
				
        me.ruleId = null;

        me.getValidatorsStore().load({
            callback: function () {
                me.ruleSetId = ruleSetId;
                me.getApplication().fireEvent('changecontentevent', widget);
                readingTypesStore.removeAll();

                if (me.validationRuleRecord) {
                    me.modelToForm(null, null, null, me.validationRuleRecord, false);
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
        createEditRuleSetPanel.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));

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
                    location.href = '#/administration/validation/rulesets/' + record.get('id');
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
        view.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));
        ruleSetsStore.load({
            callback: function () {
                view.setLoading(false);
                ruleSet = this.getById(parseInt(ruleSetId));
                view.down('#editRuleSetTitle').setTitle("Edit '" + Ext.String.htmlEncode(ruleSet.get('name')) + "'");
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

                me.getApplication().fireEvent('loadRuleSet', ruleSet);
                var actionMenu = rulesContainerWidget.down('ruleset-action-menu');
                if(actionMenu)
                    actionMenu.record = ruleSet;
            }
        });
    },

    previewValidationRuleSet: function (selectionModel, record) {
        if (record) {
            Ext.suspendLayouts();

            this.getRuleSetBrowsePreviewCt().removeAll(true);
            var rulesPreviewContainerPanel = Ext.widget('versions-preview-container-panel', {
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
            itemPanel = me.getRulePreviewContainer() || me.getRuleSetBrowsePanel() || me.getVersionsContainer() || me.getVersionRulePreviewContainer(),
            itemForm = itemPanel.down('validation-rule-preview'),
            selectedRule;
        if (grid.view) {
            selectedRule = grid.view.getSelectionModel().getLastSelected();
        } else {
            selectedRule = record;
        }
        me.ruleId = selectedRule.internalId;

        itemForm.updateValidationRule(selectedRule);

        var actionMenu = null;
        if (me.getRuleSetBrowsePanel()) {
            actionMenu = me.getRulePreview().down('validation-rule-action-menu');
        } else if (me.getRulePreviewContainer()) {
            actionMenu = itemForm.down('validation-rule-action-menu');
        } else if (me.getVersionsContainer()){
            actionMenu = me.getRulePreview().down('validation-rule-action-menu');
        } else if (me.getVersionRulePreviewContainer()) {
            actionMenu = me.getRulePreview().down('validation-rule-action-menu');
        }
        if(actionMenu)
            actionMenu.record = record;

        Ext.resumeLayouts();
    },

    showEditRuleOverview: function (ruleSetId, versionId, ruleId) {
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
                    cancelLink = '#/administration/validation/rulesets/' + ruleSetId + '/versions/' + versionId + '/rules/' + me.ruleId;
                } else {
                    cancelLink = '#/administration/validation/rulesets/' + ruleSetId + '/versions/' + versionId;
                }

                me.ruleSetId = parseInt(ruleSetId);
                me.ruleId = parseInt(ruleId);

                widget = Ext.widget('addRule', {
                    edit: true,
                    returnLink: cancelLink
                });

                me.getApplication().fireEvent('changecontentevent', widget);

                if (me.validationRuleRecord) {
                    me.modelToForm(null, null, null, me.validationRuleRecord, true);
                } else {
                    me.modelToForm(ruleSetId, versionId, ruleId, null, true);
                }

                ruleSetsStore.load({
                    callback: function () {
                        ruleSet = this.getById(parseInt(ruleSetId));
                        me.getApplication().fireEvent('loadRuleSet', ruleSet);
						
						var versionStore = me.getValidationRuleSetVersionsStore();
						versionStore.load({
							params: {
								ruleSetId: ruleSetId,
								versionId: versionId
							},
							callback: function (records, operation, success) {
								var version = versionStore.getById(parseInt(versionId));
								me.getApplication().fireEvent('loadVersion', version);

							}
						});	
                    }
                });
            }
        });
    },

    modelToForm: function (ruleSetId, versionId, ruleId, record, isEdit) {
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
            editRulePanel.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));
            rulesStore.load({
                params: {
                    ruleSetId: ruleSetId,
                    versionId: versionId,
                    ruleId: ruleId
                },
                callback: function () {
                    editRulePanel.setLoading(false);
                    rule = this.getById(me.ruleId);
                    if (!rule) {
                        crossroads.parse("/error/notfound");
                        return;
                    }
                    me.ruleModel = rule;
                    me.ruleTitle = "Edit '" + Ext.String.htmlEncode(rule.get('name')) + "'"

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
                location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('ruleSetVersionId') + '/rules/' + record.get('id');
                break;
            case 'activateRule':
                me.deactivateRule(record);
                break;
            case 'deactivateRule':
                me.deactivateRule(record);
                break;
            case 'editRule':
                location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId')  + '/versions/' + record.get('ruleSetVersionId') +  '/rules/' + record.get('id') + '/edit';
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
            versionsGrid = me.getVersionsPreviewContainerPanel(),
            grid = view.down('grid'),
            rule = record,
            isActive = record.get('active');

        if (record) {
            record.getProxy().setUrl(record.get('ruleSetId'), record.get('ruleSetVersionId'));
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

        if (!versionsGrid) {
            view.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));
        } else {
            versionsGrid.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));
        }

        record.save({
            params: {
                ruleId: record.get('id')
            },
            success: function (record, operation) {
                var ruleSet;
                if (versionsGrid) {
                    ruleSet = versionsGrid.down('#versionsList').getSelectionModel().getLastSelected();
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
                if (!versionsGrid) {
                    view.setLoading(false);
                } else {
                    versionsGrid.setLoading(false);
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
                        self.deleteRule(rule);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var self = this,
            router = this.getController('Uni.controller.history.Router'),
            view = self.getRulePreviewContainer() || self.getRuleSetBrowsePanel() || self.getRuleOverview() || self.getVersionsContainer() || self.getRulePreviewContainerPanel();

        view.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));

        rule.getProxy().setUrl(rule.get('ruleSetVersion').ruleSet.id, rule.get('ruleSetVersionId'));
        rule.destroy({
            callback: function (records, operation) {
                if (operation.success) {
                    if (self.getRulePreviewContainer()) {
                        view.down('pagingtoolbartop').totalCount = 0;
                    } else if (self.getRuleSetBrowsePanel()) {
                        view.down('#rulesTopPagingToolbar').totalCount = 0;
                    }

                    if (self.getRuleOverview()) {
                        router.getRoute('administration/rulesets/overview/versions/overview/rules').forward({ruleSetId: rule.get('ruleSetVersion').ruleSet.id, versionId: rule.get('ruleSetVersionId')});
                    } else if (self.getVersionOverview()) {
                        router.getRoute('administration/rulesets/overview/versions').forward({ruleSetId: rule.get('ruleSetVersion').ruleSet.id});
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
                        me.deleteRuleSet(ruleSet);
                        break;
                    case 'cancel':
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
			
        view.setLoading(Uni.I18n.translate('general.removing', 'CFG', 'Removing...'));
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

    showRuleOverview: function (ruleSetId, versionId, ruleId) {
        var me = this,
            rulesContainerWidget = Ext.widget('ruleOverview',
                {
                    ruleSetId: ruleSetId,
                    versionId: versionId,
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
		
		var versionStore = me.getValidationRuleSetVersionsStore();
        versionStore.load({
			params: {
				ruleSetId: ruleSetId,
				versionId: versionId
			},
			callback: function (records, operation, success) {
				var version = versionStore.getById(parseInt(versionId));
				me.getApplication().fireEvent('loadVersion', version);
				
				var rulesStore = me.getValidationRulesStore();
				rulesStore.load({
					params: {
						ruleSetId: ruleSetId,
						versionId: versionId
					},
					
					callback: function (records, operation, success) {
						var rule = rulesStore.getById(parseInt(ruleId));
						var itemForm = rulesContainerWidget.down('validation-rule-preview');
						itemForm.updateValidationRule(rule);
						var actionButton = itemForm.down('#rulePreviewActionsButton');
						if(actionButton)
							actionButton.destroy();
						itemForm.setTitle('');

						me.getApplication().fireEvent('loadRule', rule);
						actionButton = rulesContainerWidget.down('validation-rule-action-menu');
						if(actionButton)
							actionButton.record = rule;
						rulesContainerWidget.down('#stepsRuleMenu #ruleSetOverviewLink').setText(rule.get('name'));
						rulesContainerWidget.setLoading(false);
					}
				});
			}
		});		
    },

    showVersions: function (id) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');

        me.ruleSetId = id;

        ruleSetsStore.load({
            params: {
                id: id
            },
            callback: function () {
                var selectedRuleSet = ruleSetsStore.getByInternalId(id),
                    versionsContainerWidget = Ext.widget('versionsContainer', {
                        router: router,
                        ruleSetId: id
                    });

                me.getApplication().fireEvent('changecontentevent', versionsContainerWidget);
                versionsContainerWidget.down('#stepsMenu #ruleSetOverviewLink').setText(selectedRuleSet.get('name'));

                me.getApplication().fireEvent('loadRuleSet', selectedRuleSet);
            }
        });

    },

    previewVersionValidationRule: function (selectionModel, record) {
        var me = this;

        if (record) {
            Ext.suspendLayouts();

            this.getVersionValidationRulesBrowsePreviewCt().removeAll(true);
            var versionValidationRulesPreviewContainerPanel = Ext.widget('rule-preview-container-panel', {
                ruleSetId: me.ruleSetId,
                versionId: record.getId(),
                title: record.get('name'),
                isSecondPagination: true
            });
            this.versionId = record.getId();
            if (me.getRuleSetsGrid()){
                Ext.Array.each(Ext.ComponentQuery.query('#newVersion'), function (item) {
                    item.hide();
                });
            }

            if (me.getVersionsGrid()){
                Ext.Array.each(Ext.ComponentQuery.query('#addRuleLink'), function (item) {
                    item.hide();
                });
            }

            this.getVersionValidationRulesBrowsePreviewCt().add(versionValidationRulesPreviewContainerPanel);

            Ext.resumeLayouts(true);
        }

    },

    onVersionMenuShow: function (menu) {

    },

    chooseRuleSetVersionAction: function(menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            record;

        record = menu.record || me.getVersionsGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'cloneVersion':
                location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('id') + '/clone';
                break;
            case 'editVersion':
                location.href = '#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('id') + '/edit';
                break;
            case 'deleteVersion':
                me.showDeleteVersionConfirmation(record);
                break;
        }
    },

    showDeleteVersionConfirmation: function (version) {
        var self = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('validation.removeVersion.msg', 'CFG', 'This validation rule version will no longer be available on the validation rule set.'),
            title: Ext.String.format(Uni.I18n.translate('validation.removeVersion.title', 'CFG', "Remove '{0}'?"), version.get('name')),
            config: {
                version: version
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        self.deleteVersion(version);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteVersion: function (version) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            view = me.getVersionsContainer() || me.getVersionOverview() || me.getRuleSetsGrid(),
            grid = me.getVersionsGrid();

        view.setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));

        version.getProxy().setUrl(version.get('ruleSetId'), version.get('id'));
        version.destroy({
            callback: function (records, operation) {
                if (operation.success) {
                    if (me.getVersionsContainer()) {
                        view.down('pagingtoolbartop').totalCount = 0;
                    }
                    if (me.getRulePreviewContainer()) {
                        view.down('pagingtoolbartop').totalCount = 0;
                    }

                    if (me.getVersionOverview()) {
                        router.getRoute('administration/rulesets/overview/versions').forward({ruleSetId: version.get('ruleSetId')});
                    } else{

                        if (grid && grid.getStore().getCount() != 0) {
                            grid.getStore().load
                            ({
                                params: {
                                    ruleSetId: version.get('ruleSetId')
                                }
                            });
                        }

                    }

                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validation.removeVersionSuccess.msg', 'CFG', 'Validation rule set version removed'));
                }

                view.setLoading(false);
            }
        });
    },

    addVersion: function(ruleSetId){
        this.addEditCloneVersion(ruleSetId, null, false);
    },
    editVersion: function(ruleSetId, versionID){
        this.addEditCloneVersion(ruleSetId, versionID, false);
    },

    cloneVersion: function(ruleSetId, versionID){
        this.addEditCloneVersion(ruleSetId, versionID, true);
    },

    addEditCloneVersion: function(ruleSetId, versionId, isClone) {

        var me = this,
            versionsStore = Ext.create('Cfg.store.ValidationRuleSetVersions'),
            model = Ext.create('Cfg.model.ValidationRuleSetVersion'),
            widget, cancelLink,
            view, form, editVersionPanel, versionsStore, versionRecord, newVersion;

        if (me.getRuleSetsGrid()){
            cancelLink = '#/administration/validation/rulesets';
        }
        else if (me.getVersionsContainer()) {
            cancelLink = '#/administration/validation/rulesets/' + ruleSetId + '/versions';
        }
        else if (me.getVersionOverview()) {
            cancelLink = '#/administration/validation/rulesets/' + ruleSetId + '/versions/'+ versionId;
        }

        widget = Ext.widget('addVersion', {
            edit: (!isClone && versionId),
            returnLink: cancelLink
        });

		// refresh breadcrumb
		Cfg.model.ValidationRuleSet.load(ruleSetId, {
            success: function (ruleSet) {
				me.getApplication().fireEvent('loadRuleSet', ruleSet);
			}
		});
				
        if (versionId){
            versionStore = Ext.create('Cfg.store.ValidationRuleSetVersions');

            versionStore.load({
                params: {
                    ruleSetId: ruleSetId,
                    versionId: versionId
                },
                callback: function (records, operation, success) {
                    if (success) {
                        versionRecord = this.getById(parseInt(versionId));
                        me.getApplication().fireEvent('changecontentevent', widget);
                        editVersionPanel = me.getAddVersion();
                        form = editVersionPanel.down('#addVersionForm').getForm();
                        if (!isClone && (versionId!= null)){
                            editVersionPanel.down('#addVersionTitle').setTitle(Ext.String.format(Uni.I18n.translate('validation.editValidationRulesetVersion', 'CFG', "Edit '{0}'"), versionRecord.get('name')));
                            me.getApplication().fireEvent('loadVersion', versionRecord);
                            form.loadRecord(versionRecord);
                            var startDate = versionRecord.get('startDate');
                            editVersionPanel.down('#startPeriodTrigger').setValue({startPeriod: startDate && (startDate !== 0) && (startDate !== '')});
                        }
                        else if (isClone){
                            newVersion = Ext.create('Cfg.model.ValidationRuleSetVersion');
                            newVersion.set('name', versionRecord.get('name'));
                            newVersion.set('description', versionRecord.get('description'));
                            newVersion.set('startDate', versionRecord.get('startDate'));
                            editVersionPanel.down('#addVersionTitle').setTitle(Uni.I18n.translate('validation.addValidationRulesetVersion', 'CFG', 'Add validation rule set version'));
                            form.loadRecord(newVersion);
                            newVersion.set('isClone', true);

                        }
                    }
                }
            });

        }
        else {
            me.getApplication().fireEvent('changecontentevent', widget);

            editVersionPanel = me.getAddVersion();
            form = editVersionPanel.down('#addVersionForm').getForm();
            newVersion = Ext.create('Cfg.model.ValidationRuleSetVersion');
            editVersionPanel.down('#addVersionTitle').setTitle(Uni.I18n.translate('validation.addValidationRulesetVersion', 'CFG', 'Add validation rule set version'));
            newVersion.set('startDate', null);
            form.loadRecord(newVersion);

        }

    },

    createEditVersion: function (button) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            form = button.up('panel'),
            versionForm = this.getAddVersion().down('#addVersionForm'),
            formErrorsPanel = form.down('[name=form-errors]'),
            record = versionForm.getForm().getRecord(),
            startOnDate = null;

        if (form.down('#startPeriodTrigger').getValue().startPeriod) {
            startOnDate = moment(form.down('#startDate').getValue()).valueOf();
        }

        var values = form.getValues();
        record.set(values);
        record.set('startDate', startOnDate);

        form.down('#startPeriodOn').clearInvalid();
        formErrorsPanel.hide();
        record.set('ruleSet', {
            id: router.arguments.ruleSetId
        });

        me.getAddVersion().setLoading(Uni.I18n.translate('general.loading', 'CFG', 'Loading...'));
        record.getProxy().setUrl(router.arguments.ruleSetId, router.arguments.versionId, record.get('isClone'));

        record.save({
            success: function (record) {
                var messageText;
                if (button.action === 'editVersionAction') {
                    messageText = Uni.I18n.translate('validation.editVersionSuccess.msg', 'CFG', 'Validation rule set version saved');
                } else {
                    messageText = Uni.I18n.translate('validation.addVersionSuccess.msg', 'CFG', 'Validation rule set version added');
                }
                router.getRoute('administration/rulesets/overview/versions').forward();

                me.getApplication().fireEvent('acknowledge', messageText);
            },
            failure: function (record, operation) {
                me.getAddVersion().setLoading(false);
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    Ext.Array.each(json.errors, function (item) {
                        if (item.id.indexOf("name") !== -1) {
                            form.down('#startPeriodOn').setActiveError(item.msg);
                        }
                    });
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }
            }
        });
    },

    showVersionOverview: function (ruleSetId, versionId) {
  		var me = this,
		versionContainerWidget = Ext.widget('versionOverview',
			{
				ruleSetId: ruleSetId,
				versionId: versionId
			}
		);
		
		me.getApplication().fireEvent('changecontentevent', versionContainerWidget);
	
		versionContainerWidget.setLoading(true);
		
		Cfg.model.ValidationRuleSet.load(ruleSetId, {
            success: function (ruleSet) {
				me.getApplication().fireEvent('loadRuleSet', ruleSet);

				var versionStore = me.getValidationRuleSetVersionsStore();
				versionStore.load({
					params: {
						ruleSetId: ruleSetId,
						versionId: versionId
					},
					callback: function (records, operation, success) {
						var version = versionStore.getById(parseInt(versionId));
						me.getApplication().fireEvent('loadVersion', version);													
						
						var itemForm = versionContainerWidget.down('version-preview');
						itemForm.updateVersion(version);

						var actionMenu = versionContainerWidget.down('#versionActionMenu');
						if(actionMenu)
							actionMenu.record = version;
						versionContainerWidget.setLoading(false);

					}
				});			
			}
		});	
    },

    showVersionRules: function (ruleSetId, versionId) {
 		var me = this;
		
		Cfg.model.ValidationRuleSet.load(ruleSetId, {
            success: function (ruleSet) {
				me.getApplication().fireEvent('loadRuleSet', ruleSet);

				var versionStore = me.getValidationRuleSetVersionsStore();
				versionStore.load({
					params: {
						ruleSetId: ruleSetId,
						versionId: versionId
					},
					callback: function (records, operation, success) {
						var version = versionStore.getById(parseInt(versionId));
						me.getApplication().fireEvent('loadVersion', version);
						
						var  rulesContainerWidget = Ext.widget('versionRulePreviewContainer', {
							ruleSetId: ruleSetId,
							versionId: versionId
						});
						me.getApplication().fireEvent('changecontentevent', rulesContainerWidget);
						

					}
				});			
			}
		});		
    },

    setAddReadingTypesAddBtnState: function (cm, selection) {
        this.getAddReadingTypesSetup().down('#buttonsContainer button[name=add]').setDisabled(Ext.isEmpty(selection));
    }

});
