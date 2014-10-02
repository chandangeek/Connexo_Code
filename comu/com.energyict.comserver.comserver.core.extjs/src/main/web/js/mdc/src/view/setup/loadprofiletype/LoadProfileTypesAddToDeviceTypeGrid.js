Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
   // xtype: 'loadProfileTypesAddToDeviceTypeGrid',
    alias: 'widget.loadProfileTypesAddToDeviceTypeGrid',
    store: 'LoadProfileTypesOnDeviceTypeAvailable',
    overflowY: 'auto',
    height: 300,

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
            '{0} load profile types selected'
        );
    },

    columns: {
        items: [
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
                    var intervalRecord = this.intervalStore.findRecord('id', value.id);
                    return intervalRecord.getData().name;
                },
                flex: 3
            }
        ]
    },

    extraTopToolbarComponent: {
        xtype: 'container',
        layout: {
            type: 'hbox',
            align: 'right'
        },
        flex: 1,
        items: [
            {
                xtype: 'component',
                flex: 1
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('loadprofiletypes.manageloadprofiletypes', 'MDC', 'Manage load profile types'),
                ui: 'link',
                href: '#/administration/loadprofiletypes',
                hrefTarget: '_blank'
            }
        ]
    }
});