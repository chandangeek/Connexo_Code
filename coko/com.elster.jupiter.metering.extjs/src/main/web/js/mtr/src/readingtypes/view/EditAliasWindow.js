Ext.define('Mtr.readingtypes.view.EditAliasWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.edit-alias-window',
    itemId: 'edit-alias-window',
    height: 160,
    width: 450,
    layout: 'fit',
    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'aliasName',
                    msgTarget: 'under',
                    required: true,
                    margin: '16 0 0 0',
                    fieldLabel: Uni.I18n.translate('general.alias', 'MTR', 'Alias'),
                    itemId: 'edit-alias-window-textfield',
                    maxWidth: 400,
                    maxLength: 80,
                    enforceMaxLength: true
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    style: {
                        marginLeft: '116px'
                    },
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.save', 'MTR', 'Save'),
                            xtype: 'button',
                            ui: 'action',
                            itemId: 'edit-save-button'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MTR', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'edit-cancel-button',
                            handler: function(){
                                this.up('#edit-alias-window').destroy();
                            }
                        }
                    ]
                }
            ]
        }
    ]
});