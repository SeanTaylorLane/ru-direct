package me.rutgersdirect.rudirect.adapter;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jgrapht.GraphPath;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import me.rutgersdirect.rudirect.R;
import me.rutgersdirect.rudirect.data.constants.RUDirectApplication;
import me.rutgersdirect.rudirect.data.model.BusRouteEdge;
import me.rutgersdirect.rudirect.data.model.BusStop;
import me.rutgersdirect.rudirect.ui.holder.DirectionsViewHolder;
import me.rutgersdirect.rudirect.util.DirectionsUtil;

public class DirectionsAdapter extends RecyclerView.Adapter<DirectionsViewHolder> {

    private static final int MILLIS_IN_ONE_MINUTE = 60000;
    private static final int BUS_STOP = 0;
    private static final int BUS_ROUTE = 1;
    private String[] titles;
    private String[] times;
    private String[] vehicleIds;

    public DirectionsAdapter(GraphPath<BusStop, BusRouteEdge> path) {
        List<BusRouteEdge> busStopEdges = path.getEdgeList();
        titles = new String[busStopEdges.size() * 2 + 1];
        times = new String[busStopEdges.size() * 2 + 1];
        vehicleIds = new String[busStopEdges.size() * 2 + 1];
        long time = new Date().getTime();

        titles[0] = busStopEdges.get(0).getSourceBusStop().getTitle();
        time += ((int) DirectionsUtil.getInitialWait() * MILLIS_IN_ONE_MINUTE);
        times[0] = getTimeInHHMM(time);
        vehicleIds[0] = "";
        int j = 1;
        for (int i = 0; i < busStopEdges.size(); i++) {
            titles[j] = busStopEdges.get(i).getRouteName();
            titles[j + 1] = busStopEdges.get(i).getTargetBusStop().getTitle();

            time += ((int) busStopEdges.get(i).getTravelTime() * MILLIS_IN_ONE_MINUTE);
            times[j] = (int) busStopEdges.get(i).getTravelTime() + " min";
            times[j + 1] = getTimeInHHMM(time);
            vehicleIds[j] = "(Bus ID: " + busStopEdges.get(i).getVehicleId() + ")";
            vehicleIds[j + 1] = "";
            j += 2;
        }
    }

    public DirectionsAdapter(String[] titles, String[] times) {
        this.titles = titles;
        this.times = times;
    }

    public DirectionsAdapter() {
        this.titles = null;
        this.times = null;
    }

    @Override
    public DirectionsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_directions, parent, false);
        DirectionsViewHolder viewHolder = new DirectionsViewHolder(v, new DirectionsViewHolder.DirectionsViewHolderClick() {
            public void onClick(View v, int position) {
                Log.d("DirectionsAdapter", "Title: " + titles[position] + " was clicked");
            }
        });
        if (viewType == BUS_ROUTE) {
            Resources resources = RUDirectApplication.getContext().getResources();
            viewHolder.title.setTextColor(resources.getColor(android.R.color.white));
            viewHolder.title.setBackgroundColor(resources.getColor(R.color.primary_color));
            viewHolder.time.setTextColor(resources.getColor(android.R.color.white));
            viewHolder.time.setBackgroundColor(resources.getColor(R.color.primary_color));
        } else {
            viewHolder.title.setTypeface(null, Typeface.BOLD);
            viewHolder.time.setTypeface(null, Typeface.BOLD);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DirectionsViewHolder viewHolder, int position) {
        viewHolder.title.setText(titles[position]);
        if (times != null) {
            viewHolder.time.setText(times[position] + " " + vehicleIds[position]);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }

    @Override
    public int getItemCount() {
        if (titles != null) {
            return titles.length;
        }
        return 0;
    }

    // Takes a time in millis and returns the time in HH:MM format
    private String getTimeInHHMM(long time) {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(time));
    }
}