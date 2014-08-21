Ext.define('Mdc.controller.setup.DeviceLoadProfileData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice'
    ],

    stores: [
        'Mdc.store.LoadProfilesOfDeviceData',
        'DataIntervalAndZoomLevels'
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
            ref: 'readingsCountOnLoadProfile',
            selector: '#readingsCountOnLoadProfile'
        }
    ],

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
            }
        });
    },

    showOverview: function (mRID, loadProfileId) {
        var me = this,
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            loadProfilesOfDeviceDataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            loadProfilesOfDeviceDataStoreProxy = loadProfilesOfDeviceDataStore.getProxy(),
            widget,
            graphView;

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });
        loadProfileOfDeviceModel.getProxy().setUrl(mRID);
        loadProfileOfDeviceModel.load(loadProfileId, {
            success: function (record) {
                widget = Ext.widget('deviceLoadProfilesData', {
                    router: me.getController('Uni.controller.history.Router'),
                    channels: record.get('channels')
                });

                me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                me.getApplication().fireEvent('changecontentevent', widget);

                graphView = widget.down('#deviceLoadProfilesGraphView');
                graphView.setLoading(true);
                loadProfilesOfDeviceDataStoreProxy.setUrl({
                    mRID: mRID,
                    loadProfileId: loadProfileId
                });

                //TODO remove hardcoded value
                record.set('lastReading', 1407096000000 + 86400000);

                record.get('lastReading') && me.setDefaults(record, loadProfilesOfDeviceDataStoreProxy);

                loadProfilesOfDeviceDataStore.on('load', function () {
                    me.showGraphView(record);
                    graphView.setLoading(false);
                    me.showReadingsCount(loadProfilesOfDeviceDataStore);
                }, me);
                loadProfilesOfDeviceDataStore.load();
            }
        });
    },

    showReadingsCount: function (store) {
        var container = this.getReadingsCountOnLoadProfile(),
            readingsCount = store.getCount();

        if (readingsCount > 0) {
            container.removeAll();
            container.add({
                html: readingsCount + ' ' + Uni.I18n.translate('devicetype.readings', 'MDC', 'reading(s)')
            })
        } else {
            container.hide();
        }
    },

    showGraphView: function (loadProfileRecord) {
        var me = this,
            container = this.getDeviceLoadProfilesGraphView(),
            dataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            title = loadProfileRecord.get('name'),
            interval = loadProfileRecord.get('interval').count + loadProfileRecord.get('interval').timeUnit,
            currentAxisTopValue = 1,
            currentLine = 0,
            series = [],
            yAxis = [],
            channels = [],
            measurementTypeOrder = [],
            channelDataArrays = {},
            flowMeasuresNumbers = {},
            seriesToYAxisMap = {},
            intervalLengthInMs,
            lineCount,
            intervalRecord,
            zoomLevels,
            step;

        intervalRecord = zoomLevelsStore.findRecord('interval', interval);
        intervalLengthInMs = intervalRecord.get('intervalInMs');
        zoomLevels = intervalRecord.get('zoomLevels');

        Ext.Array.each(loadProfileRecord.get('channels'), function (channel, index) {
            var seriesObject = {marker: {
                    enabled: false
                }},
                flowLineNumber;
            seriesObject['name'] = channel.name;
            channelDataArrays[channel.id] = [];
            seriesObject['data'] = channelDataArrays[channel.id];
            switch (channel.flowUnit) {
                case 'flow':
                    seriesObject['type'] = 'line';
                    seriesObject['step'] = false;
                    flowLineNumber = flowMeasuresNumbers[channel.unitOfMeasure.id];
                    if (isNaN(flowLineNumber)) {
                        flowMeasuresNumbers[channel.unitOfMeasure.id] = currentLine;
                        seriesObject['yAxis'] = currentLine;
                        measurementTypeOrder.push(channel.unitOfMeasure.localizedValue);
                        currentLine += 1;
                    } else {
                        seriesObject['yAxis'] = flowLineNumber;
                    }
                    break;
                case 'volume':
                    seriesObject['type'] = 'column';
                    seriesObject['step'] = true;
                    seriesObject['yAxis'] = currentLine;
                    measurementTypeOrder.push(channel.unitOfMeasure.localizedValue);
                    currentLine += 1;
                    break;
            }
            channels.push({name: channel.name, unitOfMeasure: channel.unitOfMeasure.localizedValue });
            seriesToYAxisMap[index] = seriesObject['yAxis'];
            series.push(seriesObject);
        });

        lineCount = measurementTypeOrder.length;
        step = (100 / lineCount | 0) - 1;

        Ext.Array.each(measurementTypeOrder, function (type, index) {
            var yAxisObject = {
                opposite: false,
                gridLineDashStyle: 'Dot',
                showEmpty: false
            };

            if (index == 0) {
                yAxisObject['height'] = step + '%';
            } else {
                if (index == lineCount - 1) {
                    yAxisObject['height'] = (100 - currentAxisTopValue) + '%';
                } else {
                    yAxisObject['height'] = step + '%';
                }
                yAxisObject['top'] = currentAxisTopValue + '%';
                yAxisObject['offset'] = 0;
            }
            currentAxisTopValue += step + 2;
            yAxisObject['title'] = {
                rotation: 270,
                text: type
            };
            yAxis.push(yAxisObject);
        });

        if (dataStore.getTotalCount() > 1) {
            dataStore.each(function (record) {
                Ext.iterate(record.get('channelData'), function (key, value) {
                    if (channelDataArrays[key]) {
                        channelDataArrays[key].push([record.get('interval').end, value])
                    }
                });
            });
            container.drawGraph(title, yAxis, series, channels, seriesToYAxisMap, intervalLengthInMs, zoomLevels);
        } else {
            container.drawEmptyList();
        }

        me.getPage().doLayout();
    },

    toggleView: function (button) {
        var page = this.getPage(),
            showTable = button.action === 'showTableView',
            store = this.getStore('Mdc.store.LoadProfilesOfDeviceData');

        page.down('#deviceLoadProfilesGraphViewBtn').setDisabled(!showTable);
        page.down('#deviceLoadProfilesGraphView').setVisible(!showTable);
        if (store.getCount() > 0) {
            page.down('#readingsCountOnLoadProfile').setVisible(showTable);
        }
        page.down('#deviceLoadProfilesTableViewBtn').setDisabled(showTable);
        page.down('#deviceLoadProfilesTableView').setVisible(showTable);
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLoadProfilesDataPreview');

        preview.rendered && Ext.suspendLayouts();

        preview.setTitle(record.get('interval_end'));
        preview.down('#deviceLoadProfilesDataPreviewForm').loadRecord(record);

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