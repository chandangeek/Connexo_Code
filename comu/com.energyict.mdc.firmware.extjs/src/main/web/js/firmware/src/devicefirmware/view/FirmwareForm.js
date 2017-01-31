/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.view.FirmwareForm', {
    extend: 'Ext.form.Panel',
    xtype: 'device-firmware-form',
    defaults: {
        labelWidth: 200
    },
    frame: true,
    minButtonWidth: 50,
    itemId : null,
    requires: [
        'Uni.util.FormErrorMessage',
        'Fwc.devicefirmware.view.ActionMenu',
        'Uni.form.field.DisplayFieldWithInfoIcon',
        'Uni.DateTime'
    ],
    record: null,
    device: null,
    hydrator: 'Fwc.form.Hydrator',
    header: {
        titlePosition: 0,
        layout: {
            type: 'vbox',
            align: 'right'
        },
        items: [
            {
                xtype: 'uni-button-action',
                menu: {
                    xtype: 'device-firmware-action-menu'
                },
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.firmwareManagementActions
            }
        ]
    },

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            border: true,
            layout: 'vbox',
            items: [
                {
                    xtype: 'uni-form-error-message',
                    margin: 0,
                    padding: 10,
                    itemId: 'message-failed',
                    htmlEncode: false,
                    hidden: true,
                    layout: {
                        type: 'hbox',
                        defaultMargins: '5 10 5 5'
                    },
                    buttonAlign: 'left',
                    buttons: [
                        {
                            margin: '0 0 0 46',
                            text: Uni.I18n.translate('device.firmware.failed.retry', 'FWC', 'Retry'),
                            ui: 'action',
                            action: 'retry',
                            hidden: true,
                            itemId: 'retryBtn'
                        },
                        {
                            margin: '0 0 0 46',
                            text: Uni.I18n.translate('device.firmware.failed.check', 'FWC', 'Check Version'),
                            ui: 'action',
                            action: 'check',
                            hidden: true,
                            itemId: 'checkBtn'
                        },
                        {
                            text: Uni.I18n.translate('device.firmware.failed.deviceEvents', 'FWC', 'View device events'),
                            ui: 'link',
                            action: 'viewDeviceEvents',
                            hidden: true,
                            itemId: 'deviceEventsBtn'
                        },
                        {
                            text: Uni.I18n.translate('device.firmware.failed.log', 'FWC', 'View log'),
                            ui: 'link',
                            action: 'viewLog',
                            hidden: true,
                            itemId: 'logBtn'
                        }
                    ]
                },
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'message-pending',
                    margin: 0,
                    padding: 10,
                    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
                    defaultErrorIcon: 'icon-info',
                    hidden: true,
                    buttonAlign: 'left',
                    htmlEncode: false,
                    layout: {
                        type: 'hbox',
                        defaultMargins: '5 10 5 5'
                    },
                    buttons: [{
                        margin: '0 0 0 46',
                        text: Uni.I18n.translate('device.firmware.pending.cancel', 'FWC', 'Cancel upload'),
                        action: 'cancelUpgrade',
                        itemId: 'cancelBtn'
                    }]
                },
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'message-ongoing',
                    margin: 0,
                    padding: 10,
                    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
                    defaultErrorIcon: 'icon-info',
                    hidden: true,
                    htmlEncode: false,
                    layout: {
                        type: 'hbox',
                        defaultMargins: '5 10 5 5'
                    }
                }
            ]
        }
    ],

    items: [
        {
            xtype: 'displayfield',
            itemId: 'firmware-version-field',
            fieldLabel: Uni.I18n.translate('device.firmware.field.version', 'FWC', 'Firmware version'),
            name: 'firmwareVersion',
            renderer: function (value, field) {
                var returnValue = value;
                if (Ext.isEmpty(value)) {
                    return '-';
                }
                if(!Ext.isEmpty(field.up().record.getActiveVersion())) {
                    returnValue += ' (';
                    returnValue += field.up().record.getActiveVersion().getFirmwareVersionStatus().get('localizedValue');
                    if(field.up().record.getActiveVersion().getFirmwareVersionStatus().get('id') === 'deprecated') {
                        returnValue +=  ' ' + '<span class="icon-warning" style="color: #EB5642; font-size:12px" data-qtip="' +
                            Uni.I18n.translate('device.firmware.field.status.deprecated.tooltip', 'FWC', 'Firmware version is deprecated. Consider uploading new firmware version.') +
                            '"></span>';
                    }
                    returnValue += ')';
                }
                return returnValue;
            }
        },
        {
            xtype: 'displayfield',
            itemId: 'last-checked-date-field',
            fieldLabel: Uni.I18n.translate('device.firmware.field.date', 'FWC', 'Last verified'),
            name: 'lastCheckedDate',
            renderer: function (data) {
                return data ? Uni.DateTime.formatDateTimeShort(data) : '-';
            }
        }
    ],

    initComponent: function () {
        var me = this,
            associatedData = me.record.getAssociatedData(),
            record, status,
            formPending,
            formFailed,
            formOngoing,
            upgradeOption,
            FirmwareVersion = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareVersion')
            ;

        switch (true) {
            case !!associatedData.pendingVersion:
                status = 'pendingVersion';
                record = me.record.getPendingVersion();
                break;
            case !!associatedData.ongoingVersion:
                status = 'ongoingVersion';
                record = me.record.getOngoingVersion();
                break;
            case !!associatedData.failedVersion:
                status = 'failedVersion';
                record = me.record.getFailedVersion();
                break;
            case !!associatedData.needVerificationVersion:
                status = 'needVerificationVersion';
                record = me.record.getVerificationVersion();
                break;
            case !!associatedData.wrongVerificationVersion:
                status = 'wrongVerificationVersion';
                record = me.record.getWrongVerificationVersion();
                break;
            case !!associatedData.failedVerificationVersion:
                status = 'failedVerificationVersion';
                record = me.record.getFailedVerificationVersion();
                break;
            case !!associatedData.needActivationVersion:
                status = 'needActivationVersion';
                record = me.record.getNeedActivationVersion();
                break;
            case !!associatedData.ongoingActivatingVersion:
                status = 'ongoingActivatingVersion';
                record = me.record.getOngoingActivatingVersion();
                break;
            case !!associatedData.failedActivatingVersion:
                status = 'failedActivatingVersion';
                record = me.record.getFailedActivatingVersion();
                break;
            case !!associatedData.activatingVersion:
                status = 'activatingVersion';
                record = me.record.getActivatingVersion();
                break;
            case !!associatedData.ongoingVerificationVersion:
                status = 'ongoingVerificationVersion';
                record = me.record.getOngoingVerificationVersion();
                break;
        }

        if (status) {
            upgradeOption = record.getAssociatedData().firmwareManagementOption;
        }

        me.title = me.record.get('type');
        me.callParent(arguments);
        var formId = me.record.get('type').replace(' ', '-').toLowerCase();
        me.itemId = formId;
        me.header.items[0].itemId = 'action-button-' + formId;
        me.loadRecord(me.record.getActiveVersion() || new FirmwareVersion({firmwareVersion: Uni.I18n.translate('device.firmware.version.unknown', 'FWC', 'Unknown')}));

        formPending = me.down('#message-pending');
        formFailed = me.down('#message-failed');
        formOngoing = me.down('#message-ongoing');

        if (status === 'pendingVersion') {
            formPending.record = record;
            formPending.show();
            formPending.setText(Uni.I18n.translate('device.firmware.activateOnDate.pendingVersion',
                'FWC', 'Upload and activation of version {0} pending (Upload planned on {1}, activation planned on {2})', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedDate')),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedActivationDate'))
                ],false));
        }

        if (status === 'failedVersion' || status === 'failedVerificationVersion' || status === 'wrongVerificationVersion' || status === 'failedActivatingVersion') {
            formFailed.record = record;
            formFailed.show();
            if (status === 'failedActivatingVersion') {
                formFailed.setText(Uni.I18n.translate('device.firmware.install.failedActivatingVersion',
                    'FWC', 'Activation to version {0} failed', [
                        record.get('firmwareVersion')
                    ]));
            } else {
                formFailed.setText(Uni.I18n.translate('device.firmware.activate.failedVersion',
                    'FWC', 'Upload and activation of version {0} failed', [
                        record.get('firmwareVersion'),
                        Uni.DateTime.formatDateTimeShort(record.get('plannedDate'))
                    ]));
            }

            formFailed.down('#retryBtn').setVisible(status === 'failedVersion' || status === 'failedActivatingVersion');
            formFailed.down('#checkBtn').setVisible(status === 'failedVerificationVersion');
            formFailed.down('#logBtn').setVisible(record.get('firmwareComTaskId') && record.get('firmwareComTaskSessionId'));
            if(status === 'wrongVerificationVersion'){
                formFailed.down('#deviceEventsBtn').setVisible(true);
                formFailed.down('#deviceEventsBtn').setDisabled(!me.device.get('hasLogBooks'));
            }
        }

        if (status === 'ongoingVersion') {
            formOngoing.record = record;
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate('device.firmware.activateOnDate.ongoingVersion',
                'FWC', 'Uploading version {0} (started on {1}), version will be activated on {2})', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedDate')),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedActivationDate'))
                ]));
        }

        if (status === 'needVerificationVersion') {
            formOngoing.record = record;
            formOngoing.show();
            if (record.get('lastCheckedDate')) {
                formOngoing.setText(Uni.I18n.translate('device.firmware.activate.needVerificationVersion',
                    'FWC', 'Upload and activation of version {0} completed on {1}. Verification scheduled on {2}', [
                        record.get('firmwareVersion'),
                        Uni.DateTime.formatDateTimeShort(record.get('plannedDate')),
                        Uni.DateTime.formatDateTimeShort(record.get('lastCheckedDate'))
                    ]));
            } else {
                formOngoing.setText(Uni.I18n.translate('device.firmware.activateOnDate.needVerificationVersion.notSheduled',
                    'FWC', 'Upload and activation of version {0} completed on {1}. Verification is not scheduled', [
                        record.get('firmwareVersion'),
                        Uni.DateTime.formatDateTimeShort(record.get('plannedDate'))
                    ]));
            }
        }

        if (status === 'needActivationVersion') {
            formPending.record = record;
            formPending.show();
            formPending.setText(Uni.I18n.translate('device.firmware.install.needActivationVersion',
                'FWC', 'Upload of version {0} completed on {1}. Version has not been activated yet', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedDate'))
                ]));
            formPending.down('#cancelBtn').setText(Uni.I18n.translate('general.activate', 'FWC', 'Activate'));
            formPending.down('#cancelBtn').action = 'activateVersion';
        }

        if (status === 'ongoingActivatingVersion') {
            formOngoing.record = record;
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate('device.firmware.install.ongoingActivatingVersion',
                'FWC', 'Activating version {0}', [
                    record.get('firmwareVersion')
                ]));
        }

        if (status === 'activatingVersion') {
            formOngoing.record = record;
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate('device.firmware.activateOnDate.activatingVersion',
                'FWC', 'Upload of version {0} completed on {1}. Activation scheduled on {2}', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('uploadStartDate')),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedActivationDate'))
                ]));
        }

        if (status === 'ongoingVerificationVersion') {
            formOngoing.record = record;
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate('device.firmware.activate.ongoingVerificationVersion',
                'FWC', 'Verifying upload and activation of version {0}', [
                    record.get('firmwareVersion')
                ]));
        }
    }
});