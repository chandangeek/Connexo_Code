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

    loadRecord: function(record) {
        var me = this;
        var activationOption = record.get('activationOption');
        var showValidation = activationOption === 'immediately' || activationOption === 'onDate';
        var manuallyCancelled = record.get('manuallyCancelled');

        me.callParent(arguments);

        me.down('[name="validationComTask"]').setVisible(showValidation);
        me.down('[name="validationConnectionStrategy"]').setVisible(showValidation);
        me.down('#tou-campaigns-detail-action-menu-button') && me.down('#tou-campaigns-detail-action-menu-button').setVisible(manuallyCancelled);
    },

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
                            }) + '/timeofuse">' + Ext.String.htmlEncode(value.name) + '</a>' : '-'
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
                                res = 'Between ' + Uni.DateTime.formatDateTimeShort(value/1000) + ' and ' + (dateEndValue ? Uni.DateTime.formatDateTimeShort(dateEndValue/1000) : '-');
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
                                    res = 'Send without activation';
                                    break;
                                case 'onDate':
                                    var dateValue = this.up('tou-campaigns-detail-form').getRecord().data.activationDate;
                                    res = (!isNaN(dateValue) && parseInt(dateValue) == dateValue) ? 'On ' + Uni.DateTime.formatDateTimeLong(parseInt(dateValue)) : '-';
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
                    }, {
                        itemId: 'firmware-service-call-field',
                        fieldLabel: Uni.I18n.translate('general.firmwareServiceCall', 'FWC', 'Service call'),
                        name: 'serviceCall',
                        renderer: function (value) {
                            return value ?  '<a href="' + me.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id})+ '">' + Ext.String.htmlEncode(value.name) + '</a>' : '-'
                        }
                    }, {
                        itemId: 'unique-calendar-name-field',
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate(
                            'general.uniqueCalendarName',
                            'TOU',
                            'Upload with unique calendar name'
                        ),
                        renderer: function (item) {
                            return item
                                ? Uni.I18n.translate('general.yes', 'TOU', 'Yes')
                                : Uni.I18n.translate('general.no', 'TOU', 'No');
                        },
                        name: 'withUniqueCalendarName',
                    }, {
                        itemId: 'tou-campaign-allowed-comtask',
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate(
                            'general.sendCalendarComTask',
                            'TOU',
                            'Calendar upload communication task'
                        ),
                        name: 'sendCalendarComTask',
                        renderer: function (item) {
                            if (!item) {
                                return '-';
                            }

                            return item.name;
                        },
                    }, {
                        itemId: 'tou-campaign-send-connection-strategy',
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate(
                            'general.connectionMethodStrategy',
                            'TOU',
                            'Connection method strategy'
                        ),
                        name: 'sendCalendarConnectionStrategy',
                        renderer: function (item) {
                            if (!item) {
                                return '-';
                            }

                            return item.name;
                        },
                    }, {
                        itemId: 'tou-campaign-validation-comtask',
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate(
                            'general.validationComTask',
                            'TOU',
                            'Validation communication task'
                        ),
                        hidden: true,
                        name: 'validationComTask',
                        renderer: function (item) {
                            if (!item) {
                                return '-';
                            }

                            return item.name;
                        },
                    }, {
                        itemId: 'tou-campaign-validation-connection-strategy',
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate(
                            'general.connectionMethodStrategy',
                            'TOU',
                            'Connection method strategy'
                        ),
                        hidden: true,
                        name: 'validationConnectionStrategy',
                        renderer: function (item) {
                            if (!item) {
                                return '-';
                            }

                            return item.name;
                        },
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
                                case 'Configuration error':
                                    iconCls = 'icon-notification';
                                    break;
                                case 'Cancelled':
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
                    },
                    {
                        itemId: 'manuallyCancelled-field',
                        fieldLabel: Uni.I18n.translate('general.manuallyCancelled', 'TOU', 'Manually cancelled'),
                        name: 'manuallyCancelled',
                        renderer: function (value) {
                            return value ? 'Yes' : 'No'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
