Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Ext.form.Panel',
    xtype: 'firmware-form-edit',
    itemId: 'firmwareForm',
    ui: 'large',
    defaults: {
        labelWidth: 150
    },
    requires: [
        'Fwc.view.firmware.field.File'
    ],
    routeBack: null,
    record: null,

    items: [
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            anchor: '60%',
            required: true,
            fieldLabel: 'Version',
            allowBlank: false
        },
        {
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Firmware type',
            name: 'firmwareType'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Stauts',
            name: 'firmwareStatus'
        }
    ],

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                text: Uni.I18n.translate('general.save', 'FWC', 'Save'),
                ui: 'action',
                action: 'saveFirmware',
                itemId: 'createEditButton'
            },
            {
                text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                ui: 'link',
                itemId: 'cancelLink',
                href: me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl()
            }
        ];

        me.callParent(arguments);
        me.loadRecord(me.record);
    }
});