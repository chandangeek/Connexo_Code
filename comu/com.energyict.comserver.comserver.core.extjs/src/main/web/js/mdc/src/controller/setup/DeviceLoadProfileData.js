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
        },
        {
            ref: 'sideFilter',
            selector: '#deviceLoadProfileDataSideFilter'
        },
        {
            ref: 'sideFilterForm',
            selector: '#deviceLoadProfileDataFilterForm'
        },
        {
            ref: 'filterPanel',
            selector: 'deviceLoadProfilesData filter-top-panel'
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
            'deviceLoadProfileDataSideFilter #deviceLoadProfileDataFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceLoadProfileDataSideFilter #deviceLoadProfileDataFilterResetBtn': {
                click: this.clearFilter
            },
            'deviceLoadProfilesData #deviceLoadProfilesGraphView': {
                resize: this.onGraphResize
            },
            '#deviceloadprofilesdatafilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
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
                durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
                viewOnlySuspects;
            durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
            tabWidget = Ext.widget('tabbedDeviceLoadProfilesView',{
                device: device,
                loadProfileId: loadProfileId,
                toggleId: 'loadProfileLink',
                router: me.getController('Uni.controller.history.Router')
            });

            widget = Ext.widget('deviceLoadProfilesData', {
                router: me.getController('Uni.controller.history.Router'),
                channels: record.get('channels'),
                device: device
            });
            tabWidget.down('#loadProfileTabPanel').setTitle(record.get('name'));
            tabWidget.down('#deviceLoadProfileDataSideFilter').setVisible(true);
            me.loadProfileModel = record;
            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
            var func = function () {
                me.getApplication().fireEvent('changecontentevent', tabWidget);
                tabWidget.down('#loadProfile-data').add(widget);
                tabController.showTab(1);
                viewport.setLoading(false);
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
                if (Ext.isEmpty(router.filter.data.intervalStart)) {
                    viewOnlySuspects = (router.queryParams.onlySuspect === 'true');
                    me.setDefaults(dataIntervalAndZoomLevels, viewOnlySuspects);
                    delete router.queryParams.onlySuspect;
                }
                dataStore.setFilterModel(router.filter);
                me.getSideFilterForm().loadRecord(router.filter);
                me.setFilterView();
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
            router.getRoute('devices/device/loadprofiles/loadprofiletableData').forward(router.arguments, router.queryParams);
        } else {
            router.getRoute('devices/device/loadprofiles/loadprofiledata').forward(router.arguments, router.queryParams);
        }
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLoadProfilesDataPreview'),
            intervalEnd = record.get('interval_end');

        preview.rendered && Ext.suspendLayouts();

        preview.setTitle(Uni.DateTime.formatDateLong(intervalEnd)
            + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
            + Uni.DateTime.formatTimeLong(intervalEnd));
        preview.down('#deviceLoadProfilesDataPreviewForm').loadRecord(record);

        preview.rendered && Ext.resumeLayouts(true);
    },

    applyFilter: function () {
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        if (key === 'onlySuspect' || key === 'onlyNonSuspect') {
            record.set(key, false);
        }
        record.save();
    },

    setDefaults: function (dataIntervalAndZoomLevels, viewOnlySuspects) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            all = dataIntervalAndZoomLevels.get('all'),
            intervalStart = dataIntervalAndZoomLevels.getIntervalStart((me.loadProfileModel.get('lastReading') || new Date().getTime()));
        router.filter.beginEdit();
        router.filter.set('intervalStart', intervalStart);
        router.filter.set('duration', all.count + all.timeUnit);
        router.filter.set('onlySuspect', viewOnlySuspects);
        router.filter.set('onlyNonSuspect', false);
        router.filter.endEdit();
        me.getSideFilter().down('#suspect').setValue(viewOnlySuspects);
    },


    setFilterView: function () {
        var filterForm = this.getSideFilterForm(),
            filterView = this.getFilterPanel(),
            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalEndField = filterForm.down('[name=duration]'),
            suspectField = filterForm.down('#suspect'),
            nonSuspectField = filterForm.down('#nonSuspect'),
            intervalStart = intervalStartField.getValue(),
            intervalEnd = intervalEndField.getRawValue(),
            suspect = suspectField.boxLabel,
            nonSuspect = nonSuspectField.boxLabel,
            eventDateText = '';
        eventDateText += intervalEnd + ' ' + intervalStartField.getFieldLabel().toLowerCase() + ' '
            + Uni.DateTime.formatDateShort(intervalStart);
        filterView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
        filterView.down('#Reset').setText('Reset');
        if (suspectField.getValue()) {
            filterView.setFilter('onlySuspect', filterForm.down('#suspectContainer').getFieldLabel(), suspect);
        }
        if (nonSuspectField.getValue()) {
            filterView.setFilter('onlyNonSuspect', filterForm.down('#suspectContainer').getFieldLabel(), nonSuspect);
        }
    }
});