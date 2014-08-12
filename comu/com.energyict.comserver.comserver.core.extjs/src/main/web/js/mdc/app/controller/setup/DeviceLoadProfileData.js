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
        'Mdc.store.LoadProfilesOfDeviceData'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfilesData'
        }
    ],

    loadProfilesOfDeviceDataStoreUrl: null,
    loadProfileOfDeviceModelUrl: null,

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
            'deviceLoadProfilesData #deviceLoadProfileDataFilterResetBtn': {
                click: this.resetFilter
            }
        });
        this.loadProfilesOfDeviceDataStoreUrl = this.getStore('Mdc.store.LoadProfilesOfDeviceData').getProxy().url;
        this.loadProfileOfDeviceModelUrl = this.getModel('Mdc.model.LoadProfileOfDevice').getProxy().url;
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
        loadProfileOfDeviceModel.getProxy().url = me.loadProfileOfDeviceModelUrl.replace('{mRID}', mRID);
        loadProfileOfDeviceModel.load(loadProfileId, {
            success: function (record) {
                widget = Ext.widget('deviceLoadProfilesData', {
                    router: me.getController('Uni.controller.history.Router'),
                    channels: record.get('channels')
                });
                graphView = widget.down('#deviceLoadProfilesGraphView');

                me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                graphView.setRecord(record);
                me.getApplication().fireEvent('changecontentevent', widget);

                graphView.setLoading(true);
                loadProfilesOfDeviceDataStoreProxy.url = me.loadProfilesOfDeviceDataStoreUrl.replace('{mRID}', mRID).replace('{loadProfileId}', loadProfileId);
                loadProfilesOfDeviceDataStore.on('load', function () {
                    me.showGraphView(widget.down('#deviceLoadProfilesGraphView'));
                    graphView.setLoading(false);
                    me.showReadingsCount(widget, loadProfilesOfDeviceDataStore);
                }, me);
                loadProfilesOfDeviceDataStore.load();
            }
        });
    },

    showReadingsCount: function(widget, store) {
        var container = widget.down('#readingsCountOnLoadProfile'),
            readingsCount = store.getCount();

        if (readingsCount > 0) {
            container.removeAll();
            container.add({
              html: readingsCount + ' ' +  Uni.I18n.translate('devicetype.readings', 'MDC', 'reading(s)')
            })
        } else {
            container.hide();
        }
    },

    showGraphView: function (container) {
        var me = this,
            dataStore = me.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            loadProfileRecord = container.getRecord(),
            title = loadProfileRecord.get('name'),
            currentAxisTopValue = 1,
            currentLine = 0,
            series = [],
            yAxis = [],
            channels = [],
            measurementTypeOrder = [],
            channelDataArrays = {},
            flowMeasuresNumbers = {},
            intervalLength,
            lineCount,
            step;


        Ext.Array.each(loadProfileRecord.get('channels'), function (channel) {
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
                    channels.push({name: channel.name, unitOfMeasure: channel.unitOfMeasure.localizedValue });
                    series.push(seriesObject);
                    break;
                case 'volume':
                    seriesObject['type'] = 'column';
                    seriesObject['step'] = true;
                    seriesObject['yAxis'] = currentLine;
                    measurementTypeOrder.push(channel.unitOfMeasure.localizedValue);
                    currentLine += 1;
                    channels.push({name: channel.name, unitOfMeasure: channel.unitOfMeasure.localizedValue });
                    series.push(seriesObject);
                    break;
            }
        });


        lineCount = measurementTypeOrder.length;
        step = 100 / lineCount | 0;

        Ext.Array.each(measurementTypeOrder, function (type, index) {
            var yAxisObject = {
                opposite: false,
                gridLineDashStyle: 'Dot'
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
            currentAxisTopValue += step;
            yAxisObject['title'] = {
                rotation: 270,
                text: type
            };
            yAxis.push(yAxisObject);
        });

        if (dataStore.getTotalCount() > 1) {
            dataStore.each(function (record) {
                if (!intervalLength) {
                    intervalLength = record.get('interval').end - record.get('interval').start;
                }
                Ext.iterate(record.get('channelData'), function (key, value) {
                    channelDataArrays[key].push([record.get('interval').end, value])
                });
            });
            container.setParams(title, yAxis, series, channels, intervalLength);
            container.drawGraph();
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

    applyFilter: function () {
        var page = this.getPage(),
            graphView = page.down('#deviceLoadProfilesGraphView'),
            formValues = page.down('#deviceLoadProfileDataFilterForm').getForm().getValues(),
            store = this.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            storeProxy = store.getProxy();

        storeProxy.setExtraParam('intervalStart', new Date(formValues.intervalStart).getTime());
        storeProxy.setExtraParam('intervalEnd', new Date(formValues.intervalEnd).getTime());

        graphView.setLoading(true);

        store.load(function () {
            graphView.setLoading(false);
        });
    },

    resetFilter: function () {
        var page = this.getPage(),
            graphView = page.down('#deviceLoadProfilesGraphView'),
            formFields = page.query('#deviceLoadProfileDataFilterForm [isFormField=true]'),
            store = this.getStore('Mdc.store.LoadProfilesOfDeviceData'),
            storeProxy = store.getProxy();

        Ext.Array.each(formFields, function (field) {
            field.reset();
        });

        delete storeProxy.extraParams.intervalStart;
        delete storeProxy.extraParams.intervalEnd;

        graphView.setLoading(true);

        store.load(function (records) {
            !records && store.removeAll();
            graphView.setLoading(false);
        });
    }
});