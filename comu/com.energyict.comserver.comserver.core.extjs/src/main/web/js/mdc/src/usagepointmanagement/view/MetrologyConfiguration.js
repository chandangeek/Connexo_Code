Ext.define('Mdc.usagepointmanagement.view.MetrologyConfiguration', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration',
    itemId: 'metrology-configuration',
    title: Uni.I18n.translate('usagePointManagement.configuration', 'MDC', 'Configuration'),
    router: null,
    ui: 'tile',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'fieldcontainer',
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 155
                },
                items: [
                    {
                        name: 'metrologyConfigurationVersion',
                        itemId: 'fld-up-metrology-configuration',
                        fieldLabel: Uni.I18n.translate('usagePoint.generalAttributes.metrologyConfiguration', 'MDC', 'Metrology configuration'),
                        renderer: function (value) {
                            var result = '',
                                record = me.getRecord(),
                                startTime,
                                endTime,
                                versionsLink = '<a href="'
                                    + me.router.getRoute('usagepoints/usagepoint/history').buildUrl({
                                        usagePointId: encodeURIComponent(record.get('name')),
                                        tab: 'metrologyconfigurationversion'
                                    })
                                    + '">Versions</a>';

                            if (value) {
                                startTime = value.start;
                                endTime = value.end;
                                result += value.metrologyConfiguration.name;
                                if (startTime) {
                                    result += '<br><span style="font-size: 90%">'
                                        + Uni.I18n.translate('general.fromDate.from', 'MDC', 'From {0}', [Uni.DateTime.formatDateTimeShort(new Date(startTime))], false);
                                    if (endTime) {
                                        result += '&nbsp' + Uni.I18n.translate('general.fromDate.until', 'MDC', 'until {0}', [Uni.DateTime.formatDateTimeShort(new Date(endTime))], false);
                                    }
                                    result += '&nbsp' + versionsLink + '</span>';
                                }
                            }

                            return result || Uni.I18n.translate('usagePoint.generalAttributes.noActiveVersions', 'MDC', '(No active version) ') + versionsLink;
                        }
                    },
                    {
                        name: 'meterActivations',
                        itemId: 'fld-up-device',
                        fieldLabel: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                        renderer: function (activations) {
                            var value = '';
                            if ((activations instanceof Array) && !Ext.isEmpty(activations)) {
                                value = activations[0];
                            }
                            var result = '',
                                record = me.getRecord(),
                                canViewDevice,
                                startTime,
                                endTime,
                                versionsLink = '<a href="'
                                    + me.router.getRoute('usagepoints/usagepoint/history')
                                        .buildUrl({usagePointId: encodeURIComponent(record.get('name'))}, {historyTab: 'meterActivation'})
                                    + '">Versions</a>';

                            if (value) {
                                canViewDevice = Mdc.privileges.Device.canView();
                                startTime = value.start;
                                endTime = value.end;
                                if (canViewDevice) {
                                    result += '<a href="'
                                        + me.router.getRoute('devices/device').buildUrl({deviceId: value.meter.name})
                                        + '">';
                                }
                                result += value.meter.name;
                                if (canViewDevice) {
                                    result += '</a>';
                                }
                                if (startTime) {
                                    result += '<br><span style="font-size: 90%">'
                                        + Uni.I18n.translate('general.fromDate.from', 'MDC', 'From {0}', [Uni.DateTime.formatDateTimeShort(new Date(startTime))], false);
                                    if (endTime) {
                                        result += '&nbsp' + Uni.I18n.translate('general.untilDate.until', 'MDC', 'until {0}', [Uni.DateTime.formatDateTimeShort(new Date(endTime))], false);
                                    }
                                    result += '&nbsp' + versionsLink + '</span>';
                                }
                            }
                            return result || Uni.I18n.translate('usagePoint.generalAttributes.noActiveVersions', 'MDC', '(No active version) ') + versionsLink;
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});