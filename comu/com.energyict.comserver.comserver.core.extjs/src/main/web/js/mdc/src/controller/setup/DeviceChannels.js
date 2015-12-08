Ext.define('Mdc.controller.setup.DeviceChannels', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.TimeUnits',
        'Uni.util.Common'
    ],

    views: [
        'Mdc.view.setup.devicechannels.Setup'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.filter.DeviceChannelsFilter',
        'Mdc.model.ChannelValidationPreview'
    ],

    stores: [
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets',
        'Mdc.store.TimeUnits',
        'Mdc.store.Clipboard'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfileChannelsSetup'
        },
        {
            ref: 'preview',
            selector: 'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsPreview'
        },
        {
            ref: 'channelsFilterForm',
            selector: '#device-channels-filter nested-form'
        },
        {
            ref: 'deviceLoadProfileChannelsPreviewForm',
            selector: '#deviceLoadProfileChannelsPreviewForm'
        },
        {
            ref: 'deviceLoadProfileChannelsOverviewForm',
            selector: '#deviceLoadProfileChannelsOverviewForm'
        }

    ],

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
            }
        });
    },

    showOverview: function (mRID) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            channelsOfLoadProfilesOfDeviceStore = me.getStore('Mdc.store.ChannelsOfLoadProfilesOfDevice'),
            loadProfilesStore = me.getStore('Mdc.store.LoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
            showPage = function () {
                deviceModel.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                        widget = Ext.widget('deviceLoadProfileChannelsSetup', {
                            mRID: mRID,
                            router: router,
                            device: record
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        channelsOfLoadProfilesOfDeviceStore.load();
                    }
                });
            };

        me.getController('Mdc.controller.setup.DeviceChannelData').fromSpecification = false;
        me.mRID = mRID;
        loadProfilesStore.getProxy().setUrl(mRID);
        channelsOfLoadProfilesOfDeviceStore.getProxy().setUrl(mRID);

        Uni.util.Common.loadNecessaryStores([
            'Mdc.store.LoadProfilesOfDevice',
            'Mdc.store.TimeUnits'
        ], function () {
            showPage();
        });
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview(), me = this,
            readingType = record.get('readingType');
        preview.setLoading(true);

        preview.setTitle(readingType.aliasName + (!Ext.isEmpty(readingType.names.unitOfMeasure) ? (' (' + readingType.names.unitOfMeasure + ')') : ''));

        if (!record.data.validationInfo.validationActive) {
            !!preview.down('#validateNowChannel') && preview.down('#validateNowChannel').hide();
            !!Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0] && Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0].hide();
        } else {
            !!preview.down('#validateNowChannel') && preview.down('#validateNowChannel').show();
            !!Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0] && Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0].show();
        }
        preview.down('#deviceLoadProfileChannelsPreviewForm').loadRecord(record);
        preview.down('#deviceLoadProfileChannelsActionMenu').record = record;

        var calculatedReadingType = preview.down('#calculatedReadingType'),
            readingTypeLabel = preview.down('#readingType').labelEl;

        if (record.data.calculatedReadingType) {
            readingTypeLabel.update(Uni.I18n.translate('deviceloadprofiles.channels.readingTypeForBulk', 'MDC', 'Collected reading type'));
            calculatedReadingType.show();
        } else {
            readingTypeLabel.update(Uni.I18n.translate('deviceloadprofiles.channels.readingType', 'MDC', 'Reading type'));
            calculatedReadingType.hide();
        }

        var customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets');
        customAttributesStore.getProxy().setUrl(me.mRID, record.get('id'));
        customAttributesStore.load(function() {
            preview.down('#custom-attribute-sets-placeholder-form-id').loadStore(customAttributesStore);
            preview.setLoading(false);
        });
        var router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;
        routeParams.channelId = record.getId();

    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            route,
            filterParams = {};

        switch (item.action) {
            case 'validateNow':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.suspect = 'suspect';
                route = 'devices/device/channels/channeldata';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward({
            channelId: menu.record.getId()
        }, filterParams);
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
            mRID = me.mRID ? me.mRID : router.arguments.mRID;

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(mRID) + '/validationrulesets/validationstatus',
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
                    fieldLabel: Uni.I18n.translate('devicechannel.validateNow.item1', 'MDC', 'The data of the load profile will be validated starting from'),
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
            mRID = me.mRID ? me.mRID : router.arguments.mRID,
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
                url: '../../api/ddr/devices/' + encodeURIComponent(mRID) + '/channels/' + record.get('id') + '/validate',
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
    updateDeviceChannelDetails: function (mRID, channelId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        viewport.setLoading();
        var model = me.getModel('Mdc.model.ChannelValidationPreview');
        model.getProxy().setUrl(mRID, channelId);
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
            record.get('dataValidated')? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                : Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>');
        formRecord.set('lastChecked_formatted',
            Uni.DateTime.formatDateTimeLong(new Date(record.get('lastChecked'))));

        form.loadRecord(formRecord);
    }
});