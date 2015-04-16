Ext.define('Fwc.devicefirmware.view.FirmwareForm', {
    extend: 'Ext.form.Panel',
    xtype: 'device-firmware-form',
    itemId: 'device-firmware-form',
    defaults: {
        labelWidth: 200
    },
    frame: true,
    border: true,
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
                    padding: 10,
                    itemId: 'form-error',
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
                    itemId: 'form-info',
                    padding: 10,
                    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
                    defaultErrorIcon: 'x-message-box-warning',
                    hidden: true,
                    buttonAlign: 'left',
                    margin: 0,
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
            formInfo,
            formError;

        me.callParent(arguments);
        me.loadRecord(me.record.getActiveVersion());
        me.setTitle(me.record.get('type'));
        formInfo    = me.down('#form-info');
        formError   = me.down('#form-error');

        var pendingVersion = me.record.getAssociatedData().activeVersion; //todo: replace on pendingVersion
        var failedVersion  = me.record.getAssociatedData().activeVersion; //todo: replace on failedVerion

        if (pendingVersion) {
            formInfo.show();
            formInfo.setText(Uni.I18n.translate('device.firmware.deprecated.message', 'FWC', 'Firmware version {0} will be uploaded to device on {1}', [
                pendingVersion.firmwareVersion,
                Uni.DateTime.formatDateTimeShort(pendingVersion.plannedDate)
            ]));
        }

        if (failedVersion) {
            formError.show();
            formError.setText(Uni.I18n.translate('device.firmware.failed.message', 'FWC', 'Failed to upload version {0} to the device', [
                pendingVersion.firmwareVersion
            ]));
        }
    }
});