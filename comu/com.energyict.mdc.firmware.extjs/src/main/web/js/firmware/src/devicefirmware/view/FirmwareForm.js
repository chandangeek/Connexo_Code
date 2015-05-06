Ext.define('Fwc.devicefirmware.view.FirmwareForm', {
    extend: 'Ext.form.Panel',
    xtype: 'device-firmware-form',
    defaults: {
        labelWidth: 200
    },
    frame: true,
    minButtonWidth: 50,
    requires: [
        'Uni.util.FormErrorMessage',
        'Fwc.devicefirmware.view.ActionMenu',
        'Uni.form.field.DisplayFieldWithInfoIcon',
        'Uni.DateTime'
    ],
    record: null,
    hydrator: 'Fwc.form.Hydrator',
    header: {
        titlePosition: 0,
        layout: {
            type: 'vbox',
            align: 'right'
        },
        items: [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'FWC', Uni.I18n.translate('general.actions', 'FWC', 'Actions')),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'device-firmware-action-menu'
                }
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
                    hidden: true,
                    layout: {
                        type: 'hbox',
                        defaultMargins: '5 10 5 5'
                    },
                    buttonAlign: 'left',
                    buttons: [
                        {
                            margin: '0 0 0 46',
                            text: Uni.I18n.translate('device.firmware.failed.retry', 'MDC', 'Retry'),
                            ui: 'action',
                            action: 'retry',
                            hidden: true,
                            itemId: 'retryBtn'
                        },
                        {
                            margin: '0 0 0 46',
                            text: Uni.I18n.translate('device.firmware.failed.check', 'MDC', 'Check Version'),
                            ui: 'action',
                            action: 'check',
                            hidden: true,
                            itemId: 'checkBtn'
                        },
                        {
                            text: Uni.I18n.translate('device.firmware.failed.deviceEvents', 'MDC', 'View device events'),
                            ui: 'link',
                            action: 'viewDeviceEvents',
                            hidden: true,
                            itemId: 'deviceEventsBtn'
                        },
                        {
                            text: Uni.I18n.translate('device.firmware.failed.log', 'MDC', 'View log'),
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
                    defaultErrorIcon: 'x-message-box-warning',
                    hidden: true,
                    buttonAlign: 'left',
                    layout: {
                        type: 'hbox',
                        defaultMargins: '5 10 5 5'
                    },
                    buttons: [{
                        margin: '0 0 0 46',
                        text: Uni.I18n.translate('device.firmware.pending.cancel', 'MDC', 'Cancel upload'),
                        ui: 'action',
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
                    defaultErrorIcon: 'x-message-box-warning',
                    hidden: true,
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
            fieldLabel: Uni.I18n.translate('device.firmware.field.version', 'FWC', 'Firmware version'),
            name: 'firmwareVersion'
        },
        {
            xtype: 'displayfield-with-info-icon',
            fieldLabel: Uni.I18n.translate('device.firmware.field.status', 'FWC', 'Firmware version status'),
            beforeRenderer: function (value) {
                if (value && value.id === 'deprecated') {
                    this.infoTooltip = Uni.I18n.translate('device.firmware.field.status.deprecated.tooltip', 'FWC', 'Active firmware version is deprecated. Consider firmware upgrade');
                }
                return value.localizedValue || null;
            },
            name: 'firmwareVersionStatus'
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('device.firmware.field.date', 'FWC', 'Last checked date'),
            name: 'lastCheckedDate',
            renderer: function (data) {
                return data ? Uni.DateTime.formatDateTimeShort(data) : '';
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
        }

        if (status) {
            upgradeOption = record.getAssociatedData().firmwareUpgradeOption;
        }

        me.title = me.record.get('type');
        me.callParent(arguments);

        me.loadRecord(me.record.getActiveVersion() || new FirmwareVersion({firmwareVersion: Uni.I18n.translate('device.firmware.version.unknown', 'FWC', 'Unknown')}));

        formPending = me.down('#message-pending');
        formFailed = me.down('#message-failed');
        formOngoing = me.down('#message-ongoing');

        if (status === 'pendingVersion') {
            formPending.record = record;
            formPending.show();
            formPending.setText(Uni.I18n.translate(['device','firmware', upgradeOption.id, status].join('.'),
                'FWC', 'Upload and activation of version {0} pending (Planned on {1})', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedDate'))
                ]));
        }

        if (status === 'failedVersion' || status === 'failedVerificationVersion' || status === 'wrongVerificationVersion') {
            formFailed.record = record;
            formFailed.show();
            formFailed.setText(Uni.I18n.translate(['device','firmware', upgradeOption.id, status].join('.'),
                'FWC', 'Upload and activation of version {0} failed', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedDate'))
                ]));

            formFailed.down('#retryBtn').setVisible(status === 'failedVersion');
            formFailed.down('#checkBtn').setVisible(status === 'failedVerificationVersion');
            formFailed.down('#deviceEventsBtn').setVisible(status === 'wrongVerificationVersion');
            formFailed.down('#logBtn').setVisible(record.get('firmwareComTaskId') && record.get('firmwareComTaskSessionId'));
        }

        if (status === 'ongoingVersion') {
            formOngoing.record = record;
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate(['device','firmware', upgradeOption.id, status].join('.'),
                'FWC', 'Upload and activation of version {0} ongoing (upload started on {1})', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('uploadStartDate'))
                ]));
        }

        if (status === 'needVerificationVersion') {
            formOngoing.record = record;
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate(['device','firmware', upgradeOption.id, status].join('.'),
                'FWC', 'Upload and activation of version {0} completed on {1}. Verification scheduled on {2}', [
                    record.get('firmwareVersion'),
                    Uni.DateTime.formatDateTimeShort(record.get('plannedDate')),
                    Uni.DateTime.formatDateTimeShort(record.get('lastCheckedDate'))
                ]));
        }
    }
});