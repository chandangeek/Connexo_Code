/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.LoadProfileConfigurationDetails', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailRulesGrid',
        'Cfg.privileges.Validation',
        'Cfg.view.validation.RulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.validation.RuleActionMenu',
        'Mdc.store.Intervals'
    ],
    views: [
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailSetup',
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailInfo',
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailDockedItems',
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelGrid',
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelPreview',
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm',
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailRulesGrid',
        'Cfg.view.validation.RulePreview'
    ],

    stores: [
        'Mdc.store.Intervals',
        'Mdc.store.ReadingTypes',
        'Mdc.store.LoadProfileConfigurationDetailChannels',
        'Mdc.store.MeasurementTypesOnLoadProfileConfiguration',
        'Mdc.store.ChannelConfigValidationRules'
    ],

    models: [
        'Mdc.model.LoadProfileConfiguration',
        'Mdc.model.ReadingType'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'loadProfileConfigurationDetailSetup'
        },
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'loadConfigurationDetailForm', selector: '#loadProfileConfigurationDetailInfo'},
        {ref: 'loadProfileConfigurationChannelDetailsForm', selector: '#loadProfileConfigurationChannelDetailsForm'},
        {ref: 'loadProfileDetailChannelPreview', selector: '#loadProfileConfigurationDetailChannelPreview'},
        {ref: 'channelForm', selector: '#loadProfileConfigurationDetailChannelFormId'},
        {ref: 'channelsGrid', selector: '#loadProfileConfigurationDetailChannelGrid'},
        {
            ref: 'registerTypeCombo',
            selector: '#loadProfileConfigurationDetailChannelFormId #mdc-channel-config-registerTypeComboBox'
        },
        {
            ref: 'overruledObisCodeField',
            selector: '#loadProfileConfigurationDetailChannelFormId #mdc-channel-config-editOverruledObisCodeField'
        },
        {
            ref: 'restoreObisCodeBtn',
            selector: '#loadProfileConfigurationDetailChannelFormId #mdc-channel-config-restore-obiscode-btn'
        }
    ],

    deviceTypeId: null,
    deviceConfigurationId: null,
    channelConfigurationBeingEdited: null,
    registerTypesObisCode: null, // The OBIS code of the selected register type

    init: function () {
        this.control({
            'loadProfileConfigurationDetailSetup': {
                afterrender: this.loadStore
            },
            'loadProfileConfigurationDetailSetup loadProfileConfigurationDetailChannelGrid': {
                itemclick: this.loadGridItemDetail
            },
            'loadProfileConfigurationDetailForm button[name=loadprofilechannelaction]': {
                click: this.onSubmit
            },
            'button[action=channelnotificationerrorretry]': {
                click: this.retrySubmit
            },
            'menu menuitem[action=editloadprofileconfigurationdetailchannel]': {
                click: this.editRecord
            },
            'menu menuitem[action=deleteloadprofileconfigurationdetailchannel]': {
                click: this.showConfirmationPanel
            },
            '#loadProfileConfigurationDetailRulesGrid': {
                selectionchange: this.previewValidationRule
            },           
            'loadProfileConfigurationDetailForm #mdc-channel-config-multiplierRadioGroup': {
                change: this.onMultiplierChange
            },
            'loadProfileConfigurationDetailForm #mdc-channel-config-editOverruledObisCodeField': {
                change: this.onOverruledObisCodeChange
            },
            'loadProfileConfigurationDetailForm #mdc-channel-config-restore-obiscode-btn': {
                click: this.onRestoreObisCodeBtnClicked
            }
        });


        this.listen({
            store: {
                '#LoadProfileConfigurationDetailChannels': {
                    load: this.selectFirstChannel
                }
            }
        });

        this.intervalStore = this.getStore('Mdc.store.Intervals');
        this.store = this.getStore('LoadProfileConfigurationDetailChannels');
        this.availableMeasurementTypesStore = this.getStore('MeasurementTypesOnLoadProfileConfiguration');
    },

    showDeviceConfigurationLoadProfilesConfigurationDetailsView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            proxy = me.store.getProxy(),
            loadProfilesConfiguration = me.getModel('Mdc.model.LoadProfileConfiguration');
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.displayedItemId = null;
        proxy.extraParams = ({
            deviceType: deviceTypeId,
            deviceConfig: deviceConfigurationId,
            loadProfileConfiguration: loadProfileConfigurationId
        });
        proxy.pageParam = false;
        proxy.limitParam = false;
        proxy.startParam = false;
        Uni.util.Common.loadNecessaryStores(['Mdc.store.Intervals'], function () {
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                success: function (deviceType) {
                    me.getApplication().fireEvent('loadDeviceType', deviceType);
                    var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                    model.getProxy().setExtraParam('deviceType', deviceTypeId);
                    model.load(deviceConfigurationId, {
                        success: function (deviceConfig) {
                            me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                            loadProfilesConfiguration.getProxy().setUrl(deviceTypeId, deviceConfigurationId);
                            loadProfilesConfiguration.load(loadProfileConfigurationId, {
                                success: function (record) {
                                    var loadProfileConfiguration = record.getData(),
                                        widget = Ext.widget('loadProfileConfigurationDetailSetup', {router: router});
                                    me.loadProfileConfiguration = loadProfileConfiguration;
                                    me.getApplication().fireEvent('loadLoadProfile', loadProfileConfiguration);
                                    widget.down('#loadProfileConfigurationDetailInfo').setTitle(loadProfileConfiguration.name);
                                    me.deviceTypeName = deviceType.get('name');
                                    me.deviceConfigName = deviceConfig.get('name');
                                    me.getApplication().fireEvent('changecontentevent', widget);
                                    widget.down('menu').record = record;
                                    var detailedForm = me.getLoadConfigurationDetailForm();
                                    detailedForm.getForm().setValues(loadProfileConfiguration);
                                    detailedForm.down('[name=deviceConfigurationName]').setValue(Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}">{2}</a>', deviceTypeId, deviceConfigurationId, Ext.String.htmlEncode(me.deviceConfigName)));
                                }
                            });
                        }
                    });
                }
            });
        }, false);
    },

    editRecord: function () {
        var grid = this.getChannelsGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        window.location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.deviceConfigurationId) + '/loadprofiles/' + encodeURIComponent(this.loadProfileConfigurationId) + '/channels/' + encodeURIComponent(lastSelected.getData().id) + '/edit';
    },

    showConfirmationPanel: function () {
        var me = this, lastSelected = me.getChannelsGrid().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileConfiguration.confirmWindow.removeChannelConfiguration', 'MDC', 'This channel configuration will be removed from the load profile configuration.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[lastSelected.get('readingType')['fullAliasName']]),
            config: {me: me},
            fn: function (action) {
                if (action === 'confirm') {
                    me.confirmationPanelHandler(lastSelected);
                }
            }
        });
    },

    confirmationPanelHandler: function (channelConfigurationToDelete) {
        var me = this,
            jsonData = channelConfigurationToDelete.getRecordData(),
            id = jsonData.measurementType.id;

        jsonData.measurementType = {id: id};
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + channelConfigurationToDelete.getId(),
            method: 'DELETE',
            jsonData: jsonData,
            waitMsg: Uni.I18n.translate('general.removing', 'MDC', 'Removing...'),
            success: function () {
                me.handleSuccessRequest(Uni.I18n.translate('channelconfiguration.removeSuccessMsg', 'MDC', 'Channel configuration removed'));
                me.store.load(function () {
                    if (this.getCount() === 0) {
                        me.getPage().down('#rulesForChannelPreviewContainer').destroy();
                        me.getPage().down('#rulesForChannelConfig').destroy();
                    }
                });
            }
        });
    },

    retrySubmit: function (btn) {
        var formPanel = this.getChannelForm();
        btn.up('messagebox').hide();
        if (!Ext.isEmpty(formPanel)) {
            var submitBtn = formPanel.down('button[name=loadprofilechannelaction]');
            if (!Ext.isEmpty(submitBtn)) {
                this.onSubmit(submitBtn);
            }
        }
    },

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getChannelForm(),
            form = formPanel.getForm(),
            record = form.getRecord(),
            formErrorsPanel = formPanel.down('uni-form-error-message[name=errors]'),
            formValues = form.getValues(),
            preloader,
            jsonValues,
            useMultiplier = formPanel.down('#mdc-channel-config-multiplierRadioGroup').getValue().useMultiplier,
            collectedReadingTypeField = formPanel.down('#mdc-channel-config-collected-readingType-field'),
            calculatedReadingTypeField = formPanel.down('#mdc-channel-config-calculated-readingType-field'),
            calculatedReadingTypeCombo = formPanel.down('#mdc-channel-config-calculated-readingType-combo'),
            router = this.getController('Uni.controller.history.Router');

        Ext.suspendLayouts();
        form.clearInvalid();
        formErrorsPanel.hide();
        Ext.resumeLayouts(true);

        if (form.isValid()) {

            if (btn.action === 'add') {
                formValues.measurementType = {id: me.getRegisterTypeCombo().getValue()};
                formValues.collectedReadingType = collectedReadingTypeField.getValue();
                formValues.useMultiplier = useMultiplier;

                if (useMultiplier) {
                    if (calculatedReadingTypeField.isVisible()) {
                        formValues.multipliedCalculatedReadingType = calculatedReadingTypeField.getValue();
                    } else if (calculatedReadingTypeCombo.isVisible()) {
                        var possibleCalculatedReadingTypes = calculatedReadingTypeCombo.getStore().getRange(),
                            calculatedReadingType = null;
                        Ext.Array.forEach(possibleCalculatedReadingTypes, function (item) {
                            if (item.get('mRID') === calculatedReadingTypeCombo.getValue()) {
                                calculatedReadingType = item;
                                return false; // stop iterating
                            }
                        });
                        formValues.multipliedCalculatedReadingType = calculatedReadingType.getData();
                    }
                } else {
                    if (calculatedReadingTypeField.isVisible()) {
                        formValues.calculatedReadingType = calculatedReadingTypeField.getValue();
                    } else {
                        formValues.calculatedReadingType = null;
                    }
                }

                jsonValues = Ext.JSON.encode(formValues);

                preloader = Ext.create('Ext.LoadMask', {
                    msg: Uni.I18n.translate('general.adding', 'MDC', 'Adding...'),
                    target: formPanel
                });
                preloader.show();
                Ext.Ajax.request({
                    url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels',
                    method: 'POST',
                    jsonData: jsonValues,
                    success: function () {
                        me.handleSuccessRequest(Uni.I18n.translate('channelconfiguration.acknowledgment.added', 'MDC', 'Channel configuration added'));
                        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').forward();
                    },
                    callback: function () {
                        preloader.destroy();
                    }
                });

            } else if (btn.action === 'edit') {
                form.updateRecord();

                if (useMultiplier) {
                    if (calculatedReadingTypeField.isVisible()) {
                        record.setMultipliedCalculatedReadingType(calculatedReadingTypeField.getValue());
                    } else if (calculatedReadingTypeCombo.isVisible()) {
                        record.setMultipliedCalculatedReadingType(
                            calculatedReadingTypeCombo.getStore().findRecord(calculatedReadingTypeCombo.valueField, calculatedReadingTypeCombo.getValue())
                        );
                    }
                } else {
                    record.setMultipliedCalculatedReadingType(null);
                    if (calculatedReadingTypeField.isVisible()) {
                        record.setCalculatedReadingType(calculatedReadingTypeField.getValue());
                    } else {
                        record.setCalculatedReadingType(null);
                    }
                }

                formPanel.setLoading();
                record.save({
                    backUrl: router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').buildUrl(),
                    success: function () {
                        me.handleSuccessRequest(Uni.I18n.translate('channelconfiguration.acknowledgment.saved', 'MDC', 'Channel configuration saved'));
                        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').forward(router.arguments);
                    },
                    failure: function (record, operation) {
                        Ext.suspendLayouts();
                        formErrorsPanel.show();
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.markInvalid(json.errors);
                        }
                        Ext.resumeLayouts(true);
                    },
                    callback: function () {
                        formPanel.setLoading(false);
                    }
                });
            }
        } else {
            formErrorsPanel.show();
        }
    },

    handleSuccessRequest: function (headerText) {
        this.getApplication().fireEvent('acknowledge', headerText);
    },

    handleFailureRequest: function (response, headerText, retryAction) {
        var result = Ext.JSON.decode(response.responseText),
            errormsgs = '',
            me = this;
        Ext.each(result.errors, function (error) {
            errormsgs += error.msg + '<br>'
        });
        if (errormsgs == '') {
            errormsgs = result.message;
        }
        Ext.widget('messagebox', {
            buttons: [
                {
                    text: Uni.I18n.translate('general.retry','MDC','Retry'),
                    action: retryAction,
                    ui: 'remove'
                },
                {
                    text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                    action: 'cancel',
                    ui: 'link',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofiles/' + me.loadProfileConfigurationId + '/channels',
                    handler: function (button, event) {
                        this.up('messagebox').hide();
                    }
                }
            ]
        }).show({
            ui: 'notification-error',
            title: headerText,
            msg: errormsgs,
            icon: Ext.MessageBox.ERROR
        })
    },


    selectFirstChannel: function () {
        var grid = this.getChannelsGrid();
        if (!Ext.isEmpty(grid)) {
            var gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                channelsCount = this.store.getCount();

            if (channelsCount > 0) {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
    },

    loadStore: function () {
        this.store.load({
            params: {
                sort: 'name'
            }
        });
    },

    loadGridItemDetail: function (grid, channelConfig) {
        var me = this,
            form = this.getLoadProfileConfigurationChannelDetailsForm(),
            channelId = channelConfig.getId(),
            channelName = channelConfig.get('readingType').fullAliasName,
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: form
            }),
            calculatedReadingTypeField = me.getPage().down('#mdc-channel-config-preview-calculated');

        if (this.displayedItemId != channelId) {
            grid.clearHighlight();
            preloader.show();
            this.displayedItemId = channelId;
            this.getLoadProfileDetailChannelPreview().setTitle(Ext.htmlEncode(channelName));
            form.loadRecord(channelConfig);

            if (channelConfig.get('multipliedCalculatedReadingType') && channelConfig.get('multipliedCalculatedReadingType') !== '') {
                calculatedReadingTypeField.setValue(channelConfig.get('multipliedCalculatedReadingType'));
                calculatedReadingTypeField.show();
            } else if (channelConfig.get('calculatedReadingType') && channelConfig.get('calculatedReadingType') !== '') {
                calculatedReadingTypeField.show();
            } else {
                calculatedReadingTypeField.hide();
            }
            this.getPage().down('#rulesForChannelConfig').setTitle(
               Uni.I18n.translate('channelConfig.validationRules.list', 'MDC', '{0} validation rules', channelName, false)
            );
            if (me.getPage().down('#rulesForChannelPreviewContainer')) {
                me.getPage().down('#rulesForChannelPreviewContainer').destroy();
            }
            me.getPage().down('#validationrulesContainer').add(
                {
                    xtype: 'preview-container',
                    itemId: 'rulesForChannelPreviewContainer',
                    grid: {
                        xtype: 'load-profile-configuration-detail-rules-grid',
                        privileges: Cfg.privileges.Validation.viewOrAdmin,
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigId: me.deviceConfigurationId,
                        channelConfigId: channelId
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('channelConfig.validationRules.empty', 'MDC', 'No validation rules are applied on the channel configuration.')
                    },
                    previewComponent: {
                        xtype: 'validation-rule-preview',
                        noActionsButton: true
                    }
                }
            );
            this.getPage().down('#loadProfileConfigurationDetailRulesGrid').getStore().getProxy().extraParams =
                ({deviceType: this.deviceTypeId, deviceConfig: this.deviceConfigurationId, channelConfig: channelId});
            this.getPage().down('#loadProfileConfigurationDetailRulesGrid').getStore().load();
            preloader.destroy();
        }
    },

    showDeviceConfigurationLoadProfilesConfigurationChannelsAddView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this;
        me.channelConfigurationBeingEdited = null;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.availableMeasurementTypesStore.getProxy().extraParams = ({
            deviceType: deviceTypeId,
            deviceConfig: deviceConfigurationId,
            loadProfileConfiguration: loadProfileConfigurationId
        });
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        Ext.Ajax.request({
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
                            params: {},
                            method: 'GET',
                            success: function (response) {
                                me.getApplication().fireEvent('loadLoadProfile', JSON.parse(response.responseText));
                                var widget = Ext.widget('loadProfileConfigurationDetailForm',
                                        {
                                            loadProfileConfigurationChannelAction: 'add',
                                            deviceTypeId: deviceTypeId,
                                            deviceConfigurationId: deviceConfigurationId,
                                            loadProfileConfigurationId: loadProfileConfigurationId
                                        }),
                                    preloader = Ext.create('Ext.LoadMask', {
                                        msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                                        target: widget
                                    }),
                                    title = Uni.I18n.translate('loadprofiles.loadporfileaddChannelConfiguration', 'MDC', 'Add channel configuration');
                                me.getApplication().fireEvent('changecontentevent', widget);
                                preloader.show();
                                widget.down('form').setTitle(title);
                                me.availableMeasurementTypesStore.load({
                                    callback: function () {
                                        var registerTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.MeasurementType'});
                                        this.each(function (record) {
                                            registerTypesStore.add(record);
                                        });
                                        me.getRegisterTypeCombo().bindStore(registerTypesStore, true);
                                        preloader.destroy();
                                    }
                                });

                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');
                                me.getRegisterTypeCombo().on('change', me.onRegisterTypeChange, me);
                            }
                        });
                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesConfigurationChannelsEditView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId, channelId) {
        var me = this,
            widget = Ext.widget('loadProfileConfigurationDetailForm',
                {
                    loadProfileConfigurationChannelAction: 'edit',
                    deviceTypeId: deviceTypeId,
                    deviceConfigurationId: deviceConfigurationId,
                    loadProfileConfigurationId: loadProfileConfigurationId
                }),
            title = Uni.I18n.translate('loadprofiles.loadprofileEditChannelConfiguration', 'MDC', 'Edit channel configuration');

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.channelId = channelId;
        me.availableMeasurementTypesStore.getProxy().extraParams = ({
            deviceType: deviceTypeId,
            deviceConfig: deviceConfigurationId,
            loadProfileConfiguration: loadProfileConfigurationId
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        widget.down('form').setTitle(title);

        var channelCfgModel = Ext.ModelManager.getModel('Mdc.model.Channel');
        channelCfgModel.getProxy().extraParams = (
            {
                deviceType: deviceTypeId,
                deviceConfig: deviceConfigurationId,
                loadProfileConfig: loadProfileConfigurationId
            }
        );
        channelCfgModel.load(channelId, {
            success: function (channelConfiguration) {
                me.channelConfigurationBeingEdited = channelConfiguration;
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {

                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                        deviceConfigModel.load(deviceConfigurationId, {

                            success: function (deviceConfig) {
                                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                Ext.Ajax.request({
                                    url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
                                    params: {},
                                    method: 'GET',
                                    success: function (response) {
                                        var loadProfileConfig = Ext.JSON.decode(response.responseText),
                                            multiplierRadioGroup = widget.down('#mdc-channel-config-multiplierRadioGroup'),
                                            registerTypeCombo = widget.down('#mdc-channel-config-registerTypeComboBox'),
                                            calculatedReadingTypeCombo = widget.down('#mdc-channel-config-calculated-readingType-combo'),
                                            registerTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.MeasurementType'});

                                        me.getApplication().fireEvent('loadLoadProfile', loadProfileConfig);

                                        var measurementTypeId = channelConfiguration.getMeasurementType().get('id'),
                                            measurementTypeName = channelConfiguration.getMeasurementType().get('readingType').fullAliasName;

                                        channelConfiguration.getMeasurementType().name = measurementTypeName;
                                        widget.down('form').loadRecord(channelConfiguration);

                                        registerTypesStore.add(channelConfiguration.getMeasurementType());
                                        registerTypeCombo.bindStore(registerTypesStore, true);
                                        registerTypeCombo.setValue(measurementTypeId);
                                        registerTypeCombo.setDisabled(true);

                                        me.onMultiplierChange(multiplierRadioGroup); // (1) Keep this order
                                        if (deviceConfig.get('active')) { // (2) Keep this order
                                            multiplierRadioGroup.setDisabled(true);
                                        }
                                        calculatedReadingTypeCombo.setDisabled(deviceConfig.get('active'));

                                        me.registerTypesObisCode = channelConfiguration.getMeasurementType().get('obisCode');
                                        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.getOverruledObisCodeField().getValue());

                                        widget.setLoading(false);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    },

    previewValidationRule: function (grid, record) {
        var selectedRules = this.getPage().down('#loadProfileConfigurationDetailRulesGrid').getSelectionModel().getSelection();

        if (selectedRules.length === 1) {
            var selectedRule = selectedRules[0];
            this.getPage().down('validation-rule-preview').updateValidationRule(selectedRule);
            this.getPage().down('validation-rule-preview').show();
        } else {
            this.getPage().down('validation-rule-preview').hide();
        }
    },    

    onRegisterTypeChange: function (field, value) {
        var me = this,
            multiplierRadioGroup = me.getChannelForm().down('#mdc-channel-config-multiplierRadioGroup'),
            registerType = undefined,
            useMultiplier = undefined;

        multiplierRadioGroup.setDisabled(false);
        registerType = field.getStore().findRecord('id', value);
        useMultiplier = multiplierRadioGroup.getValue().useMultiplier;
        if (registerType != null) {
            me.updateReadingTypeFields(registerType, useMultiplier);
            me.updateOverflowField(registerType.get('isCumulative'));
            me.registerTypesObisCode = registerType.get('obisCode');
            me.getOverruledObisCodeField().setValue(me.registerTypesObisCode);
            me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.registerTypesObisCode);
        }
    },

    onMultiplierChange: function(radioGroup) {
        var me = this,
            contentContainer = this.getChannelForm().up('#mdc-loadProfileConfigurationDetailForm'),
            useMultiplier = radioGroup.getValue().useMultiplier;

        if (contentContainer.isEdit()) { // Busy editing a channel config
            me.channelConfigurationBeingEdited.set('isCumulative', me.channelConfigurationBeingEdited.get('readingType').isCumulative);
            me.updateReadingTypeFields(me.channelConfigurationBeingEdited, useMultiplier);
        } else { // Busy adding a register config
            me.updateReadingTypeFields(
                me.getRegisterTypeCombo().getStore().findRecord('id', this.getRegisterTypeCombo().getValue()),
                useMultiplier
            );
        }
    },

    updateOverflowField: function (isCumulative) {
        var form = this.getChannelForm(),
            overflowField = form.down('#mdc-lpcfg-detailForm-overflow-value-field');
        overflowField.setValue(isCumulative ? 99999999 : null);
    },

    updateReadingTypeFields: function(dataContainer, useMultiplier) {
        var me = this,
            form = me.getChannelForm(),
            multiplierRadioGroup = form.down('#mdc-channel-config-multiplierRadioGroup'),
            collectedReadingTypeField = form.down('#mdc-channel-config-collected-readingType-field'),
            calculatedReadingTypeField = form.down('#mdc-channel-config-calculated-readingType-field'),
            calculatedReadingTypeCombo = form.down('#mdc-channel-config-calculated-readingType-combo'),
            overflowField = form.down('#mdc-lpcfg-detailForm-overflow-value-field'),
            collectedReadingType = dataContainer.get('collectedReadingType'),
            calculatedReadingType = dataContainer.get('calculatedReadingType'),
            multipliedCalculatedReadingType = dataContainer.get('multipliedCalculatedReadingType'),
            possibleCalculatedReadingTypes = dataContainer.get('possibleCalculatedReadingTypes'),
            isCumulative = dataContainer.get('isCumulative');

        if (collectedReadingType && collectedReadingType !== '') {
            collectedReadingTypeField.setValue(collectedReadingType);
        } else { // fallback
            collectedReadingTypeField.setValue(dataContainer.get('readingType'));
        }
        collectedReadingTypeField.setVisible(dataContainer);
        if (!possibleCalculatedReadingTypes || possibleCalculatedReadingTypes.length === 0) {
            multiplierRadioGroup.setValue({ useMultiplier : false });
            useMultiplier = multiplierRadioGroup.getValue().useMultiplier;
            multiplierRadioGroup.setDisabled(true);
        } else {
            multiplierRadioGroup.setDisabled(false);
        }

        if (useMultiplier) {
            if (possibleCalculatedReadingTypes.length === 1) {
                calculatedReadingTypeField.setValue(possibleCalculatedReadingTypes[0]);
            } else {
                var readingTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.ReadingType'});
                Ext.Array.forEach(possibleCalculatedReadingTypes, function(item) {
                    readingTypesStore.add(item);
                });
                calculatedReadingTypeCombo.bindStore(readingTypesStore, true);
                if (multipliedCalculatedReadingType !== undefined && multipliedCalculatedReadingType !== '') {
                    calculatedReadingTypeCombo.setValue(multipliedCalculatedReadingType.mRID);
                } else {
                    calculatedReadingTypeCombo.setValue(readingTypesStore.getAt(0));
                }
            }
            calculatedReadingTypeField.setVisible(possibleCalculatedReadingTypes.length === 1);
            calculatedReadingTypeCombo.setVisible(possibleCalculatedReadingTypes.length > 1);
        } else {
            if (calculatedReadingType && calculatedReadingType !== '') {
                calculatedReadingTypeField.setValue(calculatedReadingType);
                calculatedReadingTypeField.setVisible(true);
            } else {
                calculatedReadingTypeField.setVisible(false);
            }
            calculatedReadingTypeCombo.setVisible(false);
        }

        overflowField.required = isCumulative;
        overflowField.allowBlank = !isCumulative;
        // Geert: I find the following lines of code not so neat. If anyone finds another way to make (dis)appear
        //        the label's little red star indicating the field is (not) required, please tell me.
        if (isCumulative && !overflowField.labelEl.dom.classList.contains('uni-form-item-label-required')) {
            overflowField.labelEl.dom.classList.add('uni-form-item-label-required');
        } else if (!isCumulative && overflowField.labelEl.dom.classList.contains('uni-form-item-label-required')) {
            overflowField.labelEl.dom.classList.remove('uni-form-item-label-required');
        }
        overflowField.labelEl.repaint();
    },

    onOverruledObisCodeChange: function(overruledObisCodeField, newValue) {
        var me = this;
        me.getRestoreObisCodeBtn().setDisabled(newValue === me.registerTypesObisCode);
        me.getRestoreObisCodeBtn().setTooltip(
            newValue === me.registerTypesObisCode
                ? null
                : Uni.I18n.translate('general.obisCode.reset.tooltip', 'MDC', 'Reset to {0}, the OBIS code of the register type', me.registerTypesObisCode)
        );
    },

    onRestoreObisCodeBtnClicked: function() {
        var me = this;
        me.getOverruledObisCodeField().setValue(me.registerTypesObisCode);
        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.registerTypesObisCode);
    }

});
