Ext.define('Mtr.controller.Person', {
    extend: 'Ext.app.Controller',

    stores: [
        'Persons'
    ],

    models: [
        'Person'
    ],

    views: [
        'person.Browse',
        'person.Edit'
    ],

    init: function () {
        this.initMenu();

        this.control({
            '#personList': {
                itemdblclick: this.editPerson
            },
            'personEdit button[action=save]': {
                click: this.updatePerson
            },
            'personEdit button[action=clone]': {
                click: this.clonePerson
            },
            'partyList button[action=save]': {
                click: this.savePersons
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Persons',
            href: Mtr.getApplication().getHistoryPersonController().tokenizeShowOverview(),
            glyph: 'xe01d@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('personBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editPerson: function (grid, record) {
        var view = Ext.widget('personEdit');
        view.down('form').loadRecord(record);
    },
    updatePerson: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord(),
            values = form.getValues();
        record.set(values);
        win.close();
        record.save();
        record.commit();
    },
    clonePerson: function (button) {
        var me = this,
            win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord().copy(),
            values = form.getValues();

        record.set(values);
        win.close();
        record.setId(null);
        record.phantom = true;

        record.save({
            callback: function () {
                record.commit();
                me.getPersonsStore().add(record);
            }
        });
    },
    saveSuccess: function () {
        //alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },
    savePersons: function (button) {
        this.getPersonsStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    }
});