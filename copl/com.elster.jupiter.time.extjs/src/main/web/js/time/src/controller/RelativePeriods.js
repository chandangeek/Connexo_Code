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
            formErrorsPanel.hide();
            record.set('name', nameFieldValue);

            Ext.Array.each(categories.getValue(), function (item) {
                record.categories().add(Ext.create(Tme.model.Categories, {id: item}));
            });

            record.set('categories', arrCategories);

            var startPeriodValue = form.down('#relative-date-start').getValue(),
                startDateModel = me.createModelFromRelativePeriodValue(startPeriodValue);

            record.set('from', startDateModel.data);

            var endPeriodValue = form.down('#relative-date-end').getValue(),
                endDateModel = me.createModelFromRelativePeriodValue(endPeriodValue);

            record.set('to', endDateModel.data);

            record.save({
                success: function (record, operation) {
                    var messageText;
                    if (button.action === 'editRuleAction') {
                        messageText = Uni.I18n.translate('relativeperiod.editSuccess.msg', 'TME', 'Relative period saved');
                    } else {
                        messageText = Uni.I18n.translate('relativeperiod.addSuccess.msg', 'TME', 'Relative period added');
                    }
                    if (!me.fromEditTask) {
                        location.href = '#/administration/dataexporttasks/add';
                    } else {
                        location.href = '#/administration/dataexporttasks/' + me.taskId + '/edit';
                    }

                    me.getApplication().fireEvent('acknowledge', messageText);
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    createModelFromRelativePeriodValue: function (value) {
        var model = Ext.create(Tme.model.RelativeDate);

        model.set('startPeriodAgo', value.startPeriodAgo);
        model.set('startAmountAgo', value.startAmountAgo);
        model.set('startNow', value.startNow);
        model.set('startFixedDay', value.startFixedDay);
        model.set('startFixedMonth', value.startFixedMonth);
        model.set('startFixedYear', value.startFixedYear);
        model.set('onCurrentDay', value.onCurrentDay);
        model.set('onDayOfMonth', value.onDayOfMonth);
        model.set('onDayOfWeek', value.onDayOfWeek);
        model.set('atHour', value.atHour);
        model.set('atMinute', value.atMinute);

        return model;
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
            router = me.getController('Uni.controller.history.Router'),
            cancelLink,
            view;
        me.fromEditTask = router.queryParams.fromEdit === 'true';
        if (!me.fromEditTask) {
            cancelLink = '#/administration/dataexporttasks/add';
        } else {
            me.taskId = router.queryParams.taskId;
            cancelLink = '#/administration/dataexporttasks/' + me.taskId + '/edit';
        }
        view = Ext.create('Tme.view.relativeperiod.Edit', {
            returnLink: cancelLink
        });
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
