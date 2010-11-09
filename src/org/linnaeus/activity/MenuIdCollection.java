package org.linnaeus.activity;

import android.view.Menu;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 09.11.2010
 * Time: 22:31:58
 */

public interface MenuIdCollection {

    public static final int MAX_BRUSH_WIDTH_VALUE = 30;
    public static final int MIN_BRUSH_WIDTH_VALUE = 3;
    public static final int BRUSH_VALUE_STEP = 3;

    public static final int COLOR_MENU_ID = Menu.FIRST;
    public static final int OPEN_FILE_MENU_ID = Menu.FIRST + 5;
    public static final int SAVE_FILE_MENU_ID = Menu.FIRST + 6;
    public static final int ABOUT_MENU_ID = Menu.FIRST + 7;
    public static final int DRAWING_MENU_ID = Menu.FIRST + 8;
    public static final int SHARE_MENU_ID = Menu.FIRST + 9;
    public static final int CHILD_MODE_MENU_ID = Menu.FIRST + 10;

    public static final int CLEAR_SHAPE_MENU_ID = Menu.FIRST + 14;

    public static final int PREFERENCES_MENU_ID = Menu.FIRST + 18;
    public static final int BRUSH_MENU_ID = Menu.FIRST + 19;
    public static final int BRUSH_WIDTH_MENU_ID = Menu.FIRST + 20;
    public static final int FIGURE_MENU_ID = Menu.FIRST + 21;

    public static final int SHARE_FACEBOOK_MENU_ID = Menu.FIRST + 22;
    public static final int SHARE_OTHER_MENU_ID = Menu.FIRST + 23;
    public static final int REDO_MENU_ID = Menu.FIRST + 24;
    public static final int UNDO_MENU_ID = Menu.FIRST + 25;
}