/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.UserAssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.usr-assignee-combo',
    checked: false,
    requires: [
        //'Isu.store.IssueWorkgroupAssignees'
    ],
    tpl: new Ext.XTemplate(
        '<label class="x-form-cb-label">' +
        '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("assign.showAllUsers", "UNI", "Show all users") +
        '</label><hr />',
        '<tpl for=".">',
        '<div class=" x-boundlist-item">{name}</div>',
        '</tpl>'
    ),
    workgroupId: -1,

    handleShowAll: function (checkBox) {
        var me = this,
            value = me.getValue();

        me.checked = document.getElementById("cboxShowAll").checked;
        me.checked && me.setCheckTemplate();
        !me.checked && me.setUncheckTemplate();

        me.getPicker().refresh();
        me.store.getProxy().url = me.checked ? me.configUrl.userUrl : Ext.String.format(me.configUrl.workroupUsersUrl, me.workgroupId);
        me.store.load(function (records) {
            if (Ext.isObject(records.filter(function (user) {
                    return user.id == value;
                })) == false) {
                me.select(-1);
            }
        });
    },

    setCheckTemplate: function () {
        var me = this,
            picker = me.getPicker(),
            tpl = picker.tpl;

        tpl = me.getPicker().tpl = new Ext.XTemplate(
            '<label class="x-form-cb-label">',
            '<input type="checkbox" id="cboxShowAll" checked class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("assign.showAllUsers", "UNI", "Show all users") +
            '</label><hr />',
            '<tpl for=".">',
            '<div class=" x-boundlist-item">{name}</div>',
            '</tpl>'
        );
    },

    setUncheckTemplate: function () {
        var me = this,
            picker = me.getPicker(),
            tpl = picker.tpl;

        me.getPicker().tpl = new Ext.XTemplate(
            '<label class="x-form-cb-label">',
            '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("assign.showAllUsers", "UNI", "Show all users") +
            '</label><hr />',
            '<tpl for=".">',
            '<div class=" x-boundlist-item">{name}</div>',
            '</tpl>'
        );
    },

    listeners: {
        expand: function (combo) {
            Ext.get('cboxShowAll').el.dom.onclick = function (e) {
                combo.handleShowAll(this);
            };
        },

        render1: function () {
            var me = this;

            me.store.on('load', function (store, records) {
                var checkShowAll = Ext.get('cboxShowAll');
                var users = records.filter(function (record) {
                    return record.id == me.getRawValue();
                });
                if ((users.length == 0) && (checkShowAll == false)) {
                    me.checked = true;
                    me.setCheckTemplate();
                    me.loadStore();
                }
                if (checkShowAll) {
                    checkShowAll.el.dom.onclick = function (e) {
                        me.handleShowAll(me);
                    };
                }
            });
            me.loadStore();
        },

        change1: function (combo, newValue) {
            var me = this;

            Ext.Ajax.request({
                url: Ext.String.format(me.configUrl.workroupUsersUrl, me.workgroupId),
                method: 'GET',
                success: function (response) {
                    var users = Ext.JSON.decode(response.responseText).data;
                    if (Ext.isObject(users.find(function (user) {
                            return user.id == newValue;
                        })) == false) {
                        me.checked = true;
                        me.setCheckTemplate();
                        me.loadStore();
                    }
                }
            });

        },

        workgroupChanged: function (workgroupId) {
            var me = this;

            me.workgroupId = workgroupId;
            me.checked = false;
            me.setUncheckTemplate();
            me.getPicker().refresh();
            me.select(-1);
            me.loadStore();
        }
    },

    loadStore: function () {
        var me = this;

        me.store.getProxy().url = me.checked ? me.configUrl.userUrl : Ext.String.format(me.configUrl.workroupUsersUrl, me.workgroupId),//'/api/isu/assignees/users' : '/api/isu/workgroups/' + me.workgroupId + '/users';
            me.store.load();
    },

    setWorkgroupId: function (workgroupId) {
        this.workgroupId = workgroupId;
        var me = this;

        me.store.on('load', function (store, records) {
            var checkShowAll = Ext.get('cboxShowAll');
            var users = records.filter(function (record) {
                return record.get('id') == me.getValue();
            });
            if ((users.length == 0) && (me.checked == false)) {
                me.checked = true;
                me.setCheckTemplate();
                me.loadStore();
            }
            if (checkShowAll) {
                checkShowAll.el.dom.onclick = function (e) {
                    me.handleShowAll(me);
                };
            }
        });
        me.loadStore();
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

