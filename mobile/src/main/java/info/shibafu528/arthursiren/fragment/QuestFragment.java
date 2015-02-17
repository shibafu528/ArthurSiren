package info.shibafu528.arthursiren.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import info.shibafu528.arthursiren.R;
import info.shibafu528.arthursiren.activity.MainActivity;
import info.shibafu528.arthursiren.async.QuestAsyncLoader;
import info.shibafu528.arthursiren.service.FetchService;
import info.shibafu528.arthursiren.util.Const;
import info.shibafu528.arthursiren.util.StubUtil;
import info.shibafu528.libarthur.Quest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static info.shibafu528.arthursiren.util.ContextUtil.getSystemService;

/**
 * Created by shibafu on 15/01/17.
 */
public class QuestFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Quest>>, SwipeRefreshLayout.OnRefreshListener {
    private QuestAdapter adapter;
    private List<Quest> quests = new ArrayList<>();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadQuestsStore();
        }
    };
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        swipeRefreshLayout = new SwipeRefreshLayout(getActivity()) {
            @Override
            public boolean canChildScrollUp() {
                if (getListView().getVisibility() == VISIBLE) {
                    return canListViewScrollUp(getListView());
                } else {
                    return false;
                }
            }

            private boolean canListViewScrollUp(ListView listView) {
                if (android.os.Build.VERSION.SDK_INT >= 14) {
                    return ViewCompat.canScrollVertically(listView, -1);
                } else {
                    return listView.getChildCount() > 0 &&
                            (listView.getFirstVisiblePosition() > 0
                                    || listView.getChildAt(0).getTop() < listView.getPaddingTop());
                }
            }
        };
        swipeRefreshLayout.addView(super.onCreateView(inflater, container, savedInstanceState),
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        swipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
        swipeRefreshLayout.setOnRefreshListener(this);

        return swipeRefreshLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new QuestAdapter(getActivity(), quests);
        getListView().setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        setListAdapter(adapter);

        if (getActivity().getFileStreamPath(Const.QUESTS_STORED_FILE).exists()) {
            loadQuestsStore();
        } else {
            swipeRefreshLayout.setRefreshing(true);
            getActivity().startService(new Intent(getActivity().getApplicationContext(), FetchService.class));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(R.string.title_section1);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(Const.ACTION_FETCH_QUESTS));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    public void loadQuestsStore() {
        try {
            List<Quest> quests = StubUtil.loadQuestsStore(getActivity());
            if (quests != null) {
                this.quests.clear();
                this.quests.addAll(quests);
                this.adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Loader<List<Quest>> onCreateLoader(int i, Bundle bundle) {
        return new QuestAsyncLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Quest>> loader, List<Quest> quests) {
        this.quests.clear();
        this.quests.addAll(quests);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Quest>> loader) {}

    @Override
    public void onRefresh() {
        getActivity().startService(new Intent(getActivity().getApplicationContext(), FetchService.class));
    }

    public static class QuestAdapter extends ArrayAdapter<Quest> {
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        private LayoutInflater inflater;

        public QuestAdapter(Context context, List<Quest> objects) {
            super(context, 0, objects);
            this.inflater = getSystemService(context, LayoutInflater.class);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_quest, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Quest quest = getItem(position);
            if (quest != null) {
                switch (quest.getTerm()) {
                    case GUERRILLA:
                        viewHolder.time.setText(timeFormat.format(quest.getStartDate()) + "-" + timeFormat.format(quest.getEndDate()));
                        viewHolder.date.setText(dateFormat.format(quest.getStartDate()));
                        break;
                    case STICKY:
                        viewHolder.time.setText(getContext().getString(R.string.hold));
                        viewHolder.date.setText(dateFormat.format(quest.getStartDate()) + "-" + dateFormat.format(quest.getEndDate()));
                        break;
                }
                if (quest.getStartDate().getTime() <= System.currentTimeMillis()
                        && System.currentTimeMillis() <= quest.getEndDate().getTime()) {
                    viewHolder.card.setCardBackgroundColor(getContext().getResources().getColor(R.color.primary_light));
                } else {
                    viewHolder.card.setCardBackgroundColor(Color.WHITE);
                }
                viewHolder.name.setText(quest.getName());
            }
            return convertView;
        }

        static class ViewHolder {
            @InjectView(R.id.textView)
            TextView time;
            @InjectView(R.id.textView2)
            TextView date;
            @InjectView(R.id.textView3)
            TextView name;
            @InjectView(R.id.card)
            CardView card;

            public ViewHolder(View v) {
                ButterKnife.inject(this, v);
            }
        }
    }

}
