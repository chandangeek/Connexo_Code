window.location.href = '#/workspace/datacollection/issues';

describe('IssuesController Filter events', function () {
    var delay = 5000,
        issuesController,
        issuesStore,
        filterView;

    beforeEach(function () {
        issuesController = Isu.getApplication().getController('Issues');
        issuesStore = issuesController.getStore('Isu.store.Issues');
        filterView = Ext.ComponentQuery.query('issues-filter')[0];

        expect(issuesStore).toBeTruthy();

        waitsFor(function () {
                return !issuesStore.isLoading();
            },
            'loading issues store',
            delay
        );
    });

    it('should delete sorting', function () {
        var clearSortBtn = Ext.ComponentQuery.query('button[name=clearsortbtn]')[0];
        issuesController.clearSort(clearSortBtn);
        expect(issuesController.sortParams.sort).toBeUndefined();
    });

});