/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.UserAssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.user-assignee-combo',
    checked: false,
    requires: [
        //'Bpm.store.task.TaskWorkgroupAssignees'
    ],
    tpl: new Ext.XTemplate(
        '<label class="x-form-cb-label">' +
        '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "BPM", "Show all users") +
        '</label><hr />',
        '<tpl for=".">',
        '<div class=" x-boundlist-item">{name}</div>',
        '</tpl>'
    ),
    workgroupId: -1,
    workgroupChangeEvent: false,
    userChangeEvent: false,
    handleShowAll: function (checkBox) {
        var me = this,
            value = me.getValue();

        me.checked = document.getElementById("cboxShowAll").checked;
        me.checked && me.setCheckTemplate();
        !me.checked && me.setUncheckTemplate();

        me.store.getProxy().url = me.checked ? me.allUsersUrl : Ext.String.format(me.workgroupUsersUrl, me.workgroupId);
        me.store.load(function (records) {
            var id = me.valueField;
            var foundUsers = records.filter(function (user) {
                return user.get(id) == value;
            });

            if (Ext.isArray(foundUsers) && foundUsers.length == 0){
                var unassignedObject = records.filter(function (user) {
                    return user.get('id') == -1;
                })[0];
                me.select(unassignedObject.get(me.valueField));
            }

        });
    },

    setCheckTemplate: function () {
        var me = this,
            picker = me.getPicker(),
            tpl = picker.tpl;

        tpl = me.getPicker().tpl = new Ext.XTemplate(
            '<label class="x-form-cb-label">',
            '<input type="checkbox" id="cboxShowAll" checked class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "BPM", "Show all users") +
            '</label><hr />',
            '<tpl for=".">',
            '<div class=" x-boundlist-item">{name}</div>',
            '</tpl>'
        );
        me.getPicker().refresh();
    },

    setUncheckTemplate: function () {
        var me = this,
            picker = me.getPicker(),
            tpl = picker.tpl;

        me.getPicker().tpl = new Ext.XTemplate(
            '<label class="x-form-cb-label">',
            '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "BPM", "Show all users") +
            '</label><hr />',
            '<tpl for=".">',
            '<div class=" x-boundlist-item">{name}</div>',
            '</tpl>'
        );
        me.getPicker().refresh();
    },

    setDisableCheckTemplate: function () {
        var me = this,
            picker = me.getPicker(),
            tpl = picker.tpl;

        me.getPicker().tpl = new Ext.XTemplate(
            '<label class="x-form-cb-label">',
            '<input type="checkbox" id="cboxShowAll" disabled checked class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "BPM", "Show all users") +
            '</label><hr />',
            '<tpl for=".">',
            '<div class=" x-boundlist-item">{name}</div>',
            '</tpl>'
        );
        me.getPicker().refresh();
    },

    listeners: {
        expand: function (combo) {
            Ext.get('cboxShowAll').el.dom.onclick = function (e) {
                combo.handleShowAll(this);
            };
        },

        render: function () {
            var me = this;
            me.store.on('load', function () {
                var checkShowAll = Ext.get('cboxShowAll');
                if (checkShowAll) {
                    checkShowAll.el.dom.onclick = function (e) {
                        me.handleShowAll(me);
                    };
                }
            });
            //me.loadStore();
        },

        change: function (combo, newValue) {
            var me = this;

            me.userChangeEvent = true;
            if (me.workgroupChangeEvent && me.userChangeEvent) {
                me.initializeUserCombo();
            }
        },

        workgroupFirstChanged: function (workgroupId) {
            var me = this;

            me.workgroupId = workgroupId;
            me.workgroupChangeEvent = true;
            if (me.workgroupChangeEvent && me.userChangeEvent) {
                me.initializeUserCombo();
            }
        },

        workgroupChanged: function (workgroupId) {
            var me = this;
            me.workgroupId = workgroupId;
            me.checked = false;
            me.setUncheckTemplate();
            var unassignedObject = me.getStore().getRange().filter(function (user) {
                return user.get('id') == -1;
            })[0];
            me.select(unassignedObject.get(me.valueField));
            me.loadStore();
        },
        workgroupEnableChanged: function (enable) {
            var me = this;

            if (enable == false) {
                me.checked = true;
                me.setDisableCheckTemplate();
                me.loadStore();
            }
            else {
                me.setUncheckTemplate();
            }
        }
    },

    loadStore: function () {
        var me = this;

        me.store.getProxy().url = me.checked ? me.allUsersUrl : Ext.String.format(me.workgroupUsersUrl, me.workgroupId);
        me.store.load();
    },

    initializeUserCombo: function () {
        var me = this,
            value = me.getValue();

        me.workgroupChangeEvent = false;
        me.userChangeEvent = false;
        if (me.isDisabled()) {
            return;
        }
        Ext.Ajax.request({
            url: Ext.String.format(me.workgroupUsersUrl, me.workgroupId),
            method: 'GET',
            success: function (response) {
                var users = Ext.JSON.decode(response.responseText).data;
                var id = me.valueField;
                if (Ext.isObject(users.find(function (user) {
                        return user[id] == value;
                    })) == false) {
                    me.checked = true;
                    me.setCheckTemplate();
                    me.loadStore();
                }
                else {
                    me.checked = false;
                    me.setUncheckTemplate();
                    me.loadStore();
                }
            }
        });
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        var selAll = me.onListSelectionChange;

        me.onListSelectionChange = function () {
            if (me.getPicker().getSelectionModel().selected.length != 0) {
                selAll.apply(this, arguments);
            }
        };
    }
});

