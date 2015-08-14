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
            title: '&nbsp;',
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
                    hidden: true,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'deviceGroup',
                    emptyText: Uni.I18n.translate('datacollectionkpis.selectDeviceGroup', 'MDC', 'Select a device group...'),
                    itemId: 'cmb-device-group',
                    fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
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
                    fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                    required: true,
                    htmlEncode: false,
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    name: 'frequency',
                    emptyText: Uni.I18n.translate('datacollectionkpis.selectCalculationFrequency', 'MDC', 'Select a calculation frequency...'),
                    itemId: 'cmb-frequency',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                    store: 'Mdc.store.DataCollectionKpiFrequency',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'name',
                    valueField: 'id',
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'displayRange',
                    emptyText: Uni.I18n.translate('datacollectionkpis.selectDisplayRange', 'MDC', 'Select a display range...'),
                    itemId: 'cmb-display-range',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.displayRange', 'MDC', 'Display range'),
                    store: 'Mdc.store.DataCollectionKpiRange',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'name',
                    valueField: 'id',
                    allowBlank: false,
                    required: true,
                    disabled: true,
                    lastQuery: '',
                    width: 600
                },
                {
                    xtype: 'kpi-field-container',
                    groupName: 'connectionKpiContainer',
                    itemId: 'connectionKpiField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.connectionKpi', 'MDC', 'Connection KPI')
                },
                {
                    xtype: 'kpi-field-container',
                    groupName: 'communicationKpiContainer',
                    itemId: 'communicationKpiField',
                    fieldLabel: Uni.I18n.translate('datacollectionkpis.communicationKpi', 'MDC', 'Communication KPI')
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
            ],

            loadRecord: function (record) {
                var me = this,
                    deviceGroup = record.get('deviceGroup'),
                    frequency = record.get('frequency'),
                    displayRange = record.get('displayRange'),
                    connectionTarget = record.get('connectionTarget'),
                    communicationTarget = record.get('communicationTarget'),
                    deviceGroupCombo = me.down('[name=deviceGroup]');

                me.getForm().loadRecord(record);
                if (deviceGroup) {
                    deviceGroupCombo.setRawValue(deviceGroup.name);
                }
                if (frequency) {
                    me.down('[name=frequency]').setValue(frequency.every.count + frequency.every.timeUnit);
                }
                if (displayRange) {
                    me.down('[name=displayRange]').setValue(displayRange.count + displayRange.timeUnit);
                }
                me.down('[groupName=connectionKpiContainer]').setValue(connectionTarget);
                me.down('[groupName=communicationKpiContainer]').setValue(communicationTarget);
            }
        }
    ]

});
