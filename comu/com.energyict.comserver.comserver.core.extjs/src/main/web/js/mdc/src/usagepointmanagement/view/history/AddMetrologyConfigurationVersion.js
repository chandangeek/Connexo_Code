Ext.define('Mdc.usagepointmanagement.view.history.AddMetrologyConfigurationVersion', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-metrology-configuration-version',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.usagepointmanagement.view.InstallationTimeField'
    ],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('usagePoint.addMetrologyConfigurationVersion', 'MDC', 'Add metrology configuration versions'),
                items: [
                    {
                        xtype: 'form',
                        itemId: 'add-version-form',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                itemId: 'form-errors',
                                xtype: 'uni-form-error-message',
                                name: 'form-errors',
                                width: 600,
                                hidden: true
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'mc-combo',
                                name: 'metrologyConfiguration',
                                displayField: 'name',
                                valueField: 'id',
                                store: 'Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations',
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
                                    width: 149
                                },
                                dateTimeSeparatorConfig: {
                                    html: Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase(),
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
                                required: true
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
                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        itemId: 'usage-point-add-button'
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
        currentDate.setHours(0,0,0,0);
        me.down('#start-time-date').setValue(currentDate);
    }
});