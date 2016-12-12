Ext.define('Imt.purpose.controller.RegisterData', {
    extend: 'Ext.app.Controller',

    requires: [
        'Imt.purpose.view.registers.index.AddEdit',
        'Imt.purpose.view.registers.text.AddEdit',
        'Imt.purpose.view.registers.flag.AddEdit',
        'Imt.purpose.view.registers.billing.AddEdit',
        'Imt.purpose.view.registers.RegisterTypesMap'
    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.RegisterReadings'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm'
    ],

    refs: [
        {
            ref: 'readingsList',
            selector: '#output-readings #readings-list'
        },
        {
            ref: 'readingsGraph',
            selector: '#output-readings #readings-graph'
        },
        {
            ref: 'readingPreviewPanel',
            selector: 'output-channel-main reading-preview'
        },
        {
            ref: 'registerDataGrid',
            selector: 'register-data-grid'
        },
        {
            ref: 'addReading',
            selector: 'add-main-register-reading'
        }
    ],

    init: function () {
        this.control({
            'output-channel-main register-data-grid': {
                select: function (selectionModel, record) {
                    if (selectionModel.getSelection().length === 1) {
                        this.getReadingPreviewPanel().updateForm(record);
                    }
                }
            },
            'add-main-register-reading #addEditButton': {
                click: this.saveReading
            },
            'purpose-register-readings-data-action-menu': {
                beforeshow: this.checkSuspect,
                click: this.chooseAction
            }
        });
    },

    checkSuspect: function (menu) {
        var validationResult = menu.record.get('validationResult') == 'validationStatus.suspect',
            calculatedValue = menu.record.get('calculatedValue'),
            modificationFlag = menu.record.get('modificationState') ? menu.record.get('modificationState').flag : null;

        if (menu.record.get('isConfirmed') || menu.record.isModified('value')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(validationResult);
        }

        if (menu.down('#reset-value')) {
            menu.down('#reset-value').setVisible(modificationFlag == "EDITED" || modificationFlag == "ADDED");
            menu.down('#reset-value').setText(calculatedValue ?
                Uni.I18n.translate('general.actions.reset', 'IMT', 'Reset') :
                Uni.I18n.translate('general.actions.remove', 'IMT', 'Remove')
            )
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            modificationFlag = menu.record.get('modificationState') ? menu.record.get('modificationState').flag : null,
            calculatedValue = menu.record.get('calculatedValue'),
            removeAction = (modificationFlag == "EDITED" || modificationFlag == "ADDED") && !calculatedValue;

        switch (item.action) {
            case 'resetValue':
                me.resetReadings(menu.record, removeAction);
                break;
            case 'editValue':
                router.getRoute('usagepoints/view/purpose/output/editregisterdata').forward({timestamp: menu.record.get('timeStamp')});
                break;
            case 'confirmValue':
                me.confirmValue(menu.record);
                break;
        }
    },

    showAddRegisterData: function (usagePointName, purposeId, outputId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes'),
            outputModel = me.getModel('Imt.purpose.model.Output'),
            reading = Ext.create('Imt.purpose.model.RegisterReading'),
            output,
            previousQueryString = me.getController('Uni.controller.history.EventBus').getPreviousQueryString()!==null ? '?'+me.getController('Uni.controller.history.EventBus').getPreviousQueryString() : '',
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading(true);
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(function (usagePointTypes, op, success) {
            if (success) {
                me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointName, {
                    success: function (usagePoint) {
                        app.fireEvent('usagePointLoaded', usagePoint);
                        purposesStore.getProxy().extraParams = {
                            usagePointId: usagePointName
                        };
                        purposesStore.load(function (purposes) {
                            usagePoint.set('purposes', purposes);
                            app.fireEvent('purposes-loaded', purposes);
                            outputModel.getProxy().extraParams = {usagePointId: usagePointName, purposeId: purposeId};
                            outputModel.load(outputId, {
                                success: function (record) {
                                    output = record;
                                    app.fireEvent('output-loaded', output);
                                    me.output = output;

                                    var widget = Ext.widget(Imt.purpose.view.registers.RegisterTypesMap.getAddEditForms(record.get('deliverableType')), {
                                        edit: false,
                                        router: router,
                                        returnLink: router.getRoute('usagepoints/view/purpose/output').buildUrl() + previousQueryString,
                                        menuHref: router.getRoute('usagepoints/view/purpose/output/addregisterdata').buildUrl()
                                    });
                                    widget.down('#registerDataEditForm').loadRecord(reading);
                                    me.getApplication().fireEvent('changecontentevent', widget);
                                    widget.setValues(output);
                                }
                            });
                            mainView.setLoading(false);
                        });
                    }
                });
            } else {
                mainView.setLoading(false);
            }
        });

    },

    showEditRegisterData: function (usagePointName, purposeId, outputId, tab, timestamp) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes'),
            outputModel = me.getModel('Imt.purpose.model.Output'),
            readingModel = me.getModel('Imt.purpose.model.RegisterReading'),
            output,
            previousQueryString = me.getController('Uni.controller.history.EventBus').getPreviousQueryString()!==null ? '?'+me.getController('Uni.controller.history.EventBus').getPreviousQueryString() : '',
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading(true);
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(function (usagePointTypes, op, success) {
            if (success) {
                me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointName, {
                    success: function (usagePoint) {
                        app.fireEvent('usagePointLoaded', usagePoint);
                        purposesStore.getProxy().extraParams = {
                            usagePointId: usagePointName
                        };
                        purposesStore.load(function (purposes) {
                            usagePoint.set('purposes', purposes);
                            app.fireEvent('purposes-loaded', purposes);
                            outputModel.getProxy().extraParams = {usagePointId: usagePointName, purposeId: purposeId};
                            outputModel.load(outputId, {
                                success: function (record) {
                                    output = record;
                                    app.fireEvent('output-loaded', output);
                                    me.output = output;
                                    readingModel.getProxy().extraParams = {usagePointId: usagePointName, purposeId: purposeId, outputId: outputId};
                                    readingModel.load(timestamp, {
                                        success: function (record) {
                                            var widget = Ext.widget(Imt.purpose.view.registers.RegisterTypesMap.getAddEditForms(output.get('deliverableType')), {
                                                edit: true,
                                                router: router,
                                                returnLink: router.getRoute('usagepoints/view/purpose/output').buildUrl() + previousQueryString,
                                                menuHref: router.getRoute('usagepoints/view/purpose/output/editregisterdata').buildUrl()
                                            });
                                            widget.down('#registerDataEditForm').loadRecord(record);
                                            me.getApplication().fireEvent('changecontentevent', widget);
                                            widget.setValues(output);
                                        }
                                    });
                                }
                            });
                            mainView.setLoading(false);
                        });
                    }
                });
            } else {
                mainView.setLoading(false);
            }
        });

    },

    saveReading: function () {
        var me = this,
            reading,
            addReadingView = me.getAddReading(),
            router = me.getController('Uni.controller.history.Router');

        if(!addReadingView.isValid()){
            addReadingView.showErrors();
        } else {
            addReadingView.hideErrors();
            addReadingView.down('#registerDataEditForm').updateRecord();
            reading = addReadingView.down('#registerDataEditForm').getRecord();
            reading.set('type', me.output.get('deliverableType'));
            if (me.output.get('deliverableType') == 'billing') {
                reading.set("interval", {start: reading.get('interval.start'), end: reading.get('interval.end')});
            }
            reading.getProxy().setParams(router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId);
            reading.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagepoint.registerData.updated', 'IMT', 'Register data saved'));
                    window.history.back();
                },
                failure: function (record, resp) {
                    var response = resp.response;
                    if (response.status == 400) {
                        var responseText = Ext.decode(response.responseText, true);
                        if (responseText && !Ext.isEmpty(responseText.errors)) {
                            addReadingView.showErrors(responseText.errors);
                        }
                    }
                },
                callback: function () {
                }
            });
        }
    },

    resetReadings: function (record, removeAction) {
        var me = this,
            grid = me.getRegisterDataGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            msg = removeAction ?
                Ext.String.format(
                    Uni.I18n.translate('usagepoint.registerData.delete.message', 'IMT', 'The register reading with measurement time {0} will no longer be available'),
                    Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(lastSelected.get('timeStamp')), Uni.DateTime.formatTimeShort(lastSelected.get('timeStamp'))]
                    )
                ) :
                Ext.String.format(
                    Uni.I18n.translate('usagepoint.registerData.reset.message', 'IMT', 'The register reading with the reading time {0} will be reset to the value of the aggregated reading from the meter'),
                    Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(lastSelected.get('timeStamp')), Uni.DateTime.formatTimeShort(lastSelected.get('timeStamp'))]
                    )
                );
        me.recordToDestroy = record;

        Ext.create('Uni.view.window.Confirmation', {
            confirmText: removeAction ?
                Uni.I18n.translate('general.actions.remove', 'IMT', 'Remove') :
                Uni.I18n.translate('general.actions.reset', 'IMT', 'Reset')

        }).show({
            msg: msg,
            title: removeAction ?
                Uni.I18n.translate('usagepoint.registerData.delete.title.question', 'IMT', 'Remove the reading?') :
                Uni.I18n.translate('usagepoint.registerData.reset.title.question', 'IMT', 'Reset the reading?'),
            config: {
                readingToDelete: lastSelected,
                removeAction: removeAction,
                me: me
            },
            fn: me.removeRegisterDataRecord
        });
    },

    removeRegisterDataRecord: function (btn, text, cfg) {
        var me = cfg.config.me,
            msg = cfg.removeAction ?
                Uni.I18n.translate('usagepoint.registerData.removed', 'IMT', 'Register data sucessfully removed') :
                Uni.I18n.translate('usagepoint.registerData.reset', 'IMT', 'Register data sucessfully reset to aggregated'),
            record = cfg.config.readingToDelete,
            router = me.getController('Uni.controller.history.Router');
        if (btn === 'confirm') {
            record.getProxy().setParams(router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId);
            record.destroy({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', msg);
                    window.history.back();
                }
            })
        }
    },

    confirmValue: function(record){
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];
        mainView.setLoading();
        record.getProxy().setParams(router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId);
        record.set('isConfirmed', true);
        record.save({
            callback: function (rec, operation, success) {
                if (success) {
                    router.getRoute('usagepoints/view/purpose/output').forward();
                }
                mainView.setLoading(false);
            }
        });
    }
});