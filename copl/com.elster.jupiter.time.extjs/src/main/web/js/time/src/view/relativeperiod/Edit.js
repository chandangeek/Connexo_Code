Ext.define('Tme.view.relativeperiod.Edit', {
    extend: 'Ext.container.Container',
    xtype: 'tme-relativeperiod-edit',
    overflowY: 'auto',

    requires: [
        'Uni.form.RelativePeriod',
        'Uni.form.RelativePeriodPreview',
        'Tme.store.RelativePeriodCategories',
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage'
    ],

    edit: false,
    returnLink: null,
    categoryStore: undefined,

    setEdit: function () {
        this.down('#cancel-link').href = this.returnLink;
    },

    initComponent: function () {
        var me = this;

        me.categoryStore = Ext.getStore('Tme.store.RelativePeriodCategories');

        me.items = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
                ui: 'large',
                margin: '0px 16px 16px 16px',
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'form',
                        itemId: 'edit-relative-period-form',
                        defaults: {
                            labelWidth: 160,
                            validateOnChange: false,
                            validateOnBlur: false,
                            anchor: '100%'
                        },
                        items: [
                            {
                                itemId: 'form-errors',
                                xtype: 'uni-form-error-message',
                                name: 'form-errors',
                                margin: '0 0 10 0',
                                hidden: true
                            },
                            {
                                xtype: 'textfield',
                                itemId: 'edit-relative-period-name',
                                name: 'name',
                                required: true,
                                msgTarget: 'under',
                                maxLength: 80,
                                enforceMaxLength: true,
                                allowBlank: false,
                                width: 600,
                                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name')
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'categories-combo-box',
                                width: 600,
                                name: 'category',
                                store: me.categoryStore,
                                editable: false,
                                multiSelect: true,
                                required: true,
                                allowBlank: false,
                                queryMode: 'local',
                                triggerAction: 'all',
                                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                                emptyText: Uni.I18n.translate('relativeperiod.form.selectcategory', 'TME', 'Select 1 or more categories'),
                                displayField: 'name',
                                valueField: 'id',
                                listConfig: {
                                    getInnerTpl: function () {
                                        return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + this.displayField + '}</div>';
                                    }
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.startdate', 'TME', 'Define the start of the relative period'),
                                margin: '8 0 12 0'
                            },
                            {
                                xtype: 'uni-form-relativeperiod',
                                itemId: 'relative-date-start',
                                startPeriodCfg: {
                                    fieldLabel: Uni.I18n.translate('general.start', 'TME', 'Start'),
                                    showOptionNow: false
                                },
                                defaults: {
                                    labelWidth: 160
                                },
                                formatPreviewTextFn: function (dateString) {
                                    return Uni.I18n.translate(
                                        'relativePeriod.form.start.previewText',
                                        'UNI',
                                        'The start date of the relative period is {0}.',
                                        [dateString]
                                    );
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.enddate', 'TME', 'Define the end of the relative period'),
                                margin: '8 0 12 0'
                            },
                            {
                                xtype: 'uni-form-relativeperiod',
                                itemId: 'relative-date-end',
                                startPeriodCfg: {
                                    fieldLabel: Uni.I18n.translate('general.end', 'TME', 'End'),
                                    showOptionDate: false
                                },
                                defaults: {
                                    labelWidth: 160
                                },
                                formatPreviewTextFn: function (dateString) {
                                    return Uni.I18n.translate(
                                        'relativePeriod.form.end.previewText',
                                        'UNI',
                                        'The end date of the relative period is {0}.',
                                        [dateString]
                                    );
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.preview', 'TME', 'Preview'),
                                margin: '8 0 12 0'
                            },
                            {
                                xtype: 'uni-form-relativeperiodpreview'
                            },
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'button',
                                        itemId: 'create-edit-button',
                                        text: Uni.I18n.translate('general.add', 'DXP', 'Add'),
                                        ui: 'action'
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'cancel-link',
                                        text: Uni.I18n.translate('general.cancel', 'DXP', 'Cancel'),
                                        ui: 'link',
                                        href: '#/administration/dataexporttasks/add'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.setEdit();
        me.on('afterrender', me.onAfterRender, me);
    },

    onAfterRender: function () {
        var me = this;

        me.getStartRelativePeriodField().on('periodchange', me.updatePreview, me);
        me.getEndRelativePeriodField().on('periodchange', me.updatePreview, me);
        me.updatePreview();
    },

    updatePreview: function () {
        var me = this;

        me.getRelativePeriodPreview().updateStartPeriodValue(me.getStartRelativePeriodField().getValue());
        me.getRelativePeriodPreview().updateEndPeriodValue(me.getEndRelativePeriodField().getValue());
        me.getRelativePeriodPreview().updatePreview();
    },

    getStartRelativePeriodField: function () {
        return this.down('uni-form-relativeperiod:first');
    },

    getEndRelativePeriodField: function () {
        return this.down('uni-form-relativeperiod:last');
    },

    getRelativePeriodPreview: function () {
        return this.down('uni-form-relativeperiodpreview');
    }
});
