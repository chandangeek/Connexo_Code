Ext.define('Cfg.view.validation.CreateRuleSet', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.createRuleSet',
    itemId: 'createRuleSet',

    requires: [

    ],

    edit: false,

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditNewRuleSet').setText(Uni.I18n.translate('general.save', 'CFG', 'Save'));
            this.down('#createEditNewRuleSet').action = 'editNewRuleSet';
        } else {
            this.edit = edit;
            this.down('#createEditNewRuleSet').setText(Uni.I18n.translate('general.add', 'CFG', 'Add'));
            this.down('#createEditNewRuleSet').action = 'createNewRuleSet';
        }
        this.down('#cancelAddRuleSetLink').href = returnLink;
    },

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'editRuleSetTitle',
            items: [
                {
                    xtype: 'form',
                    itemId: 'newRuleSetForm',
                    width: '100%',
                    defaults: {
                        labelWidth: 250,
                        validateOnChange: false,
                        validateOnBlur: false
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            width: 400,
                            margin: '0 0 10 0',
                            hidden: true
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            itemId: 'addRuleSetName',
                            required: true,
                            allowBlank: false,
                            maxLength: 80,
                            width: 600,
                            msgTarget: 'under',
                            fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                            enforceMaxLength: true
                        },
                        {
                            xtype: 'textarea',
                            name: 'description',
                            itemId: 'addRuleSetDescription',
                            width: 600,
                            maxLength: 256,
                            height: 150,
                            fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                            enforceMaxLength: true
                        }
                    ],
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'createEditNewRuleSet',
                            itemId: 'createEditNewRuleSet'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelAddRuleSetLink',
                            href: '#/administration/validation'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.setEdit(this.edit, this.returnLink);
    }
});

