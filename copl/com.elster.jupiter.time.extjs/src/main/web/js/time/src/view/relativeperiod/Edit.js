/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'tme-relativeperiod-edit',

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
    firstDayOfWeekUrl: '/api/tmr/relativeperiods/weekstarts',

    initComponent: function () {
        var me = this;

        me.categoryStore = Ext.getStore('Tme.store.RelativePeriodCategories');

        me.content = [
            {
                xtype: 'panel',
                title: Ext.isEmpty(me.record) ? Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period') : Uni.I18n.translate('relativeperiod.editX', 'TME', "Edit '{0}'", me.record.get('name')),
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
                                width: 600,
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
                                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name'),
                                listeners: {
                                    afterrender: function (field) {
                                        field.focus(false, 200);
                                    }
                                }
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
                                queryMode: 'local',
                                triggerAction: 'all',
                                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                                emptyText: Uni.I18n.translate('relativeperiod.form.selectCategory', 'TME', 'Select one or more categories...'),
                                displayField: 'name',
                                valueField: 'id',
                                listConfig: {
                                    getInnerTpl: function () {
                                        return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox"  style="  top: 2px; left: -2px; position: relative;"/> {' + this.displayField + '}</div>';
                                    }
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.startdate', 'TME', 'Define the start of the relative period'),
                                margin: '20 0 12 0'
                            },
                            {
                                xtype: 'uni-form-relativeperiod',
                                suspend: !Ext.isEmpty(me.record),
                                itemId: 'relative-date-start',
                                startPeriodCfg: {
                                    fieldLabel: Uni.I18n.translate('general.start', 'TME', 'Start'),
                                    showOptionNow: false,
                                    name: 'from'
                                },
                                defaults: {
                                    labelWidth: 160
                                },
                                formatPreviewTextFn: function (dateString) {
                                    return Uni.I18n.translate(
                                        'relativePeriod.form.start.previewText',
                                        'TME',
                                        'The start date of the relative period is {0}.',
                                        [dateString]
                                    );
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.enddate', 'TME', 'Define the end of the relative period'),
                                margin: '25 0 12 0',
                                ui: 'medium'
                            },
                            {
                                xtype: 'uni-form-relativeperiod',
                                itemId: 'relative-date-end',
                                suspend: !Ext.isEmpty(me.record),
                                startPeriodCfg: {
                                    fieldLabel: Uni.I18n.translate('general.end', 'TME', 'End'),
                                    showOptionDate: false,
                                    errorId: 'to'
                                },
                                defaults: {
                                    labelWidth: 160
                                },
                                formatPreviewTextFn: function (dateString) {
                                    return Uni.I18n.translate(
                                        'relativePeriod.form.end.previewText',
                                        'TME',
                                        'The end date of the relative period is {0}.',
                                        [dateString]
                                    );
                                }
                            },
                            {
                                xtype: 'label',
                                text: Uni.I18n.translate('relativeperiod.form.preview', 'TME', 'Preview'),
                                ui: 'medium',
                                margin: '25 0 0 0'
                            },
                            {
                                xtype: 'fieldcontainer',
                                margin: '0 0 0 0',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'uni-form-relativeperiodpreview'
                                    }
                                ]
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
                                        text: Ext.isEmpty(me.record) ? Uni.I18n.translate('general.add', 'TME', 'Add') : Uni.I18n.translate('general.save', 'TME', 'Save'),
                                        action: Ext.isEmpty(me.record) ? 'addPeriod' : 'editPeriod',
                                        ui: 'action'
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'cancel-link',
                                        text: Uni.I18n.translate('general.cancel', 'TME', 'Cancel'),
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

    setEdit: function () {
        this.down('#cancel-link').href = this.returnLink;
    },

    onAfterRender: function () {
        var me = this;
        me.getFirstDayOfWeek();
    },

    continueAfterRender: function () {
        var me = this;
        if (!Ext.isEmpty(me.record)) {
            me.setValues(me.record);
        }
        me.getStartRelativePeriodField().on('periodchange', me.updatePreview, me);
        me.getEndRelativePeriodField().on('periodchange', me.updatePreview, me);
        me.updatePreview();
    },

    setValues: function (record) {
        var me = this,
            categories = [];
        me.down('#edit-relative-period-name').setValue(record.get('name'));
        record.get('categories').forEach(function (category) {
            categories.push(category.id);
        });
        me.down('#categories-combo-box').setValue(categories);
        me.down('form').record = record;
        me.down('#relative-date-end').setValues(record.get('to'));
        me.down('#relative-date-start').setValues(record.get('from'));
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
    },

    getFirstDayOfWeek: function () {
        var me = this;
        Ext.Ajax.request({
            url: me.firstDayOfWeekUrl,
            method: 'GET',
            success: function (response, data) {
                var dayNumber = Ext.decode(response.responseText, true);

                me.getStartRelativePeriodField().down('uni-form-field-onperiod #option-dow-combo').setValue(dayNumber);
                me.getEndRelativePeriodField().down('uni-form-field-onperiod #option-dow-combo').setValue(dayNumber);
                var intervalsComboStart = me.getStartRelativePeriodField().down('uni-form-field-startperiod #period-interval');
                var intervalsComboEnd = me.getEndRelativePeriodField().down('uni-form-field-startperiod #period-interval');
                var intervalsRecordStart = intervalsComboStart.findRecord(intervalsComboStart.valueField, 'weeks');
                var intervalsRecordEnd = intervalsComboEnd.findRecord(intervalsComboEnd.valueField, 'weeks');
                var startsOn = Uni.I18n.translate('period.weeks.startsOn', 'TME', 'week(s) (starts on') + ' ';
                switch (dayNumber) {
                    case 1:
                        startsOn = startsOn + Uni.I18n.translate('general.day.monday', 'TME', 'Monday') + ')';
                        break;
                    case 2:
                        startsOn = startsOn + Uni.I18n.translate('general.day.tuesday', 'TME', 'Tuesday') + ')';
                        break;
                    case 3:
                        startsOn = startsOn + Uni.I18n.translate('general.day.wednesday', 'TME', 'Wednesday') + ')';
                        break;
                    case 4:
                        startsOn = startsOn + Uni.I18n.translate('general.day.thursday', 'TME', 'Thursday') + ')';
                        break;
                    case 5:
                        startsOn = startsOn + Uni.I18n.translate('general.day.friday', 'TME', 'Friday') + ')';
                        break;
                    case 6:
                        startsOn = startsOn + Uni.I18n.translate('general.day.saturday', 'TME', 'Saturday') + ')';
                        break;
                    case 7:
                        startsOn = startsOn + Uni.I18n.translate('general.day.sunday', 'TME', 'Sunday') + ')';
                        break;
                }
                intervalsRecordStart.set('name', startsOn);
                intervalsRecordEnd.set('name', startsOn);
            },
            failure: function (response) {
                // Already caught be the default value of the date string.
            },
            callback: function () {
                me.continueAfterRender();
            }
        });
    }
});
