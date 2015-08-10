Ext.define('Tme.controller.RelativePeriods', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Tme.view.relativeperiod.Setup',
        'Tme.view.relativeperiod.Edit',
        'Tme.view.relativeperiod.Details'
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
        {ref: 'categoriesTextFields', selector: 'tme-relativeperiod-edit #categories-combo-box'},
        {ref: 'periodsPage', selector: 'relative-periods-setup'}
    ],

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

        var record = Ext.create('Tme.model.RelativePeriod');
        var categories = this.getCategoriesTextFields();
        if (form.isValid()) {
            formErrorsPanel.hide();
            record.beginEdit();
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
            record.endEdit();
            record.save({
                success: function (record, operation) {
                    var messageText;
                    if (button.action === 'editRuleAction') {
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
        } else {
            formErrorsPanel.show();
        }
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
        model.set('onDayOfMonth', value.onDayOfMonth);
        model.set('onDayOfWeek', value.onDayOfWeek);
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
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            cancelLink,
            view;
        me.fromEditTask = router.queryParams.fromEdit === 'true';
        me.fromAddTask = router.queryParams.fromEdit === 'false';
        if (me.fromAddTask) {
            cancelLink = '#/administration/dataexporttasks/add';
        } else if (me.fromEditTask) {
            me.taskId = router.queryParams.taskId;
            cancelLink = '#/administration/dataexporttasks/' + me.taskId + '/edit';
        } else {
            cancelLink = '#/administration/relativeperiods';
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
                            text: Uni.I18n.translate('general.clearSelectedItems', 'TME', 'Clear selected item(s)'),
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
    },

    showRelativePeriods: function () {
        var me = this,
            view = Ext.widget('relative-periods-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPeriodsPage(),
            preview = page.down('relative-periods-preview'),
            previewForm = page.down('relative-periods-preview-form');

        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('relative-periods-action-menu').record = record;
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
            case 'viewDetails':
                route = 'administration/relativeperiods/relativeperiod';
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
            title: Uni.I18n.translate('general.remove', 'TME', 'Remove') + ' ' + record.data.name + ' ?',
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
            relativePeriodPreview = view.down('uni-form-relativeperiodpreview'),
            actionsMenu = view.down('relative-periods-action-menu');

        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(periodId, {
            success: function (record) {
                var detailsForm = view.down('relative-periods-preview-form');
                actionsMenu.record = record;
                actionsMenu.down('#view-details').hide();
                me.getApplication().fireEvent('relativeperiodload', record);
                detailsForm.loadRecord(record);
                view.down('relative-periods-menu #relative-period-overview-link').setText(record.get('name'));
                relativePeriodPreview.updateStartPeriodValue(record.data.from);
                relativePeriodPreview.updateEndPeriodValue(record.data.to);
                relativePeriodPreview.updatePreview();
            }
        });
    }
});
