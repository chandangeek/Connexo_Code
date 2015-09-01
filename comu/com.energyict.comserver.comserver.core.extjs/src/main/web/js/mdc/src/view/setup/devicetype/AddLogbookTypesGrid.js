Ext.define('Mdc.view.setup.devicetype.AddLogbookTypesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-logbook-types-grid',
    store: 'AvailableLogbookTypes',
    requires: [
        'Uni.grid.column.Obis'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfLogbookTypes.selected', count, 'MDC',
            'No logbook types selected', '{0} logbook type selected', '{0} logbook types selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode',
                flex: 2
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
                text: Uni.I18n.translate('logbooktype.managelogbooktypes', 'MDC', 'Manage logbook types'),
                ui: 'link',
                href: '#/administration/logbooktypes',
                hrefTarget: '_blank'
            }
        ]
    }
})
;
