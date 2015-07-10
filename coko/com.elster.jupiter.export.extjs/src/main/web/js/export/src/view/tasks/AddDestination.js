Ext.define('Dxp.view.tasks.AddDestination', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-destination',
    edit: false,
    returnLink: null,
    router: null,
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-destination-button').setText(Uni.I18n.translate('general.save', 'DES', 'Save'));
            this.down('#add-destination-button').action = 'editDestination';
        } else {
            this.edit = edit;
            this.down('#add-destination-button').setText(Uni.I18n.translate('general.add', 'DES', 'Add'));
            this.down('#add-destination-button').action = 'addDestination';
        }
        if (this.returnLink) {
            this.down('#cancel-link').href = this.returnLink;
        }
    },
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add destination'),
                itemId: 'add-destination-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 500
                    },
                    {
                        xtype: 'textfield',
                        name: 'method',
                        itemId: 'destination-method',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('dataExport.medthod', 'DES', 'Method'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'fileName',
                        itemId: 'destination-file-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('dataExport.fileName', 'DES', 'File name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'fileExtension',
                        itemId: 'destination-file-extension',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.fileExtension', 'DES', 'File extension'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'fileLocation',
                        itemId: 'destination-file-location',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.fileLocation', 'DES', 'File location'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },


                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-destination-button',
                                text: Uni.I18n.translate('general.add', 'DES', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-add-destination-link',
                                text: Uni.I18n.translate('general.cancel', 'DES', 'Cancel'),
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit);
    },
});





