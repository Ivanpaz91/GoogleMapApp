package com.superiorinfotech.publicbuddy.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.superiorinfotech.publicbuddy.R;
import com.superiorinfotech.publicbuddy.adapters.NothingSelectedSpinnerAdapter;
import com.superiorinfotech.publicbuddy.db.model.Category;
import com.superiorinfotech.publicbuddy.db.model.Incident;
import com.superiorinfotech.publicbuddy.db.model.SubCategory;
import com.superiorinfotech.publicbuddy.db.model.SubcategoryValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alex on 18.01.15.
 */
public class FillFormDataDialog extends DialogFragment {
    private Fragment fragment;
    private Incident incident;
    private Category category;

    private TableLayout mFillFormTable;
    private Button mCancel;
    private Button mSave;

    private HashMap<View, Long> selectedOptions;


    public FillFormDataDialog(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_fill_form_data, null);

        fragment = new Fragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fillFormTable, fragment).commit();

        selectedOptions = new HashMap<>();

        mFillFormTable = (TableLayout) layout.findViewById(R.id.fillFormTable);

        getDialog().setTitle("Fill Form Data");

        Bundle arguments = getArguments();
        if(arguments!=null) {
            incident = arguments.getParcelable(Incident.TYPE);
            category = arguments.getParcelable(Category.TYPE);
        }

        TableLayout tl = mFillFormTable;
        for(SubCategory subCategory : category.getSubCategories()){
            View tr = inflater.inflate(R.layout.row_fill_form_data, null,false);

            final Spinner spinner = (Spinner) tr.findViewById(R.id.values);
            TextView label  = (TextView) tr.findViewById(R.id.label);

            label.setText(subCategory.getSubCategoryName()+":");
            final NothingSelectedSpinnerAdapter adapter = new NothingSelectedSpinnerAdapter(new ArrayAdapter(getActivity(), R.layout.spinner_item, R.id.value, subCategory.getValues()), R.layout.spinner_row_nothing_selected, getActivity());
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(spinner.getSelectedItem()!=null) {
                        selectedOptions.put(spinner, ((SubcategoryValue) spinner.getSelectedItem()).getServerID());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 5, 0, 0);
            tl.addView(tr, layoutParams);
        }

        mCancel = (Button)layout.findViewById(R.id.cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mSave = (Button)layout.findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incident.setSubcategories(new ArrayList<Long>(selectedOptions.values()));
                dismiss();
            }
        });

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}
