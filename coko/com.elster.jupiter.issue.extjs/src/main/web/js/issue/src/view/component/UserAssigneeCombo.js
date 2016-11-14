Ext.define('Isu.view.component.UserAssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-user-assignee-combo',
    checked: false,
    requires: [
        'Isu.store.IssueWorkgroupAssignees'
    ],
    tpl: new Ext.XTemplate(
        '<label class="x-form-cb-label">' +
        '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "ISU", "Show all users") +
        '</label><hr />',
        '<tpl for=".">',
        '<div class=" x-boundlist-item">{name}</div>',
        '</tpl>'
    ),
    workgroupId: -1,

    handleShowAll: function (checkBox) {
        var me = this;

        me.checked = document.getElementById("cboxShowAll").checked;
        me.checked && me.setCheckTemplate();
        !me.checked && me.setUncheckTemplate();

        me.getPicker().refresh();
        me.loadStore();
    },

    setCheckTemplate: function () {
        var me = this,
            picker = me.getPicker(),
            tpl = picker.tpl;

        tpl = me.getPicker().tpl = new Ext.XTemplate(
            '<label class="x-form-cb-label">',
                '<input type="checkbox" id="cboxShowAll" checked class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "ISU", "Show all users") +
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
                '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>' + Uni.I18n.translate("action.assign.showAllUsers", "ISU", "Show all users") +
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
            me.loadStore();
        },

        change: function (combo, newValue) {
            var me = this;

            Ext.Ajax.request({
                url: '/api/isu/workgroups/' + me.workgroupId + '/users',
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
            if ((workgroupId == -1) && me.getValue() && me.getValue() > 0) {
                me.checked = true;
                me.setCheckTemplate();
                me.loadStore();
            }
            else {
                var selectedValue = me.value;


                if (selectedValue && me.checked == false) {
                    me.store.getProxy().url = '/api/isu/workgroups/' + me.workgroupId + '/users';
                    me.store.load(function (users) {

                        if (Ext.isObject(users.find(function (user) {
                                return user.get('id') == selectedValue;
                            })) == false) {
                            me.select(-1);
                        }
                    });
                }
            }

        }
    },

    loadStore: function () {
        var me = this;

        me.store.getProxy().url = me.checked ? '/api/isu/assignees/users' : '/api/isu/workgroups/' + me.workgroupId + '/users';
        me.store.load();
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

