package info.shibafu528.arthursiren;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.FragmentGridPagerAdapter;
import info.shibafu528.libarthur.Quest;

import java.util.List;

/**
 * Created by shibafu on 15/01/18.
 */
public class QuestsGridPagerAdapter extends FragmentGridPagerAdapter {
    private final Context context;
    private final List<Quest> quests;

    public QuestsGridPagerAdapter(FragmentManager fm, Context context, List<Quest> quests) {
        super(fm);
        this.context = context;
        this.quests = quests;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        Quest quest = quests.get(row);
        switch (column) {
            case 0:
                return QuestCardFragment.newInstance(quest);
            default:
                return null;
        }
    }

    @Override
    public int getRowCount() {
        return quests.size();
    }

    @Override
    public int getColumnCount(int row) {
        return 1;
    }

    @Override
    public Drawable getBackgroundForPage(int row, int column) {
        return new ColorDrawable(context.getResources().getColor(R.color.primary));
    }
}
