Ext.define('Mdc.view.setup.devicegroup.StaticGroupDevicesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.static-group-devices-grid',
    store: 'Mdc.store.StaticGroupDevices',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfDevices.selected', count, 'MDC',
            'No devices selected', '{0} device selected', '{0} devices selected'
        );
    },

    allLabel: Uni.I18n.translate('deviceGroup.bulk.allDevices', 'MDC', 'All devices'),
    allDescription: Uni.I18n.translate('deviceGroup.bulk.selectMsg', 'MDC', 'Select all devices (according to search criteria)'),

    selectedLabel: Uni.I18n.translate('deviceGroup.bulk.selectedDevices', 'MDC', 'Selected devices'),
    selectedDescription: Uni.I18n.translate('deviceGroup.bulk.selectedDevicesInTable', 'MDC', 'Select devices in table'),

    bottomToolbarHidden: true,

    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    listeners: {
        afterrender: {
            fn: function () {
                var me = this;

                me.down('#topToolbarContainer').insert(3, '->');
                me.down('#topToolbarContainer').insert(4, {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'uni-search-column-picker',
                            itemId: 'static-column-picker',
                            grid: me
                        }
                    ]
                });
            }
        }
    },

    devices: null,

    setDevices: function (devices) {
        var me = this,
            mRIDs = [],
            selectionGroupType = {};

        me.un('selectionchange', me.onSelectionChange, me);
        selectionGroupType[me.radioGroupName] = me.allInputValue;
        me.getSelectionGroupType().setValue(selectionGroupType);
        Ext.Array.each(devices, function (device) {
            mRIDs.push(device.get('mRID'));
        });
        me.devices = mRIDs;
        me.getSelectionCounter().setText(me.counterTextFn(me.devices.length));
        me.getUncheckAllButton().setDisabled(me.devices.length === 0);
        me.on('select', me.onSelect, me);
        me.on('beforedeselect', me.onBeforeDeselect, me);
        me.getStore().on('prefetch', me.onPrefetch, me);
        me.on('destroy', function () {
            me.un('select', me.onSelect, me);
            me.un('beforedeselect', me.onBeforeDeselect, me);
            me.getStore().un('prefetch', me.onPrefetch, me);
        });
    },

    onSelect: function (selectionModel, record) {
        var me = this;

        Ext.Array.include(me.devices, record.get('mRID'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.devices.length));
        me.getUncheckAllButton().setDisabled(me.devices.length === 0);
        Ext.resumeLayouts(true);
    },

    onBeforeDeselect: function (selectionModel, record) {
        var me = this;

        Ext.Array.remove(me.devices, record.get('mRID'));
        Ext.suspendLayouts();
        me.getSelectionCounter().setText(me.counterTextFn(me.devices.length));
        me.getUncheckAllButton().setDisabled(me.devices.length === 0);
        Ext.resumeLayouts(true);
    },

    onPrefetch: function (store, records) {
        var me = this,
            selectionModel = me.getSelectionModel(),
            toSelect = [];

        Ext.Array.each(records, function (record) {
            if (Ext.Array.contains(me.devices, record.get('mRID'))) {
                toSelect.push(record);
            }
        });

        if (toSelect.length) {
            selectionModel.select(toSelect, true, true);
        }
    },

    onClickUncheckAllButton: function (button) {
        var me = this;

        Ext.suspendLayouts();
        me.getSelectionModel().deselectAll();
        button.disable();
        me.getSelectionCounter().setText(me.counterTextFn(0));
        Ext.resumeLayouts(true);
        if (me.devices) {
            me.devices = [];
        }
    }
});