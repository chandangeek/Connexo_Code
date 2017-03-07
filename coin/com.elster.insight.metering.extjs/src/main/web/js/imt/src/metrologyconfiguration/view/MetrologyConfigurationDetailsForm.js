/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.SearchCriteriaDisplay'
    ],
    alias: 'widget.metrology-config-details-form',
    router: null,
    displayPurposes: true,

    initComponent: function () {
        var me = this,
            defaults = {
                xtype: 'displayfield',
                labelWidth: 200
            },
            mainInfo = [
                {
                    name: 'name',
                    itemId: 'fld-mc-name',
                    fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
                },
                {
                    name: 'description',
                    itemId: 'fld-mc-description',
                    fieldLabel: Uni.I18n.translate('general.label.description', 'IMT', 'Description')
                },
                {
                    name: 'status',
                    itemId: 'fld-mc-status',
                    fieldLabel: Uni.I18n.translate('general.label.status', 'IMT', 'Status'),
                    renderer: function (value) {
                        return value ? value.name : '';
                    }
                },
                {
                    xtype: 'displayfield',
                    name: 'isGapAllowed',
                    itemId: 'fld-mc-isGapAllowed',
                    fieldLabel: Uni.I18n.translate('general.label.isGapAllowed', 'IMT', 'Allow gaps'),
                    renderer: function (value) {
                        var isAllowGaps = 'None';

                        if (value !== null) {
                            isAllowGaps = value ? 'Yes' : 'No';
                        }

                        return Ext.String.format(
                            '<span style="display: inline-block; float: left; margin: 0px 10px 0px 0px">' + isAllowGaps + '</span>' +
                            '<span style="display: inline-block; float: left; width: 16px; height: 16px;" class="uni-icon-info-small" data-qtip="{0}"></span>',
                            Uni.I18n.translate('metrologyconfiguration.tooltip.isGapAllowed', 'IMT', 'Indicates whether gaps between meters on a meter role and between metrology configuration start/stop date and meter activations are allowed on usage points.')
                        );
                    }
                },
                {
                    xtype: 'search-criteria-display',
                    name: 'usagePointRequirements',
                    itemId: 'area-mc-requirements',
                    fieldLabel: Uni.I18n.translate('general.label.usagePointRequirements', 'IMT', 'Usage point requirements'),
                    labelAlign: 'top',
                    defaults: defaults,
                    style: 'margin-left: 5px'
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'area-mc-meter-spec',
                    fieldLabel: Uni.I18n.translate('general.label.metersSpecifications', 'IMT', 'Meters specifications'),
                    labelAlign: 'top',
                    defaults: defaults,
                    style: 'margin-left: 5px',
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
                            name: 'meterRoles',
                            renderer: function (value) {
                                var result = '';

                                Ext.Array.each(value, function (role, index) {
                                    if (index) {
                                        result += '<br>';
                                    }
                                    if (Ext.isObject(role)) {
                                        result += role.name;
                                    }
                                });

                                return result || '-';
                            }
                        }
                    ]
                }
            ];

        if (me.displayPurposes) {
            me.layout = 'column';
            me.defaults = {
                xtype: 'container',
                columnWidth: 0.5,
                defaults: defaults
            };
            me.items = [
                {
                    items: mainInfo
                },
                {
                    items: [
                        {
                            name: 'purposes',
                            itemId: 'fld-mc-purposes',
                            fieldLabel: Uni.I18n.translate('general.label.purposes', 'IMT', 'Purposes'),
                            renderer: function (value) {
                                var result = '';

                                Ext.Array.each(value, function (role, index) {
                                    //var url;

                                    if (index) {
                                        result += '<br>';
                                    }
                                    if (Ext.isObject(role)) {
                                        //will be implemented later
                                        //url = me.router.getRoute().buildUrl();
                                        //result += '<a href="' + url + '"' + role.name + '</a>';
                                        result += role.name;
                                    }
                                });

                                return result || '-';
                            }
                        }
                    ]
                }
            ];
        } else {
            me.defaults = defaults;
            me.items = mainInfo;
        }
        me.callParent();
    }
});