window.location.href = '#/workspace/datacollection/issues';

describe('IssuesController events', function () {
    var delay = 5000,
        issuesController,
        issuesStore;

    beforeEach(function () {
        issuesController = Isu.getApplication().getIssuesController();
        issuesStore = issuesController.getStore('Isu.store.Issues');

        expect(issuesStore).toBeTruthy();

        waitsFor(function () {
                return !issuesStore.isLoading();
            },
            'loading issues store',
            delay
        );
    });

    afterEach(function() {
        issuesController.sortParams.sort = 'dueDate';
        issuesController.sortParams.order = 'asc';
    });

    it('has loaded view', function () {
        var view = issuesController.getIssuesList();
        expect(view.isVisible()).toBeTruthy();
    });

    it('has deleted sorting', function () {
        var clearSortBtn = Ext.ComponentQuery.query('button[name=clearsortbtn]')[0];
        issuesController.clearSort(clearSortBtn);
        expect(issuesController.sortParams.sort).toBeUndefined();
    });

    it('has grouping by reason', function () {
        var field = Ext.ComponentQuery.query('combobox[name=groupnames]')[0],
            itemPanel = issuesController.getItemPanel();
        issuesController.setGroupFields(field);
        var newValue = field.store.getAt(1);
        issuesController.setGroup(field, newValue);
        expect(itemPanel.down('toolbar')).toBeNull();
        expect(issuesController.getIssueNoGroup().isVisible()).toBeTruthy();
        expect(issuesController.getIssuesList().isHidden()).toBeTruthy();
        expect(issuesController.groupStore.proxy.extraParams.reason).toBe(newValue);
    });

    it('has default asc sorting', function () {
        expect(issuesController.sortParams.order).toBe('asc');
    });
});