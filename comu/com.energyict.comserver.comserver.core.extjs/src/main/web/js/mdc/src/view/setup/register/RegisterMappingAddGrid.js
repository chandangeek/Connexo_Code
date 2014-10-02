Ext.define('Mdc.view.setup.register.RegisterMappingAddGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.registerMappingAddGrid',
    overflowY: 'auto',
    height: 300,
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
                header: Uni.I18n.translate('registerMappings.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType'
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            },
            {
                header: Uni.I18n.translate('registerMappings.type', 'MDC', 'Type'),
                renderer: function (value, metaData, record) {
                    return '<div style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.getReadingType().get('measurementKind')
                        + '</div>'
                },
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