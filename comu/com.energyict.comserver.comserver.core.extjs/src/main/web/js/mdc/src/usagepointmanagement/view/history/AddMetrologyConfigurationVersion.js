/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.AddMetrologyConfigurationVersion', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-metrology-configuration-version',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.usagepointmanagement.view.InstallationTimeField',
        'Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations'
    ],
    edit: false,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: me.edit
                    ? Uni.I18n.translate('usagePoint.editMetrologyConfigurationVersion', 'MDC', 'Edit metrology configuration version')
                    : Uni.I18n.translate('usagePoint.addMetrologyConfigurationVersion', 'MDC', 'Add metrology configuration version'),
                items: [
                    {
                        xtype: 'form',
                        itemId: 'add-version-form',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                itemId: 'form-info',
                                xtype: 'uni-form-info-message',
                                name: 'form-info',
                                width: 600,
                                hidden: true,
                                text: Uni.I18n.translate('usagePointManagement.metrologyConfiguration.edit.current', 'MDC', 'Metrology configuration and start date can be modified for future versions only.')
                            },
                            {
                                itemId: 'form-errors',
                                xtype: 'uni-form-error-message',
                                name: 'form-errors',
                                width: 600,
                                hidden: true
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-mc-available-msg',
                                fieldLabel: Uni.I18n.translate('usagePointManagement.metrologyConfiguration', 'MDC', 'Metrology configuration'),
                                required: true,
                                hidden: true,
                                htmlEncode: false,
                                style: 'font-style: italic',
                                value: '<span style="color: #686868; font-style: italic">'
                                + Uni.I18n.translate('usagePointManagement.metrologyConfiguration.noMcAvailable', 'MDC', 'No active metrology configurations with appropriate service category')
                                + '</span>'
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'mc-combo',
                                name: 'metrologyConfiguration',
                                displayField: 'name',
                                valueField: 'id',
                                store: 'Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations',
                                queryMode: 'local',
                                required: true,
                                width: 600,
                                fieldLabel: Uni.I18n.translate('usagePointManagement.metrologyConfiguration', 'MDC', 'Metrology configuration')
                            },
                            {
                                xtype: 'date-time',
                                fieldLabel: Uni.I18n.translate('general.label.start', 'MDC', 'Start'),
                                name: 'start',
                                itemId: 'start-time-date',
                                required: true,
                                layout: 'hbox',
                                valueInMilliseconds: true,
                                dateConfig: {
                                    width: 149,
                                    fieldLabel: Uni.I18n.translate('general.on', 'MDC', 'On'),
                                    labelWidth: 16
                                },
                                dateTimeSeparatorConfig: {
                                    html: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
                                    style: 'color: #686868'
                                },
                                hoursConfig: {
                                    width: 75
                                },
                                minutesConfig: {
                                    width: 75
                                }
                            },
                            {
                                xtype: 'installationtimefield',
                                defaultValueLabel: Uni.I18n.translate('general.none', 'MDC', 'None'),
                                midnight: true,
                                dateFieldName: 'end',
                                itemId: 'end-time-date',
                                fieldLabel: Uni.I18n.translate('general.label.end', 'MDC', 'End'),
                                required: true,
                                dateOnLabel: Uni.I18n.translate('general.on', 'MDC', 'On')
                            },
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: me.edit
                                            ? Uni.I18n.translate('general.save', 'MDC', 'Save')
                                            : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        itemId: 'usage-point-add-edit-button',
                                        action: me.edit ? 'edit' : 'add'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: me.router.getRoute('usagepoints/usagepoint/history').buildUrl()
                                    }
                                ]
                            }
                        ]

                    }
                ]
            }
        ];

        me.callParent(arguments);

        var currentDate = new Date();
        currentDate.setHours(0, 0, 0, 0);
        me.down('#start-time-date').setValue(currentDate);
    },

    loadRecordToForm: function (record) {
        this.down('#add-version-form').loadRecord(record);
        this.down('#mc-combo').setValue(record.get('metrologyConfiguration').id);
        if (record.get('end')) {
            this.down('installationtimefield').setValue({"installation-time": false});
            this.down('installationtimefield date-time[name=end]').setValue(record.get('end'));
        }
        if (record.get('current')) {
            this.down('#form-info').show();
            this.down('#mc-combo').disable();
            this.down('#start-time-date').disable();
        }
    }
});