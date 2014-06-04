Ext.define('Isu.view.administration.datacollection.issuecreationrules.EditAction', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [

    ],
    alias: 'widget.issues-creation-rules-edit-action',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    xtype: 'panel',
                    name: 'pageTitle',
                    margin: '0 0 40 32',
                    ui: 'large',
                    title: 'Add action'
                },
                {
                    xtype: 'form',
                    width: '75%',
                    bodyPadding: '0 30 0 0',
                    defaults: {
                        labelWidth: 150,
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true,
                            margin: '0 0 20 50'
                        },
                        {
                            xtype: 'radiogroup',
                            itemId: 'phasesRadioGroup',
                            name: 'phasesRadioGroup',
                            fieldLabel: 'When to perform',
                            labelSeparator: ' *',
                            columns: 1,
                            vertical: true
                        },
                        {
                            itemId: 'actionType',
                            xtype: 'combobox',
                            name: 'actionType',
                            fieldLabel: 'Action',
                            labelSeparator: ' *',
                            store: 'Isu.store.Actions',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            allowBlank: false,
                            editable: false
                        },
                        {
                            itemId: 'actionTypeDetails',
                            xtype: 'container',
                            name: 'actionTypeDetails',
                            layout: 'fit',
                            margin: 0,
                            anchor: '100%',
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 20 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                anchor: '100%'
                            }
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: 'hbox',
                    defaultType: 'button',
                    margin: '20 165',
                    items: [
                        {
                            itemId: 'actionOperation',
                            name: 'actionOperation',
                            ui: 'action',
                            formBind: false,
                            action: 'actionOperation'
                        },
                        {
                            itemId: 'cancel',
                            text: 'Cancel',
                            action: 'cancel',
                            ui: 'link',
                            name: 'cancel'
                        }
                    ]
                }
            ]
        }
    ]
});