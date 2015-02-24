Ext.define('Mdc.view.setup.register.RegisterMappingAddGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.registerMappingAddGrid',
    store: 'AvailableRegisterTypes',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'registerTypes.selectedItems',
            count,
            'MDC',
            '{0} register types selected'
        );
    },

    columns: {
        items: [
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 3
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode',
                flex: 1
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
                text: Uni.I18n.translate('registertype.manageregistertypes', 'MDC', 'Manage register types'),
                ui: 'link',
                href: '#/administration/registertypes',
                hrefTarget: '_blank'
            }
        ]
    }

})
;