window.location.href = '#/workspace/datacollection/issues';

describe('Data collection issues', function () {
    var delay = 5000,
        issuesController,
        issuesStore;

    beforeEach(function () {
        issuesController = Mtr.getApplication().getController('Issues');
        issuesStore = issuesController.getStore('Mtr.store.Issues');

        expect(issuesStore).toBeTruthy();

        waitsFor(function () {
                return !issuesStore.isLoading();
            },
            'loading issues store',
            delay
        );
    });

    it('has loaded issue detail', function () {
        var grid = issuesController.getIssuesList().getView(),
            record = issuesStore.getAt(0),
            itemPanel = issuesController.getItemPanel(),
            flag;

        runs(function () {
            itemPanel.on('change', function () {
                flag = true;
            });
            issuesController.loadGridItemDetail(grid, record);
        });

        waitsFor(function () {
                return flag;
            },
            'loading issue detail',
            delay
        );

        runs(function () {
            expect(itemPanel.down('toolbar')).not.toBeUndefined();
        });
    });

    it('has not loaded issue detail', function () {
        var itemPanel = issuesController.getItemPanel();

        issuesController.showDefaultItems();

        expect(itemPanel.down('toolbar')).toBeNull();
    });

    it('has clicked issues grid action icon', function () {
        var actionCells = Ext.query('[id^=issues-list] .x-grid-cell-inner-action-col'),
            grid = issuesController.getIssuesList().getView(),
            firstActionCell = actionCells[0],
            firstActionCellEll = Ext.get(firstActionCell),
            menuBtn;

        firstActionCell.click();
        menuBtn = firstActionCellEll.next();

        expect(firstActionCellEll.getHeight()).toEqual(0);
        expect(firstActionCellEll.isVisible()).toBeFalsy();
        expect(menuBtn).not.toBeNull();
        expect(menuBtn.isVisible()).toBeTruthy();
    });
});