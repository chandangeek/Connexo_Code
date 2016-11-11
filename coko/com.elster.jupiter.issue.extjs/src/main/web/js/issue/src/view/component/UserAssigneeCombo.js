Ext.define('Isu.view.component.UserAssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-user-assignee-combo',
    checked: false,
    tpl: new Ext.XTemplate(
        '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>',
        '<label class="x-form-cb-label">' + Uni.I18n.translate("action.assign.showAllUsers", "ISU", "Show all users") + '</label><hr />',
        '<tpl for=".">',
        '<div class=" x-boundlist-item">{name}</div>',
        '</tpl>'
    ),
    workgroupID: -1,

    handleShowAll: function (checkBox) {
        var me = this;

        me.value = -1;
        me.checked = document.getElementById("cboxShowAll").checked;
        if (me.checked) {
            me.getPicker().tpl = new Ext.XTemplate(
                '<input type="checkbox" id="cboxShowAll" checked class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>',
                '<label class="x-form-cb-label">' + Uni.I18n.translate("action.assign.showAllUsers", "ISU", "Show all users") + '</label><hr />',
                '<tpl for=".">',
                '<div class=" x-boundlist-item">{name}</div>',
                '</tpl>'
            );
        }
        else {
            me.getPicker().tpl = new Ext.XTemplate(
                '<input type="checkbox" id="cboxShowAll" class=" x-form-checkbox x-form-field .x-form-cb" style="vertical-align: middle; bottom: 20px;"/>',
                '<label class="x-form-cb-label">' + Uni.I18n.translate("action.assign.showAllUsers", "ISU", "Show all users") + '</label><hr />',
                '<tpl for=".">',
                '<div class=" x-boundlist-item">{name}</div>',
                '</tpl>'
            );
        }
        me.getPicker().refresh();
        me.store.getProxy().url = me.checked ? '/api/isu/workgroups/' + me.workgroupID + '/users' : '/api/isu/assignees/users';
        me.store.load();
    },


    listeners: {
        expand: function (combo) {
            Ext.get('cboxShowAll').el.dom.onclick = function (e) {
                combo.handleShowAll(this);
            };
        },
        collapse: function (combo) {
            Ext.get('cboxShowAll').el.dom.onclick = function () {
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
            me.store.getProxy().url = me.checked ? '/api/isu/workgroups/' + me.workgroupID + '/users' : '/api/isu/assignees/users';
            me.store.load();
        }
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

