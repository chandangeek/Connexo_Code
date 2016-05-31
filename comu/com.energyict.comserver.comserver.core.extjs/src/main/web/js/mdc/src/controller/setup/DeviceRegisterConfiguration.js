Ext.define('Mdc.controller.setup.DeviceRegisterConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterconfiguration.numerical.Preview',
        'setup.deviceregisterconfiguration.numerical.Detail',
        'setup.deviceregisterconfiguration.text.Preview',
        'setup.deviceregisterconfiguration.text.Detail',
        'setup.deviceregisterconfiguration.flags.Preview',
        'setup.deviceregisterconfiguration.flags.Detail',
        'setup.deviceregisterconfiguration.billing.Preview',
        'setup.deviceregisterconfiguration.billing.Detail',
        'setup.deviceregisterconfiguration.Setup',
        'setup.deviceregisterconfiguration.Grid',
        'setup.deviceregisterconfiguration.ValidationPreview',
        'setup.deviceregisterconfiguration.TabbedDeviceRegisterView',
        'Mdc.view.setup.deviceregisterconfiguration.EditCustomAttributes',
        'Mdc.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Mdc.view.setup.deviceregisterconfiguration.EditRegister'
    ],

    models: [
        'Mdc.model.RegisterValidationPreview',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister',
        'Mdc.model.DeviceRegister',
        'Mdc.model.DeviceRegisterForPreview'
    ],

    stores: [
        'Mdc.customattributesonvaluesobjects.store.RegisterCustomAttributeSets',
        'RegisterConfigsOfDevice'
    ],

    refs: [
        {ref: 'deviceRegisterConfigurationGrid', selector: '#deviceRegisterConfigurationGrid'},
        {ref: 'deviceRegisterConfigurationSetup', selector: '#deviceRegisterConfigurationSetup'},
        {ref: 'deviceRegisterConfigurationPreview', selector: '#deviceRegisterConfigurationPreview'},
        {ref: 'deviceRegisterConfigurationPreviewForm', selector: '#deviceRegisterConfigurationPreviewForm'},
        {ref: 'deviceRegisterConfigurationDetailForm', selector: '#deviceRegisterConfigurationDetailForm'},
        {ref: 'stepsMenu', selector: '#stepsMenu'},
        {ref: 'editPropertyForm', selector: '#deviceRegisterConfigurationEditCustomAttributes property-form'},
        {ref: 'editCustomAttributesPanel', selector: '#deviceRegisterConfigurationEditCustomAttributes'},
        {ref: 'editCustomRestoreBtn', selector: '#deviceRegisterCustomRestoreBtn'},
        {ref: 'deviceRegistersOverview', selector: 'tabbedDeviceRegisterView'},
        {ref: 'registerEditForm', selector: '#mdc-device-register-edit-form'},
        {ref: 'restoreObisCodeBtn', selector: '#mdc-device-register-edit-form #mdc-restore-obiscode-btn'},
        {ref: 'restoreOverflowBtn', selector: '#mdc-device-register-edit-form #mdc-restore-overflow-btn'},
        {ref: 'restoreNumberOfFractionDigitsBtn', selector: '#mdc-device-register-edit-form #mdc-restore-fractionDigits-btn'},
        {ref: 'overruledObisCodeField', selector: '#mdc-device-register-edit-form #mdc-editOverruledObisCodeField'},
        {ref: 'overflowContainer', selector: '#mdc-device-register-edit-form #overflowValue-container'},
        {ref: 'overflowField', selector: '#mdc-device-register-edit-form #mdc-editOverflowValueField'},
        {ref: 'numberOfFractionDigitsContainer', selector: '#mdc-device-register-edit-form #fractionDigits-container'},
        {ref: 'numberOfFractionDigitsField', selector: '#mdc-device-register-edit-form #mdc-editNumberOfFractionDigitsField'}

    ],

    fromSpecification: false,
    originalObisCodeOfConfig: null, // The OBIS code of the configuration
    originalOverflowOfConfig: null, // The overflow value of the configuration
    originalNumberOfFractionDigitsOfConfig: null, // The NumberOfFractionDigits value of the configuration

    init: function () {
        var me = this;
        me.control({
            '#deviceRegisterConfigurationGrid': {
                select: me.onDeviceRegisterConfigurationGridSelect
            },
            '#deviceRegisterConfigurationActionMenu': {
                click: this.chooseAction
            },
            '#registerActionMenu': {
                click: this.chooseAction
            },
            '#deviceRegisterConfigurationEditCustomAttributes #deviceRegisterCustomSaveBtn': {
                click: this.saveRegisterConfigurationCustomAttributes
            },
            '#deviceRegisterConfigurationEditCustomAttributes #deviceRegisterCustomRestoreBtn': {
                click: this.restoreRegisterConfigurationCustomAttributes
            },
            '#deviceRegisterConfigurationEditCustomAttributes #deviceRegisterCustomCancelBtn': {
                click: this.toPreviousPage
            },
            '#deviceRegisterConfigurationEditCustomAttributes #device-register-configuration-property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            },
            '#mdc-device-register-edit-form #mdc-editOverruledObisCodeField': {
                change: this.onOverruledObisCodeChange
            },
            '#mdc-device-register-edit-form #mdc-restore-obiscode-btn': {
                click: this.onRestoreObisCodeBtnClicked
            },
            '#mdc-device-register-edit-form #mdc-editOverflowValueField': {
                change: this.onOverflowChange
            },
            '#mdc-device-register-edit-form #mdc-restore-overflow-btn': {
                click: this.onRestoreOverflowBtnClicked
            },
            '#mdc-device-register-edit-form #mdc-editNumberOfFractionDigitsField': {
                change: this.onNumberOfFractionDigitsChange
            },
            '#mdc-device-register-edit-form #mdc-restore-fractionDigits-btn': {
                click: this.onRestoreNumberOfFractionDigitsBtnClicked
            },
            '#btn-save-register[action=saveRegister]': {
                click: this.saveRegister
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};

        routeParams.registerId = menu.record.getId();
        switch (item.action) {
            case 'validate':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.suspect = 'suspect';
                route = 'devices/device/registers/registerdata';
                break;
            case 'edit':
                route = 'devices/device/registers/register/edit';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
    },

    showDeviceRegisterConfigurationsView: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        me.mRID = mRID;
        me.fromSpecification = false;
        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = Ext.widget('deviceRegisterConfigurationSetup', {
                    device: device,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                viewport.setLoading(false);
            }
        });
    },

    updateDeviceRegisterConfigurationsDetails: function (mRID, registerId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        me.mRID = mRID;
        me.registerId = registerId;

        viewport.setLoading();
        var model = me.getModel('Mdc.model.RegisterValidationPreview');
        model.getProxy().setUrl(mRID, registerId);

        model.load('', {
            success: function (record) {
                me.updateValidationData(record);
            }
        });
        viewport.setLoading(false);
    },

    updateValidationData: function (record) {
        var me = this,
            form = me.getDeviceRegisterConfigurationPreviewForm(),
            formRecord;
        if (form) {
            formRecord = form.getRecord();
        } else {
            form = me.getDeviceRegisterConfigurationDetailForm();
            formRecord = form.getRecord();
        }
        formRecord.set('validationInfo_dataValidated',
            record.get('dataValidated')
                ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                : Uni.I18n.translate('general.no', 'MDC', 'No') + '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>');
        formRecord.set('lastChecked_formatted', Uni.DateTime.formatDateTimeLong(new Date(record.get('lastChecked'))));

        form.loadRecord(formRecord);
    },

    onDeviceRegisterConfigurationGridSelect: function (rowmodel, record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;

        routeParams.registerId = record.getId();
        me.previewRegisterConfiguration(record);
    },

    previewRegisterConfiguration: function (record) {
        var me = this,
            type = record.get('type'),
            widget = Ext.widget('deviceRegisterConfigurationPreview-' + type, {router: me.getController('Uni.controller.history.Router')}),
            form = widget.down('#deviceRegisterConfigurationPreviewForm'),
            previewContainer = me.getDeviceRegisterConfigurationSetup().down('#previewComponentContainer'),
            multiplierField = widget.down('[name=multiplier]'),
            calculatedReadingTypeField = widget.down('[name=calculatedReadingType]');

        me.registerId = record.get('id');
        me.registerName = record.get('name');
        previewContainer.setLoading(true);
        Ext.suspendLayouts();

        widget.setTitle(record.get('readingType').fullAliasName);

        if (multiplierField) {
            if (record.get('multiplier')) {
                multiplierField.show();
            } else {
                multiplierField.hide();
            }
        }
        if (record.get('calculatedReadingType')) {
            calculatedReadingTypeField.show();
        } else {
            calculatedReadingTypeField.hide();
        }

        previewContainer.removeAll();
        previewContainer.add(widget);

        var model = Ext.ModelManager.getModel('Mdc.model.DeviceRegisterForPreview');
        model.getProxy().setUrl(record.get('mRID'));
        form.setLoading(true);
        model.load(record.getId(), {
            callback: function(record, operation, success) {
                previewContainer.setLoading(false);

                if (form.rendered) {
                    form.loadRecord(record);
                    form.setLoading(false);
                }
            }
        });

        var customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.RegisterCustomAttributeSets');
        customAttributesStore.getProxy().setUrl(me.mRID, record.get('id'));
        customAttributesStore.load(function () {
            var placeHolderForm = widget.down('#custom-attribute-sets-placeholder-form-id');
            if (placeHolderForm) {
                placeHolderForm.loadStore(customAttributesStore);
            }
        });

        widget.on('render', function () {
            widget.down('#deviceRegisterConfigurationActionMenu').record = record;
        }, me, {single: true});

        Ext.resumeLayouts(true);
    },

    showDeviceRegisterConfigurationDetailsView: function (mRID, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registersOfDeviceStore = me.getStore('RegisterConfigsOfDevice');
        me.fromSpecification = true;
        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceRegisterForPreview');
                model.getProxy().setUrl(mRID);
                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type'),
                            widget = Ext.widget('tabbedDeviceRegisterView', {
                                device: device,
                                router: me.getController('Uni.controller.history.Router')
                            }),
                            func = function () {
                                var customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.RegisterCustomAttributeSets'),
                                    config = Ext.widget('deviceRegisterConfigurationDetail-' + type, {
                                        mRID: encodeURIComponent(mRID),
                                        registerId: registerId,
                                        router: me.getController('Uni.controller.history.Router')
                                    }),
                                    form = config.down('#deviceRegisterConfigurationDetailForm'),
                                    multiplierField = form.down('[name=multiplier]'),
                                    calculatedReadingTypeField = form.down('[name=calculatedReadingType]');

                                customAttributesStore.getProxy().setUrl(mRID, registerId);
                                customAttributesStore.load(function () {
                                    widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(customAttributesStore);
                                });
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.down('#registerTabPanel').setTitle(register.get('readingType').fullAliasName);
                                me.getApplication().fireEvent('loadRegisterConfiguration', register);
                                form.loadRecord(register);
                                if (multiplierField) {
                                    if (register.get('multiplier')) {
                                        multiplierField.show();
                                    } else {
                                        multiplierField.hide();
                                    }
                                }
                                if (register.get('calculatedReadingType')) {
                                    calculatedReadingTypeField.show();
                                } else {
                                    calculatedReadingTypeField.hide();
                                }
                                if (!register.get('detailedValidationInfo').validationActive) {
                                    config.down('#validateNowRegister').hide();
                                }
                                config.down('#deviceRegisterConfigurationActionMenu').record = register;
                                widget.down('#register-specifications').add(config);
                            };

                        if (registersOfDeviceStore.getTotalCount() === 0) {
                            registersOfDeviceStore.getProxy().url = registersOfDeviceStore.getProxy().url.replace('{mRID}', encodeURIComponent(mRID));
                            registersOfDeviceStore.load(function () {
                                func();
                            });
                        } else {
                            func();
                        }
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                        tabController.showTab(0);
                    }
                });
            }
        });
    },

    showValidateNowMessage: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowRegisterConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'MDC', 'Validate'),
                confirmation: function () {
                    me.activateDataValidation(record, this);
                }
            });
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/registers/' + me.registerId + '/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (res.hasValidation) {
                    if (res.lastChecked) {
                        me.dataValidationLastChecked = new Date(res.lastChecked);
                    } else {
                        me.dataValidationLastChecked = new Date();
                    }
                    confirmationWindow.insert(1, me.getValidationContent());
                    confirmationWindow.show({
                        title: Uni.I18n.translate('registerconfiguration.validation.validateNow', 'MDC', 'Validate data of register configuration {0}?', [record.get('name')]),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translate('registerconfiguration.validateNow.error', 'MDC', 'Failed to validate data of register configuration {0}', [record.get('name')]),
                        message = Uni.I18n.translate('registerconfiguration.validation.noData', 'MDC', 'There is currently no data for this register configuration'),
                        config = {
                            icon: Ext.MessageBox.WARNING
                        };
                    me.getApplication().getController('Uni.controller.Error').showError(title, message, config);
                }
            }
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },

    activateDataValidation: function (record, confWindow) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        if (confWindow.down('#validateRegisterFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateRegisterDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateRegisterDateErrors').setVisible(true);
        } else {
            confWindow.down('button').setDisabled(true);
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/registers/' + me.registerId + '/validate',
                method: 'PUT',
                isNotEdit: true,
                jsonData: Ext.merge({
                    lastChecked: confWindow.down('#validateRegisterFromDate').getValue().getTime()
                }, _.pick(record.getRecordData(), 'mRID', 'version', 'parent')),
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('registerconfiguration.validation.completed', 'MDC', 'Data validation completed'));
                    router.getRoute().forward();
                },
                failure: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                }
            });

        }
    },

    getValidationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left',
                labelStyle: 'font-weight: normal; padding-left: 50px'
            },
            items: [
                {
                    xtype: 'datefield',
                    itemId: 'validateRegisterFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('registerconfiguration.validation.item1', 'MDC', 'The data of register configuration will be validated starting from'),
                    labelWidth: 375,
                    labelPad: 0.5
                },
                {
                    xtype: 'panel',
                    itemId: 'validateRegisterDateErrors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
                },
                {
                    xtype: 'displayfield',
                    value: '',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item2', 'MDC', 'Note: The date displayed by default is the last checked (the moment when the last interval was checked in the validation process).'),
                    labelWidth: 500
                }
            ]
        });
    },

    loadRegisterConfigurationCustomAttributes: function (mRID, registerId, customAttributeSetId) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport')[0];
        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setUrl(mRID);
                model.load(registerId, {
                    success: function (register) {
                        var widget = Ext.widget('deviceRegisterConfigurationEditCustomAttributes', {device: device});
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.loadPropertiesRecord(widget, mRID, registerId, customAttributeSetId);
                    },
                    failure: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }
        });

    },

    loadPropertiesRecord: function (widget, mRID, registerId, customAttributeSetId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister'),
            form = widget.down('property-form');

        model.getProxy().setUrl(mRID, registerId);

        model.load(customAttributeSetId, {
            success: function (record) {
                widget.down('#registerEditPanel').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]));
                me.getApplication().fireEvent('loadRegisterConfigurationCustomAttributes', record);
                form.loadRecord(record);
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    restoreRegisterConfigurationCustomAttributes: function () {
        this.getEditPropertyForm().restoreAll();
    },

    toPreviousPage: function () {
        if (this.fromSpecification) {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/registers/register').forward();
        } else {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/registers').forward();
        }
    },

    getPreviousPageUrl: function () {
        if (this.fromSpecification) {
            return this.getController('Uni.controller.history.Router').getRoute('devices/device/registers/register').buildUrl();
        } else {
            return this.getController('Uni.controller.history.Router').getRoute('devices/device/registers').buildUrl();
        }
    },

    saveRegisterConfigurationCustomAttributes: function () {
        var me = this,
            form = me.getEditPropertyForm(),
            editView = me.getEditCustomAttributesPanel();

        editView.setLoading();

        form.updateRecord();
        form.getRecord().save({
            backUrl: me.getPreviousPageUrl(),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerAttributes.saved', 'MDC', 'Register attributes saved'));
                me.toPreviousPage();
            },
            callback: function () {
                editView.setLoading(false);
            }
        });
    },

    showRestoreAllBtn: function (value) {
        var restoreBtn = this.getEditCustomRestoreBtn();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },

    editRegister: function(deviceMRID, registerIdAsString) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.mRID = deviceMRID;
        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(me.mRID, {
            success: function(device) {
                var widget = Ext.widget('device-register-edit', {
                    itemId: 'mdc-device-register-edit',
                    device: device,
                    returnLink: me.getPreviousPageUrl()
                });
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceRegister');
                model.getProxy().setUrl(me.mRID);
                model.load(registerIdAsString, {
                    success: function(register) {
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        widget.setRegister(register);
                        me.updateEditRegisterFields(register);
                        me.originalObisCodeOfConfig = register.get('obisCode');
                        me.originalOverflowOfConfig = register.get('overflow');
                        me.originalNumberOfFractionDigitsOfConfig = register.get('numberOfFractionDigits');
                        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), register.get('overruledObisCode'));
                        me.onOverflowChange(me.getOverflowField(), register.get('overruledOverflow'));
                        me.onNumberOfFractionDigitsChange(me.getNumberOfFractionDigitsField(), register.get('overruledNumberOfFractionDigits'));
                        viewport.setLoading(false);
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    updateEditRegisterFields: function(register) {
        var me = this,
            type = register.get('type'),
            isCumulative = register.get('isCumulative'),
            overflowContainer = me.getOverflowContainer(),
            overflowField = me.getOverflowField();

        if (type === 'text') {
            overflowContainer.hide();
            overflowField.setDisabled(true);
            me.getNumberOfFractionDigitsContainer().hide();
            me.getNumberOfFractionDigitsField().setDisabled(true);
        } else {
            overflowField.setDisabled(false);
            overflowContainer.show();
            me.getNumberOfFractionDigitsField().setDisabled(false);
            me.getNumberOfFractionDigitsContainer().show();

            overflowContainer.required = isCumulative;
            overflowField.required = isCumulative;
            overflowField.allowBlank = !isCumulative;
            // Geert: I find the following lines of code not so neat. If anyone finds another way to make (dis)appear
            //        the label's little red star indicating the field is (not) required, please tell me.
            if (isCumulative && !overflowContainer.labelEl.dom.classList.contains('uni-form-item-label-required')) {
                overflowContainer.labelEl.dom.classList.add('uni-form-item-label-required');
            } else if (!isCumulative && overflowContainer.labelEl.dom.classList.contains('uni-form-item-label-required')) {
                overflowContainer.labelEl.dom.classList.remove('uni-form-item-label-required');
            }
            overflowContainer.labelEl.repaint();
        }
    },

    onOverruledObisCodeChange: function(overruledObisCodeField, newValue) {
        var me = this;
        me.getRestoreObisCodeBtn().setDisabled(newValue === me.originalObisCodeOfConfig);
        me.getRestoreObisCodeBtn().setTooltip(
            newValue === me.originalObisCodeOfConfig
                ? null
                : Uni.I18n.translate('general.obisCode.reset.tooltip4', 'MDC', 'Reset to {0}, the OBIS code of the device configuration', me.originalObisCodeOfConfig)
        );
    },

    onRestoreObisCodeBtnClicked: function() {
        var me = this;
        me.getOverruledObisCodeField().setValue(me.originalObisCodeOfConfig);
        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.originalObisCodeOfConfig);
    },

    onOverflowChange: function(overflowField, newValue) {
        var me = this;
        me.getRestoreOverflowBtn().setDisabled(newValue === me.originalOverflowOfConfig);
        me.getRestoreOverflowBtn().setTooltip(
            newValue === me.originalOverflowOfConfig
                ? null
                : Uni.I18n.translate(
                    'general.overflow.reset.tooltip',
                    'MDC',
                    'Reset to {0}, the overflow value of the device configuration',
                    me.originalOverflowOfConfig)
        );
    },

    onRestoreOverflowBtnClicked: function() {
        var me = this;
        me.getOverflowField().setValue(me.originalOverflowOfConfig);
        me.onOverflowChange(me.getOverflowField(), me.originalOverflowOfConfig);
    },

    onNumberOfFractionDigitsChange: function(fractionField, newValue) {
        var me = this;
        me.getRestoreNumberOfFractionDigitsBtn().setDisabled(newValue === me.originalNumberOfFractionDigitsOfConfig);
        me.getRestoreNumberOfFractionDigitsBtn().setTooltip(
            newValue === me.originalNumberOfFractionDigitsOfConfig
                ? null
                : Uni.I18n.translate(
                    'general.numberOfFractionDigits.reset.tooltip',
                    'MDC',
                    'Reset to {0}, the number of fraction digits of the device configuration',
                    me.originalNumberOfFractionDigitsOfConfig)
        );
    },

    onRestoreNumberOfFractionDigitsBtnClicked: function() {
        var me = this;
        me.getNumberOfFractionDigitsField().setValue(me.originalNumberOfFractionDigitsOfConfig);
        me.onNumberOfFractionDigitsChange(me.getNumberOfFractionDigitsField(), me.originalNumberOfFractionDigitsOfConfig);
    },

    saveRegister: function () {
        var me = this,
            form = me.getRegisterEditForm(),
            record = form.getRecord(),
            baseForm = form.getForm(),
            errorMsgPnl = form.down('uni-form-error-message');

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMsgPnl.hide();
        Ext.resumeLayouts(true);
        if (!form.isValid()) {
            errorMsgPnl.show();
            return;
        }

        form.setLoading();
        form.updateRecord(record);
        if (record.get('type') === 'text') {
            delete record.data.overflow;
            delete record.data.overruledOverflow;
            delete record.data.numberOfFractionDigits;
            delete record.data.overruledNumberOfFractionDigits;
        }
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('register.acknowledgment.saved', 'MDC', 'Register saved'));
                me.toPreviousPage();
            },
            failure: function (record, operation) {
                Ext.suspendLayouts();
                errorMsgPnl.show();
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    baseForm.markInvalid(json.errors);
                }
                Ext.resumeLayouts(true);
            },
            callback: function () {
                form.setLoading(false);
            }
        });
    }
});

