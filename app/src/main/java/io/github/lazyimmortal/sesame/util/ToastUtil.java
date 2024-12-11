package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.widget.Toast;

import io.github.lazyimmortal.sesame.model.normal.base.BaseModel;

public class ToastUtil {

    public static void show(Context context, CharSequence cs) {
        Toast toast = Toast.makeText(context, cs, Toast.LENGTH_SHORT);
        show(toast);
    }

    public static void show(Toast toast) {
        toast.setGravity(toast.getGravity(), toast.getXOffset(), BaseModel.getToastOffsetY().getValue());
        toast.show();
    }
}
