Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrologyConfigurationEdit',
    itemId: 'metrologyConfigurationEdit',

    edit: false,

    content: [
        {
            xtype: 'form',
            itemId: 'metrologyConfigurationEditForm',
            ui: 'large',
            width: '100%',
            title: Uni.I18n.translate('metrologyConfiguration.add.title', 'IMT', 'Add metrology configuration'),
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
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('metrologyConfiguration.formFieldLabel.name', 'IMT', 'Name'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },               
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'saveModel',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                            xtype: 'button',
//                            ui: 'link',
//                            itemId: 'cancelLink',
//                            href: '#',
                            ui: 'action',
                            action: 'cancelButton',
                            itemId: 'cancelButton'
                        }
                    ]
                }
            ]
        }
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('form').setTitle(Uni.I18n.translate('metrologyConfiguration.edit.title', 'IMT', 'Edit metrology configuration'));
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'IMT', 'Save'));
            this.down('textfield[name="name"]').setDisabled(false);
        } else {
            this.edit = edit;
            this.down('form').setTitle(Uni.I18n.translate('metrologyConfiguration.add.title', 'IMT', 'Add metrology configuration'));
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'IMT', 'Add'));
        }
    }
});
