Ext.define('Fwc.view.firmware.FormAdd', {
    extend: 'Ext.form.Panel',
    xtype: 'firmware-form-add',
    itemId: 'firmwareForm',
    ui: 'large',
    defaults: {
        labelWidth: 150
    },
    requires: [
        'Fwc.view.firmware.field.File',
        'Fwc.view.firmware.field.FirmwareType'
    ],
    routeBack: null,
    record: null,

    items: [
        {
            xtype: 'firmware-field-file',
            anchor: '60%'
        },
        {
            xtype: 'textfield',
            name: 'version',
            anchor: '60%',
            required: true,
            fieldLabel: 'Version',
            allowBlank: false
        },
        {
            xtype: 'firmware-type',
            required: true
        },
        {
            xtype: 'radiogroup',
            fieldLabel: 'Status',
            required: true,
            columns: 1,
            vertical: true,
            items: [
                {
                    boxLabel: 'Final',
                    name: 'status',
                    inputValue: 'final',
                    id: 'final'
                }, {
                    boxLabel: 'Test',
                    name: 'status',
                    inputValue: 'test',
                    id: 'test'
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                ui: 'action',
                action: 'saveFirmware',
                itemId: 'createEditButton'
            },
            {
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                ui: 'link',
                itemId: 'cancelLink',
                href: me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl()
            }
        ];

        me.callParent(arguments);
        me.loadRecord(me.record);
    }
});