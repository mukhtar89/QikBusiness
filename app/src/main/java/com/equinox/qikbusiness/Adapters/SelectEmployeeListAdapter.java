package com.equinox.qikbusiness.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikbusiness.Enums.EmployeeStatus;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Employee;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.StringManipulation;

import java.util.List;

/**
 * Created by mukht on 11/23/2016.
 */

public class SelectEmployeeListAdapter extends BaseAdapter {

    private List<Employee> employeeSelectionList;
    private Context context;
    private Integer selected;

    public SelectEmployeeListAdapter(List<Employee> employeeSelectionList, Context context) {
        this.employeeSelectionList = employeeSelectionList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return employeeSelectionList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employee employee = employeeSelectionList.get(position);
        EmployeeViewHolder holder;
        if (convertView == null) {
            holder = new EmployeeViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.order_select_list_item, parent, false);
            holder.employeeName = (TextView) convertView.findViewById(R.id.user_name);
            holder.employeeStatus = (TextView) convertView.findViewById(R.id.employee_status_text);
            holder.lastTask = (TextView) convertView.findViewById(R.id.last_task_timestamp);
            holder.employeeImage = (NetworkImageView) convertView.findViewById(R.id.user_image);
            holder.selectButton = (RadioButton) convertView.findViewById(R.id.order_select_checkbox);
            holder.employeeStatusIcon = (ImageView) convertView.findViewById(R.id.employee_status_icon);
            convertView.setTag(holder);
        } else holder = (EmployeeViewHolder) convertView.getTag();
        holder.employeeName.setText(StringManipulation.CapsFirst(employee.getName()));
        if (employee.getEmployeeStatus() == null) employee.setEmployeeStatus(EmployeeStatus.IDLE);
        holder.employeeStatus.setTextColor(context.getResources().getColor(employee.getEmployeeStatus().getColor()));
        holder.employeeStatus.setText(employee.getEmployeeStatus().toString());
        holder.lastTask.setText(employee.getLastAssignedTask() == null ? "0" : employee.getLastAssignedTask().toString());
        holder.employeeImage.setImageUrl(employee.getPhotoURL(), DataHolder.getInstance().getImageLoader());
        if (selected == null || selected != position) holder.selectButton.setChecked(false);
        else holder.selectButton.setChecked(true);
        return convertView;
    }

    public void selectedPosition(int position) {
        selected = position;
    }

    static class EmployeeViewHolder {
        TextView employeeName, employeeStatus, lastTask;
        NetworkImageView employeeImage;
        ImageView employeeStatusIcon;
        RadioButton selectButton;
    }
}
