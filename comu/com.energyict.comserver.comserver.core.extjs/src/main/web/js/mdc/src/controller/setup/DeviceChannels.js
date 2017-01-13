Ext.define('Mdc.controller.setup.DeviceChannels', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.TimeUnits',
        'Uni.util.Common'
    ],

    views: [
        'Mdc.view.setup.devicechannels.Setup',
        'Mdc.view.setup.devicechannels.EditChannel'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.ChannelValidationPreview',
        'Mdc.model.DeviceChannel'
    ],

    stores: [
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets',
        'Mdc.store.TimeUnits',
        'Mdc.store.Clipboard'
    ],

    refs: [
        {ref: 'page',selector: 'deviceLoadProfileChannelsSetup'},
        {ref: 'preview',selector: 'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsPreview'},
        {ref: 'deviceLoadProfileChannelsPreviewForm',selector: '#deviceLoadProfileChannelsPreviewForm'},
        {ref: 'deviceLoadProfileChannelsOverviewForm',selector: '#deviceLoadProfileChannelsOverviewForm'},
        {ref: 'channelEditForm', selector: '#mdc-device-channel-edit-form'},
        {ref: 'restoreObisCodeBtn', selector: '#mdc-device-channel-edit-form #mdc-restore-obiscode-btn'},
        {ref: 'restoreOverflowBtn', selector: '#mdc-device-channel-edit-form #mdc-restore-overflow-btn'},
        {ref: 'restoreNumberOfFractionDigitsBtn', selector: '#mdc-device-channel-edit-form #mdc-restore-fractionDigits-btn'},
        {ref: 'overruledObisCodeField', selector: '#mdc-device-channel-edit-form #mdc-editOverruledObisCodeField'},
        {ref: 'overflowContainer', selector: '#mdc-device-channel-edit-form #overflowValue-container'},
        {ref: 'overflowField', selector: '#mdc-device-channel-edit-form #mdc-editOverflowValueField'},
        {ref: 'numberOfFractionDigitsContainer', selector: '#mdc-device-channel-edit-form #fractionDigits-container'},
        {ref: 'numberOfFractionDigitsField', selector: '#mdc-device-channel-edit-form #mdc-editNumberOfFractionDigitsField'}

    ],

    fromSpecification: false,
    originalObisCodeOfConfig: null, // The OBIS code of the configuration
    originalOverflowValueOfConfig: null, // The overflow value of the configuration
    originalNumberOfFractionDigitsOfConfig: null, // The number of fraction digits of the configuration

    init: function () {
        this.control({
            'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsGrid': {
                select: this.showPreview
            },
            '#deviceLoadProfileChannelsActionMenu': {
                click: this.chooseAction
            },
            '#channelActionMenu': {
                click: this.chooseAction
            },
            '#mdc-device-channel-edit-form #mdc-editOverruledObisCodeField': {
                change: this.onOverruledObisCodeChange
            },
            '#mdc-device-channel-edit-form #mdc-restore-obiscode-btn': {
                click: this.onRestoreObisCodeBtnClicked
            },
            '#mdc-device-channel-edit-form #mdc-editOverflowValueField': {
                change: this.onOverflowChange
            },
            '#mdc-device-channel-edit-form #mdc-restore-overflow-btn': {
                click: this.onRestoreOverflowBtnClicked
            },
            '#mdc-device-channel-edit-form #mdc-editNumberOfFractionDigitsField': {
                change: this.onNumberOfFractionDigitsChange
            },
            '#mdc-device-channel-edit-form #mdc-restore-fractionDigits-btn': {
                click: this.onRestoreNumberOfFractionDigitsBtnClicked
            },
            '#btn-save-channel[action=saveChannel]': {
                click: this.saveChannel
            }
        });
    },

    showOverview: function (deviceId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            channelsOfLoadProfilesOfDeviceStore = me.getStore('Mdc.store.ChannelsOfLoadProfilesOfDevice'),
            loadProfilesStore = me.getStore('Mdc.store.LoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
            showPage = function () {
                deviceModel.load(deviceId, {
                    success: function (record) {
                        if (record.get('hasLoadProfiles')) {
                            me.getApplication().fireEvent('loadDevice', record);
                            widget = Ext.widget('deviceLoadProfileChannelsSetup', {
                                deviceId: deviceId,
                                router: router,
                                device: record
                            });
                            me.getApplication().fireEvent('changecontentevent', widget);
                            channelsOfLoadProfilesOfDeviceStore.load();
                        } else {
                            window.location.replace(router.getRoute('notfound').buildUrl());
                        }
                    }
                });
            };

        me.fromSpecification = false;
        me.getController('Mdc.controller.setup.DeviceChannelData').fromSpecification = false;
        me.deviceId = deviceId;
        loadProfilesStore.getProxy().setExtraParam('deviceId', deviceId);
        channelsOfLoadProfilesOfDeviceStore.getProxy().setExtraParam('deviceId', deviceId);

        Uni.util.Common.loadNecessaryStores([
            'Mdc.store.LoadProfilesOfDevice',
            'Mdc.store.TimeUnits'
        ], function () {
            showPage();
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            readingType = record.get('readingType'),
            calculatedReadingTypeField = preview.down('#calculatedReadingType'),
            multiplierField = preview.down('#mdc-channel-preview-multiplier'),
            customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets'),
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;

        preview.setLoading(true);
        preview.setTitle(readingType.fullAliasName);
        Ext.getStore('Mdc.store.Clipboard').set('latest-device-channels-filter', Uni.util.QueryString.getQueryString());
        if (!record.data.validationInfo.validationActive) {
            !!preview.down('#validateNowChannel') && preview.down('#validateNowChannel').hide();
            !!Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0] && Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0].hide();
        } else {
            !!preview.down('#validateNowChannel') && preview.down('#validateNowChannel').show();
            !!Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0] && Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0].show();
        }
        preview.down('#deviceLoadProfileChannelsPreviewForm').loadRecord(record);
        preview.down('#deviceLoadProfileChannelsActionMenu').record = record;

        if (record.get('calculatedReadingType')) {
            calculatedReadingTypeField.show();
        } else {
            calculatedReadingTypeField.hide();
        }
        if (record.get('multiplier')) {
            multiplierField.show();
        } else {
            multiplierField.hide();
        }

        customAttributesStore.getProxy().setParams(me.deviceId, record.get('id'));
        customAttributesStore.load(function() {
            if (preview.rendered) {
                preview.down('#custom-attribute-sets-placeholder-form-id').loadStore(customAttributesStore);
                preview.setLoading(false);
            }
        });
        routeParams.channelId = record.getId();
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};

        routeParams.channelId = menu.record.getId();
        switch (item.action) {
            case 'validateNow':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.suspect = 'suspect';
                route = 'devices/device/channels/channeldata';
                break;
            case 'edit':
                route = 'devices/device/channels/channel/edit';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
    },

    showValidateNowMessage: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowChannelConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'MDC', 'Validate'),
                confirmation: function () {
                    me.activateDataValidation(record, this);
                }
            }),
            router = this.getController('Uni.controller.history.Router'),
            deviceId = me.deviceId || router.arguments.deviceId;

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(deviceId) + '/validationrulesets/validationstatus',
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
                        title: Uni.I18n.translate('deviceloadprofiles.channels.validateNow', 'MDC', 'Validate data of channel {0}?', [record.get('name')]),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translate('deviceloadprofiles.channels.validateNow.error', 'MDC', 'Failed to validate data of channel {0}', [record.get('name')]),
                        message = Uni.I18n.translate('deviceloadprofiles.channels.noData', 'MDC', 'There is currently no data for this channel'),
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
                    itemId: 'validateChannelFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('devicechannel.validateNow.item1', 'MDC', 'The data of the channel will be validated starting from'),
                    labelWidth: 375,
                    labelPad: 0.5
                },
                {
                    xtype: 'panel',
                    itemId: 'validateChannelDateErrors',
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

    activateDataValidation: function (record, confWindow) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            deviceId = me.deviceId || router.arguments.deviceId,
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            lastChecked = confWindow.down('#validateChannelFromDate').getValue().getTime(),
            timeout;

        if (confWindow.down('#validateChannelFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateChannelDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateChannelDateErrors').setVisible(true);
        } else {
            confWindow.removeAll(true);
            confWindow.destroy();
            viewport.setLoading();
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + encodeURIComponent(deviceId) + '/channels/' + record.get('id') + '/validate',
                method: 'PUT',
                timeout: 1800000,
                isNotEdit: true,
                jsonData: Ext.merge({
                    lastChecked: lastChecked
                }, _.pick(record.getRecordData(), 'id', 'name', 'version', 'parent')),
                success: function () {
                    clearTimeout(timeout);
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('deviceloadprofiles.channels.activation.completed', 'MDC', 'Data validation completed'));
                    router.getRoute().forward();
                },
                callback: function () {
                    viewport.setLoading(false);
                }
            });
            timeout = setTimeout(function () {
                viewport.setLoading(false);
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('deviceloadprofiles.channels.activation.putToBackground', 'MDC', 'The data validation takes longer as expected and will continue in the background.'));
            }, 180000);
        }
    },
    updateDeviceChannelDetails: function (deviceId, channelId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        viewport.setLoading();
        var model = me.getModel('Mdc.model.ChannelValidationPreview');
        model.getProxy().setParams(deviceId, channelId);
        model.load('', {
            success: function (record) {
                me.updateValidationData(record);
            }
        });
        viewport.setLoading(false);
    },
    updateValidationData: function(record)
    {
        var me = this,
            form = me.getDeviceLoadProfileChannelsPreviewForm(),
            formRecord;
        if(form)
            formRecord = form.getRecord();
        else{
            form = me.getDeviceLoadProfileChannelsOverviewForm();
            formRecord = form.getRecord();
        }
        formRecord.set('validationInfo_dataValidated',
            record.get('dataValidated')
                ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                : Uni.I18n.translate('general.no', 'MDC', 'No') + '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>');
        formRecord.set('lastChecked_formatted', Uni.DateTime.formatDateTimeLong(new Date(record.get('lastChecked'))));

        form.loadRecord(formRecord);
    },

    editChannel: function (deviceId, channelIdAsString) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceId = deviceId;
        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function(device) {
                var widget = Ext.widget('device-channel-edit', {
                    itemId: 'mdc-device-channel-edit',
                    device: device,
                    returnLink: me.getPreviousPageUrl()
                });
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceChannel');
                model.getProxy().setExtraParam('deviceId', deviceId);
                model.load(channelIdAsString, {
                    success: function(channel) {
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                        widget.setChannel(channel);
                        me.updateEditChannelFields(channel);
                        me.originalObisCodeOfConfig = channel.get('obisCode');
                        me.originalOverflowValueOfConfig = channel.get('overflowValue');
                        me.originalNumberOfFractionDigitsOfConfig = channel.get('nbrOfFractionDigits');
                        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), channel.get('overruledObisCode'));
                        me.onOverflowChange(me.getOverflowField(), channel.get('overruledOverflowValue'));
                        me.onNumberOfFractionDigitsChange(me.getNumberOfFractionDigitsField(), channel.get('overruledNbrOfFractionDigits'));
                        viewport.setLoading(false);
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    toPreviousPage: function () {
        if (this.fromSpecification) {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channel').forward();
        } else {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/channels').forward();
        }
    },

    getPreviousPageUrl: function () {
        if (this.fromSpecification) {
            return this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channel').buildUrl();
        } else {
            return this.getController('Uni.controller.history.Router').getRoute('devices/device/channels').buildUrl();
        }
    },

    updateEditChannelFields: function(channel) {
        var me = this,
            isCumulative = channel.get('readingType').isCumulative,
            overflowContainer = me.getOverflowContainer(),
            overflowField = me.getOverflowField();

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
    },

    onOverruledObisCodeChange: function(overruledObisCodeField, newValue) {
        var me = this;
        me.getRestoreObisCodeBtn().setDisabled(newValue === me.originalObisCodeOfConfig);
        me.getRestoreObisCodeBtn().setTooltip(
            newValue === me.originalObisCodeOfConfig
                ? null
                : Uni.I18n.translate(
                    'general.obisCode.reset.tooltip4',
                    'MDC',
                    'Reset to {0}, the OBIS code of the device configuration',
                    me.originalObisCodeOfConfig
                  )
        );
    },

    onRestoreObisCodeBtnClicked: function() {
        var me = this;
        me.getOverruledObisCodeField().setValue(me.originalObisCodeOfConfig);
        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.originalObisCodeOfConfig);
    },

    onOverflowChange: function(overflowField, newValue) {
        var me = this;
        me.getRestoreOverflowBtn().setDisabled(newValue === me.originalOverflowValueOfConfig);
        me.getRestoreOverflowBtn().setTooltip(
            newValue === me.originalOverflowValueOfConfig
                ? null
                : Uni.I18n.translate(
                    'general.overflow.reset.tooltip',
                    'MDC',
                    'Reset to {0}, the overflow value of the device configuration',
                    me.originalOverflowValueOfConfig
                  )
        );
    },

    onRestoreOverflowBtnClicked: function() {
        var me = this;
        me.getOverflowField().setValue(me.originalOverflowValueOfConfig);
        me.onOverflowChange(me.getOverflowField(), me.originalOverflowValueOfConfig);
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
                    me.originalNumberOfFractionDigitsOfConfig
                  )
        );
    },

    onRestoreNumberOfFractionDigitsBtnClicked: function() {
        var me = this;
        me.getNumberOfFractionDigitsField().setValue(me.originalNumberOfFractionDigitsOfConfig);
        me.onNumberOfFractionDigitsChange(me.getNumberOfFractionDigitsField(), me.originalNumberOfFractionDigitsOfConfig);
    },

    saveChannel: function () {
        var me = this,
            form = me.getChannelEditForm(),
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
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('channel.acknowledgment.saved', 'MDC', 'Channel saved'));
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