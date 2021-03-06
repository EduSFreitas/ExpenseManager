package com.ramitsuri.expensemanagerlegacy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.ramitsuri.expensemanagerlegacy.entities.Category;
import com.ramitsuri.expensemanagerlegacy.entities.Expense;
import com.ramitsuri.expensemanagerlegacy.entities.PaymentMethod;
import com.ramitsuri.expensemanagerlegacy.helper.AppHelper;
import com.ramitsuri.expensemanagerlegacy.helper.DateHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDB extends BaseDB {

    public ExpenseDB(Context context) {
        super(context);
    }

    public String[] getAllColumns() {
        return new String[] {
                DBConstants.COLUMN_EXPENSE_ROW_ID,
                DBConstants.COLUMN_EXPENSE_DATE_TIME,
                DBConstants.COLUMN_EXPENSE_AMOUNT,
                DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID,
                DBConstants.COLUMN_EXPENSE_CATEGORY_ID,
                DBConstants.COLUMN_EXPENSE_NOTES,
                DBConstants.COLUMN_EXPENSE_STORE,
                DBConstants.COLUMN_EXPENSE_SYNC_STATUS,
                DBConstants.COLUMN_EXPENSE_FLAGGED
        };
    }

    public String[] getAllJoinColumns() {
        String rowIdColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_ROW_ID);
        String dateTimeColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_DATE_TIME);
        String amountColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_AMOUNT);
        String notesColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_NOTES);
        String storeColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_STORE);
        String syncStatusColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_SYNC_STATUS);
        String flagColumn =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_FLAGGED);
        String paymentMethodIdColumn =
                getCol(DBConstants.TABLE_PAYMENT_METHOD, DBConstants.COLUMN_PAYMENT_METHOD_ID);
        String paymentMethodNameColumn =
                getCol(DBConstants.TABLE_PAYMENT_METHOD, DBConstants.COLUMN_PAYMENT_METHOD_NAME);
        String categoryIdColumn =
                getCol(DBConstants.TABLE_CATEGORIES, DBConstants.COLUMN_CATEGORIES_ID);
        String categoryNameColumn =
                getCol(DBConstants.TABLE_CATEGORIES, DBConstants.COLUMN_CATEGORIES_NAME);

        String[] columns = {
                rowIdColumn,
                dateTimeColumn,
                amountColumn,
                notesColumn,
                storeColumn,
                syncStatusColumn,
                flagColumn,
                paymentMethodIdColumn,
                paymentMethodNameColumn,
                categoryIdColumn,
                categoryNameColumn
        };
        return columns;
    }

    public ContentValues getExpensesContentValues(Expense expense) {

        long dateTime = expense.getDateTime();
        String amount = String.valueOf(expense.getAmount());
        int paymentMethodId = expense.getPaymentMethod().getId();
        int categoryID = expense.getCategory().getId();
        String notes = expense.getDescription();
        String store = expense.getStore();
        boolean syncStatus = expense.isSynced();
        boolean flagged = expense.isFlagged();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_DATE_TIME, dateTime);
        contentValues.put(DBConstants.COLUMN_EXPENSE_AMOUNT, amount);
        contentValues.put(DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID, paymentMethodId);
        contentValues.put(DBConstants.COLUMN_EXPENSE_CATEGORY_ID, categoryID);
        contentValues.put(DBConstants.COLUMN_EXPENSE_NOTES, notes);
        contentValues.put(DBConstants.COLUMN_EXPENSE_STORE, store);
        contentValues.put(DBConstants.COLUMN_EXPENSE_SYNC_STATUS, syncStatus);
        contentValues.put(DBConstants.COLUMN_EXPENSE_FLAGGED, flagged);

        return contentValues;
    }

    private Expense getExpenseFromCursor(Cursor cursor) {
        Expense expense = new Expense();
        Category category = new Category();
        PaymentMethod paymentMethod = new PaymentMethod();
        for (String column : cursor.getColumnNames()) {
            if (column.equals(DBConstants.COLUMN_EXPENSE_ROW_ID)) {
                String value = cursor.getString(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_ROW_ID));
                expense.setRowIdentifier(value);
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_DATE_TIME)) {
                long value = cursor.getLong(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_DATE_TIME));
                expense.setDateTime(value);
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_AMOUNT)) {
                String value = cursor.getString(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_AMOUNT));
                expense.setAmount(new BigDecimal(value));
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_NOTES)) {
                String value = cursor.getString(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_NOTES));
                expense.setDescription(value);
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_STORE)) {
                String value = cursor.getString(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_STORE));
                expense.setStore(value);
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_SYNC_STATUS)) {
                int value = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_SYNC_STATUS));
                expense.setIsSynced(isTrue(value));
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_FLAGGED)) {
                int value = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_FLAGGED));
                expense.setIsFlagged(isTrue(value));
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID)) {
                int value = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID));
                paymentMethod.setId(value);
            } else if (column.equals(DBConstants.COLUMN_PAYMENT_METHOD_NAME)) {
                String value = cursor.getString(
                        cursor.getColumnIndex(DBConstants.COLUMN_PAYMENT_METHOD_NAME));
                paymentMethod.setName(value);
            } else if (column.equals(DBConstants.COLUMN_EXPENSE_CATEGORY_ID)) {
                int value = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.COLUMN_EXPENSE_CATEGORY_ID));
                category.setId(value);
            } else if (column.equals(DBConstants.COLUMN_CATEGORIES_NAME)) {
                String value = cursor.getString(
                        cursor.getColumnIndex(DBConstants.COLUMN_CATEGORIES_NAME));
                category.setName(value);
            }
        }
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        return expense;
    }

    public synchronized boolean setExpense(Expense expense) {
        open();
        long date = DateHelper.getTodaysLongDate();
        if (date != (AppHelper.getLastAddedID() / 1000)) {
            AppHelper.setLastAddedID(date * 1000L);
        }
        long rowIdLong = AppHelper.getLastAddedID() + 1;
        AppHelper.setLastAddedID(rowIdLong);
        boolean insertSuccess = true;
        ContentValues contentValues = getExpensesContentValues(expense);
        contentValues.put(DBConstants.COLUMN_EXPENSE_ROW_ID, String.valueOf(rowIdLong));
        long result = mDatabase.insertOrThrow(DBConstants.TABLE_EXPENSES, null,
                contentValues);
        if (result <= 0) {
            insertSuccess = false;
        }
        close();
        return insertSuccess;
    }

    public Expense getExpense(String id) {
        open();
        String table = getJoinTable(
                DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID,
                DBConstants.COLUMN_EXPENSE_CATEGORY_ID,
                DBConstants.TABLE_PAYMENT_METHOD, DBConstants.COLUMN_PAYMENT_METHOD_ID,
                DBConstants.TABLE_CATEGORIES,
                DBConstants.COLUMN_CATEGORIES_ID);

        String[] columns = getAllJoinColumns();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ? ";
        String[] selectionArgs = new String[] {
                id
        };

        Cursor cursor = getCursor(table, columns, selection,
                selectionArgs, null, null, null, null);

        Expense expense = null;
        try {
            if (cursor.moveToFirst()) {
                expense = getExpenseFromCursor(cursor);
            }
        } catch (Exception e) {

        }

        cursor.close();
        close();
        return expense;
    }

    public Expense getTopExpenseOnDay(long date) {

        Expense expense = getTopExpenseInPeriod(date, 0);
        return expense;
    }

    public Expense getTopExpenseInPeriod(long startDate, long endDate) {
        open();
        String table = getJoinTable(
                DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID,
                DBConstants.COLUMN_EXPENSE_CATEGORY_ID,
                DBConstants.TABLE_PAYMENT_METHOD, DBConstants.COLUMN_PAYMENT_METHOD_ID,
                DBConstants.TABLE_CATEGORIES,
                DBConstants.COLUMN_CATEGORIES_ID);

        String[] columns = getAllJoinColumns();

        String selection =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_DATE_TIME) +
                        " BETWEEN ? AND ?";
        String[] selectionArgs = {
                String.valueOf(startDate),
                String.valueOf(endDate)
        };
        if (endDate == 0) {
            selection = getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_DATE_TIME) +
                    " = ?";
            selectionArgs = new String[] {String.valueOf(startDate)};
        }

        String orderBy = getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_AMOUNT) +
                " DESC";
        Cursor cursor = getCursor(table, columns, selection,
                selectionArgs, null, null, orderBy, null);

        Expense expense = null;
        try {
            if (cursor.moveToFirst()) {
                expense = getExpenseFromCursor(cursor);
            }
        } catch (Exception e) {

        }

        cursor.close();
        close();
        return expense;
    }

    public List<Expense> getAllExpense(String selection, String[] selectionArgs) {
        open();

        String table = getJoinTable(
                DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID,
                DBConstants.COLUMN_EXPENSE_CATEGORY_ID,
                DBConstants.TABLE_PAYMENT_METHOD, DBConstants.COLUMN_PAYMENT_METHOD_ID,
                DBConstants.TABLE_CATEGORIES,
                DBConstants.COLUMN_CATEGORIES_ID);

        String[] columns = getAllJoinColumns();

        Cursor cursor = getCursor(table, columns, selection, selectionArgs, null, null, null, null);

        List<Expense> expenses = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Expense expense = getExpenseFromCursor(cursor);
                    expenses.add(expense);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {

        }

        cursor.close();
        close();

        return expenses;
    }

    public List<Expense> getAllExpenseInDateRange(long startDate, long endDate) {
        open();

        String selection =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_DATE_TIME) +
                        " BETWEEN ? AND ?";
        String[] selectionArgs = {
                String.valueOf(startDate),
                String.valueOf(endDate)
        };

        return getAllExpense(selection, selectionArgs);
    }

    public List<Expense> getAllExpenseForDay(long date) {
        open();

        String selection =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_DATE_TIME) +
                        " = ?";
        String[] selectionArgs = {
                String.valueOf(date)
        };

        return getAllExpense(selection, selectionArgs);
    }

    public boolean editExpenseDateTime(String rowId, long dateTime) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_DATE_TIME, dateTime);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpenseAmount(String rowId, BigDecimal amount) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_AMOUNT, String.valueOf(amount));

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpenseDescription(String rowId, String description) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_NOTES, description);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpenseStore(String rowId, String store) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_STORE, store);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpenseSyncStatus(String rowId, boolean syncStatus) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_SYNC_STATUS, syncStatus);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpenseFlag(String rowId, boolean isFlagged) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_FLAGGED, isFlagged);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpensePaymentMethodId(String rowId, int id) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_PAYMENT_METHOD_ID, id);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public boolean editExpenseCategoryId(String rowId, int id) {
        open();

        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                rowId
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_EXPENSE_CATEGORY_ID, id);

        int result = mDatabase.update(
                DBConstants.TABLE_EXPENSES,
                contentValues,
                selection,
                selectionArgs
        );
        close();

        return result > 0;
    }

    public synchronized boolean deleteExpense(String id) {
        open();
        String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
        String[] selectionArgs = new String[] {
                id
        };

        int result = mDatabase.delete(
                DBConstants.TABLE_EXPENSES,
                selection,
                selectionArgs
        );

        close();

        return result > 0;
    }

    public synchronized boolean deleteBackedUpExpense() {
        open();
        String selection = DBConstants.COLUMN_EXPENSE_SYNC_STATUS + " = ?";
        String[] selectionArgs = new String[] {
                String.valueOf(1)
        };

        int result = mDatabase.delete(
                DBConstants.TABLE_EXPENSES,
                selection,
                selectionArgs
        );

        close();

        return result > 0;
    }

    public void deleteAllExpense() {
        open();

        mDatabase.execSQL("delete from " + DBConstants.TABLE_EXPENSES);
        close();
    }

    public List<Expense> getAllExpensesRequiringBackup() {
        open();

        String selection =
                getCol(DBConstants.TABLE_EXPENSES, DBConstants.COLUMN_EXPENSE_SYNC_STATUS) +
                        " = ?";
        String[] selectionArgs = {
                String.valueOf(0)
        };

        return getAllExpense(selection, selectionArgs);
    }

    public synchronized boolean updateExpensesSyncStatus(List<Expense> backedUpExpenses) {
        open();
        List results = new ArrayList();
        for (Expense expense : backedUpExpenses) {
            String selection = DBConstants.COLUMN_EXPENSE_ROW_ID + " = ?";
            String[] selectionArgs = new String[] {
                    expense.getRowIdentifier()
            };

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBConstants.COLUMN_EXPENSE_SYNC_STATUS, 1);

            int result = mDatabase.update(
                    DBConstants.TABLE_EXPENSES,
                    contentValues,
                    selection,
                    selectionArgs
            );
            results.add(result);
        }
        return (results.size() == backedUpExpenses.size());
    }
}
