Ext.define('Mdc.controller.setup.RegisterGroups', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification'
    ],

    views: [
        'setup.registergroup.RegisterGroupSetup',
        'setup.registergroup.RegisterGroupGrid',
        'setup.registergroup.RegisterGroupPreview',
        'setup.registergroup.RegisterGroupEdit'
    ],

    stores: [
        'RegisterGroups',
        'RegisterTypes',
        'AvailableRegisterTypesForRegisterGroup'
    ],

    refs: [
        {ref: 'registerGroupSetup', selector: '#registerGroupSetup'},
        {ref: 'registerTypeGrid', selector: '#registertypegrid'},
        {ref: 'registerGroupGrid', selector: '#registerGroupGrid'},
        {ref: 'registerGroupPreviewForm', selector: '#registerGroupPreviewForm'},
        {ref: 'registerGroupPreview', selector: '#registerGroupPreview'},
        {ref: 'registerGroupPreviewTitle', selector: '#registerGroupPreviewTitle'},
        {ref: 'registerGroupEditView', selector: '#registerGroupEdit'},
        {ref: 'registerGroupEditForm', selector: '#registerGroupEditForm'},
        {ref: 'registerEditEmptyGrid', selector: '#registerEditEmptyGrid'},
        {ref: 'registerGroupPreviewDetails', selector: '#registerGroupPreviewDetails'},
        {ref: 'registerTypePreviewForm', selector: '#registerTypePreviewForm'},
        {ref: 'registerTypePreview', selector: '#registerGroupSetup #registerTypePreview'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerGroupGridContainer', selector: '#registerGroupGridContainer'},
        {ref: 'registerGroupEmptyGrid', selector: '#registerGroupEmptyGrid'},
        {ref: 'registerTypeEmptyGrid', selector: '#registerTypeEmptyGrid'},
        {ref: 'contentPanel', selector: 'viewport > #contentPanel'}
    ],

    init: function () {
        this.getRegisterGroupsStore().on('load', this.onStoreLoad, this);

        this.control({
            '#editRegisterGroupGridField': {
                selectionchange: this.checkboxChanged
            },
            '#editRegisterGroupGridField actioncolumn': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerGroupSetup #registertypegrid': {
                selectionchange: this.previewRegisterType
            },
            '#registerGroupGrid': {
                selectionchange: this.previewRegisterGroup
            },
            '#registerGroupGrid actioncolumn': {
                editRegisterGroup: this.editRegisterGroupHistory
            },
            '#registerGroupSetup button[action = createRegisterGroup]': {
                click: this.createRegisterGroupHistory
            },
            '#registerGroupPreview menuitem[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            },
            '#registerGroupEdit button[action = createRegisterType]': {
                click: this.createRegisterTypeHistory
            },
            '#createEditButton[action=createRegisterGroup]': {
                click: this.saveRegisterGroup
            },
            '#createEditButton[action=saveRegisterGroup]': {
                click: this.saveRegisterGroup
            },
            '#editEmptyPreviewButton[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            }

        });
    },

    showRegisterGroups: function (grid, record) {
        var widget = Ext.widget('registerGroupSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onStoreLoad: function () {
        if(this.getRegisterGroupsStore().data.items.length > 0){
            this.getRegisterGroupSetup().show();
            this.getRegisterGroupGrid().getSelectionModel().doSelect(0);
        }
    },

    previewRegisterGroup: function (grid, record) {
        var registerGroups = this.getRegisterGroupGrid().getSelectionModel().getSelection();
        if (registerGroups.length == 1) {
            var me=this;

            this.getRegisterTypeGrid().store.getProxy().setExtraParam('registerGroup', registerGroups[0].get('id'));
            this.getRegisterTypeGrid().store.load({
                callback: function (store) {
                    if(this.totalCount > 0){
                        me.getRegisterTypeEmptyGrid().getLayout().setActiveItem('gridContainer');
                        me.getRegisterGroupPreviewTitle().update('<b>' + Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + registerGroups[0].get('name') + '</b><br>' +
                            Ext.String.format(Uni.I18n.translate('registerGroup.previewCount', 'MDC', '{0} register types'), this.totalCount));
                        me.getRegisterTypeGrid().getSelectionModel().doSelect(0);
                    }
                    else{
                        me.getRegisterTypeEmptyGrid().getLayout().setActiveItem('emptyContainer');
                        me.getRegisterGroupPreviewTitle().update('<b>' + Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + registerGroups[0].get('name') + '</b>');
                    }
                    me.getRegisterTypeEmptyGrid().setVisible(true);
                }
            });
        }
    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().setTitle(registerTypes[0].get('name'));
        }
    },

    createRegisterGroupHistory: function () {
        location.href = '#/administration/registergroups/add';
    },

    editRegisterGroupHistory: function (item) {
        location.href = '#/administration/registergroups/' + item.get('id') + '/edit';
    },

    editRegisterGroupHistoryFromPreview: function () {
        location.href = '#/administration/registergroups/' + this.getRegisterGroupGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    createRegisterTypeHistory: function () {
        location.href = '#/administration/registertypes/add';
    },

    showRegisterGroupEditView: function (registerGroupId) {
        var widget = Ext.widget('registerGroupEdit', {
            edit: true
        });

        var me = this;

        // TODO: change this to activate infinite scrolling when JP-2844 is fixed
        Ext.ModelManager.getModel('Mdc.model.RegisterGroup').load(registerGroupId, {
            success: function (registerGroup) {
                me.getApplication().fireEvent('loadRegisterGroup', registerGroup);
                me.getStore('Mdc.store.RegisterTypes').load({
                    limit: 500,
                    callback: function (registerTypes) {
                        widget.down('form').loadRecord(registerGroup);
                        widget.down('panel').setTitle(registerGroup.get('name') + ' > ' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + Uni.I18n.translate('registerGroup.registerGroup', 'MDC', 'Register group'));
                        if(this.data.items.length > 0){
                            me.getRegisterEditEmptyGrid().getLayout().setActiveItem('gridContainer');
                            widget.down('#editRegisterGroupGridField').getSelectionModel().doSelect(registerGroup.registerTypes().data.items);
                            widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                        }
                        else{
                            widget.down('#editRegisterGroupSelectedField').hide();
                            widget.down('#editRegisterGroupNameField').hide();
                            //widget.down('#btnsContainer').hide();
                            widget.down('#separator').hide();
                            me.getRegisterEditEmptyGrid().getLayout().setActiveItem('emptyContainer');
                        }
                        me.getRegisterEditEmptyGrid().setVisible(true);
                        widget.setLoading(false);
                    }
                });
            }
        });

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
    },

    showRegisterGroupCreateView: function () {
        var widget = Ext.widget('registerGroupEdit', {
            edit: false
        });

        var me = this;

        // TODO: change this to activate infinite scrolling when JP-2844 is fixed
        //widget.down('#editRegisterGroupGridField').store.on('load', function () {
        me.getStore('Mdc.store.RegisterTypes').load({
            limit: 500,
            callback: function (registerTypes) {
                var registerGroup = Ext.create(Ext.ModelManager.getModel('Mdc.model.RegisterGroup'));
                widget.down('form').loadRecord(registerGroup);
                widget.down('panel').setTitle(Uni.I18n.translate('registerGroup.create', 'MDC', 'Add register group'));
                if(this.totalCount > 0){
                    me.getRegisterEditEmptyGrid().getLayout().setActiveItem('gridContainer');
                    widget.down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', '{0} register types selected'), 0));
                    widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                }
                else{
                    widget.down('#editRegisterGroupSelectedField').hide();
                    widget.down('#editRegisterGroupNameField').hide();
                    //widget.down('#btnsContainer').hide();
                    widget.down('#separator').hide();
                    me.getRegisterEditEmptyGrid().getLayout().setActiveItem('emptyContainer');
                    //me.getRegisterEditEmptyGrid().setMargin('0 0 0 10');
                }
                me.getRegisterEditEmptyGrid().setVisible(true);
                widget.show();
                widget.setLoading(false);
            }
        });

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        widget.hide();
    },

    checkboxChanged: function (grid, selected) {
        grid.view.up('#registerGroupEdit').down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', '{0} register types selected'), selected.length));
    },

    saveRegisterGroup: function () {
        var form = this.getRegisterGroupEditForm(),
            record = form.getRecord(),
            values = form.getValues(),
            selected = form.down('#editRegisterGroupGridField').getSelectionModel().getSelection();

        record.set(values);
        record.registerTypes().removeAll();
        record.registerTypes().add(selected);

        var me=this;
        record.save({
            success: function (record) {
                /*Ext.create('widget.uxNotification', {
                    position: 'tc',
                    manager: me.getContentPanel(),
                    cls: 'ux-notification-light',
                    width: me.getContentPanel().getWidth()-20,
//                  iconCls: 'ux-notification-icon-information',
                    html: 'Register group saved',
                    slideInDuration: 200,
                    slideBackDuration: 200,
                    autoCloseDelay: 7000,
                    slideInAnimation: 'linear',
                    slideBackAnimation: 'linear'
                }).show();*/
                location.href = '#/administration/registergroups/';
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    me.getRegisterGroupEditForm().getForm().markInvalid(json.errors);
                }
            }
        });
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
    }
});
