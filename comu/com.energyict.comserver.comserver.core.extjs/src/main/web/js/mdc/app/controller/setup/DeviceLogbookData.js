Ext.define('Mdc.controller.setup.DeviceLogbookData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicelogbooks.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice',
        'Mdc.model.LogbookOfDeviceDataFilter'
    ],

    stores: [
        'Mdc.store.LogbookOfDeviceData'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLogbookData'
        },
        {
            ref: 'filterForm',
            selector: 'deviceLogbookData #deviceLogbookDataSideFilterForm'
        },
        {
            ref: 'filterView',
            selector: 'deviceLogbookData #deviceLogbookDataTopFilter'
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
                click: this.applyFilter
            },
            'deviceLogbookData #deviceLogbookDataTopFilter #Reset': {
                click: this.applyFilter
            },
            'deviceLogbookData #deviceLogbookDataTopFilter tag-button': {
                closeclick: this.removeFilterItem
            },
            'deviceLogbookData #deviceLogbookDataSideFilterForm [name=intervalStart]': {
                change: this.changeFilterByIntervalStart
            }
        });
    },

    showOverview: function (mRID, logbookId) {
        var me = this,
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            widget;

        dataStoreProxy.setUrl({
            mRID: mRID,
            logbookId: logbookId
        });
        dataStoreProxy.extraParams = {};

        widget = Ext.widget('deviceLogbookData', {
            router: me.getController('Uni.controller.history.Router')
        });
        me.getApplication().fireEvent('changecontentevent', widget);

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
        var me = this,
            reset = button.action !== 'filter',
            filterModel = Ext.create('Mdc.model.LogbookOfDeviceDataFilter'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            dataStoreProxy = dataStore.getProxy(),
            filterForm = me.getFilterForm();

        reset && filterForm.loadRecord(filterModel);

        if (filterForm.getForm().isValid()) {
            dataStoreProxy.extraParams = {};

            if (!reset) {
                filterForm.updateRecord(filterModel);
                Ext.iterate(filterModel.getData(), function (key, value) {
                    Ext.isDate(value) && (value = value.getTime());
                    value && dataStoreProxy.setExtraParam(key, value);
                });
            }

            me.getFilterView().addButtons(filterModel);
            me.resetGridToolbars();
            dataStore.loadPage(1);
        }
    },

    removeFilterItem: function (button) {
        var me = this,
            filterForm = me.getFilterForm();

        filterForm.down('[name=' + button.target + ']').reset();

        me.applyFilter({action: 'filter'});
    },

    changeFilterByIntervalStart: function (intervalStartField, newValue) {
        this.getFilterForm().down('[name=intervalEnd]').setMinValue(newValue);
    },

    resetGridToolbars: function () {
        var me = this,
            page = me.getPage();

        page.down('#deviceLogbookDataGrid pagingtoolbartop').totalCount = 0;
        page.down('#deviceLogbookDataGrid pagingtoolbarbottom').resetPaging();
    }
});