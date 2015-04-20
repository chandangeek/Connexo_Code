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
                    buttons: [{
                        margin: '0 0 0 46',
                        text: Uni.I18n.translate('device.firmware.failed.retry', 'MDC', 'Retry'),
                        ui: 'action',
                        action: 'retry',
                        itemId: 'retryBtn'
                    }, {
                        text: Uni.I18n.translate('device.firmware.failed.log', 'MDC', 'View log'),
                        ui: 'link',
                        action: 'viewLog',
                        itemId: 'logBtn'
                    }]
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
                return value.displayValue || null;
            },
            name: 'firmwareVersionStatus'
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('device.firmware.field.date', 'FWC', 'Last checked date'),
            name: 'lastCheckedDate'
        }
    ],

    initComponent: function () {
        var me = this,
            associatedData = me.record.getAssociatedData(),
            pendingVersion = associatedData.pendingVersion,
            ongoingVersion = associatedData.ongoingVersion,
            failedVersion = associatedData.failedVersion,
            formPending,
            formFailed,
            formOngoing,
            mclass = Ext.ModelManager.getModel('Fwc.devicefirmware.model.FirmwareVersion')
        ;

        me.callParent(arguments);

        me.loadRecord(me.record.getActiveVersion() || new mclass);
        me.setTitle(me.record.get('type'));
        formPending  = me.down('#message-pending');
        formFailed   = me.down('#message-failed');
        formOngoing  = me.down('#message-ongoing');

        if (pendingVersion) {
            formPending.show();
            formPending.setText(Uni.I18n.translate('device.firmware.deprecated.message', 'FWC', 'Firmware version {0} will be uploaded to device on {1}', [
                pendingVersion.firmwareVersion,
                Uni.DateTime.formatDateTimeShort(pendingVersion.plannedDate)
            ]));
        }

        if (failedVersion) {
            formFailed.show();
            formFailed.setText(Uni.I18n.translate('device.firmware.failed.message', 'FWC', 'Failed to upload version {0} to the device', [
                failedVersion.firmwareVersion
            ]));
        }

        if (ongoingVersion) {
            formOngoing.show();
            formOngoing.setText(Uni.I18n.translate('device.firmware.ongoing.message', 'FWC', 'Firmware version {0} has been uploaded to the device and will be activated on {1}', [
                ongoingVersion.firmwareVersion,
                Uni.DateTime.formatDateTimeShort(ongoingVersion.uploadStartDate)
            ]));
        }
    }
});