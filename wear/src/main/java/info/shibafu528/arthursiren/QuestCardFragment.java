package info.shibafu528.arthursiren;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import info.shibafu528.libarthur.Quest;

import java.text.SimpleDateFormat;

/**
 * Created by shibafu on 15/01/18.
 */
public class QuestCardFragment extends CardFragment {
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    @InjectView(R.id.textView)
    TextView time;
    @InjectView(R.id.textView2)
    TextView date;
    @InjectView(R.id.textView3)
    TextView name;

    public static QuestCardFragment newInstance(Quest quest) {
        QuestCardFragment fragment = new QuestCardFragment();
        Bundle args = new Bundle();
        args.putSerializable("quest", quest);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.card_quest, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        Quest quest = (Quest) args.getSerializable("quest");

        switch (quest.getTerm()) {
            case GUERRILLA:
                time.setText(timeFormat.format(quest.getStartDate()) + "-" + timeFormat.format(quest.getEndDate()));
                date.setText(dateFormat.format(quest.getStartDate()));
                break;
            case STICKY:
                time.setText("常駐");
                date.setText(dateFormat.format(quest.getStartDate()) + "-" + dateFormat.format(quest.getEndDate()));
                break;
        }
        name.setText(quest.getName());
    }
}
