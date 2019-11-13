/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.controller.ConfigProperties', {
    extend: 'Ext.app.Controller',

    requires: [
        'Cfg.properties.view.Properties'
    ],

    views: [
        'Cfg.properties.view.Properties',
        'Cfg.properties.view.EditPropertiesPage'
    ],

    stores: [
        'Cfg.properties.store.ConfigProperties'
    ],

    models: [
        'Cfg.properties.model.ConfigProperties',
        'Cfg.properties.model.Properties'
    ],

    refs: [
        {ref: 'editForm', selector: '#cfg-properties-form'},
        {ref: 'errorsPanel', selector: '#cfg-config-properties-form #form-errors'},
    ],

    init: function () {
        this.control({
            'cfg-config-properties-page button[action=editConfigProperties]': {
                click: this.saveProperties
            },
            'cfg-properties-action-menu': {
                click: this.chooseAction
            },
        })
    },

    showOverview: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            title = router.getRoute(router.currentRoute).title,
            scope = router.getRoute(router.currentRoute).scope,
            widget = Ext.widget('cfg-config-properties', {
                title: title
            }),
            taskModel = me.getModel('Cfg.properties.model.ConfigProperties');

            taskModel.load(scope, {
                success: function (operation) {

                    var response = Ext.JSON.decode(operation.responseText),
                        store = Ext.create('Cfg.properties.store.ConfigProperties');
                    store.loadRawData([operation]);
                    store.each(function (record) {
                        var detailsForm = widget.down('#cfg-config-properties-form');
                        me.getApplication().fireEvent('changecontentevent', widget);

                        detailsForm.loadRecord(record);
                        var taskForm = detailsForm;
                        if (record.properties && record.properties().count() > 0) {

                            Ext.Array.each(record.properties(), function (property) {
                                var propertiesContainer = taskForm.down('#cfg-properties');
                                property.each(function (propertyRecord) {
                                    var propertyForm = Ext.create('Uni.property.form.Property', {
                                        itemId: 'cfg-group-' + propertyRecord.get('name'),
                                        defaults: {
                                            resetButtonHidden: true,
                                            labelWidth: 250,
                                            width: 500
                                        },
                                        isEdit: false,
                                        isReadOnly: true
                                    });
                                    if (propertyRecord && propertyRecord.properties() && propertyRecord.properties().count()) {
                                        propertyForm.loadRecord(propertyRecord);
                                        propertyForm.show();
                                    } else {
                                        propertyForm.hide();
                                    }

                                    var fieldContainer = Ext.create('Ext.form.FieldContainer', {
                                        fieldLabel: propertyRecord.get('displayName'),
                                        labelAlign: 'top',
                                        layout: 'vbox',
                                        items: propertyForm
                                    });
                                    propertiesContainer.add(fieldContainer);
                                });
                            });
                        }
                    });
            }
        });

    },

    editProperties: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            scopeTitle = router.getRoute(router.currentRoute).scopeTitle,
            scope = router.getRoute(router.currentRoute).scope,
            widget = Ext.widget('cfg-config-properties-page', {
                title: Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.edit', 'CFG', 'Edit'), scopeTitle),
                cancelLink: router.getRoute(router.currentRoute.replace('/edit', '')).buildUrl()
            }),
            taskModel = me.getModel('Cfg.properties.model.ConfigProperties');

        taskModel.load(scope, {
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.properties.store.ConfigProperties');
                store.loadRawData([operation]);
                store.each(function (record) {

                    me.getApplication().fireEvent('configPropertiesLoaded', scopeTitle);
                    var taskForm = widget.down('#cfg-properties-form');
                    me.getApplication().fireEvent('changecontentevent', widget);
                    Ext.suspendLayouts();
                    taskForm.loadRecord(record);

                    if (record.properties && record.properties().count() > 0) {

                        Ext.Array.each(record.properties(), function (property) {
                            var propertiesContainer = taskForm.down('#cfg-properties');
                            property.each(function (propertyRecord) {
                                    var propertyForm = Ext.create('Uni.property.form.Property', {
                                        itemId: 'cfg-group-' + propertyRecord.get('name'),
                                        title: propertyRecord.get('displayName'),
                                        defaults: {
                                            labelWidth: 235,
                                            width: 335
                                        },
                                        ui: 'medium'
                                    });
                                    if (propertyRecord && propertyRecord.properties() && propertyRecord.properties().count()) {
                                        propertyForm.loadRecord(propertyRecord);
                                        propertyForm.show();
                                    } else {
                                        propertyForm.hide();
                                    }

                                    propertiesContainer.add(propertyForm);
                                }
                            );
                        });
                    }
                    taskForm.doLayout();
                    Ext.resumeLayouts(true);
                });
            }
        })
    },

    saveProperties: function (button) {
        var me = this,
            form = me.getEditForm(),
            errorsPanel = me.getErrorsPanel(),
            record = form.getRecord(),
            router = me.getController('Uni.controller.history.Router'),
            returnLink = router.getRoute(router.currentRoute.replace('/edit', ''));

        form.getForm().clearInvalid();
        form.setLoading();
        if (!errorsPanel.isHidden()) {
            errorsPanel.hide();
        }
        // set properties
        record.beginEdit();
        if (record.properties && record.properties().count() > 0) {
            Ext.Array.each(record.properties(), function (property) {
                property.each(function (propertyRecord) {

                        form.down('#cfg-group-' + propertyRecord.get('name')).updateRecord();
                        propertyRecord.beginEdit();
                        propertyRecord.propertiesStore = form.down('#cfg-group-' + propertyRecord.get('name')).getRecord().properties();
                        propertyRecord.endEdit();
                    }
                );
            });
        }
        record.endEdit();

        // save properties
        record.save({
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('config.properties.successMsg.saved', 'CFG', 'Settings saved'));
                returnLink.forward();
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        errorsPanel.show();
                    }
                }
            },
            callback: function () {
                form.setLoading(false);
            }
        })
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;
        switch (item.action) {
            case 'editConfigProperties':
                route = router.currentRoute + '/edit';
                break;
        }
        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    }
});
