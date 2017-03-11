package com.ramitsuri.expensemanager.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ramitsuri.expensemanager.R;
import com.ramitsuri.expensemanager.RecyclerViewActivity;
import com.ramitsuri.expensemanager.helper.ActivityHelper;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>{

    private List<String> mValues;
    private RecyclerView mRecyclerView;
    private View mViewClicked;
    private Context mContext;
    private ValueEditCallbacks mCallbacks;

    public interface ValueEditCallbacks{
        void onValueEditMode(boolean isEditMode);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mValue;
        private EditText mValueEdit;
        private ImageView mBtnSaveValue;
        private LinearLayout mValueEditContainer;

        public CustomViewHolder(View itemView) {
            super(itemView);
            setupViews();
        }

        private void setupViews() {
            mValue = (TextView)itemView.findViewById(R.id.value);
            mValueEdit = (EditText)itemView.findViewById(R.id.value_edit);
            mBtnSaveValue = (ImageView)itemView.findViewById(R.id.btn_save_value);
            mValueEditContainer = (LinearLayout)itemView.findViewById(R.id.edit_container);
            mValue.setOnClickListener(this);
            mBtnSaveValue.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view == mValue){
                handleEditValue();
            } else if (view == mBtnSaveValue){
                handleSaveValueClicked();
            }
        }

        private void handleSaveValueClicked() {
            mValueEditContainer.setVisibility(View.GONE);
            mValue.setText(mValueEdit.getEditableText().toString());
            mValue.setVisibility(View.VISIBLE);
            mValueEdit.clearFocus();
            ActivityHelper.hideSoftKeyboard(mRecyclerView.getContext(), mValueEdit);
        }

        private void handleEditValue() {
            mCallbacks.onValueEditMode(true);
            mViewClicked = mValueEdit;
            mValueEditContainer.setVisibility(View.VISIBLE);
            mValue.setVisibility(View.GONE);
            mValueEdit.requestFocus();
            ActivityHelper.showSoftKeyboard(mRecyclerView.getContext(), mValueEdit);
        }
    }

    public RecyclerViewAdapter(Context context, List<String> values){
        mValues = values;
        mContext = context;
        mCallbacks = (RecyclerViewActivity)context;
    }

    @Override
    public RecyclerViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.values_row, null);
        mRecyclerView = (RecyclerView)parent;
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewAdapter.CustomViewHolder holder, int position) {
        holder.mValue.setText(mValues.get(position));
        holder.mValueEdit.setText(mValues.get(position));
        holder.mValueEdit.setSelection(mValues.get(position).length());
        holder.mValueEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    handleSave(holder);
                }
            }
        });
        holder.mValueEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                holder.mValueEdit.clearFocus();
                return true;
            }
        });
    }
    private void handleSave(CustomViewHolder holder) {
        mCallbacks.onValueEditMode(false);
        //((RecyclerViewActivity)mContext).switchFabIconToEditingMode(false);
        holder.mValue.setText(holder.mValueEdit.getEditableText().toString());
        holder.mValue.setVisibility(View.VISIBLE);
        holder.mValueEditContainer.setVisibility(View.GONE);
        ActivityHelper.hideSoftKeyboard(mRecyclerView.getContext(), holder.mValueEdit);
    }

    public void saveValue(){
        mViewClicked.clearFocus();
    }

    public void setAddNew(List<String> values){
        mValues = values;
        int a = this.getItemCount();
        long as = getItemId(getItemCount()-1);
        //this.notifyItemInserted(mValues.size() - 1);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
