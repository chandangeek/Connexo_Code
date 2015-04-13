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
        'Fwc.devicefirmware.view.ActionMenu'
    ],
    record: null,

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
            items: {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                hidden: true
            }
        }
    ],

    items: [
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('device.firmware.field.version', 'FWC', 'Firmware version'),
            name: 'firmwareVersion'
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('device.firmware.field.status', 'FWC', 'Firmware version status'),
            name: 'status'
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('device.firmware.field.date', 'FWC', 'Last checked date'),
            name: 'lastCheckedDate'
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.loadRecord(me.record.getActiveVersion());
        me.setTitle(me.record.get('type'));
    }
});