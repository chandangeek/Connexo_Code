Ext.define('Mdc.controller.setup.DeviceLogbookData', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common'
    ],

    views: [
        'Mdc.view.setup.devicelogbooks.Data'
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
        'Mdc.store.EventsOrActions'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLogbookData'
        },
        {
            ref: 'sideFilter',
            selector: 'deviceLogbookData #device-logbook-data-side-filter'
        },
        {
            ref: 'filterForm',
            selector: 'deviceLogbookData #deviceLogbookDataSideFilterForm'
        },
        {
            ref: 'filterToolbar',
            selector: 'deviceLogbookData #device-logbook-data-filter-toolbar'
        }
    ],

    loadProfileModel: null,

    init: function () {
        this.control({
            'deviceLogbookData #deviceLogbookDataGrid': {
                select: this.showPreview
            },
            'deviceLogbookData #deviceLogbookDataSideFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceLogbookData #deviceLogbookDataSideFilterResetBtn': {
                click: this.clearFilter
            },
            'deviceLogbookData #device-logbook-data-filter-toolbar': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            },
            'deviceLogbookData #deviceLogbookDataSideFilterForm [name=intervalStart]': {
                change: this.changeFilterByIntervalStart
            },
            'deviceLogbookData #deviceLogbookDataSideFilterForm [name=domain]': {
                change: this.changeComboFilter
            },
            'deviceLogbookData #deviceLogbookDataSideFilterForm [name=subDomain]': {
                change: this.changeComboFilter
            },
            'deviceLogbookData #deviceLogbookDataSideFilterForm [name=eventOrAction]': {
                change: this.changeComboFilter
            }
        });
    },

    showOverview: function (mRID, logbookId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            widget,
            sideFilter;

        dataStoreProxy.setUrl({
            mRID: mRID,
            logbookId: logbookId
        });

        widget = Ext.widget('deviceLogbookData', {
            router: me.getController('Uni.controller.history.Router')
        });
        me.getApplication().fireEvent('changecontentevent', widget);

        sideFilter = me.getSideFilter();
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

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });

        logbookModel.getProxy().setUrl(mRID);
        logbookModel.load(logbookId, {
            success: function (record) {
                me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                widget.down('#deviceLogbookSubMenuPanel').setParams(mRID, record);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLogbookDataPreview');

        preview.setTitle(Uni.I18n.formatDate('devicelogbooks.eventDate.dateFormat', record.get('eventDate'), 'MDC', 'M d, Y H:i:s'));
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
            record.set('intervalStart', null);
            record.set('intervalEnd', null);
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
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalStart, 'MDC', 'd/m/Y') + ' ';
            }
            if (intervalEnd) {
                eventDateText += (intervalStart ? intervalEndField.getFieldLabel().toLowerCase() : intervalEndField.getFieldLabel()) + ' '
                    + Uni.I18n.formatDate('devicelogbooks.topFilter.tagButton.dateFormat', intervalEnd, 'MDC', 'd/m/Y');
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