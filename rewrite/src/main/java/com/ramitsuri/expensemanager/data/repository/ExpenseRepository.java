package com.ramitsuri.expensemanager.data.repository;

import com.ramitsuri.expensemanager.AppExecutors;
import com.ramitsuri.expensemanager.IntDefs.SourceType;
import com.ramitsuri.expensemanager.data.DummyData;
import com.ramitsuri.expensemanager.data.ExpenseManagerDatabase;
import com.ramitsuri.expensemanager.entities.Expense;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ExpenseRepository {

    @SourceType
    private int mSourceType;
    private AppExecutors mExecutors;
    private ExpenseManagerDatabase mDatabase;

    private LiveData<List<Expense>> mExpenses;

    public ExpenseRepository(AppExecutors executors, ExpenseManagerDatabase database,
            @SourceType int sourceType) {
        mExecutors = executors;
        mDatabase = database;
        mSourceType = SourceType.DB;
        if (mSourceType == SourceType.LOCAL) {
            //mExpenses  = DummyData.get();
        } else if (mSourceType == SourceType.DB) {
            mExpenses = mDatabase.expenseDao().getAll();
        }
    }

    public LiveData<List<Expense>> getExpenses() {
        return mExpenses;
    }

    public LiveData<List<Expense>> getStarredExpenses() {
        final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Expense> values = null;
                if (mSourceType == SourceType.LOCAL) {
                    values = DummyData.getAllStarred();
                } else if (mSourceType == SourceType.DB) {
                    values = mDatabase.expenseDao().getAllStarred();
                }
                expenses.postValue(values);
            }
        });
        return expenses;
    }

    public LiveData<List<Expense>> getUnsyncedExpenses() {
        final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Expense> values = null;
                if (mSourceType == SourceType.LOCAL) {
                    values = DummyData.getAllUnsynced();
                } else if (mSourceType == SourceType.DB) {
                    values = mDatabase.expenseDao().getAllUnsynced();
                }
                expenses.postValue(values);
            }
        });
        return expenses;
    }

    public void insertExpense(final Expense expense) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().insert(expense);
            }
        });
    }

    public void editExpense(final Expense expense) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().updateExpense(expense);
            }
        });
    }

    public void deleteExpense(final Expense expense) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().deleteExpense(expense.getId());
            }
        });
    }

    public void deleteExpenses() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().deleteAll();
            }
        });
    }
}