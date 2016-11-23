package com.equinox.qikbusiness.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikbusiness.Models.DataHolder;
import com.equinox.qikbusiness.Models.Order;
import com.equinox.qikbusiness.R;
import com.equinox.qikbusiness.Utils.StringManipulation;

import java.util.List;

/**
 * Created by mukht on 11/9/2016.
 */

public class SelectOrderListAdapter extends BaseAdapter {

    private List<Order> orderSelectionList;
    private Context context;
    private Integer selected;

    public SelectOrderListAdapter(List<Order> orderSelectionList, Context context) {
        this.orderSelectionList = orderSelectionList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return orderSelectionList.size();
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
        Order order = orderSelectionList.get(position);
        CheckoutViewHolder holder;
        if (convertView == null) {
            holder = new CheckoutViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.order_select_list_item, parent, false);
            holder.userName = (TextView) convertView.findViewById(R.id.user_name);
            holder.orderStatus = (TextView) convertView.findViewById(R.id.order_status);
            holder.deadlineMessage = (TextView) convertView.findViewById(R.id.deadline_message);
            holder.userImage = (NetworkImageView) convertView.findViewById(R.id.user_image);
            holder.selectButton = (RadioButton) convertView.findViewById(R.id.order_select_checkbox);
            convertView.setTag(holder);
        } else holder = (CheckoutViewHolder) convertView.getTag();
        holder.userName.setText(StringManipulation.CapsFirst(order.getFrom().getName()));
        holder.orderStatus.setTextColor(context.getResources().getColor(order.getOrderStatus().getColor()));
        holder.orderStatus.setText(order.getOrderStatus().toString());
        holder.deadlineMessage.setText(order.getDeadline() < System.currentTimeMillis() ? "Go Now!" : "");
        holder.userImage.setImageUrl(order.getFrom().getPhotoURL(), DataHolder.getInstance().getImageLoader());
        if (selected == null || selected != position) holder.selectButton.setChecked(false);
        else holder.selectButton.setChecked(true);
        return convertView;
    }

    public void selectedPosition(int position) {
        selected = position;
    }

    static class CheckoutViewHolder {
        TextView userName, orderStatus, deadlineMessage;
        NetworkImageView userImage;
        RadioButton selectButton;
    }
}
