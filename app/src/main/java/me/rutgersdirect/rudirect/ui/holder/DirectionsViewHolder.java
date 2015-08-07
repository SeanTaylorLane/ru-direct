package me.rutgersdirect.rudirect.ui.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.rutgersdirect.rudirect.R;

public class DirectionsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView title;
    public TextView time;
    public DirectionsViewHolderClick mListener;

    public DirectionsViewHolder(View v, DirectionsViewHolderClick listener) {
        super(v);

        title = (TextView) v.findViewById(R.id.directions_title);
        time = (TextView) v.findViewById(R.id.directions_time);
        mListener = listener;

        v.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onClick(v, getLayoutPosition());
    }

    public interface DirectionsViewHolderClick {
        void onClick(View v, int position);
    }
}