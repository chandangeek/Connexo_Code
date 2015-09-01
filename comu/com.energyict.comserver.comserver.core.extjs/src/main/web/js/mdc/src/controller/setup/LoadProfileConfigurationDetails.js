Ext.define('Mdc.controller.setup.LoadProfileConfigurationDetails', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailRulesGrid',
        'Cfg.privileges.Validation',
        'Cfg.view.validation.RulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.validation.RuleActionMenu',
        'Mdc.store.Intervals'
    ],
    views: [
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailSetup',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailInfo',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailDockedItems',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelGrid',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelPreview',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailRulesGrid',
        'Cfg.view.validation.RulePreview'
    ],

    stores: [
        'Mdc.store.Intervals',
        'Mdc.store.ReadingTypes',
        'Mdc.store.LoadProfileConfigurationDetailChannels',
        'Mdc.store.MeasurementTypesOnLoadProfileConfiguration',
        'Mdc.store.ChannelConfigValidationRules'
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
        {ref: 'channelsGrid', selector: '#loadProfileConfigurationDetailChannelGrid'}
    ],

    deviceTypeId: null,
    deviceConfigurationId: null,

    init: function () {
        this.control({
            'loadProfileConfigurationDetailSetup': {
                afterrender: this.loadStore
            },
            'loadProfileConfigurationDetailSetup loadProfileConfigurationDetailChannelGrid': {
                itemclick: this.loadGridItemDetail
            },
            'loadProfileConfigurationDetailForm reading-type-combo': {
                change: this.onReadingTypeChange
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
            'loadProfileConfigurationDetailSetup validation-rule-actionmenu': {
                click: this.chooseRuleAction
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

    editRecord: function () {
        var grid = this.getChannelsGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        window.location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.deviceConfigurationId) + '/loadprofiles/' + encodeURIComponent(this.loadProfileConfigurationId) + '/channels/' + encodeURIComponent(lastSelected.getData().id) + '/edit';
    },

    showConfirmationPanel: function () {
        var me = this, lastSelected = me.getChannelsGrid().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileConfiguration.confirmWindow.removeChannelConfiguration', 'MDC', 'This channel configuration will be removed from the load profile configuration.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + lastSelected.get('readingType')['fullAliasName'] + "'?",
            config: {me: me},
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me, lastSelected = me.getChannelsGrid().getSelectionModel().getLastSelected();

        if (state === 'confirm') {
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + lastSelected.get('id'),
                method: 'DELETE',
                waitMsg: 'Removing...',
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
        }


    },

    getAssociatedMeasurementType: function (readingType) {
        var associatedMeasurementType = null;
        this.availableMeasurementTypesStore.each(function (record) {
            if (record.get('readingType').mRID == readingType.get('mRID')) {
                associatedMeasurementType = record;
            }
        });
        return associatedMeasurementType;
    },

    onReadingTypeChange: function (combo) {
        var readingType = combo.valueModels[0];
        if (readingType) {
            var form = this.getChannelForm(),
                measurementType = this.getAssociatedMeasurementType(readingType);

            Ext.Ajax.request({
                url: '/api/mtr/readingtypes/' + readingType.get('mRID') + '/calculated',
                method: 'GET',
                success: function (response) {
                    var resp = Ext.JSON.decode(response.responseText);
                    if (!Ext.isEmpty(resp.readingTypes)) {
                        var calculatedReadingType = resp.readingTypes[0],
                            calculatedReadingTypeDisplayField = form.down('reading-type-displayfield[name=calculatedReadingType]'),
                            calculatedReadingTypeDisplayFieldEl = calculatedReadingTypeDisplayField.el.down('[role=textbox]');

                        calculatedReadingTypeDisplayField.setValue(calculatedReadingType);
                        calculatedReadingTypeDisplayField.show();

                        Ext.DomHelper.append(calculatedReadingTypeDisplayFieldEl, {
                            tag: 'span',
                            html: Uni.I18n.translate('channelConfig.bulkQuantityReadingTypeDescription', 'MDC', 'Selected reading type is bulk quantity reading type. Calculated reading type will hold a delta between two neighbour interval readings.'),
                            style: {
                                position: 'absolute',
                                top: '2em',
                                left: '0',
                                width: '1000px',
                                lineHeight: '1em',
                                color: '#999'
                            }
                        });
                    }
                }
            });

            if (measurementType) {
                form.down('[name=obiscode]').setValue(measurementType.get('obisCode'));
            }
        }
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
            formErrorsPanel = formPanel.down('uni-form-error-message[name=errors]'),
            selectedReadingType = formPanel.down('reading-type-combo').valueModels[0],
            formValue = form.getValues(),
            preloader, jsonValues,
            router = this.getController('Uni.controller.history.Router');

        if (form.isValid()) {
            formValue.measurementType = {id: this.getAssociatedMeasurementType(selectedReadingType).get('id') || null};
            jsonValues = Ext.JSON.encode(formValue);
            formErrorsPanel.hide();
            switch (btn.action) {
                case 'Add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Adding channel",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels',
                        method: 'POST',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest(Uni.I18n.translate('channelconfiguration.acknowlegment.added', 'MDC', 'Channel configuration added'));
                            router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').forward();
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
                case 'Save':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Editing channel",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + me.channelId,
                        method: 'PUT',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest(Uni.I18n.translate('channelconfiguration.acknowlegment.saved', 'MDC', 'Channel configuration saved'));
                            router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').forward(router.arguments);
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
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
            recordData = channelConfig.getData(),
            channelId = channelConfig.getId(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: form
            });
        if (this.displayedItemId != recordData.id) {
            grid.clearHighlight();
            preloader.show();
            this.displayedItemId = recordData.id;
            this.getLoadProfileDetailChannelPreview().setTitle(recordData.readingType.fullAliasName);
            form.loadRecord(channelConfig);

            if (channelConfig.get('calculatedReadingType')) {
                var readingTypeField = me.getPage().down('reading-type-displayfield[name=readingType]'),
                    calculatedReadingTypeField = me.getPage().down('reading-type-displayfield[name=calculatedReadingType]');
                readingTypeField.labelEl.update(Uni.I18n.translate('deviceloadprofiles.channels.readingTypeForBulk', 'MDC', 'Collected reading type'));
                calculatedReadingTypeField.labelEl.update(Uni.I18n.translate('deviceloadprofiles.channels.calculatedReadingType', 'MDC', 'Calculated reading type'));
                calculatedReadingTypeField.setVisible(true)
            }

            this.getPage().down('#rulesForChannelConfig').setTitle(
                Uni.I18n.translate('channelConfig.validationRules.list', 'MDC', '{0} validation rules', [recordData.readingType.fullAliasName])
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
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('validation.empty.rules.title', 'MDC', 'No validation rules found'),
                        reasons: [
                            Uni.I18n.translate('channelConfig.validationRules.empty.list.item1', 'MDC', 'No validation rules are applied on the channel configuration.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'validation-rule-preview',
                        tools: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                                iconCls: 'x-uni-action-iconD',
                                menu: {
                                    xtype: 'validation-rule-actionmenu'
                                }
                            }
                        ]
                    }
                }
            );
            this.getPage().down('#loadProfileConfigurationDetailRulesGrid').getStore().getProxy().extraParams =
                ({deviceType: this.deviceTypeId, deviceConfig: this.deviceConfigurationId, channelConfig: channelId});
            this.getPage().down('#loadProfileConfigurationDetailRulesGrid').getStore().load();
            preloader.destroy();
        }
    },

    showDeviceConfigurationLoadProfilesConfigurationDetailsView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            proxy = me.store.getProxy();
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
                            Ext.Ajax.request({
                                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
                                params: {},
                                method: 'GET',
                                success: function (response) {
                                    var loadProfileConfiguration = Ext.JSON.decode(response.responseText).data[0],
                                        widget = Ext.widget('loadProfileConfigurationDetailSetup', {router: router});
                                    me.loadProfileConfiguration = loadProfileConfiguration;
                                    me.getApplication().fireEvent('loadLoadProfile', loadProfileConfiguration);
                                    widget.down('#loadProfileConfigurationDetailInfo').setTitle(loadProfileConfiguration.name);
                                    me.deviceTypeName = deviceType.get('name');
                                    me.deviceConfigName = deviceConfig.get('name');
                                    me.getApplication().fireEvent('changecontentevent', widget);
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

    showDeviceConfigurationLoadProfilesConfigurationChannelsAddView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this;
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
                                var widget = Ext.widget('loadProfileConfigurationDetailForm',
                                        {
                                            loadProfileConfigurationChannelAction: 'Add',
                                            deviceTypeId: deviceTypeId,
                                            deviceConfigurationId: deviceConfigurationId,
                                            loadProfileConfigurationId: loadProfileConfigurationId
                                        }),
                                    preloader = Ext.create('Ext.LoadMask', {
                                        msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                                        target: widget
                                    }),
                                    readingTypeCombo = widget.down('reading-type-combo'),
                                    title = Uni.I18n.translate('loadprofiles.loadporfileaddChannelConfiguration', 'MDC', 'Add channel configuration');
                                me.getApplication().fireEvent('changecontentevent', widget);
                                preloader.show();
                                widget.down('form').setTitle(title);
                                me.availableMeasurementTypesStore.getProxy().pageParam = false;
                                me.availableMeasurementTypesStore.getProxy().limitParam = false;
                                me.availableMeasurementTypesStore.getProxy().startParam = false;

                                me.availableMeasurementTypesStore.load({
                                    callback: function () {
                                        var readingTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.ReadingType'});
                                        this.each(function (record) {
                                            readingTypesStore.add(record.get('readingType'));
                                        });
                                        readingTypeCombo.bindStore(readingTypesStore);
                                        preloader.destroy();
                                    }
                                });

                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');

                            }
                        });
                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesConfigurationChannelsEditView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId, channelId) {
        var me = this;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.channelId = channelId;
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
                                Ext.Ajax.request({
                                    url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + me.channelId,
                                    params: {},
                                    method: 'GET',
                                    success: function (response) {
                                        var channel = Ext.JSON.decode(response.responseText).data[0],
                                            widget = Ext.widget('loadProfileConfigurationDetailForm',
                                                {
                                                    loadProfileConfigurationChannelAction: 'Save',
                                                    deviceTypeId: deviceTypeId,
                                                    deviceConfigurationId: deviceConfigurationId,
                                                    loadProfileConfigurationId: loadProfileConfigurationId
                                                }),
                                            preloader = Ext.create('Ext.LoadMask', {
                                                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                                                target: widget
                                            }),
                                            title = Uni.I18n.translate('loadprofiles.loadprofileEditChannelConfiguration', 'MDC', 'Edit channel configuration'),
                                            readingTypeCombo = widget.down('reading-type-combo'),
                                            overruledObisField = widget.down('textfield[name=overruledObisCode]'),
                                            overflowValueField = widget.down('textfield[name=overflowValue]');

                                        readingTypeDisplayField = widget.down('reading-type-displayfield[name=readingType]');

                                        me.getApplication().fireEvent('changecontentevent', widget);
                                        preloader.show();
                                        widget.down('form').setTitle(title);
                                        if (channel.isLinkedByActiveDeviceConfiguration) {
                                            readingTypeCombo.hide();
                                            readingTypeDisplayField.setValue(channel.measurementType.readingType);
                                            readingTypeDisplayField.show();
                                        }

                                        me.availableMeasurementTypesStore.load({
                                            callback: function () {
                                                var readingTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.ReadingType'});
                                                this.add(channel.measurementType);
                                                this.each(function (record) {
                                                    readingTypesStore.add(record.get('readingType'));
                                                });
                                                readingTypeCombo.bindStore(readingTypesStore);
                                                readingTypeCombo.setValue(channel.measurementType.readingType.mRID);
                                                preloader.destroy();
                                            }
                                        });

                                        overruledObisField.setValue(channel.overruledObisCode);
                                        overflowValueField.setValue(channel.overflowValue);
                                        widget.down('[name=nbrOfFractionDigits]').setValue(channel.nbrOfFractionDigits);

                                        me.deviceTypeName = deviceType.get('name');
                                        me.deviceConfigName = deviceConfig.get('name');

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

    chooseRuleAction: function (menu, item) {
        var record = menu.record || this.getPage().down('#loadProfileConfigurationDetailRulesGrid').getSelectionModel().getLastSelected();
        location.href = '#/administration/validation/rulesets/' + encodeURIComponent(record.get('ruleSet').id) + '/rules/' + encodeURIComponent(record.getId());
    }
});
