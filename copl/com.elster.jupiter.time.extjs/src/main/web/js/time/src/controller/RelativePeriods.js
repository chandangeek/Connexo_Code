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
            'tme-relativeperiod-edit #create-edit-button': {
                click: this.createEditPeriod
            }

        });
    },

    createEditPeriod: function (button) {
        var me = this,
            form = me.getPage().down('#edit-relative-period-form'),
            arrCategories = [],
            nameFieldValue = form.down('#edit-relative-period-name').getValue(),
            formErrorsPanel = form.down('#form-errors');

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

        var record = Ext.create('Tme.model.RelativePeriod');
        var categories = this.getCategoriesTextFields();

        if (form.isValid()) {

            record.set('name', nameFieldValue);
            Ext.Array.each(categories.getValue(), function (item) {
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
                    location.href = '#/administration/dataexporttasks/add';

                    me.getApplication().fireEvent('acknowledge', messageText);
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    var title = 'Attention please:',
                        message = json.errors[0].msg;

                    me.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    showValidationActivationErrors: function (errors) {
        if (Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').update(errors);
            Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').setVisible(true);
        }
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
    }
});
