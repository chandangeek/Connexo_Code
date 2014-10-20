Ext.define('Tme.view.relativeperiod.Edit', {
    extend: 'Ext.panel.Panel',
    xtype: 'tme-relativeperiod-edit',

    requires: [
        'Uni.form.RelativePeriod'
    ],

    title: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
    ui: 'large',

    margin: '0px 16px 16px 16px',
    edit: false,

    layout: {
        type: 'vbox'
    },

    isEdit: function () {
        return this.edit;
    },

    categoryStore: undefined,

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'TME', 'Save'));
            this.down('#createEditButton').action = 'editRelativePeriod';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'TME', 'Add'));
            this.down('#createEditButton').action = 'addRelativePeriod';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;

        me.categoryStore = Ext.getStore('Tme.store.RelativePeriodCategories');

        me.items = [
            {
                xtype: 'form',
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
                        store: me.categoryStore,
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
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('relativeperiod.form.startdate', 'DXP', 'Define the start of the relative period')
                    },
                    {
                        xtype: 'uni-form-relativeperiod',
                        startPeriodCfg: {
                            showOptionNow: false
                        }
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('relativeperiod.form.enddate', 'DXP', 'Define the end of the relative period')
                    },
                    {
                        xtype: 'uni-form-relativeperiod',
                        startPeriodCfg: {
                            showOptionDate: false
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
