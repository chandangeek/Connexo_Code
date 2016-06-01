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
                        name: 'metrologyConfiguration',
                        itemId: 'fld-up-metrology-configuration',
                        fieldLabel: Uni.I18n.translate('usagePoint.generalAttributes.metrologyConfiguration', 'MDC', 'Metrology configuration'),
                        renderer: function (value) {
                            var result = '',
                                record = me.getRecord(),
                                startTime,
                                endTime,
                                versionsLink = '<a href="'
                                    + me.router.getRoute('usagepoints/usagepoint/history/metrologyconfiguration').buildUrl({usagePointId: record.get('mRID')})
                                    + '">Versions</a>';

                            if (value) {
                                startTime = value.start;
                                endTime = value.end;
                                result += value.name;
                                if (startTime) {
                                    result += '<br><span style="font-size: 90%">'
                                        + Uni.I18n.translate('general.fromDate.from', 'IMT', 'From {0}', [Uni.DateTime.formatDateTimeShort(new Date(startTime))], false);
                                    if (endTime) {
                                        result += '&nbsp' + Uni.I18n.translate('general.fromDate.untill', 'IMT', 'untill {0}', [Uni.DateTime.formatDateTimeShort(new Date(endTime))], false);
                                    }
                                    result += '&nbsp' + versionsLink + '</span>';
                                }
                            }

                            return result || Uni.I18n.translate('usagePoint.generalAttributes.noActiveVersions', 'MDC', '(No active version) ') + versionsLink;
                        }
                    },
                    {
                        name: 'meterActivation',
                        itemId: 'fld-up-device',
                        fieldLabel: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                        renderer: function (value) {
                            var result = '',
                                record = me.getRecord(),
                                canViewDevice,
                                startTime,
                                endTime,
                                versionsLink = '<a href="'
                                    + me.router.getRoute('usagepoints/usagepoint/history/meteractivation').buildUrl({usagePointId: record.get('mRID')})
                                    + '">Versions</a>';

                            if (value) {
                                canViewDevice = Mdc.privileges.Device.canView();
                                startTime = value.start;
                                endTime = value.end;
                                if (canViewDevice) {
                                    result += '<a href="'
                                        + me.router.getRoute('devices/device').buildUrl({mRID: value.meter.mRID})
                                        + '">';
                                }
                                result += value.meter.mRID;
                                if (canViewDevice) {
                                    result += '</a>';
                                }
                                if (startTime) {
                                    result += '<br><span style="font-size: 90%">'
                                        + Uni.I18n.translate('general.fromDate.from', 'IMT', 'From {0}', [Uni.DateTime.formatDateTimeShort(new Date(startTime))], false);
                                    if (endTime) {
                                        result += '&nbsp' + Uni.I18n.translate('general.untillDate.untill', 'IMT', 'untill {0}', [Uni.DateTime.formatDateTimeShort(new Date(endTime))], false);
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