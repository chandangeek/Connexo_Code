/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.controller.RelativePeriods', {
    extend: 'Ext.app.Controller',

    requires: [],

    views: [
        'Tme.view.relativeperiod.Setup',
        'Tme.view.relativeperiod.Edit',
        'Tme.view.relativeperiod.Details',
        'Tme.view.relativeperiod.UsagePage'
    ],

    stores: [
        'Tme.store.RelativePeriods',
        'Tme.store.RelativePeriodCategories',
        'Tme.store.RelativePeriodUsageCategories',
        'Tme.store.RelativePeriodUsage',
        'Uni.store.Periods',
        'Uni.store.DaysOfWeek'
    ],

    models: [
        'Tme.model.RelativePeriod',
        'Tme.model.Categories',
        'Tme.model.RelativeDate',
        'Tme.model.RelativePeriodUsage'
    ],

    refs: [
        {ref: 'page', selector: 'tme-relativeperiod-edit'},
        {ref: 'categoriesTextFields', selector: 'tme-relativeperiod-edit #categories-combo-box'},
        {ref: 'periodsPage', selector: 'relative-periods-setup'}
    ],

    fromDetail: false,

    init: function () {
        this.control({
            'tme-relativeperiod-edit #create-edit-button': {
                click: this.createEditPeriod
            },
            'relative-periods-setup relative-periods-grid': {
                select: this.showPreview
            },
            'relative-periods-action-menu': {
                click: this.chooseAction
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

        var record;
        if (button.action === 'editPeriod') {
            record = form.record;
        } else {
            record = Ext.create('Tme.model.RelativePeriod');
        }
        var categories = this.getCategoriesTextFields();

        record.beginEdit();
        record.set('name', nameFieldValue);
        record.categories().removeAll(true);

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
        record.endEdit();
        form.getForm().clearInvalid();
        record.save({
            success: function (record, operation) {
                var messageText;
                if (button.action === 'editPeriod') {
                    messageText = Uni.I18n.translate('relativeperiod.editSuccess.msg', 'TME', 'Relative period saved');
                } else {
                    messageText = Uni.I18n.translate('relativeperiod.addSuccess.msg', 'TME', 'Relative period added');
                }
                if (me.fromAddTask) {
                    location.href = '#/administration/dataexporttasks/add';
                } else if (me.fromEditTask) {
                    location.href = '#/administration/dataexporttasks/' + me.taskId + '/edit';
                } else {
                    location.href = '#/administration/relativeperiods';
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
    },

    createModelFromRelativePeriodValue: function (value) {
        var model = Ext.create(Tme.model.RelativeDate);
        model.beginEdit();
        model.set('startPeriodAgo', value.startPeriodAgo);
        model.set('startAmountAgo', value.startAmountAgo);
        model.set('startTimeMode', value.startTimeMode);
        model.set('startNow', value.startNow);
        model.set('startFixedDay', value.startFixedDay);
        model.set('startFixedMonth', value.startFixedMonth);
        model.set('startFixedYear', value.startFixedYear);
        model.set('onCurrentDay', value.onCurrentDay);
        model.set('onCurrentDayOfYear', value.onCurrentDayOfYear);
        model.set('onDayOfMonth', value.onDayOfMonth);
        model.set('onDayOfWeek', value.onDayOfWeek);
        model.set('onDayOfYear', value.onDayOfYear);
        model.set('onMonthOfYear', value.onMonthOfYear);
        model.set('atHour', value.atHour);
        model.set('atMinute', value.atMinute);
        model.endEdit();
        return model;
    },

    showValidationActivationErrors: function (errors) {
        if (Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').update(errors);
            Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').setVisible(true);
        }
    },

    showAddRelativePeriod: function () {
        this.showAddEditRelativePeriod();
    },

    showEditRelativePeriod: function (relativePeriodId) {
        this.showAddEditRelativePeriod(relativePeriodId);
    },

    showAddEditRelativePeriod: function (relativePeriodId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            cancelLink,
            view,
            record;


        if (me.fromDetail) {
            cancelLink = '#/administration/relativeperiods/' + relativePeriodId;
        } else {
            cancelLink = '#/administration/relativeperiods';
        }
        if (!Ext.isEmpty(relativePeriodId)) {
            record = Ext.ModelManager.getModel('Tme.model.RelativePeriod');
            record.load(relativePeriodId, {
                success: function (record) {
                    view = Ext.create('Tme.view.relativeperiod.Edit', {
                        returnLink: cancelLink,
                        record: record
                    });
                    me.getApplication().fireEvent('relativeperiodload', record);
                    me.getApplication().fireEvent('changecontentevent', view);
                    view.setLoading(true);
                    view.down('uni-form-relativeperiod:first').on('periodchange', function () {
                        view.setLoading(false);
                    });
                    var categoriesCombo = view.down('#categories-combo-box');
                    categoriesCombo.store.load();
                }
            })
        } else {
            view = Ext.create('Tme.view.relativeperiod.Edit', {
                returnLink: cancelLink
            });
            me.getApplication().fireEvent('changecontentevent', view);
            var categoriesCombo = view.down('#categories-combo-box');
            categoriesCombo.store.load();
        }
    },

    showRelativePeriods: function () {
        var me = this,
            view = Ext.widget('relative-periods-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.fromDetail = false;
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPeriodsPage(),
            preview = page.down('relative-periods-preview'),
            previewForm = page.down('relative-periods-preview-form'),
            relativePeriodPreview = page.down('uni-form-relativeperiodpreview-basedOnId');

        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        preview.down('relative-periods-action-menu').record = record;
        relativePeriodPreview.setRelativePeriodId(record.get('id'));
        relativePeriodPreview.updatePreview();
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.periodId = menu.record.getId();

        switch (item.action) {
            case 'removePeriod':
                me.removePeriod(menu.record);
                break;
            case 'editDetails':
                route = 'administration/relativeperiods/relativeperiod/edit';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },

    removePeriod: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            msg: Uni.I18n.translate('relativeperiod.removeMsg', 'TME', 'This relative period will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'TME', "Remove '{0}'?", [record.data.name]),
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        success: function () {
                            if (me.getPeriodsPage()) {
                                var grid = me.getPeriodsPage().down('relative-periods-grid');
                                grid.down('pagingtoolbartop').totalCount = 0;
                                grid.down('pagingtoolbarbottom').resetPaging();
                                grid.getStore().load();
                            } else {
                                me.getController('Uni.controller.history.Router').getRoute('administration/relativeperiods').forward();
                            }
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('relativePeriod.confirm.msg', 'TME', 'Relative period removed'));
                        }
                    });
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    showRelativePeriodDetails: function (periodId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Tme.model.RelativePeriod'),
            view = Ext.widget('relative-periods-details', {
                router: router
            }),
            relativePeriodPreview = view.down('uni-form-relativeperiodpreview-basedOnId'),
            actionsMenu = view.down('relative-periods-action-menu');

        me.fromDetail = true;
        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(periodId, {
            success: function (record) {
                var detailsForm = view.down('relative-periods-preview-form');
                actionsMenu.record = record;
                me.getApplication().fireEvent('relativeperiodload', record);
                detailsForm.loadRecord(record);
                view.down('relative-periods-menu').setHeader(record.get('name'));
                relativePeriodPreview.setRelativePeriodId(periodId);
                relativePeriodPreview.updatePreview();
            }
        });
    },

    showRelativePeriodUsage: function (periodId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view,
            usageStore = me.getStore('Tme.store.RelativePeriodUsage'),
            taskModel = me.getModel('Tme.model.RelativePeriod');

        usageStore.getProxy().setUrl(periodId);

        taskModel.load(periodId, {
            success: function (record) {
                view = Ext.widget('relative-period-usage', {
                    router: router,
                    record: record
                });

                view.down('relative-periods-menu').setHeader(record.get('name'));
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });



    }
});
