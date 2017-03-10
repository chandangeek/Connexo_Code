/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Uni.store.DataIntervalAndZoomLevels',
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

    showTableOverview: function (deviceId, loadProfileId, tabController, loadProfile) {
        this.showOverview(deviceId, loadProfileId, true, tabController, loadProfile);
    },

    showGraphOverview: function (deviceId, loadProfileId, tabController, loadProfile) {
        this.showOverview(deviceId, loadProfileId, false, tabController, loadProfile);
    },

    showOverview: function (deviceId, loadProfileId, isTable, tabController, loadProfile) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            loadProfileModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            loadProfilesStore = me.getStore('Mdc.store.LoadProfilesOfDevice'),
            dataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
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

        if(loadProfile){
            if (loadProfile.data.id != loadProfileId || loadProfile.data.parent.id != deviceId) {
                loadProfile = null;
            }
        }

        dataStore.removeAll(true);
        dataStore.getProxy().setParams(deviceId, loadProfileId);

        viewport.setLoading();
        me.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                defer.setParam(record)
            }
        });

        var initGasDayYearStart = function (device) {
                var loadProfile = me.getLoadProfile(),
                    isGasLoadProfile = loadProfile.get('channels').filter(function (channel) {
                            return channel.readingType.isGasRelated;
                        }).length > 0;
                if (isGasLoadProfile) {
                    var yearStartStore = me.getStore('Uni.store.GasDayYearStart');
                    yearStartStore.on('load',
                        function (store, records) {
                            initView(device, records[0]);
                        },
                        me, {single: true});
                    yearStartStore.load();
                } else {
                    initView(device);
                }
            },
            initView = function (device, gasDayYearStart) {
            var record = me.getLoadProfile(),
                dataIntervalAndZoomLevels = me.getStore('Uni.store.DataIntervalAndZoomLevels').getIntervalRecord(record.get('interval')),
                durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations');

            durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));

            me.loadProfileModel = record;
            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
            var func = function () {
                viewport.setLoading(false);
                var all = dataIntervalAndZoomLevels.get('all'),
                    intervalStart,
                    fromDate;

                if (Ext.isEmpty(me.loadProfileModel.get('lastReading'))) {
                    fromDate = moment().startOf('day');
                    if (!Ext.isEmpty(gasDayYearStart)) {
                        fromDate.add(gasDayYearStart.get('hours'), 'hours')
                            .add(gasDayYearStart.get('minutes'), 'minutes');
                    }
                    intervalStart = dataIntervalAndZoomLevels.getIntervalStart(fromDate.toDate());
                } else {
                    fromDate = me.loadProfileModel.get('lastReading');
                    if (!Ext.isEmpty(gasDayYearStart)) {
                        var lastReading = moment(me.loadProfileModel.get('lastReading')),
                            lastReadingDayAtGasDayOffset = moment(me.loadProfileModel.get('lastReading')).startOf('day').add(gasDayYearStart.get('hours'), 'hours').add(gasDayYearStart.get('minutes'), 'minutes');
                        if (lastReading.isBefore(lastReadingDayAtGasDayOffset) || lastReading.isSame(lastReadingDayAtGasDayOffset)) {
                            fromDate = lastReadingDayAtGasDayOffset;
                        } else {
                            lastReadingDayAtGasDayOffset.add(1, 'days');
                            fromDate = lastReadingDayAtGasDayOffset;
                        }
                    }
                    intervalStart = dataIntervalAndZoomLevels.getIntervalStart(fromDate);
                }
                widget = Ext.widget('tabbedDeviceLoadProfilesView',{
                    device: device,
                    loadProfileId: loadProfileId,
                    toggleId: 'loadProfileLink',
                    router: router,
                    title: record.get('name'),
                    widget: {
                        xtype: 'deviceLoadProfilesData',
                        router: router,
                        loadProfile: record,
                        channels: record.get('channels'),
                        device: device,
                        isTable: isTable,
                        widget: isTable ? {
                            xtype: 'deviceLoadProfilesTableView',
                            router: router,
                            channels: record.get('channels')
                        } : {
                            xtype: 'deviceLoadProfilesGraphView',
                            router: router
                        },
                        filter: {
                            fromDate: intervalStart,
                            duration: all.count + all.timeUnit,
                            durationStore: durationsStore
                        }
                    }
                });

                Ext.suspendLayouts();
                me.getApplication().fireEvent('changecontentevent', widget);
                tabController.showTab(1);
                Ext.resumeLayouts(true);

                if (!isTable) {
                    widget.setLoading(); // If you entered via the URL
                    var applyBtn = widget.down('#filter-apply-all');
                    if (applyBtn) {
                        applyBtn.on('click', function () {
                            widget.setLoading(); // If you entered by clicking the Apply
                        }, me);
                    }
                }
                dataStore.on('load', function () {
                    if (!widget.isDestroyed) {
                        Ext.suspendLayouts();
                        if (!isTable) {
                            me.showGraphView(record);
                        }

                        widget.down('#readingsCount') &&
                            widget.down('#readingsCount').setVisible(
                                widget.down('#deviceLoadProfilesTableView').isVisible() && dataStore.count()
                            );
                        Ext.resumeLayouts(true);
                        widget.setLoading(false);
                    }
                }, me);

                dataStore.load();
            };
            if (loadProfilesStore.getTotalCount() === 0) {
                loadProfilesStore.getProxy().setExtraParam('deviceId', deviceId);
                loadProfilesStore.load(function () {
                    func();
                });
            } else {
                func();
            }
        };
        if (loadProfile) {
            me.setLoadProfile(loadProfile);
            defer.setCallback(initGasDayYearStart);
        } else {
            loadProfileModel.getProxy().setExtraParam('deviceId', deviceId);
            loadProfileModel.load(loadProfileId, {
                success: function (record) {
                    me.setLoadProfile(record);
                    defer.setCallback(initGasDayYearStart);
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
            zoomLevelsStore = me.getStore('Uni.store.DataIntervalAndZoomLevels'),
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
            var channelName = !Ext.isEmpty(channel.calculatedReadingType) ? channel.calculatedReadingType.fullAliasName : channel.readingType.fullAliasName;
            channels.push(
                {
                    id: channel.id,
                    name: channelName,
                    unitOfMeasure: !Ext.isEmpty(channel.calculatedReadingType)
                        ? channel.calculatedReadingType.names.unitOfMeasure : channel.readingType.names.unitOfMeasure
                }
            );
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
                    showEmpty: false,
                    labels: {
                        style: {
                            color: '#686868',
                            fontWeight: 'normal',
                            fontSize: '13px',
                            fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                        }
                    }
                },
                yAxisTitle = channel.name;


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
                margin: -6 * yAxisTitle.length,
                text: yAxisTitle,
                style: {
                    color: '#686868',
                    fontWeight: 'normal',
                    fontSize: '13px',
                    fontFamily: 'Lato, Helvetica, Arial, Verdana, Sans-serif'
                }
            };
            yAxis.push(yAxisObject);
        });

        if (dataStore.getTotalCount() > 0) {
            var showDeviceQualityIcon = {};
            dataStore.each(function (record) {
                showDeviceQualityIcon[record.get('interval').start] = me.arrayHasNonEmptyItems(record.get('readingQualities'), channels);
                if (record.get('channelData')) {
                    Ext.iterate(record.get('channelData'), function (key, value) {
                        if (channelDataArrays[key]) {
                            if (value) {
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
            container.drawGraph(title, yAxis, series, channels, seriesToYAxisMap, intervalLengthInMs, zoomLevels, showDeviceQualityIcon);
        } else {
            container.down('#graphContainer').hide();
            container.down('#emptyGraphMessage').show();
        }

        me.getPage().doLayout();
    },

    arrayHasNonEmptyItems: function(readingQualitiesArray, channels) {
        var hasNonEmptyItems = false;
        Ext.Array.each(channels, function (channel) {
            if (!Ext.isEmpty(readingQualitiesArray[channel.id])) {
                hasNonEmptyItems |= true;
            }
        });
        return hasNonEmptyItems;
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