package com.ramitsuri.expensemanager.data.repository;

import com.ramitsuri.expensemanager.AppExecutors;
import com.ramitsuri.expensemanager.data.ExpenseManagerDatabase;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.Filter;

import java.util.List;

import javax.annotation.Nonnull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ExpenseRepository {
    private AppExecutors mExecutors;
    private ExpenseManagerDatabase mDatabase;
    private MutableLiveData<List<Expense>> mExpenses;
    private MutableLiveData<List<String>> mStores;

    public ExpenseRepository(AppExecutors executors, ExpenseManagerDatabase database) {
        mExecutors = executors;
        mDatabase = database;
        mExpenses = new MutableLiveData<>();
        mStores = new MutableLiveData<>();
    }

    public LiveData<List<Expense>> getExpenses() {
        return mExpenses;
    }

    public void getForFilter(@Nonnull final Filter filter) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Expense> values = mDatabase.expenseDao().getForFilter(filter);
                mExpenses.postValue(values);
            }
        });
    }

    public void getIncomes() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Expense> values = mDatabase.expenseDao().getIncomes();
                mExpenses.postValue(values);
            }
        });
    }

    public LiveData<List<String>> getStores() {
        return mStores;
    }

    public LiveData<List<String>> getCategories() {
        final MutableLiveData<List<String>> categories = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<String> values = mDatabase.expenseDao().getCategories();
                categories.postValue(values);
            }
        });
        return categories;
    }

    public LiveData<List<String>> getPaymentMethods() {
        final MutableLiveData<List<String>> paymentMethods = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<String> values = mDatabase.expenseDao().getPaymentMethods();
                paymentMethods.postValue(values);
            }
        });
        return paymentMethods;
    }

    public void refreshStores(final String startsWith) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<String> values = mDatabase.expenseDao().getStores(startsWith);
                mStores.postValue(values);
            }
        });
    }

    public LiveData<Expense> getForStore(@Nonnull final String store) {
        final MutableLiveData<Expense> expense = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Expense value = mDatabase.expenseDao().getForStore(store);
                expense.postValue(value);
            }
        });
        return expense;
    }

    public void insert(final Expense expense) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().insert(expense);
            }
        });
    }

    public void edit(final Expense expense) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().updateExpense(expense);
            }
        });
    }

    public void delete(final Expense expense,
            @Nonnull Filter filter) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().deleteExpense(expense.getId());
            }
        });

        // Refresh expenses as they don't refresh automatically
        getForFilter(filter);
    }

    public void delete() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().deleteAll();
            }
        });
    }

    public void updateSetAllUnsynced() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().updateSetAllUnsynced();
            }
        });
    }

    public void updateSetUnsynced(final int monthIndex) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().updateSetUnsynced(monthIndex);
            }
        });
    }

    public void insert(@Nonnull final List<Expense> expenses, Filter filter) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.expenseDao().insert(expenses);
            }
        });

        // Refresh expenses as they don't refresh automatically
        getForFilter(filter);
    }

    public LiveData<Expense> insertAndGet(@Nonnull final Expense expense,
            @Nonnull Filter filter) {
        final MutableLiveData<Expense> duplicate = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Expense value = mDatabase.expenseDao().insertAndGetExpense(expense);
                duplicate.postValue(value);
            }
        });

        // Refresh expenses as they don't refresh automatically
        getForFilter(filter);
        return duplicate;
    }
}
