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
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
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
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Save'),
                        ui: 'action',
                        action: 'saveFirmware',
                        itemId: 'createEditButton'
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
            formInfo;
        me.callParent(arguments);
        me.loadRecord(me.record.getActiveVersion());
        me.setTitle(me.record.get('type'));
        formInfo = me.down('#form-info');

        var version = me.record.getAssociatedData().activeVersion;
        if (version) {
            formInfo.show();
            formInfo.setText(Uni.I18n.translate('device.firmware.field.status.deprecated.tooltip', 'FWC', 'Firmware version {0} will be uploaded to device on {1}', [
                version.firmwareVersion,
                Uni.DateTime.formatDateTimeShort(version.plannedDate)
            ]));
        }
    }
});