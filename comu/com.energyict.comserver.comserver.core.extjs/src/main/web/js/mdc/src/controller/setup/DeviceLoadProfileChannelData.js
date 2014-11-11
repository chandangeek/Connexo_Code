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
        {ref: 'deviceLoadProfileChannelDataPreview', selector: '#deviceLoadProfileChannelDataPreview'},
        {
            ref: 'sideFilter',
            selector: 'deviceLoadProfileChannelData #deviceLoadProfileChannelDataSideFilter'
        },
        {
            ref: 'sideFilterForm',
            selector: 'deviceLoadProfileChannelData #deviceLoadProfileChannelDataFilterForm'
        },
        {
            ref: 'filterPanel',
            selector: 'deviceLoadProfileChannelData filter-top-panel'
        }
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
            'deviceLoadProfileChannelData #deviceLoadProfileDataFilterResetBtn': {
                click: this.clearFilter
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelGraphView': {
                resize: this.onGraphResize
            },
            'deviceLoadProfileChannelData #deviceloadprofileschanneldatafilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            }
        });
    },

    showTableOverview: function (mRID, loadProfileId, channelId) {
        this.showOverview(mRID, loadProfileId, channelId, true);
    },

    showGraphOverview: function (mRID, loadProfileId, channelId) {
        this.showOverview(mRID, loadProfileId, channelId, false);
    },

    showOverview: function (mRID, loadProfileId, channelId, isTable) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            models = {
                device: me.getModel('Mdc.model.Device'),
                loadProfile: me.getModel('Mdc.model.LoadProfileOfDevice'),
                channel: me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice')
            },
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            router = me.getController('Uni.controller.history.Router'),
            widget;

        dataStore.removeAll(true);

        viewport.setLoading();
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
                var dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(record.get('interval')),
                    durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
                    viewOnlySuspects;
                durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
                widget = Ext.widget('deviceLoadProfileChannelData', {
                    router: me.getController('Uni.controller.history.Router'),
                    channel: record
                });
                me.channelModel = record;
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                if (isTable) {
                    widget.down('#deviceLoadProfileChannelSubMenuPanel #channelOfLoadProfileOfDeviceDataLink').hide();
                    widget.down('#deviceLoadProfileChannelSubMenuPanel #channelOfLoadProfileOfDeviceDataTableLink').show();
                } else {
                    widget.down('#deviceLoadProfileChannelSubMenuPanel #channelOfLoadProfileOfDeviceDataTableLink').hide();
                    widget.down('#deviceLoadProfileChannelSubMenuPanel #channelOfLoadProfileOfDeviceDataLink').show();
                }
                widget.down('#deviceLoadProfileChannelSubMenuPanel').setParams(mRID, loadProfileId, record);
                me.getApplication().fireEvent('changecontentevent', widget);
                viewport.setLoading(false);
                widget.setLoading();
                widget.down('#deviceLoadProfileChannelGraphViewBtn').setDisabled(!isTable);
                widget.down('#deviceLoadProfileChannelTableViewBtn').setDisabled(isTable);
                widget.down('#deviceLoadProfileChannelTableView').setVisible(isTable);
                dataStore.on('load', function () {
                    if (!widget.isDestroyed) {
                        if (!isTable) {
                            me.showGraphView(record);
                        }
                        widget.down('#deviceLoadProfileChannelGraphView').setVisible(!isTable);
                        widget.setLoading(false);
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
                } else {
                    seriesObject['data'].unshift([record.get('interval').end, null]);
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
        var router = this.getController('Uni.controller.history.Router'),
            showTable = button.action === 'showTableView';

        if (showTable) {
            router.getRoute('devices/device/loadprofiles/loadprofile/channels/channel/tableData').forward(router.arguments, router.queryParams);
        } else {
            router.getRoute('devices/device/loadprofiles/loadprofile/channels/channel/data').forward(router.arguments, router.queryParams);
        }

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
            intervalStart = dataIntervalAndZoomLevels.getIntervalStart((me.channelModel.get('lastReading') || new Date().getTime()));
        router.filter = Ext.create('Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter');
        router.filter.set('intervalStart', intervalStart);
        router.filter.set('duration', all.count + all.timeUnit);
        router.filter.set('onlySuspect', viewOnlySuspects);
        router.filter.set('onlyNonSuspect', false);
        me.getPage().down('#suspect').setValue(viewOnlySuspects);
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
            + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalStart, 'MDC', 'd/m/Y') + ' ';
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