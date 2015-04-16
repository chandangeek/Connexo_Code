Ext.define('Fwc.devicefirmware.view.UploadForm', {
    extend: 'Ext.form.Panel',
    xtype: 'device-firmware-upload-form',
    itemId: 'device-firmware-upload-form',
    ui: 'large',
    defaults: {
        labelWidth: 150
    },
    minButtonWidth: 50,
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Fwc.devicefirmware.view.form.UploadFieldContainer'
    ],
    record: null,

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        border: true,
        items: {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',
            hidden: true
        }
    }],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'property-form',

                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            },
            {
                xtype: 'upload-field-container',
                groupName: 'uploadFileContainer',
                itemId: 'uploadFileField',
                fieldLabel: Uni.I18n.translate('deviceFirmware.uploadFile', 'FWC', 'Upload file')
            }
        ];

        me.buttons = [
            {
                text: Uni.I18n.translate('general.confirm', 'FWC', 'Confirm'),
                ui: 'action',
                action: 'uploadFirmware',
                itemId: 'uploadBtn'
            },
            {
                text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                ui: 'link',
                itemId: 'cancelLink',
                href: me.router.getRoute('devices/device/firmware').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});