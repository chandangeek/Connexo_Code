Ext.define('Mdc.controller.setup.DeviceLoadProfileData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.LoadProfilesOfDeviceDataFilter'
    ],

    stores: [
        'Mdc.store.LoadProfilesOfDeviceData',
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfilesData'
        },
        {
            ref: 'deviceLoadProfilesGraphView',
            selector: '#deviceLoadProfilesGraphView'
        },
        {
            ref: 'readingsCount',
            selector: 'deviceLoadProfilesData #readingsCount'
        },
        {
            ref: 'sideFilterForm',
            selector: 'deviceLoadProfilesData #deviceLoadProfileDataFilterForm'
        },
        {
            ref: 'filterPanel',
            selector: 'deviceLoadProfilesData filter-top-panel'
        }
    ],

    prefix: 'deviceLoadProfilesData',

    loadProfileModel: null,

    init: function () {
        this.control({
            'deviceLoadProfilesData #deviceLoadProfilesTableViewBtn': {
                click: this.toggleView
            },
            'deviceLoadProfilesData #deviceLoadProfilesGraphViewBtn': {
                click: this.toggleView
            },
            'deviceLoadProfilesData #deviceLoadProfilesDataGrid': {
                select: this.showPreview
            },
            'deviceLoadProfilesData #deviceLoadProfileDataFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceLoadProfilesData #deviceLoadProfileDataFilterForm #hourField': {
                blur: this.filterTimeFieldBlurHandler
            },
            'deviceLoadProfilesData #deviceLoadProfileDataFilterForm #minuteField': {
                blur: this.filterTimeFieldBlurHandler
            },
            'deviceLoadProfilesData #deviceLoadProfileDataFilterResetBtn': {
                click: this.setDefaults
            },
            'deviceLoadProfilesData #deviceLoadProfileDataTopFilter #Reset': {
                click: this.setDefaults
            },
            'deviceLoadProfilesData #deviceLoadProfilesGraphView': {
                resize: this.onGraphResize
            }
        });
    },

    showOverview: function (mRID, loadProfileId) {
        var me = this,
            loadProfileModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            dataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            widget;

        dataStore.getProxy().setUrl({
            mRID: mRID,
            loadProfileId: loadProfileId
        });

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });
        loadProfileModel.getProxy().setUrl(mRID);
        loadProfileModel.load(loadProfileId, {
            success: function (record) {
                widget = Ext.widget('deviceLoadProfilesData', {
                    router: me.getController('Uni.controller.history.Router'),
                    channels: record.get('channels')
                });
                me.loadProfileModel = record;

                me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                me.getApplication().fireEvent('changecontentevent', widget);
                dataStore.on('load', function () {
                    if (!widget.isDestroyed) {
                        me.showReadingsCount(dataStore);
                        me.showGraphView(record);
                        widget.down('#readingsCount') && widget.down('#readingsCount').setVisible(widget.down('#deviceLoadProfilesTableView').isVisible() && dataStore.count());
                        widget.setLoading(false);
                    }
                }, me);

                me.setDefaults();

            }
        });
    },

    showGraphView: function (loadProfileRecord) {
        var me = this,
            container = this.getDeviceLoadProfilesGraphView(),
            dataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            title = loadProfileRecord.get('name'),
            currentAxisTopValue = 2,
            currentLine = 0,
            series = [],
            yAxis = [],
            channels = [],
            measurementTypeOrder = [],
            channelDataArrays = {},
            seriesToYAxisMap = {},
            intervalLengthInMs,
            axisBacklash,
            lineCount,
            intervalRecord,
            zoomLevels,
            step;

        intervalRecord = zoomLevelsStore.getIntervalRecord(loadProfileRecord.get('interval'));
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(loadProfileRecord.get('interval'));
        zoomLevels = intervalRecord.get('zoomLevels');

        Ext.Array.each(loadProfileRecord.get('channels'), function (channel, index) {
            var seriesObject = {marker: {
                    enabled: false
                }};
            seriesObject['name'] = channel.name;
            channelDataArrays[channel.id] = [];
            seriesObject['data'] = channelDataArrays[channel.id];
            switch (channel.flowUnit) {
                case 'flow':
                    seriesObject['type'] = 'line';
                    seriesObject['step'] = false;
                    break;
                case 'volume':
                    seriesObject['type'] = 'column';
                    seriesObject['step'] = true;
                    break;
            }
            measurementTypeOrder.push(channel.name);
            seriesObject['yAxis'] = currentLine;
            currentLine += 1;
            channels.push({name: channel.name, unitOfMeasure: channel.unitOfMeasure.unit });
            seriesToYAxisMap[index] = seriesObject['yAxis'];
            series.push(seriesObject);
        });

        lineCount = measurementTypeOrder.length;
        step = (100 / lineCount | 0) - 1;
        axisBacklash = (4 -lineCount) > 0 ? (4 -lineCount) : 0;

        Ext.Array.each(channels, function (channel, index) {
            var yAxisObject = {
                opposite: false,
                gridLineDashStyle: 'Dot',
                showEmpty: false
            },
                yAxisTitle = channel.name + ', ' + channel.unitOfMeasure;


            if (index == 0) {
                yAxisObject['height'] = step + '%';
            } else {
                if (index == lineCount - 1) {
                    yAxisObject['height'] = (100 - currentAxisTopValue) + '%';
                } else {
                    yAxisObject['height'] = step + '%';
                }
                yAxisObject['offset'] = 0;
            }
            yAxisObject['top'] = currentAxisTopValue + '%';
            currentAxisTopValue += step + 2 + axisBacklash;
            yAxisObject['title'] = {
                rotation: 0,
                align: 'high',
                margin: -40 - 5 * yAxisTitle.length,
                text: yAxisTitle
            };
            yAxis.push(yAxisObject);
        });

        if (dataStore.getTotalCount() > 0) {
            dataStore.each(function (record) {
                Ext.iterate(record.get('channelData'), function (key, value) {
                    if (channelDataArrays[key]) {
                        channelDataArrays[key].unshift([record.get('interval').end, parseFloat(value)]);
                    }
                });
            });
            container.down('#graphContainer').show();
            container.down('#emptyGraphMessage').hide();
            container.drawGraph(title, yAxis, series, channels, seriesToYAxisMap, intervalLengthInMs, zoomLevels);
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

        page.down('#deviceLoadProfilesGraphViewBtn').setDisabled(!showTable);
        page.down('#deviceLoadProfilesGraphView').setVisible(!showTable);
        page.down('#deviceLoadProfilesTableViewBtn').setDisabled(showTable);
        page.down('#deviceLoadProfilesTableView').setVisible(showTable);
        page.down('#readingsCount').setVisible(showTable && this.getStore('Mdc.store.LoadProfilesOfDeviceData').count());
        showTable && page.down('#deviceLoadProfilesTableView').down('#deviceLoadProfilesDataGrid').getView().refresh();
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLoadProfilesDataPreview');

        preview.rendered && Ext.suspendLayouts();

        preview.setTitle(record.get('interval_end'));
        preview.down('#deviceLoadProfilesDataPreviewForm').loadRecord(record);

        preview.rendered && Ext.resumeLayouts(true);
    },

    showReadingsCount: function (store) {
        this.getReadingsCount().update(store.getCount() + ' ' + Uni.I18n.translate('devicetype.readings', 'MDC', 'reading(s)'));
    },

    applyFilter: function () {
        var filterModel = Ext.create('Mdc.model.LoadProfilesOfDeviceDataFilter'),
            page = this.getPage(),
            dataStore = this.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            dataStoreProxy = dataStore.getProxy();

        page.down('#deviceLoadProfileDataFilterForm').updateRecord(filterModel);

        dataStoreProxy.extraParams = {};

//        Ext.iterate(filterModel.getFilterQueryParams(), function (key, value) {
//            value && dataStoreProxy.setExtraParam(key, value);
//        });

        page.down('#deviceLoadProfilesGraphView').isVisible() && page.setLoading(true);
//        page.down('#deviceLoadProfileDataTopFilter').addButtons(filterModel);
        dataStore.load();
    },

    filterTimeFieldBlurHandler: function (field) {
        var value = field.getValue();

        ((!value && value !== 0) || value > field.maxValue || value < field.minValue) && field.setValue(0);
    },

    setDefaults: function () {
        var me = this,
            page = me.getPage(),
            filterModel = Ext.create('Mdc.model.LoadProfilesOfDeviceDataFilter'),
            interval = me.loadProfileModel.get('interval'),
            dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(interval),
            all = dataIntervalAndZoomLevels.get('all'),
            intervalStart = dataIntervalAndZoomLevels.getIntervalStart((me.loadProfileModel.get('lastReading') || new Date().getTime())),
            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations');



        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));

        filterModel.set('intervalStart', intervalStart);
        filterModel.setDuration(durationsStore.getById(all.count + all.timeUnit));
        page.down('#deviceLoadProfileDataFilterForm').loadRecord(filterModel);
        me.applyFilter();
    }
});