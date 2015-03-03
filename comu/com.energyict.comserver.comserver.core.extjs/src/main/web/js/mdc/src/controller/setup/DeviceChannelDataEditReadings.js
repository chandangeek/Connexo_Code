Ext.define('Mdc.controller.setup.DeviceChannelDataEditReadings', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicechannels.EditReadings'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    stores: [
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-loadprofile-channel-edit-readings'
        }
    ],

    init: function () {
        this.control({
            'device-loadprofile-channel-edit-readings #device-loadprofile-channel-edit-readings-save': {
                click: this.saveChanges
            },
            '#device-loadprofile-channel-edit-readings-action-menu': {
                click: this.chooseAction
            },
            'device-loadprofile-channel-edit-readings #device-loadprofile-channel-edit-readings-grid': {
                beforeedit: this.beforeEditRecord,
                edit: this.resumeEditorFieldValidation,
                canceledit: this.resumeEditorFieldValidation
            }
        });
    },

    showOverview: function (mRID, channelId) {
        var me = this,
            models = {
                device: me.getModel('Mdc.model.Device'),
                channel: me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice')
            },
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            router = me.getController('Uni.controller.history.Router'),
            preloader = Ext.create('Ext.container.Container'),
            widget;
        dataStore.removeAll(true);

        me.getApplication().fireEvent('changecontentevent', preloader);
        preloader.setLoading(true);

        dataStore.getProxy().setUrl({
            mRID: mRID,
            channelId: channelId
        });

        models.device.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });

        models.channel.getProxy().setUrl({
            mRID: mRID
        });

        models.channel.load(channelId, {
            success: function (record) {
                var dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(record.get('interval')),
                    durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations');

                durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
                widget = Ext.widget('device-loadprofile-channel-edit-readings', {
                    router: router,
                    channel: record
                });
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                me.getApplication().fireEvent('changecontentevent', widget);

                if (Ext.isEmpty(router.filter.data.intervalStart)) {
                    me.setDefaults(dataIntervalAndZoomLevels, record);
                }
                dataStore.setFilterModel(router.filter);
                dataStore.load(function () {
                    dataStore.rejectChanges();
                });
            }
        });
    },

    setDefaults: function (dataIntervalAndZoomLevels, record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            all = dataIntervalAndZoomLevels.get('all'),
            intervalStart = dataIntervalAndZoomLevels.getIntervalStart((record.get('lastReading') || new Date().getTime()));
        router.filter = Ext.create('Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter');
        router.filter.beginEdit();
        router.filter.set('intervalStart', intervalStart);
        router.filter.set('duration', all.count + all.timeUnit);
        router.filter.set('onlySuspect', false);
        router.filter.endEdit();
    },

    chooseAction: function (menu, menuItem) {
        switch (menuItem.action) {
            case 'editValue':
                this.getPage().down('device-loadprofile-channel-edit-readings-grid').getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'removeReading':
                menu.record.set('value', null);
                menu.record.set('intervalFlags', []);
                break;
        }
    },

    beforeEditRecord: function (editor, event) {
        var intervalFlags = event.record.get('intervalFlags');

        event.column.getEditor().allowBlank = !(intervalFlags && intervalFlags.length);
    },

    resumeEditorFieldValidation: function (editor, event) {
        event.column.getEditor().allowBlank = true;
    },

    saveChanges: function () {
        var me = this,
            page = me.getPage(),
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'));

        if (changedData.length) {
            page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
            Ext.Ajax.request({
                url: Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data', encodeURIComponent(router.arguments.mRID), router.arguments.channelId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                callback: function () {
                    page.setLoading(false);
                },
                success: function () {
                    router.getRoute('devices/device/channels/channeltableData').forward(router.arguments, router.queryParams);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'MDC', 'Channel data have been saved'));
                },
                failure: function (response) {
                    var failureResponseText;

                    if (response.status == 400) {
                        failureResponseText = Ext.decode(response.responseText, true);

                        if (failureResponseText) {
                            Ext.create('Uni.view.window.Confirmation', {
                                confirmText: Uni.I18n.translate('general.retry', 'MDC', 'Retry'),
                                closeAction: 'destroy',
                                confirmation: function () {
                                    this.close();
                                    me.saveChanges();
                                },
                                cancellation: function () {
                                    this.close();
                                    router.getRoute('devices/device/channels/channel/tableData').forward(router.arguments, router.queryParams);
                                }
                            }).show({
                                msg: failureResponseText.message ? failureResponseText.message :
                                    Uni.I18n.translate('general.emptyField', 'MDC', 'Value field can not be empty'),
                                title: failureResponseText.error ? failureResponseText.error :
                                    Uni.I18n.translate('general.during.editing', 'MDC,', 'Error during editing')
                            });
                        }
                    }
                }
            });
        }
    },

    getChangedData: function (store) {
        var changedData = [];

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            changedData.push(_.pick(record.getData(), 'interval', 'value'));
        });

        return changedData;
    }
});