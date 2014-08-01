Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'loadProfileTypesAddToDeviceTypeGrid',
    itemId: 'loadProfileTypesAddToDeviceTypeGrid',
    store: 'LoadProfileTypesOnDeviceType',

    intervalStore: null,
    deviceTypeId: undefined,

    requires: [
        'Uni.grid.column.Obis'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid.counterText',
            count,
            'MDC',
            '{0} load profiles selected'
        );
    },

    allLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid.allLabel', 'MDC', 'All load profile types'),
    allDescription: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid.allDescription', 'MDC', 'Select all items (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid.selectedLabel', 'MDC', 'Selected load profile types'),
    selectedDescription: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid.selectedDescription', 'MDC', 'Select items in table'),

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            },
            {
                header: 'Interval',
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = me.intervalStore.findRecord('id', value.id);
                    return intervalRecord.getData().name;
                },
                flex: 3
            }
        ];

        me.cancelHref = '#/administration/devicetypes/' + me.deviceTypeId + '/loadprofiles';
        me.callParent(arguments);

        me.down('#topToolbarContainer').add({
            xtype: 'component',
            flex: 1
        });

        me.down('#topToolbarContainer').add({
            xtype: 'button',
            ui: 'link',
            text: Uni.I18n.translate('loadprofiletypes.manageloadprofiletypes', 'MDC', 'Manage load profile types'),
            handler: function (button, event) {
                window.open('#/administration/loadprofiletypes');
            }
        });
    }
});