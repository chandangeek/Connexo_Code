Ext.define('Mdc.view.setup.datacollectionkpis.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dataCollectionKpiEdit',
    itemId: 'dataCollectionKpiEdit',

    requires: [
        'Mdc.view.setup.datacollectionkpis.KpiFieldContainer'
    ],

    content: [
        {
            xtype: 'form',
            itemId: 'dataCollectionKpiEditForm',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    name: 'deviceGroup',
                    emptyText: Uni.I18n.translate('datacollectionkpis.selectDeviceGroup', 'MDC', 'Select a device group'),
                    itemId: 'cmb-device-group',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.deviceGroup', 'MDC', 'Device group'),
                    store: 'Mdc.store.MeterExportGroups',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'name',
                    valueField: 'id',
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'displayfield',
                    itemId: 'devicegroupDisplayField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.deviceGroup', 'MDC', 'Device group'),
                    required: true,
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    name: 'frequency',
                    emptyText: Uni.I18n.translate('datacollectionkpis.selectFrequency', 'MDC', 'Select a frequency'),
                    itemId: 'cmb-frequency',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.frequency', 'MDC', 'Frequency'),
                    store: 'Mdc.store.DataCollectionKpiFrequency',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'name',
                    valueField: 'every',
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'displayfield',
                    itemId: 'frequencyDisplayField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.frequency', 'MDC', 'Frequency'),
                    required: true,
                    hidden: true
                },
                {
                    xtype: 'kpi-field-container',
                    groupName: 'connectionKpiContainer',
                    itemId: 'connectionKpiField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.connectionKpi', 'MDC', 'Connection KPI')
                },
                {
                    xtype: 'displayfield',
                    name: 'connectionTarget',
                    itemId: 'connectionKpiDisplayField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.connectionKpi', 'MDC', 'Connection KPI'),
                    required: true,
                    hidden: true,
                    renderer: function(value) {
                        return value + '%'
                    }
                },
                {
                    xtype: 'kpi-field-container',
                    groupName: 'communicationKpiContainer',
                    itemId: 'communicationKpiField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.communicationKpi', 'MDC', 'Communication KPI')
                },
                {
                    xtype: 'displayfield',
                    name: 'communicationTarget',
                    itemId: 'communicationKpiDisplayField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.connectionKpi', 'MDC', 'Communication KPI'),
                    required: true,
                    hidden: true,
                    renderer: function(value) {
                        return value + '%'
                    }
                },
                {
                    xtype: 'component',
                    itemId: 'kpiErrorContainer',
                    cls: 'x-form-invalid-under',
                    hidden: true,
                    margin: '-10 0 -20 270'
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'add',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            action: 'cancelAction'
                        }
                    ]
                }
            ]
        }
    ]

});
