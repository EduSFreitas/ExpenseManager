package com.ramitsuri.expensemanager.viewModel;

import com.ramitsuri.expensemanager.MainApplication;
import com.ramitsuri.expensemanager.data.repository.ExpenseRepository;
import com.ramitsuri.expensemanager.entities.EditedSheet;
import com.ramitsuri.expensemanager.entities.Expense;
import com.ramitsuri.expensemanager.entities.Filter;
import com.ramitsuri.expensemanager.ui.adapter.ExpenseWrapper;
import com.ramitsuri.expensemanager.utils.Calculator;
import com.ramitsuri.expensemanager.utils.TransformationHelper;

import java.math.BigDecimal;
import java.util.Calendar;
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
            MainApplication.getInstance().getEditedSheetRepo()
                    .insertEditedSheet(new EditedSheet(expense.getSheetId()));
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
        Calendar calendar = Calendar.getInstance();

        // First day of month - 00:00:00 001ms
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        long fromDateTime = calendar.getTimeInMillis();

        // Last Day of month - 23:59:59 999ms
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long toDateTime = calendar.getTimeInMillis();

        return new Filter()
                .setFromDateTime(fromDateTime)
                .setToDateTime(toDateTime);
    }
}
