Ext.define('Mdc.controller.setup.DeviceLoadProfileChannelData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofilechannels.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'
    ],

    stores: [
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfileChannelData'
        },
        {
            ref: 'deviceLoadProfileChannelGraphView',
            selector: '#deviceLoadProfileChannelGraphView'
        },
        {
            ref: 'readingsCount',
            selector: 'deviceLoadProfileChannelData #readingsCount'
        },
        {ref: 'deviceLoadProfileChannelDataPreview', selector: '#deviceLoadProfileChannelDataPreview'}
    ],

    channelModel: null,

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
            },
            'deviceLoadProfileChannelData #deviceLoadProfileDataFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelDataFilterForm #hourField': {
                blur: this.filterTimeFieldBlurHandler
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelDataFilterForm #minuteField': {
                blur: this.filterTimeFieldBlurHandler
            },
            'deviceLoadProfileChannelData #deviceLoadProfileDataFilterResetBtn': {
                click: this.setDefaults
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelDataTopFilter #Reset': {
                click: this.setDefaults
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelGraphView': {
                resize: this.onGraphResize
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
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            widget;

        dataStore.getProxy().setUrl({
            mRID: mRID,
            loadProfileId: loadProfileId,
            channelId: channelId
        });

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
                me.channelModel = record;
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                widget.down('#deviceLoadProfileChannelSubMenuPanel').setParams(mRID, loadProfileId, record);
                me.getApplication().fireEvent('changecontentevent', widget);

                dataStore.on('load', function () {
                    if (!widget.isDestroyed) {
                        me.showReadingsCount(dataStore);
                        me.showGraphView(record);
                        widget.setLoading(false);
                        widget.down('#readingsCount') && widget.down('#readingsCount').setVisible(widget.down('#deviceLoadProfileChannelTableView').isVisible() && dataStore.count());
                    }
                }, me);

                me.setDefaults();
            }
        });
    },

    showGraphView: function (channelRecord) {
        var me = this,
            container = this.getDeviceLoadProfileChannelGraphView(),
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            channelName = channelRecord.get('name'),
            unitOfMeasure = channelRecord.get('unitOfMeasure').unit,
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

        intervalRecord = zoomLevelsStore.getIntervalRecord(channelRecord.get('interval'));
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(channelRecord.get('interval'));
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

        if (dataStore.getTotalCount() > 0) {
            dataStore.each(function (record) {
                if (record.get('value')) {
                    seriesObject['data'].unshift([record.get('interval').end, parseFloat(record.get('value'))]);
                }
            });
            series.push(seriesObject);
            container.down('#graphContainer').show();
            container.down('#emptyGraphMessage').hide();
            container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels);
        } else {
            container.down('#graphContainer').hide();
            container.down('#emptyGraphMessage').show();
        }
        me.getPage().doLayout();
    },

    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    },


    toggleView: function (button) {
        var page = this.getPage(),
            showTable = button.action === 'showTableView';

        page.down('#deviceLoadProfileChannelGraphViewBtn').setDisabled(!showTable);
        page.down('#deviceLoadProfileChannelGraphView').setVisible(!showTable);
        page.down('#deviceLoadProfileChannelTableViewBtn').setDisabled(showTable);
        page.down('#deviceLoadProfileChannelTableView').setVisible(showTable);
        page.down('#readingsCount').setVisible(showTable && this.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData').count());
        showTable && page.down('#deviceLoadProfileChannelTableView').down('#deviceLoadProfileChannelDataGrid').getView().refresh();
    },

    showPreview: function (selectionModel, record) {
        /*var preview = this.getPage().down('#deviceLoadProfileChannelDataPreview');

         preview.rendered && Ext.suspendLayouts();

         preview.setTitle(record.get('interval_end'));
         preview.down('#deviceLoadProfileChannelDataPreviewForm').loadRecord(record);

         preview.rendered && Ext.resumeLayouts(true);     */
        var me = this,
            previewPanel = me.getDeviceLoadProfileChannelDataPreview(),
            form = previewPanel.down('form');

        previewPanel.setTitle(record.get('interval_end'));
        form.loadRecord(record);

    },

    showReadingsCount: function (store) {
        this.getReadingsCount().update(store.getCount() + ' ' + Uni.I18n.translate('devicetype.readings', 'MDC', 'reading(s)'));
    },

    applyFilter: function () {
        var filterModel = Ext.create('Mdc.model.LoadProfilesOfDeviceDataFilter'),
            page = this.getPage(),
            dataStore = this.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            dataStoreProxy = dataStore.getProxy();

        page.down('#deviceLoadProfileChannelDataFilterForm').updateRecord(filterModel);

        dataStoreProxy.extraParams = {};

        Ext.iterate(filterModel.getFilterQueryParams(), function (key, value) {
            value && dataStoreProxy.setExtraParam(key, value);
        });

        page.down('#deviceLoadProfileChannelGraphView').isVisible() && page.setLoading(true);
        page.down('#deviceLoadProfileChannelDataTopFilter').addButtons(filterModel);
        dataStore.load();
    },

    filterTimeFieldBlurHandler: function (field) {
        var value = field.getValue();

        ((!value && value !== 0) || value > field.maxValue || value < field.minValue) && field.setValue(0);
    },

    setDefaults: function () {
        var me = this,
            filterModel = Ext.create('Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'),
            interval = me.channelModel.get('interval'),
            dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(interval),
            all = dataIntervalAndZoomLevels.get('all'),
            intervalStart = dataIntervalAndZoomLevels.getIntervalStart((me.channelModel.get('lastReading') || new Date().getTime())),
            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations');

        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));

        filterModel.set('intervalStart', intervalStart);
        filterModel.setDuration(durationsStore.getById(all.count + all.timeUnit));
        me.getPage().down('#deviceLoadProfileChannelDataFilterForm').loadRecord(filterModel);
        me.applyFilter();
    }
});