/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.cumulative.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-cumulative-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer'
        }
    ],
    register: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                dataIndex: 'measurementPeriod',
                flex: 1,
                renderer: function (value) {
                    if(!Ext.isEmpty(value)) {
                        var endDate = new Date(value.end);
                        if (!!value.start && !!value.end) {
                            var startDate = new Date(value.start);
                            return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                        } else {
                            return Uni.DateTime.formatDateTimeShort(endDate);
                        }
                    }
                    return '-';
                }
            },
            {
                header: Uni.I18n.translate('general.deltaValue', 'MDC', 'Delta value'),
                dataIndex: 'deltaValue',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right'
            },
            {
                dataIndex: 'validationResult',
                flex: 1,
                renderer: function (value) {
                    var validationMap = {
                        NOT_VALIDATED: '<span class="icon-flag6" style="margin-left: -15px; line-height: 12px" data-qtip="' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + '"></span>',
                        SUSPECT: '<span class="icon-flag5" style="margin-left: -15px; color:red; line-height: 12px" data-qtip="' + Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + '"></span>'
                    };

                    return validationMap[value];
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} readings'),
                displayMoreMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} readings'),
                emptyMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addReading', 'MDC', 'Add reading'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        href: '#/devices/' + encodeURIComponent(me.deviceId) + '/registers/' + me.registerId + '/data/add',
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                deferLoading: true,
                params: [
                    {deviceId: me.deviceId},
                    {registerId: me.registerId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('device.registerData.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Readings per page')
            }
        ];

        me.callParent(arguments);
    }
});