package info.shibafu528.arthursiren.util;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Created by shibafu on 15/01/18.
 */
public class ContextUtil {
    @SuppressLint("ServiceCast")
    public static <T> T getSystemService(Context context, Class<T> clazz) {
        switch (clazz.getSimpleName()) {
            case "LayoutInflater":
                return (T) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        throw new RuntimeException();
    }
}
