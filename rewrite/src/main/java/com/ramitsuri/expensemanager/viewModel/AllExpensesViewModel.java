package com.ramitsuri.expensemanager.viewModel;

import com.ramitsuri.expensemanager.MainApplication;
import com.ramitsuri.expensemanager.data.repository.ExpenseRepository;
import com.ramitsuri.expensemanager.entities.EditedSheet;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.Filter;
import com.ramitsuri.expensemanager.ui.adapter.ExpenseWrapper;
import com.ramitsuri.expensemanager.utils.Calculator;
import com.ramitsuri.expensemanager.utils.DateHelper;
import com.ramitsuri.expensemanager.utils.SharedExpenseHelper;
import com.ramitsuri.expensemanager.utils.SharedExpenseManager;
import com.ramitsuri.expensemanager.utils.TransformationHelper;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import timber.log.Timber;

public class AllExpensesViewModel extends ViewModel {

    private ExpenseRepository mRepository;

    private List<Expense> mExpenses;
    private Filter mFilter;
    private MutableLiveData<String> mFilterInfo;
    private SharedExpenseManager mSharedExpenseManager;

    public AllExpensesViewModel() {
        super();
        mRepository = MainApplication.getInstance().getExpenseRepo();
        mFilter = getDefaultFilter();
        mFilterInfo = new MutableLiveData<>();
        updateFilterInfo();
    }

    /*
     * Expenses
     */
    @Nullable
    public LiveData<List<ExpenseWrapper>> getExpenseWrappers() {
        return Transformations.map(mRepository.getExpenses(),
                new Function<List<Expense>, List<ExpenseWrapper>>() {
                    @Override
                    public List<ExpenseWrapper> apply(List<Expense> input) {
                        mExpenses = input;
                        return TransformationHelper.toExpenseWrapperList(input);
                    }
                });
    }

    @Nullable
    public List<Expense> getExpenses() {
        return mExpenses;
    }

    public int getExpensesSize() {
        if (mExpenses == null) {
            return 0;
        } else {
            return mExpenses.size();
        }
    }

    public BigDecimal getExpensesTotal() {
        if (mExpenses == null) {
            return BigDecimal.ZERO;
        }
        Calculator calculator = new Calculator(mExpenses, null, false, false);
        calculator.calculate();
        return calculator.getExpenseTotalValue();
    }

    @Nonnull
    public Filter getFilter() {
        return mFilter;
    }

    public void onExpenseFilterApplied(@Nullable Filter filter) {
        if (filter == null) {
            filter = getDefaultFilter();
        }
        Timber.i("Filtering for %s", filter.toString());
        mFilter = filter;
        mRepository.getForFilter(mFilter);
        updateFilterInfo();
    }

    public LiveData<Expense> duplicateExpense(@Nonnull Expense expense) {
        Expense duplicate = new Expense(expense);
        duplicate.setIsSynced(false);
        return mRepository.insertAndGet(duplicate, mFilter);
    }

    public void deleteExpense(@Nonnull Expense expense) {
        mRepository.delete(expense, mFilter);
        // Backed up expense was deleted, update Edited Sheets table to add this expense's sheet id
        if (expense.isSynced()) {
            MainApplication.getInstance().getEditedSheetRepo().insertEditedSheet(
                    new EditedSheet(DateHelper.getMonthIndexFromDate(expense.getDateTime())));
        }
    }

    public LiveData<String> getFilterInfo() {
        return mFilterInfo;
    }

    private void updateFilterInfo() {
        mFilterInfo.postValue(mFilter.toFriendlyString());
    }

    @Nonnull
    private Filter getDefaultFilter() {
        return new Filter()
                .getDefault();
    }

    public void clearFilter() {
        Timber.i("Clearing filter");
        mFilter
                .clear()
                .getDefault();
        mRepository.getForFilter(mFilter);
        updateFilterInfo();
    }

    public boolean isEnableSharedExpenses() {
        return getSharedExpenseManager().isEnabled();
    }

    public void getAndSaveAndDeleteFromRemoteShared() {
        final String otherSource = SharedExpenseHelper.getOtherSource();
        getSharedExpenseManager().getForOtherSource(otherSource);
    }

    public void pushToRemoteShared(@Nonnull Expense expense) {
        getSharedExpenseManager().add(expense);
    }

    private SharedExpenseManager getSharedExpenseManager() {
        if (mSharedExpenseManager == null) {
            mSharedExpenseManager = SharedExpenseHelper.getSharedExpenseManager(mCallbacks);
        }
        return mSharedExpenseManager;
    }

    private SharedExpenseManager.Callbacks mCallbacks = new SharedExpenseManager.Callbacks() {
        @Override
        public void addSuccess() {
            Timber.i("Expense added");
        }

        @Override
        public void deleteForOtherSuccess(@Nonnull String source) {
            Timber.i("Deleted for %s", source);
        }

        @Override
        public void getForOtherSuccess(@Nonnull String source, @Nonnull List<Expense> expenses) {
            Timber.i("Expenses %s", expenses.toString());
            getSharedExpenseManager().deleteForOtherSource(source);
            mRepository.insert(expenses, mFilter);
        }

        @Override
        public void failure(@Nonnull String message, @Nonnull Exception e) {
            Timber.i(e, message);
        }
    };
}
