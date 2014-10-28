Ext.define('Tme.controller.RelativePeriods', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Tme.view.relativeperiod.Edit'
    ],

    stores: [
        'Tme.store.RelativePeriods',
        'Tme.store.RelativePeriodCategories'
    ],

    models: [
        'Tme.model.RelativePeriod',
        'Tme.model.Categories',
        'Tme.model.RelativeDate'
    ],

    refs: [
        {ref: 'page', selector: 'tme-relativeperiod-edit'},
        {ref: 'categoriesTextFields', selector: 'tme-relativeperiod-edit #categories-combo-box'}
    ],

    init: function () {
        this.control({
            'tme-relativeperiod-edit #createEditButton': {
                click: this.createEditPeriod
            }

        });
    },

    createEditPeriod: function (button) {
        var me = this,
            form = me.getPage().down('#edit-relative-period-form'),
            arrCategories = [],
            nameFieldValue = form.down('#edit-relative-period-name').getValue();


        var record = Ext.create('Tme.model.RelativePeriod');
        var categories = this.getCategoriesTextFields();


        record.set('name', nameFieldValue);
        Ext.Array.each(categories.getValue(), function(item) {
            record.categories().add(Ext.create(Tme.model.Categories, {id: item}));
        });
        record.set('categories', arrCategories);

        var relativeDateStartStart = form.down('#relative-date-start uni-form-field-startperiod').getValue();
        var relativeDateStartOn = form.down('#relative-date-start uni-form-field-onperiod').getValue();
        var relativeDateStartAt = form.down('#relative-date-start uni-form-field-atperiod').getValue();

        var relativeDateStart = Ext.create(Tme.model.RelativeDate);

        relativeDateStart.set('startPeriodAgo', relativeDateStartStart.startPeriodAgo);
        relativeDateStart.set('startAmountAgo', relativeDateStartStart.startAmountAgo);
        relativeDateStart.set('startNow', relativeDateStartStart.startNow);
        relativeDateStart.set('startFixedDay', relativeDateStartStart.startFixedDay);
        relativeDateStart.set('startFixedMonth', relativeDateStartStart.startFixedMonth);
        relativeDateStart.set('startFixedYear', relativeDateStartStart.startFixedYear);
        relativeDateStart.set('onCurrentDay', relativeDateStartOn.onCurrentDay);
        relativeDateStart.set('onDayOfMonth', relativeDateStartOn.onDayOfMonth);
        relativeDateStart.set('onDayOfWeek', relativeDateStartOn.onDayOfWeek);
        relativeDateStart.set('atHour', relativeDateStartAt.atHour);
        relativeDateStart.set('atMinute', relativeDateStartAt.atMinute);

        record.set('from', relativeDateStart.data);

        var relativeDateEnd = Ext.create(Tme.model.RelativeDate);

        var relativeDateEndEnd = form.down('#relative-date-end uni-form-field-startperiod').getValue();
        var relativeDateEndOn = form.down('#relative-date-end uni-form-field-onperiod').getValue();
        var relativeDateEndAt = form.down('#relative-date-end uni-form-field-atperiod').getValue();

        relativeDateEnd.set('startPeriodAgo', relativeDateEndEnd.startPeriodAgo);
        relativeDateEnd.set('startAmountAgo', relativeDateEndEnd.startAmountAgo);
        relativeDateEnd.set('startNow', relativeDateEndEnd.startNow);
        relativeDateEnd.set('startFixedDay', relativeDateEndEnd.startFixedDay);
        relativeDateEnd.set('startFixedMonth', relativeDateEndEnd.startFixedMonth);
        relativeDateEnd.set('startFixedYear', relativeDateEndEnd.startFixedYear);
        relativeDateEnd.set('onCurrentDay', relativeDateEndOn.onCurrentDay);
        relativeDateEnd.set('onDayOfMonth', relativeDateEndOn.onDayOfMonth);
        relativeDateEnd.set('onDayOfWeek', relativeDateEndOn.onDayOfWeek);
        relativeDateEnd.set('atHour', relativeDateEndAt.atHour);
        relativeDateEnd.set('atMinute', relativeDateEndAt.atMinute);

        record.set('to', relativeDateEnd.data);

        record.save({
/*            params: {
                id: ruleSetId
            },*/
            success: function (record, operation) {
                var messageText;
                if (button.action === 'editRuleAction') {
                    messageText = Uni.I18n.translate('relativeperiod.editSuccess.msg', 'TME', 'Relative period saved');
                } else {
                    messageText = Uni.I18n.translate('relativeperiod.addSuccess.msg', 'TME', 'Relative period added');
                }
/*                if (me.fromRulePreview) {
                    location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/' + me.ruleId;
                } else {
                    location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/rules';
                }*/
                me.getApplication().fireEvent('acknowledge', messageText);
            },
            failure: function (record, operation) {

                var json = Ext.decode(operation.response.responseText, true);
                // TODO
/*                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }*/
            }
        });
    },

    showOverview: function () {
        // Nothing to do here, placeholder for now.
    },

    showAddRelativePeriod: function () {
        var me = this,
            view = Ext.create('Tme.view.relativeperiod.Edit');
        me.getApplication().fireEvent('changecontentevent', view);
        var categoriesCombo = view.down('#categories-combo-box');
        categoriesCombo.store.load(function () {
            categoriesCombo.on({
                expand: {
                    fn: function () {
                        var button = Ext.create('Ext.button.Button', {
                            text: Uni.I18n.translate('general.clearSelectedItems', 'DXP', 'Clear selected item(s)'),
                            ui: 'link',
                            handler: function () {
                                categoriesCombo.reset();
                            }
                        });
                        var spec = {
                            id: 'clear-all',
                            tag: 'div'
                        };
                        var container = Ext.DomHelper.insertFirst(categoriesCombo.getPicker().el.down('ul'), spec);
                        button.render(container);
                    },
                    single: true
                }
            });
        });
        var preview = view.down('#reference-date-preview-label');
        me.updatePreview(preview);
    },

    updatePreview: function (label) {
        var me = this,
        //label = me.down('#preview-label'),
        //value = me.getValue(),
            date = new Date(),
            dateString = '';

        // TODO Calculate the correct preview date and time.

        dateString1 = Uni.I18n.formatDate('datetime.longdate', date, 'UNI', 'l F j, Y \\a\\t H:i a');
        dateString2 = Uni.I18n.formatDate('datetime.longdate', date, 'UNI', 'l F j, Y \\a\\t H:i a');

        label.setText(me.formatPreviewTextFn(dateString1, dateString2));
    },
    formatPreviewTextFn: function (dateString1, dateString2) {
        return Uni.I18n.translate(
            'relativeperiod.form.previewText',
            'TME',
            'From {0} to {1}.',
            [dateString1, dateString2]
        );
    }
});
