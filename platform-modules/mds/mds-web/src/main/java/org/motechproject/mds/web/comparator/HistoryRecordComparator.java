package org.motechproject.mds.web.comparator;

import org.motechproject.mds.web.domain.FieldRecord;
import org.motechproject.mds.web.domain.HistoryRecord;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * The <code>HistoryRecordComparator</code> class compares two objects of
 * {@link HistoryRecord} by value of their field property.
 */
public class HistoryRecordComparator implements Comparator<HistoryRecord>, Serializable {
    private static final long serialVersionUID = -6246986278201461196L;

    private boolean sortAscending;
    private String compareField;

    public HistoryRecordComparator(boolean sortAscending, String compareField) {
        this.sortAscending = sortAscending;
        this.compareField = compareField;
    }

    @Override
    public int compare(HistoryRecord one, HistoryRecord two) {
        FieldRecord fieldFromOne = findFieldByName(one, compareField);
        FieldRecord fieldFromTwo = findFieldByName(two, compareField);
        int ret;

        try {
            DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy hh:mm aaa", Locale.ENGLISH);
            Date dateOne = dateFormat.parse(fieldFromOne.getValue().toString());
            Date dateTwo = dateFormat.parse(fieldFromTwo.getValue().toString());

            ret = dateOne.compareTo(dateTwo);
        } catch (ParseException e) {
            ret = 0;
        }

        return (sortAscending) ? ret : -ret;
    }

    private FieldRecord findFieldByName(HistoryRecord historyRecord, String fieldName) {
        for (FieldRecord fieldRecord : historyRecord.getFields()) {
            if (fieldRecord.getDisplayName().equals(fieldName)) {
                return fieldRecord;
            }
        }

        return null;
    }
}
