package com.ramitsuri.expensemanager.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ramitsuri.expensemanager.R;
import com.ramitsuri.expensemanager.adapter.ListPickerAdapter;
import com.ramitsuri.expensemanager.entities.Category;
import com.ramitsuri.expensemanager.helper.CategoryHelper;

public class CategoryPickerDialogFragment extends DialogFragment {


    public static String TAG = CategoryPickerDialogFragment.class.getName();

    private ListPickerAdapter<Category> mAdapter;
    private CategoryPickerCallbacks mCallbacks;
    private TextView mTitle;
    private RecyclerView mCategoriesRecyclerView;

    public interface CategoryPickerCallbacks{
        void onCategoryPicked(Category category);
    }

    public static CategoryPickerDialogFragment newInstance(){
        return new CategoryPickerDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_list_picker, container, false);
        mTitle = (TextView) v.findViewById(R.id.title);
        mTitle.setText(getString(R.string.category_picker_title));
        mAdapter = new ListPickerAdapter<>(CategoryHelper.getAllCategories());
        mCategoriesRecyclerView = (RecyclerView) v.findViewById(R.id.values);
        mCategoriesRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager recyclerViewLManager = new LinearLayoutManager(getContext());
        mCategoriesRecyclerView.setLayoutManager(recyclerViewLManager);
        mCategoriesRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (CategoryPickerCallbacks) context;
    }
}
