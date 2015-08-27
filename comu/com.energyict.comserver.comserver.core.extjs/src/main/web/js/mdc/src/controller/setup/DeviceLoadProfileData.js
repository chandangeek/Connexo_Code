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
        'Mdc.store.LoadProfileDataDurations',
        'Mdc.store.LoadProfilesOfDevice'
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
        }
    ],

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
            'deviceLoadProfilesData #deviceLoadProfilesGraphView': {
                resize: this.onGraphResize
            }
        });
    },

    showTableOverview: function (mRID, loadProfileId, tabController, loadProfile) {
        this.showOverview(mRID, loadProfileId, true, tabController, loadProfile);
    },

    showGraphOverview: function (mRID, loadProfileId, tabController, loadProfile) {
        this.showOverview(mRID, loadProfileId, false, tabController, loadProfile);
    },

    showOverview: function (mRID, loadProfileId, isTable, tabController, loadProfile) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            loadProfileModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            loadProfilesStore = me.getStore('Mdc.store.LoadProfilesOfDevice'),
            dataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
            tabWidget,
            defer = {
                param: null,
                callback: null,
                resolve: function (arg) {
                    arg && this.callback.apply(this, this.param)
                },
                setCallback: function (fn) {
                    this.callback = fn;
                    this.resolve(this.param)
                },
                setParam: function () {
                    this.param = arguments;
                    this.resolve(this.callback)
                }
            };

        dataStore.removeAll(true);
        dataStore.getProxy().setUrl({
            mRID: mRID,
            loadProfileId: loadProfileId
        });

        viewport.setLoading();
        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                defer.setParam(record)
            }
        });

        initView = function (device) {
            var record = me.getLoadProfile(),
                dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(record.get('interval')),
                durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations')
                ;

            durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));

            me.loadProfileModel = record;
            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
            var func = function () {
                viewport.setLoading(false);
                var all = dataIntervalAndZoomLevels.get('all'),
                    intervalStart = dataIntervalAndZoomLevels.getIntervalStart((me.loadProfileModel.get('lastReading') || new Date().getTime()));

                tabWidget = Ext.widget('tabbedDeviceLoadProfilesView',{
                    device: device,
                    loadProfileId: loadProfileId,
                    toggleId: 'loadProfileLink',
                    router: router
                });
                widget = Ext.widget('deviceLoadProfilesData', {
                    router: router,
                    loadProfile: record,
                    channels: record.get('channels'),
                    device: device,
                    filter: {
                        fromDate: intervalStart,
                        duration: all.count + all.timeUnit,
                        durationStore: durationsStore
                    }
                });

                tabWidget.down('#loadProfileTabPanel').setTitle(record.get('name'));
                tabWidget.down('#loadProfile-data').add(widget);
                me.getApplication().fireEvent('changecontentevent', tabWidget);
                tabController.showTab(1);
                Ext.getBody().mask('Loading...');
                widget.setLoading();
                widget.down('#deviceLoadProfilesGraphViewBtn').setDisabled(!isTable);
                widget.down('#deviceLoadProfilesTableViewBtn').setDisabled(isTable);
                widget.down('#deviceLoadProfilesTableView').setVisible(isTable);
                widget.down('#deviceLoadProfilesGraphView').setVisible(!isTable);
                dataStore.on('load', function () {
                    if (!widget.isDestroyed) {
                        if (!isTable) {
                            me.showGraphView(record);
                        }
                        widget.down('#readingsCount') && widget.down('#readingsCount').setVisible(widget.down('#deviceLoadProfilesTableView').isVisible() && dataStore.count());
                        widget.setLoading(false);
                        Ext.getBody().unmask();
                    }
                }, me);

                dataStore.load();
            };
            if (loadProfilesStore.getTotalCount() === 0) {
                loadProfilesStore.getProxy().setUrl(mRID);
                loadProfilesStore.load(function () {
                    func();
                });
            } else {
                func();
            }
        };

        if (loadProfile) {
            me.setLoadProfile(loadProfile);
            defer.setCallback(initView);
        } else {
            loadProfileModel.getProxy().setUrl(mRID);
            loadProfileModel.load(loadProfileId, {
                success: function (record) {
                    me.setLoadProfile(record);
                    defer.setCallback(initView);
                }
            });
        }
    },

    getLoadProfile: function(){
        return this.loadProfile
    },

    setLoadProfile: function(loadProfile){
        this.loadProfile = loadProfile
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
            var channelHeader = !Ext.isEmpty(channel.calculatedReadingType) ? channel.calculatedReadingType.measuringPeriod + ' ' + channel.calculatedReadingType.aliasName + ' (' + channel.calculatedReadingType.unit + ')' : channel.readingType.measuringPeriod + ' ' + channel.readingType.aliasName + ' (' + channel.readingType.unit + ')';
            channels.push({name: channelHeader, unitOfMeasure: channel.unitOfMeasure.unit });
            seriesToYAxisMap[index] = seriesObject['yAxis'];
            series.push(seriesObject);
        });

        lineCount = measurementTypeOrder.length;
        step = (100 / lineCount | 0) - 1;
        axisBacklash = (4 - lineCount) > 0 ? (4 - lineCount) : 0;

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
                margin: -5 * yAxisTitle.length,
                text: yAxisTitle
            };
            yAxis.push(yAxisObject);
        });

        if (dataStore.getTotalCount() > 0) {
            dataStore.each(function (record) {
                if (record.get('channelData')) {
                    Ext.iterate(record.get('channelData'), function (key, value) {
                        if (channelDataArrays[key]) {
                            if (value) {
                                if (value.indexOf(',') !== -1) {
                                    value = value.replace(',', '');
                                }
                                channelDataArrays[key].unshift([record.get('interval').start, parseFloat(value)]);
                            } else {
                                channelDataArrays[key].unshift([record.get('interval').start, null]);
                            }
                        }
                    });
                }
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
        var router = this.getController('Uni.controller.history.Router'),
            showTable = button.action === 'showTableView';

        if (showTable) {
            router.getRoute('devices/device/loadprofiles/loadprofiletableData').forward(router.arguments, router.getQueryStringValues());
        } else {
            router.getRoute('devices/device/loadprofiles/loadprofiledata').forward(router.arguments, router.getQueryStringValues());
        }
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('deviceLoadProfileChannelDataPreview');

        preview.updateForm(record);
    }
});