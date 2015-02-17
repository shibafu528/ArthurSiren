package info.shibafu528.libarthur;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shibafu on 15/01/16.
 */
public class Quest implements Serializable {
    private static final long serialVersionUID = -1404569162135687362L;

    private String name;
    private QuestTerm term;
    private Date startDate;
    private Date endDate;

    public Quest(String name, QuestTerm term, Date startDate) {
        this.name = name;
        this.term = term;
        this.startDate = startDate;
        this.endDate = new Date(startDate.getTime() + 86399999L);
    }

    public Quest(String name, QuestTerm term, Date startDate, Date endDate) {
        this.name = name;
        this.term = term;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public QuestTerm getTerm() {
        return term;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return String.format("%s(%s) %s〜%s", name, term, sdf.format(startDate), sdf.format(endDate));
    }
}
