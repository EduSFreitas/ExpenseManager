package com.ramitsuri.expensemanager;

import android.util.Log;

import com.ramitsuri.expensemanager.data.dao.ExpenseDao;
import com.ramitsuri.expensemanager.data.dummy.Expenses;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.Filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ExpenseDatabaseTest extends BaseDatabaseTest {

    private ExpenseDao mExpenseDao;

    @Before
    @Override
    public void createDb() {
        super.createDb();
        mExpenseDao = mDb.expenseDao();

        for (Expense expense : Expenses.all()) {
            mExpenseDao.insert(expense);
        }
    }

    @Test
    public void testGetAll() throws Exception {
        // Get all
        Assert.assertEquals(Expenses.getExpenses().size(), mExpenseDao.getExpenses().size());
        Log.d(TAG, mExpenseDao.getExpenses().toString());
    }

    @Test
    public void testGetAllStarred() throws Exception {
        // Get all starred
        Assert.assertEquals(
                Expenses.getAllStarred().size(),
                mExpenseDao.getAllStarred().size());
    }

    @Test
    public void testGetAllUnsynced() throws Exception {
        // get All unsynced
        Assert.assertEquals(
                Expenses.getAllUnsynced().size(),
                mExpenseDao.getAllUnsynced().size());
    }

    @Test
    public void testGetAllForBackup() throws Exception {
        // get All for backup
        List<Integer> monthIndices = new ArrayList<>();
        monthIndices.add(6);
        monthIndices.add(5);
        monthIndices.add(4);
        Assert.assertEquals(
                Expenses.getAllForBackup(monthIndices).size(),
                mExpenseDao.getAllForBackup(monthIndices).size());

        monthIndices = new ArrayList<>();
        monthIndices.add(0);
        Assert.assertEquals(
                Expenses.getAllForBackup(monthIndices).size(),
                mExpenseDao.getAllForBackup(monthIndices).size());

        monthIndices = new ArrayList<>();
        monthIndices.add(11);
        monthIndices.add(9);
        Assert.assertEquals(
                Expenses.getAllForBackup(monthIndices).size(),
                mExpenseDao.getAllForBackup(monthIndices).size());

        monthIndices = new ArrayList<>();
        Assert.assertEquals(
                Expenses.getAllForBackup(monthIndices).size(),
                mExpenseDao.getAllForBackup(monthIndices).size());
    }

    @Test
    public void testGetAllForDateRange() throws Exception {
        // get All within date range
        long start = Expenses.BASE_DATE_TIME;
        long end = Expenses.BASE_DATE_TIME + Expenses.ONE_DAY;
        Assert.assertEquals(Expenses.getAllForDateRange(start, end).size(),
                mExpenseDao.getExpensesForDateRange(start, end).size());
        start = Expenses.BASE_DATE_TIME - Expenses.ONE_DAY;
        end = Expenses.BASE_DATE_TIME + 2 * Expenses.ONE_DAY;
        Assert.assertEquals(Expenses.getAllForDateRange(start, end).size(),
                mExpenseDao.getExpensesForDateRange(start, end).size());
        start = Expenses.BASE_DATE_TIME - 3 * Expenses.ONE_DAY;
        end = Expenses.BASE_DATE_TIME + 3 * Expenses.ONE_DAY;
        Assert.assertEquals(Expenses.getAllForDateRange(start, end).size(),
                mExpenseDao.getExpensesForDateRange(start, end).size());
    }

    @Test
    public void testDeleteSynced() throws Exception {
        // delete synced
        mExpenseDao.deleteSynced();
        Assert.assertEquals(
                0,
                mExpenseDao.getIncomes().size() + mExpenseDao.getExpenses().size()
                        - mExpenseDao.getAllUnsynced().size());
    }

    @Test
    public void testDeleteAll() throws Exception {
        // delete all
        mExpenseDao.deleteAll();
        Assert.assertEquals(
                0,
                mExpenseDao.getExpenses().size());
        Assert.assertEquals(
                0,
                mExpenseDao.getIncomes().size());
    }

    @Test
    public void testAddAll() throws Exception {
        // add all
        mExpenseDao.deleteAll();
        mExpenseDao.insert(Expenses.all());

        Assert.assertEquals(
                Expenses.all().size(),
                mExpenseDao.getExpenses().size() + mExpenseDao.getIncomes().size());
    }

    @Test
    public void testUpdateUnsynced() throws Exception {
        // update unsynced
        mExpenseDao.updateUnsynced();
        Assert.assertEquals(
                0,
                mExpenseDao.getAllUnsynced().size());
    }

    @Test
    public void testSetStarred() throws Exception {
        // set starred
        List<Expense> expenses = mExpenseDao.getExpenses();
        for (Expense expense : expenses) {
            if (!expense.isStarred()) {
                mExpenseDao.setStarred(expense.getId());
                break;
            }
        }
        Assert.assertEquals(
                Expenses.getAllStarred().size() + 1,
                mExpenseDao.getAllStarred().size());
    }

    @Test
    public void testSetUnstarred() throws Exception {
        // set unstarred
        mExpenseDao.setUnstarred(mExpenseDao.getExpenses().get(3).getId());
        Assert.assertEquals(
                Expenses.getAllStarred().size() - 1,
                mExpenseDao.getAllStarred().size());
    }

    @Test
    public void testUpdateSetAllUnsynced() {
        Assert.assertNotEquals(mExpenseDao.getAllUnsynced().size(),
                mExpenseDao.getExpenses().size());

        mExpenseDao.updateSetAllUnsynced();
        Assert.assertEquals(mExpenseDao.getAllUnsynced().size(),
                mExpenseDao.getExpenses().size() + mExpenseDao.getIncomes().size());
    }

    @Test
    public void testGetIncomes() {
        Assert.assertEquals(Expenses.getIncomes().size(), mExpenseDao.getIncomes().size());
    }

    @Test
    public void testGetForFilter() {
        Filter filter;
        for (int i = 0; i < 12; i++) {
            filter = new Filter();
            filter.setIsIncome(false);
            filter.addMonthIndex(i);
            Assert.assertEquals(Expenses.getForFilter(filter).size(),
                    mExpenseDao.getForFilter(filter).size());
        }
        filter = new Filter();
        for (int i = 0; i < 12; i++) {
            filter.addMonthIndex(i);
            filter.setIsIncome(true);
            Assert.assertEquals(Expenses.getForFilter(filter).size(),
                    mExpenseDao.getForFilter(filter).size());
        }
        filter = new Filter();
        for (int i = 0; i < 12; i++) {
            filter.addMonthIndex(i);
            filter.setIsIncome(false);
            Assert.assertEquals(Expenses.getForFilter(filter).size(),
                    mExpenseDao.getForFilter(filter).size());
        }

        filter = new Filter();
        filter.setIsIncome(true);
        Assert.assertEquals(Expenses.getForFilter(filter).size(),
                mExpenseDao.getForFilter(filter).size());

        filter = new Filter();
        filter.setIsIncome(false);
        Assert.assertEquals(Expenses.getForFilter(filter).size(),
                mExpenseDao.getForFilter(filter).size());

        filter = new Filter();
        filter.addMonthIndex(7);
        filter.addMonthIndex(4);
        filter.setIsIncome(false);
        filter.setIsStarred(false);

        filter.addCategory("Food")
                .addCategory("Travel")
                .addCategory("Groceries");

        filter.addPaymentMethod("Chase")
                .addPaymentMethod("amazon");
        Assert.assertEquals(Expenses.getForFilter(filter).size(),
                mExpenseDao.getForFilter(filter).size());

        filter = new Filter();
        filter.addMonthIndex(5);
        filter.addMonthIndex(4);
        filter.setIsIncome(false);
        filter.setIsStarred(false);

        filter.addCategory("Utilities");

        filter.addPaymentMethod("Card4");
        Assert.assertEquals(Expenses.getForFilter(filter).size(),
                mExpenseDao.getForFilter(filter).size());
    }

    @Test
    public void testGetStores() {
        String startsWith = "PN";
        Assert.assertEquals(Expenses.getStores(startsWith).size(),
                mExpenseDao.getStores(startsWith).size());

        startsWith = "pn";
        Assert.assertEquals(Expenses.getStores(startsWith).size(),
                mExpenseDao.getStores(startsWith).size());

        startsWith = "CO";
        Assert.assertEquals(Expenses.getStores(startsWith).size(),
                mExpenseDao.getStores(startsWith).size());

        startsWith = "zx";
        Assert.assertEquals(Expenses.getStores(startsWith).size(),
                mExpenseDao.getStores(startsWith).size());

        startsWith = "H";
        Assert.assertEquals(Expenses.getStores(startsWith).size(),
                mExpenseDao.getStores(startsWith).size());

        startsWith = "Jess";
        Assert.assertEquals(Expenses.getStores(startsWith).size(),
                mExpenseDao.getStores(startsWith).size());
    }

    @Test
    public void testGetEntities() {
        String store = "Publix";

        Assert.assertNotNull(Expenses.getForStore(store));
        Assert.assertNotNull(mExpenseDao.getForStore(store));
        Assert.assertEquals(Objects.requireNonNull(Expenses.getForStore(store)).getCategory(),
                mExpenseDao.getForStore(store).getCategory());
        Assert.assertEquals(Objects.requireNonNull(Expenses.getForStore(store)).getPaymentMethod(),
                mExpenseDao.getForStore(store).getPaymentMethod());

        store = "publix";

        Assert.assertNotNull(Expenses.getForStore(store));
        Assert.assertNotNull(mExpenseDao.getForStore(store));
        Assert.assertEquals(Objects.requireNonNull(Expenses.getForStore(store)).getCategory(),
                mExpenseDao.getForStore(store).getCategory());
        Assert.assertEquals(Objects.requireNonNull(Expenses.getForStore(store)).getPaymentMethod(),
                mExpenseDao.getForStore(store).getPaymentMethod());

        store = "Root down";

        Assert.assertNull(Expenses.getForStore(store));
        Assert.assertNull(mExpenseDao.getForStore(store));
    }

    @Test
    public void testUpdateSyncedForQuery() {
        TimeZone timeZone = TimeZone.getDefault();

        for (int i = 0; i < 12; i++) {
            Filter filter = new Filter();
            filter.setSynced(true);
            filter.addMonthIndex(i, timeZone);

            mExpenseDao.updateSetUnsynced(i);
            Assert.assertEquals(
                    0,
                    mExpenseDao.getForFilter(filter).size());
        }
    }
}
