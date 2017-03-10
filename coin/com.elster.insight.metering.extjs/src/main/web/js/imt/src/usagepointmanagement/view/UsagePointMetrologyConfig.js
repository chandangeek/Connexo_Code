/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.UsagePointMetrologyConfig', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usage-point-metrology-config',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 120
    },

    router: null,
    usagePoint: null,

    initComponent: function () {
        var me = this,
            metrologyConfiguration = me.usagePoint.get('metrologyConfiguration'),
            meterRolesStore = Ext.getStore('Imt.usagepointmanagement.store.MeterRoles');

        me.items = [
            {
                itemId: 'up-metrology-config-name',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                renderer: function (value) {
                    var result = '',
                        record = me.getRecord(),
                        canViewMetrologyConfig,
                        activationTime;

                    if (record) {
                        canViewMetrologyConfig = Imt.privileges.MetrologyConfig.canView();
                        activationTime = record.get('activationTime');
                        if (value) {
                            if (canViewMetrologyConfig) {
                                result += '<a href="'
                                    + me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: record.getId()})
                                    + '">';
                            }
                            result += Ext.htmlEncode(value);
                            if (canViewMetrologyConfig) {
                                result += '</a>';
                            }
                        }
                        if (activationTime) {
                            result += '<br><span style="font-size: 90%">'
                                + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(activationTime))], false)
                                + '</span>';
                        }
                    }

                    return result || '-';
                }
            },
            {
                itemId: 'up-metrology-config-empty',
                fieldLabel: ' ',
                htmlEncode: false,
                privileges: Imt.privileges.UsagePoint.canAdministrate()
                && me.usagePoint.get('state').stage === 'PRE_OPERATIONAL'
                && Ext.isEmpty(metrologyConfiguration),
                renderer: function () {
                    var url = me.router.getRoute('usagepoints/view/definemetrology').buildUrl({}, {fromLandingPage: true});
                    return Uni.I18n.translate('general.label.linkMetrologyConfiguration', 'IMT', '<a href="{0}">Link metrology configuration</a>', url);
                }
            },
            {
                itemId: 'up-metrology-config-purposes',
                xtype: 'fieldcontainer'
            },
            {
                itemId: 'up-metrology-config-meters',
                xtype: 'fieldcontainer'
            },
            {
                itemId: 'up-metrology-config-meters-empty',
                fieldLabel: ' ',
                privileges: me.usagePoint.get('state').stage === 'PRE_OPERATIONAL'
                && !Ext.isEmpty(metrologyConfiguration)
                && !Ext.isEmpty(metrologyConfiguration.meterRoles)
                && Imt.privileges.UsagePoint.canAdministrate(),
                htmlEncode: false,
                renderer: function () {
                    var url = me.router.getRoute('usagepoints/view/metrologyconfiguration/activatemeters').buildUrl({}, {fromLandingPage: true});
                    return Uni.I18n.translate('general.label.linkMeters', 'IMT', '<a href="{0}">Link meters</a>', url);
                }
            }
        ];

        me.bbar = [
            {
                itemId: 'up-metrology-config-more-details-link',
                ui: 'link',
                text: Uni.I18n.translate('general.metrologyConfiguration.manage', 'IMT', 'Manage metrology configuration'),
                href: me.router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl()
            }
        ];

        me.callParent(arguments);

        me.loadPurposes();
        me.setLoading();
        meterRolesStore.getProxy().extraParams = {usagePointId: me.usagePoint.get('name')};
        meterRolesStore.load({
            scope: me,
            callback: function (records) {
                me.addMeters(records);
                me.setLoading(false);
            }
        });
    },

    addMeters: function (meterRolesWithMeters) {
        var me = this,
            first = true,
            count = meterRolesWithMeters.length,
            metersContainer = me.down('#up-metrology-config-meters');

        if (count && count <= 2) {
            Ext.Array.each(meterRolesWithMeters, function (meterRoleWithMeter) {
                metersContainer.add({
                    xtype: 'displayfield',
                    labelWidth: 120,
                    fieldLabel: first ? Uni.I18n.translate('general.meters', 'IMT', 'Meters') : '&nbsp;',
                    renderer: function () {
                        var result = '',
                            data = meterRoleWithMeter.getData(),
                            link = data.url
                                ? '<a href="' + data.url + '" target="_blank">' + Ext.String.htmlEncode(data.meter) + '</a>'
                                : Ext.String.htmlEncode(data.meter),
                            activationTime = data.activationTime;
                        result += link;

                        if (activationTime) {
                            result += '<br><span style="font-size: 90%">'
                                + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(activationTime))], false)
                                + '</span>';
                        }

                        return result;
                    }
                });
                first = false;
            });
            // add WhatsGoingOn info to linked meters
            me.loadMeterActivations();
        } else if (count && count > 2) {
            metersContainer.add({
                xtype: 'displayfield',
                labelWidth: 120,
                fieldLabel: Uni.I18n.translate('general.label.countedMeters', 'IMT', '{0} meters', count),
                value: '-'
            });
        } else if (!count) {
            metersContainer.add({
                xtype: 'displayfield',
                labelWidth: 120,
                fieldLabel: Uni.I18n.translate('general.label.meters', 'IMT', 'Meters'),
                value: '-'
            });
        }
    },

    loadPurposes: function () {
        var me = this,
            purposesContainer = me.down('#up-metrology-config-purposes'),
            first = true,
            purposes;

        Ext.suspendLayouts();
        if (me.usagePoint.get('metrologyConfiguration')) {
            purposes = me.usagePoint.get('metrologyConfiguration').purposes
        }

        if (!Ext.isEmpty(purposes)) {
            Ext.Array.each(purposes, function (purpose) {
                if (purpose.active) {
                    purposesContainer.add({
                        xtype: 'displayfield',
                        labelWidth: 120,
                        margin: first ? 0 : '-13 0 0 0',
                        fieldLabel: first ? Uni.I18n.translate('general.label.activePurposes', 'IMT', 'Active purposes') : '&nbsp;',
                        value: purpose.name,
                        renderer: function (value) {
                            var icon = '&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon '
                                    + (purpose.status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle')
                                    + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                                    + purpose.status.name
                                    + '"></i>',
                                url = me.router.getRoute('usagepoints/view/purpose').buildUrl({purposeId: purpose.id}),
                                link = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                            return link + icon;
                        }
                    });
                    first = false;
                }
            });
        }
        if (first) {
            purposesContainer.add({
                xtype: 'displayfield',
                labelWidth: 120,
                fieldLabel: Uni.I18n.translate('general.label.activePurposes', 'IMT', 'Active purposes'),
                value: '-'
            })
        }
        Ext.resumeLayouts(true);
    },

    loadMeterActivations: function () {
        var me = this,
            first = true,
            metersContainer = me.down('#up-metrology-config-meters'),
            store = Ext.getStore('Imt.usagepointmanagement.store.MeterActivations'),
            usagePointId = me.usagePoint.get('name'),
            makeWGOTooltip = function (watsGoingOnMeterStatus) {
                var result = '',
                    first = true;
                if (watsGoingOnMeterStatus.openIssues) {
                    result += Uni.I18n.translate('general.label.openIssues', 'IMT', 'Open issues({0})', watsGoingOnMeterStatus.openIssues);
                    first = false;
                }
                if (watsGoingOnMeterStatus.ongoingProcesses) {
                    if (!first) {
                        result += '<br>';
                    }
                    first = false;
                    result += Uni.I18n.translate('general.label.ongoingProcesses', 'IMT', 'Ongoing processes({0})', watsGoingOnMeterStatus.ongoingProcesses);
                }
                if (watsGoingOnMeterStatus.ongoingServiceCalls) {
                    if (!first) {
                        result += '<br>';
                    }
                    first = false;
                    result += Uni.I18n.translate('general.label.ongoingServiceCalls', 'IMT', 'Ongoing service calls({0})', watsGoingOnMeterStatus.ongoingServiceCalls);
                }

                return {result: result, status: !first};

            };

        store.getProxy().setExtraParam('usagePointId', usagePointId);
        store.load(function () {
            Ext.suspendLayouts();
                // replace rendered meters with meters + icon
                metersContainer.removeAll();
                store.each(function (meterActivation) {
                    if (meterActivation.get('meter')) {
                        metersContainer.add({
                            xtype: 'displayfield',
                            labelWidth: 120,
                            fieldLabel: first ? Uni.I18n.translate('general.meters', 'IMT', 'Meters') : '&nbsp;',
                            value: meterActivation.get('meter'),
                            renderer: function (value) {
                                var result = '',
                                    gotConfig = makeWGOTooltip(value.watsGoingOnMeterStatus),
                                    tooltip = gotConfig.result,
                                    icon = '&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon '
                                        + 'icon-warning2'
                                        + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                                        + tooltip
                                        + '"></i>',
                                    link = value.url ? '<a href="' + value.url + '" target="_blank">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name),
                                    activationTime = meterActivation.get('meterRole').activationTime;
                                result += gotConfig.status ? link + icon : link;

                                if (activationTime) {
                                    result += '<br><span style="font-size: 90%">'
                                        + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(activationTime))], false)
                                        + '</span>';
                                }

                                return result;
                            }
                        });
                        first = false;
                    }
                });
            Ext.resumeLayouts(true);
            }
        )
    }
});