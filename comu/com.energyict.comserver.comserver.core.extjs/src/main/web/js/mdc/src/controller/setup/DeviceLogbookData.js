Ext.define('Mdc.controller.setup.DeviceLogbookData', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common'
    ],

    views: [
//        'Mdc.view.setup.devicelogbooks.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice',
        'Mdc.model.LogbookOfDeviceDataFilter'
    ],

    stores: [
        'Mdc.store.LogbookOfDeviceData',
        'Mdc.store.Domains',
        'Mdc.store.Subdomains',
        'Mdc.store.EventsOrActions',
        'Mdc.store.LogbooksOfDevice'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLogbookData'
        },
        {
            ref: 'sideFilter',
            selector: '#logBookFilter'
        },
        {
            ref: 'filterForm',
            selector: '#deviceLogbookDataSideFilterForm'
        },
        {
            ref: 'filterToolbar',
            selector: 'deviceLogbookData #device-logbook-data-filter-toolbar'
        },
        {
            ref: 'deviceLogBookDetailTitle',
            selector: '#deviceLogBookDetailTitle'
        }
    ],

    loadProfileModel: null,

    init: function () {
        this.control({
            'deviceLogbookData #deviceLogbookDataGrid': {
                select: this.showPreview
            },
            '#deviceLogbookDataSideFilterApplyBtn': {
                click: this.applyFilter
            },
            '#deviceLogbookDataSideFilterResetBtn': {
                click: this.clearFilter
            },
            '#device-logbook-data-filter-toolbar': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            },
            '#deviceLogbookDataSideFilterForm [name=intervalStart]': {
                change: this.changeFilterByIntervalStart
            },
            '#deviceLogbookDataSideFilterForm [name=domain]': {
                change: this.changeComboFilter
            },
            '#deviceLogbookDataSideFilterForm [name=subDomain]': {
                change: this.changeComboFilter
            },
            '#deviceLogbookDataSideFilterForm [name=eventOrAction]': {
                change: this.changeComboFilter
            }
        });
    },

    showOverview: function (mRID, logbookId, tabController) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            widget,
            data,
            logbooksOfDeviceStore = me.getStore('Mdc.store.LogbooksOfDevice'),
            sideFilter;

        dataStoreProxy.setUrl({
            mRID: mRID,
            logbookId: logbookId
        });



        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                widget = Ext.widget('tabbedDeviceLogBookView', {
                    device: record,
                    router: me.getController('Uni.controller.history.Router')
                });

                data = Ext.widget('deviceLogbookData', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: record,
                    side: false,
                    eventsView: false
                });
                var func = function () {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('#logBook-data').add(data);
                    tabController.showTab(1);
                    sideFilter = me.getSideFilter();
                    sideFilter.show();
                    sideFilter.disable();
                    Uni.util.Common.loadNecessaryStores([
                        'Mdc.store.Domains',
                        'Mdc.store.Subdomains',
                        'Mdc.store.EventsOrActions'
                    ], function () {
                        me.getFilterForm().loadRecord(router.filter);
                        sideFilter.enable();
                        me.setFilterView();
                    });
                    logbookModel.getProxy().setUrl(mRID);
                    logbookModel.load(logbookId, {
                        success: function (record) {
                            me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                            widget.down('#logBookTabPanel').setTitle(record.get('name'));
                        }
                    });
                };
                if (logbooksOfDeviceStore.getTotalCount() === 0) {
                    logbooksOfDeviceStore.getProxy().setUrl(mRID);
                    logbooksOfDeviceStore.load(function () {
                        func();
                    });
                } else {
                    func();
                }
            }
        });


    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLogbookDataPreview');

        preview.setTitle(Uni.DateTime.formatDateTimeLong(record.get('eventDate')));
        preview.down('#deviceLogbookDataPreviewForm').loadRecord(record);
    },

    applyFilter: function (button) {
        var filterForm = this.getFilterForm();

        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    clearFilter: function () {
        this.getFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        if (key === 'eventDate') {
            record.beginEdit();
            record.set('intervalStart', null);
            record.set('intervalEnd', null);
            record.endEdit();
        } else {
            record.set(key, null);
        }

        record.save();
    },

    changeFilterByIntervalStart: function (intervalStartField, newValue) {
        this.getFilterForm().down('[name=intervalEnd]').setMinValue(newValue);
    },

    changeComboFilter: function (combo, newValue) {
        if (!newValue) {
            combo.reset();
        }
    },

    setFilterView: function () {
        var filterForm = this.getFilterForm(),
            filterView = this.getFilterToolbar(),
            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalEndField = filterForm.down('[name=intervalEnd]'),
            intervalStart = intervalStartField.getValue(),
            intervalEnd = intervalEndField.getValue(),
            eventDateText = '';

        if (intervalStart || intervalEnd) {
            if (intervalStart) {
                eventDateText += intervalStartField.getFieldLabel() + ' '
                    + Uni.DateTime.formatDateShort(intervalStart) + ' ';
            }
            if (intervalEnd) {
                eventDateText += (intervalStart ? intervalEndField.getFieldLabel().toLowerCase() : intervalEndField.getFieldLabel()) + ' '
                    + Uni.DateTime.formatDateShort(intervalEnd);
            }
            filterView.setFilter('eventDate', filterForm.down('#event-date-container').getFieldLabel(), eventDateText);
        }

        Ext.Array.each(filterForm.query('combobox'), function (combo) {
            var value = combo.getRawValue();

            if (!_.isEmpty(value)) {
                filterView.setFilter(combo.getName(), combo.getFieldLabel(), value);
            }
        });
    }
});