Ext.define('Isu.view.creationrules.EditAction', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-creation-rules-edit-action',
    content: [
        {
            name: 'pageTitle',
            ui: 'large',
            items: [
                {
                    xtype: 'form',
                    width: '75%',
                    defaults: {
                        labelWidth: 150,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 700
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true
                        },
                        {
                            xtype: 'radiogroup',
                            itemId: 'phasesRadioGroup',
                            name: 'phasesRadioGroup',
                            fieldLabel: 'When to perform',
                            required: true,
                            columns: 1,
                            vertical: true
                        },
                        {
                            itemId: 'actionType',
                            xtype: 'combobox',
                            name: 'actionType',
                            fieldLabel: 'Action',
                            required: true,
                            store: 'Isu.store.CreationRuleActions',
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
                            defaults: {
                                labelWidth: 150,
                                margin: '0 0 10 0',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 700
                            }
                        }
                    ],
                    buttons: [
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