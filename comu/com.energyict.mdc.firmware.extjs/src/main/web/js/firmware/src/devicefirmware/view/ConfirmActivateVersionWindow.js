Ext.define('Fwc.devicefirmware.view.ConfirmActivateVersionWindow', {
    extend: 'Ext.window.Window',
    requires: [
        'Fwc.devicefirmware.view.form.UploadFieldContainer'
    ],
    alias: 'widget.confirm-activate-version-window',
    modal: true,
    versionName: '',
    activateHandler: undefined,
    initComponent: function () {
        var me = this;
        me.title = Uni.I18n.translate('deviceFirmware.activateVersion', 'FWC', 'Activate version \'{0}\'', [me.versionName]);
        me.items = {
            xtype: 'form',
                itemId: 'confirm-activate-version-form',
                ui: 'medium',
                padding: 0,
                items: [
                {
                    xtype: 'upload-field-container',
                    itemId: 'release-date-field-container',
                    fieldLabel: Uni.I18n.translate('general.activate', 'FWC', 'Activate'),
                    required: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: ' ',
                    margin: '30 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'confirm-activate-version',
                            text: Uni.I18n.translate('general.activate', 'FWC', 'Activate'),
                            ui: 'action',
                            handler: me.activateHandler
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-activate-version',
                            text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                this.up('window').close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});
