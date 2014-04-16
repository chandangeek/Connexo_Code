Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connectionMethodEdit',
    itemId: 'connectionMethodEdit',
    autoScroll: true,
    cls: 'content-container',
    edit: false,
    isEdit: function(){
        return this.edit
    },
    setEdit: function(edit,returnLink){
        if(edit){
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#addEditButton').action = 'editConnectionMethod';
        } else {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'addConnectionMethod';
        }
        this.down('#cancelLink').autoEl.href=returnLink;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'connectionMethodEditAddTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                items: [
                                    {
                                        xtype: 'form',
                                        border: false,
                                        itemId: 'connectionMethodEditForm',
                                        padding: '10 10 0 10',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        defaults:{
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                validator:function(text){
                                                    if(Ext.util.Format.trim(text).length==0)
                                                        return Uni.I18n.translate('deviceconfiguration.emptyName', 'MDC', 'The name of a device configuration can not be empty.')
                                                    else
                                                        return true;
                                                },
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceConfigurationNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: '&nbsp',
                                                //width: 430,
                                                layout: {
                                                    type: 'hbox',
                                                    align: 'stretch'
                                                },
                                                items: [
                                                    {
                                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                                        xtype: 'button',
                                                        action: 'addAction',
                                                        itemId: 'addEditButton'
//                                                        formBind: true
                                                    },
                                                    {
                                                        xtype: 'component',
                                                        padding: '3 0 0 10',
                                                        itemId: 'cancelLink',
                                                        autoEl: {
                                                            tag: 'a',
                                                            href: '#setup/devicetypes/',
                                                            html: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                                                        }
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }


                ]
            }
        ];
        this.callParent(arguments);
        if(this.isEdit()){
            this.down('#addEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#addEditButton').action = 'editConnectionMethod';
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'createConnectionMethod';
        }
        this.down('#cancelLink').autoEl.href=this.returnLink;

    }


});



