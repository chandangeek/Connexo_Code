Ext.define('Fwc.view.firmware.FormEditGhost', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit-ghost',
    hydrator: 'Fwc.form.Hydrator',
    edit: true,
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',
            margin: '0 0 10 0',
            anchor: '60%',
            hidden: true
        },
        {
            xtype: 'displayfield',
            itemId: 'displayFirmwareVersion',
            name: 'firmwareVersion',
            fieldLabel: Uni.I18n.translate('general.version', 'FWC', 'Version')
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            name: 'type'
        },
        {
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'firmware-status',
            defaultType: 'radiofield',
            value: {id: 'final'},
            required: true,
            msgTarget: 'under'
        },
        {
            xtype: 'textfield',
            itemId: 'firmwareStatus',
            name: 'firmwareStatus',
            hidden: true
        }
    ]
});