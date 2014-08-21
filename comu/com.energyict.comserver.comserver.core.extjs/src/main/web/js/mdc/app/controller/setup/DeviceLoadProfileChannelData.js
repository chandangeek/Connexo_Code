Ext.define('Mdc.controller.setup.DeviceLoadProfileChannelData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofilechannels.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    stores: [
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Mdc.store.DataIntervalAndZoomLevels'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfileChannelData'
        },
        {
            ref: 'deviceLoadProfileChannelGraphView',
            selector: '#deviceLoadProfileChannelGraphView'
        }
    ],

    init: function () {
        this.control({
            'deviceLoadProfileChannelData #deviceLoadProfileChannelTableViewBtn': {
                click: this.toggleView
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelGraphViewBtn': {
                click: this.toggleView
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelDataGrid': {
                select: this.showPreview
            }
        });
    },

    showOverview: function (mRID, loadProfileId, channelId) {
        var me = this,
            models = {
                device: me.getModel('Mdc.model.Device'),
                loadProfile: me.getModel('Mdc.model.LoadProfileOfDevice'),
                channel: me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice')
            },
            channelDataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            channelDataStoreProxy = channelDataStore.getProxy(),
            widget,
            graphView;

        models.device.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });

        models.loadProfile.getProxy().setUrl(mRID);
        models.loadProfile.load(loadProfileId, {
            success: function (record) {
                me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
            }
        });

        models.channel.getProxy().setUrl({
            mRID: mRID,
            loadProfileId: loadProfileId
        });
        models.channel.load(channelId, {
            success: function (record) {
                widget = Ext.widget('deviceLoadProfileChannelData', {
                    router: me.getController('Uni.controller.history.Router'),
                    channel: record
                });
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                widget.down('#deviceLoadProfileChannelSubMenuPanel').setParams(mRID, loadProfileId, record);
                me.getApplication().fireEvent('changecontentevent', widget);
                graphView = widget.down('#deviceLoadProfileChannelGraphView');
                channelDataStoreProxy.setUrl({
                    mRID: mRID,
                    loadProfileId: loadProfileId,
                    channelId: channelId
                });
                graphView.setLoading(true);

                //TODO remove hardcoded value
                record.set('lastReading', 1407096000000 + 86400000);

                record.get('lastReading') && me.setDefaults(record, channelDataStoreProxy);

                channelDataStore.on('load', function () {
                    me.showGraphView(record);
                    graphView.setLoading(false);
                }, me);
                channelDataStore.load();
            }
        });
    },

    showGraphView: function (channelRecord) {
        var me = this,
            container = this.getDeviceLoadProfileChannelGraphView(),
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            channelName = channelRecord.get('name'),
            interval = channelRecord.get('interval').count + channelRecord.get('interval').timeUnit,
            unitOfMeasure = channelRecord.get('unitOfMeasure').localizedValue,
            seriesObject = {marker: {
                enabled: false
            },
                name: channelName
            },
            yAxis = {
                opposite: false,
                gridLineDashStyle: 'Dot',
                showEmpty: false,
                title: {
                    rotation: 270,
                    text: unitOfMeasure
                }
            },
            series = [],
            intervalRecord,
            zoomLevels,
            intervalLengthInMs;

        seriesObject['data'] = [];

        intervalRecord = zoomLevelsStore.findRecord('interval', interval);
        intervalLengthInMs = intervalRecord.get('intervalInMs');
        zoomLevels = intervalRecord.get('zoomLevels');

        switch (channelRecord.get('flowUnit')) {
            case 'flow':
                seriesObject['type'] = 'line';
                seriesObject['step'] = false;
                break;
            case 'volume':
                seriesObject['type'] = 'column';
                seriesObject['step'] = true;
                break;
        }


        if (dataStore.getTotalCount() > 1) {
            dataStore.each(function (record) {
                seriesObject['data'].push([record.get('interval').end, record.get('value')]);
            });
            series.push(seriesObject);
            container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels);
        } else {
            container.drawEmptyList();
        }
        me.getPage().doLayout();
    },


    toggleView: function (button) {
        var page = this.getPage(),
            showTable = button.action === 'showTableView';

        page.down('#deviceLoadProfileChannelGraphViewBtn').setDisabled(!showTable);
        page.down('#deviceLoadProfileChannelGraphView').setVisible(!showTable);
        page.down('#deviceLoadProfileChannelTableViewBtn').setDisabled(showTable);
        page.down('#deviceLoadProfileChannelTableView').setVisible(showTable);
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLoadProfileChannelDataPreview');

        preview.rendered && Ext.suspendLayouts();

        preview.setTitle(record.get('interval_end'));
        preview.down('#deviceLoadProfileChannelDataPreviewForm').loadRecord(record);

        preview.rendered && Ext.resumeLayouts(true);
    },

    setDefaults: function (record, proxy) {
        var me = this,
            interval = record.get('interval'),
            lastReading = record.get('lastReading'),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            all = zoomLevelsStore.getById(interval.count + interval.timeUnit).get('all'),
            intervalStart = zoomLevelsStore.getIntervalStart(all.count, all.timeUnit, lastReading),
            intervalEnd = lastReading.getTime();

        proxy.setExtraParam('intervalStart', intervalStart);
        proxy.setExtraParam('intervalEnd', intervalEnd);
    }
});