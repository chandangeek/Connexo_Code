/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Tou.view.ActionMenu'
    ],
    alias: 'widget.tou-campaigns-detail-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5,
        defaults: {
            xtype: 'displayfield',
            labelWidth: 250
        }
    },
    router: null,
    isPreview: false,
    stores: ['Tou.store.DeviceTypes'],
    initComponent: function () {
        var me = this;

        me.tools = [{
                xtype: 'uni-button-action',
                privileges: Tou.privileges.TouCampaign.administrate,
                itemId: 'tou-campaigns-detail-action-menu-button',
                menu: {
                    xtype: 'tou-campaigns-action-menu',
                    itemId: 'tou-campaigns-action-menu',
                    returnToCampaignOverview: !me.isPreview
                }
            }
        ];

        me.items = [{
                items: [{
                        itemId: 'name-field',
                        fieldLabel: Uni.I18n.translate('general.name', 'TOU', 'Name'),
                        name: 'name',
                        renderer: function (value, field) {
                            var result = '';
                            if (me.isPreview) {
                                result = value ? '<a href="' + me.router.getRoute('workspace/toucampaigns/toucampaign').buildUrl({
                                        touCampaignId: field.up('form').getRecord().getId()
                                    }) + '">' + Ext.String.htmlEncode(value) + '</a>' : '-';
                            } else {
                                result = Ext.String.htmlEncode(value) || '';
                            }

                            return result;
                        }
                    }, {
                        itemId: 'device-type-field',
                        fieldLabel: Uni.I18n.translate('general.deviceType', 'TOU', 'Device type'),
                        name: 'deviceType',
                        renderer: function (value) {
                            return value && value.id ? '<a href="' + me.router.getRoute('administration/devicetypes/view').buildUrl({
                                deviceTypeId: value.id
                            }) + '">' + Ext.String.htmlEncode(value.name) + '</a>' : '-'
                        }
                    }, {
                        itemId: 'activation-field',
                        xtype: 'displayfield',
                        name: 'activationStart',
                        fieldLabel: Uni.I18n.translate('general.timeBoundary', 'TOU', 'Time boundary'),
                        renderer: function (value) {
                            var res = '-';
                            if (value) {
                                var dateEndValue = this.up('tou-campaigns-detail-form').getRecord().data.activationEnd;
                                res = 'Between ' + Uni.DateTime.formatDateTimeShort(value) + ' and ' + (dateEndValue ? Uni.DateTime.formatDateTimeShort(dateEndValue) : '-');
                            }
                            return res;
                        }
                    }, {
                        itemId: 'update-type-field',
                        fieldLabel: Uni.I18n.translate('general.updateType', 'TOU', 'Update type'),
                        name: 'updateType',
                        renderer: function (value) {
                            var res = '-';
                            if (value) {
                                switch (value) {
                                case 'fullCalendar':
                                    res = 'Full calendar';
                                    break;
                                case 'specialDays':
                                    res = 'Only special days';
                                    break;
                                default:
                                    res = value;
                                    break;
                                }
                            }
                            return res;
                        }
                    }, {
                        itemId: 'calendar-field',
                        fieldLabel: Uni.I18n.translate('general.calendar', 'TOU', 'Calendar'),
                        name: 'calendar',
                        renderer: function (value) {
                            return value && value.name ? '<span>' + value.name + '</span>' : '-'
                        }
                    }, {
                        itemId: 'activation-date-field',
                        fieldLabel: Uni.I18n.translate('general.activateCalendar', 'TOU', 'Activate calendar'),
                        name: 'activationOption',
                        renderer: function (value) {
                            var res = '-';
                            if (value) {

                                switch (value) {
                                case 'immediately':
                                    res = 'Immediately';
                                    break;
                                case 'withoutActivation':
                                    res = 'Without Activation';
                                    break;
                                case 'onDate':
                                    var dateValue = this.up('tou-campaigns-detail-form').getRecord().data.activationDate;
                                    res = (!isNaN(dateValue) && parseInt(dateValue) == dateValue) ? Uni.DateTime.formatDateTimeShort(parseInt(dateValue)) : '-';
                                    break;
                                default:
                                    res = Ext.String.htmlEncode(value);
                                    break;
                                }
                            }
                            return res;
                        }
                    }, {
                        itemId: 'time-validation-field',
                        fieldLabel: Uni.I18n.translate('general.touTimeout', 'TOU', 'Timeout before validation'),
                        name: 'validationTimeout',
                        renderer: function (validationTimeout) {
                            var res = "-";
                            if (validationTimeout) {
                                var timeUnit = 'minutes';
                                validationTimeout = validationTimeout / 60;
                                if (validationTimeout % 60 === 0) {
                                    validationTimeout = validationTimeout / 60;
                                    timeUnit = 'hours';
                                    if (validationTimeout % 24 === 0) {
                                        validationTimeout = validationTimeout / 24;
                                        timeUnit = 'days';
                                        if (validationTimeout % 7 === 0) {
                                            validationTimeout = validationTimeout / 7;
                                            timeUnit = 'weeks';
                                        }
                                    }
                                }
                                res = Ext.String.format('{0} {1}', validationTimeout, timeUnit);
                            }
                            return res;
                        }
                    }
                ]
            }, {
                items: [{
                        itemId: 'status-field',
                        fieldLabel: Uni.I18n.translate('general.status', 'TOU', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    }, {
                        itemId: 'devices-field',
                        fieldLabel: Uni.I18n.translate('general.devices', 'TOU', 'Devices'),
                        name: 'devices',
                        renderer: function (value, field) {
                            var result = '';

                            if (!Ext.isArray(value)) {
                                return result;
                            }

                            field.addCls('tou-campaign-status');
                            Ext.Array.each(value, function (devicesStatus, index) {
                                var iconCls = '';

                                switch (devicesStatus.status) {
                                case 'Failed':
                                    iconCls = 'icon-cancel-circle';
                                    break;
                                case 'Successful':
                                    iconCls = 'icon-checkmark-circle';
                                    break;
                                case 'Ongoing':
                                    iconCls = 'icon-spinner3';
                                    break;
                                case 'Pending':
                                    iconCls = 'icon-forward2';
                                    break;
                                case 'Configuration Error':
                                    iconCls = 'icon-notification';
                                    break;
                                case 'Canceled':
                                    iconCls = 'icon-blocked';
                                    break;
                                }

                                if (index) {
                                    result += '<br>';
                                }

                                result += '<span class="' + iconCls + '" data-qtip="' + devicesStatus.status + '"></span>' + devicesStatus.quantity;
                            });
                            return result;
                        }
                    }, {
                        itemId: 'started-on-field',
                        fieldLabel: Uni.I18n.translate('general.startedOn', 'TOU', 'Started on'),
                        name: 'startedOn',
                        renderer: function (value) {
                            return value ? '<span>' + Uni.DateTime.formatDateTimeLong(value) + '</span>' : '-'
                        }
                    }, {
                        itemId: 'finished-on-field',
                        fieldLabel: Uni.I18n.translate('general.finishedOn', 'TOU', 'Finished on'),
                        name: 'finishedOn',
                        renderer: function (value) {
                            return value ? '<span>' + Uni.DateTime.formatDateTimeShort(value) + '</span>' : '-'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
