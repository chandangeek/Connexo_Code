Ext.define('Tme.view.relativeperiod.RelativePeriodEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.RelativePeriodEdit',


    requires: [
        'Tme.store.RelativePeriodEdit',
        'Tme.model.RelativePeriodEdit'
    ],
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'TME', 'Save'));
            this.down('#createEditButton').action = 'editRelativePeriod';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'TME', 'Add'));
            this.down('#createEditButton').action = 'createRelativePeriod';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: 'Add relative period',
                itemId: 'addRelativePeriod',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'form',
                        width: '50%',
                        defaults: {
                            labelWidth: 160,
                            validateOnChange: false,
                            validateOnBlur: false,
                            anchor: '100%'
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name')
                            },
                            {
                                xtype: 'combobox',
                                name: 'category',
                                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                                itemId: 'comTaskComboBox',
                                store: this.categoryStore,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                emptyText: Uni.I18n.translate('relativeperiod.form.selectcategory', 'TME', 'Select 1 or more categories'),
                                allowBlank: false,
                                forceSelection: true,
                                required: true,
                                editable: false,
                                msgTarget: 'under',
                                width: 600
                            },
                            // TODO define the start of the relative period
                            {
                                xtype: 'displayfield',
                                name: 'preview',
                                fieldLabel: Uni.I18n.translate('relativeperiod.preview', 'TME', 'Preview')
                            },
                            // TODO define the end of the relative period
                            {
                                xtype: 'displayfield',
                                name: 'preview',
                                fieldLabel: Uni.I18n.translate('relativeperiod.preview', 'TME', 'Preview')
                            },
                            {
                                //TODO preview
                            }
                        ]
                    }
                ]
            }
        ]
    }
});
