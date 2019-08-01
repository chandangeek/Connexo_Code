/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.webservices.WebServiceIssue', {
    extend: 'Uni.property.view.property.Base',

    requires: [
        'Uni.property.view.property.webservices.AddWebServiceView',
        'Uni.property.model.PropertyWebService',
        'Uni.property.store.PropertyWebServices',
        'Uni.model.BreadcrumbItem',
        'Uni.grid.column.RemoveAction'
    ],

    currentPageView: null,
    addWebServiceView: null,

    getEditCmp: function() {
        var me = this;

        return {
            xtype: 'fieldcontainer',
            itemId: 'add-web-service-fieldcontainer',
            name: me.getName(),
            width: 800,
            readOnly: me.isReadOnly,
            layout: 'hbox',
            items: [
                {
                    xtype: 'displayfield',
                    itemId: 'add-web-service-empty-text',
                    value: Uni.I18n.translate('webService.empty', 'UNI', 'There are no web services added yet to this rule')
                },
                {
                    xtype: 'grid',
                    itemId: 'add-web-service-grid',
                    store: Ext.create('Ext.data.Store', {
                        model: 'Uni.property.model.PropertyWebService'
                    }),
                    width: 500,
                    padding: 0,
                    maxHeight: 323,
                    columns: [
                        {
                            header: Uni.I18n.translate('webService.title.endpoint', 'UNI', 'Web service endpoint'),
                            dataIndex: 'name',
                            flex: 1,
                        },
                        {
                            header: Uni.I18n.translate('general.type', 'UNI', 'Type'),
                            dataIndex: 'name',
                            flex: 1
                        },
                        {
                            xtype: 'uni-actioncolumn-remove',
                            handler: function (grid, rowIndex) {
                                grid.getStore().removeAt(rowIndex);
                                me.setValue();
                            }
                        }
                    ]
                },
                {
                    xtype: 'button',
                    itemId: 'add-web-service-button',
                    text: Uni.I18n.translate('webService.addWebService', 'UNI', 'Add web services'),
                    action: 'addWebService',
                    margin: '0 0 0 10',
                    handler: Ext.bind(me.showAddView, me)
                }
            ],

            isFormField: true,
            markInvalid: function (errors) {
                this.down('displayfield').markInvalid(errors);
            },
            clearInvalid: function () {
                this.down('displayfield').clearInvalid();
            },
            isValid: function () {
                return true;
            },
            getModelData: function () {
                return null;
            }
        }
    },

    getValue: function () {
        var me = this,
            result = me.recordsToIds(me.down('grid').getStore().getRange());

        return result.length ? result : null;
    },

    setValue: function (value) {
        var me = this,
            grid = me.down('#add-web-service-grid'),
            emptyMessage = me.down('#add-web-service-empty-text'),
            store = grid.getStore();

        Ext.suspendLayouts();
        if (Ext.isArray(value)) {
            store.loadData(me.idsToRecords(value), true);
        }

        if (store.getCount()) {
            grid.show();
            emptyMessage.hide();
        } else {
            grid.hide();
            emptyMessage.show();
        }
        Ext.resumeLayouts(true);
    },

    showAddView: function () {
        var me = this,
            selectionGrid,
            store;

        me.currentPageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0].down();
        me.addWebServiceView = Ext.widget('add-web-service-view', {
            itemId: me.key + 'add-web-service-view',
            store: 'Uni.property.store.PropertyWebServices',
        });
        selectionGrid = me.addWebServiceView.down('add-web-service-grid');
        store = selectionGrid.getStore();

        me.addWebServiceView.getAddButton().on(
            'click',
            function(event) {
                var webServices = me.addWebServiceView.down('add-web-service-grid').getSelectionModel().getSelection();
                me.hideAddView(event.action, webServices);
        });
        me.addWebServiceView.getCancelButton().on(
            'click',
            function(event) {
                me.hideAddView(event.action);
            }
        );

        Ext.suspendLayouts();
        me.currentPageView.hide();
        Ext.ComponentQuery.query('viewport > #contentPanel')[0].add(me.addWebServiceView);
        store.loadData(me.getProperty().getPossibleValues() || []);
        me.setPseudoNavigation(true);
        Ext.resumeLayouts(true);
        store.fireEvent('load');
    },

    hideAddView: function (eventType, webServices) {
        var me = this;

        if (webServices && eventType === 'add') {
            me.setValue(me.recordsToIds(webServices));
        }

        Ext.suspendLayouts();
        me.setPseudoNavigation();
        me.addWebServiceView.destroy();
        me.currentPageView.show();
        Ext.resumeLayouts(true);
    },

    setPseudoNavigation: function (toAddWebServices) {
        var me = this,
            breadcrumbTrail = Ext.ComponentQuery.query('breadcrumbTrail')[0],
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];

        if (toAddWebServices) {
            lastBreadcrumbLink.renderData.href = window.location.href;
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
            breadcrumbTrail.addBreadcrumbItem(Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('webService.addWebService', 'UNI', 'Add web services'),
                relative: false
            }));
            lastBreadcrumbLink.getEl().on('click', me.hideAddView, me, {single: true});
        } else {
            lastBreadcrumbLink.destroy();
            breadcrumbTrail.query('breadcrumbSeparator:last')[0].destroy();
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];
            lastBreadcrumbLink.renderData.href = '';
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
        }
    },

    recordsToIds: function (data) {
        var modifiedData = [];

        Ext.Array.each(data, function (record) {
            modifiedData.push(record.getId());
        });

        return modifiedData;
    },

    idsToRecords: function (data) {
        var me = this,
            modifiedData = [],
            store = Ext.getStore('Uni.property.store.PropertyWebServices');

        if (!store.getCount()) {
            store.loadData(me.getProperty().getPossibleValues());
        }

        Ext.Array.each(data, function (id) {
            modifiedData.push(store.getById(id));
        });

        return modifiedData;
    }
});
