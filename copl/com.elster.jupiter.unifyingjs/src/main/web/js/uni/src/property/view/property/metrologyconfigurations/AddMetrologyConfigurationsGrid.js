/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.metrologyconfigurations.AddMetrologyConfigurationsGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.uni-add-metrology-configurations-grid',
    store: 'Uni.property.store.PropertyDeviceConfigurations',
    cancelHref: undefined,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('metrologyconfigurations.counterText', count, 'UNI',
            'No metrology configurations selected', '{0} metrology configuration selected', '{0} metrology configurations selected'
        );
    },

    allLabel: Uni.I18n.translate('metrologyconfigurations.allMetrologyConfigurations', 'UNI', 'All metrology configurations'),
    allDescription: Uni.I18n.translate('metrologyconfigurations.selectAllMetrologyConfigurations', 'UNI', 'Select all metrology configurations'),

    selectedLabel: Uni.I18n.translate('metrologyconfigurations.selectedMetrologyConfigurations', 'UNI', 'Selected metrology configurations'),
    selectedDescription: Uni.I18n.translate('metrologyconfigurations.selectedMetrologyConfigurationsTable', 'UNI', 'Select metrology configurations in table'),

    columns: [
        {
            header: Uni.I18n.translate('metrologyconfigurations.metrologyConfiguration', 'UNI', 'Metrology configuration'),
            dataIndex: 'name',
            flex: 1,
            renderer: function (value, metaData, record) {
                return '<a href="#/administration/devicetypes/' + record.get('deviceTypeId') + '/deviceconfigurations/' + record.getId() + '">' + Ext.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('general.status', 'UNI', 'Status'),
            dataIndex: 'active',
            renderer: function (value) {
                return value
                    ? Uni.I18n.translate('general.active', 'UNI', 'Active')
                    : Uni.I18n.translate('general.inactive', 'UNI', 'Inactive');
            }
        }
    ]
});