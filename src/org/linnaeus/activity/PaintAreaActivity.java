package org.linnaeus.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import org.linnaeus.AppPreferences;
import org.linnaeus.ShakeManager;
import org.linnaeus.actions.*;
import org.linnaeus.actions.ShareToAction;
import org.linnaeus.bean.PaintAction;
import org.linnaeus.dialog.ColorPickerDialog;
import org.linnaeus.drawing.*;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.ImageRotator;
import org.linnaeus.utils.WarningAlert;
import org.openintents.about.AboutActivity;
import org.openintents.colorpicker.ColorPickerActivity;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.intents.AboutIntents;
import org.openintents.intents.ColorPickerIntents;
import org.openintents.intents.FileManagerIntents;

import java.io.File;
import java.util.Random;
import java.util.Stack;

public class PaintAreaActivity extends GraphicsActivity
                               implements MenuIdCollection,
                                          ColorPickerDialog.OnColorChangedListener,
                                          ShakeManager.ShakeEventListener {

    private PaintView _paintView;
    private ShakeManager _shakeManager;
    private AppPreferences _preferences;
    private Stack<PaintAction> _paintActions;
    private int _currentAction;
    private Boolean _childMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _paintActions = new Stack<PaintAction>();
        _preferences = AppPreferences.getAppPreferences(this);

        initPaintViewLayout();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1},
                0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

        registerForContextMenu(_paintView);

        _shakeManager = new ShakeManager(this);
        _shakeManager.addListener(this);

        if (_preferences.isSaveStateOnExit()) {
            new RestorePaintViewAction().doAction(this, _paintView);
        }

        _paintActions.push(new PaintAction(Bitmap.createBitmap(_paintView.getDrawableBitmap())));
        _currentAction = _paintActions.size();
    }

    private void initPaintViewLayout() {

        setContentView(R.layout.paint_area);

        LinearLayout layout = (LinearLayout) findViewById(R.id.paint_view_layout);

        _paintView = new PaintView(this);
        layout.addView(_paintView);

        ImageButton btnColor = (ImageButton) findViewById(R.id.btn_color);
        btnColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaintAreaActivity.this, ColorPickerActivity.class);
                startActivityForResult(intent, COLOR_MENU_ID);
            }
        });

        ImageButton btnBrush = (ImageButton) findViewById(R.id.btn_brush);
        btnBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBrushMenuItemSelected();
            }
        });

        ImageButton btnUndo = (ImageButton) findViewById(R.id.btn_undo);
        btnUndo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                redo();
            }
        });

        ImageButton btnRedo = (ImageButton) findViewById(R.id.btn_redo);
        BitmapDrawable bmd = ImageRotator.rotate(this, R.drawable.ic_menu_revert);
        btnRedo.setImageDrawable(bmd);
        btnRedo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                undo();
            }
        });

        ImageButton btnChildMode = (ImageButton) findViewById(R.id.btn_child);
        btnChildMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                _childMode = !_childMode;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (_preferences.isSaveStateOnExit()) {
            new SavePaintViewAction().doAction(this, _paintView);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //new RestorePaintViewAction().doAction(this, _paintView);
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (_shakeManager != null) {
            _shakeManager.onPause();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        _shakeManager.onResume();
    }

    private Paint mPaint;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void onShakeEvent() {

        // TODO: add smart shake mamager initialization.
        if (!_preferences.isShakeFeatureEnabled()) {
            return;
        }

        new OnShakeAction().doAction(this, _paintView);
    }

    public class PaintView extends View {

        //private static final float MINP = 0.25f;
        //private static final float MAXP = 0.75f;

        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Shape mShape;
        private Paint mBitmapPaint;

        public PaintView(Context c) {
            super(c);

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            // TODO: fix delta height
            int menusHeight = 124;

            // Initial size = 320x480
            mBitmap = Bitmap.createBitmap(width, height - menusHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawColor(0xFFFFFFFF);
            mShape = new PathShape();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        public void onNewShapeSelect(Shape shape) {
            mShape = shape;
        }

        public void onClear() {
            mCanvas.drawColor(0xFFFFFFFF);
            invalidate();
            addActionToHistory();
        }

        private void addActionToHistory(){
             _paintActions.push(new PaintAction(Bitmap.createBitmap(mBitmap)));
            _currentAction = _paintActions.size();    
        }

        public Bitmap getDrawableBitmap() {
            return Bitmap.createBitmap(mBitmap);
        }

        public void setDrawableBitmap(Bitmap bitmap) {
            mCanvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFFFFFFF);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            mShape.draw(canvas, mPaint);
        }

        public void repaintAction(PaintAction paintAction) {
            mCanvas.drawBitmap(paintAction.getBitmap(), 0, 0, mBitmapPaint);
            invalidate();
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if(ActivityManager.isUserAMonkey()){
                _childMode = true;
            }

            float x = event.getX();
            float y = event.getY();

            int action = event.getAction();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {

                    if(_childMode){
                        applyChildCorrections();
                    }

                    mX = x;
                    mY = y;

                    mShape.onTouchEvent((int) x, (int) y, action);
                    invalidate();

                    break;
                }
                case MotionEvent.ACTION_MOVE: {

                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);

                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

                        mX = x;
                        mY = y;

                        mShape.onTouchEvent((int) x, (int) y, action);
                    }
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    mShape.onTouchEvent((int) x, (int) y, action);
                    mShape.draw(mCanvas, mPaint);
                    mShape.reset();
                    invalidate();

                    if (_currentAction < _paintActions.size()) {
                        for (int i = 0; i < _paintActions.size() - _currentAction + 1; i++) {
                            _paintActions.removeElementAt(_paintActions.size() - 1);
                        }
                    }
                    addActionToHistory();
                    break;
                }
            }
            return true;
        }

        private Random random = new Random(0);
        private void applyChildCorrections(){

            int color = Color.rgb(random.nextInt(256),
                                  random.nextInt(256),
                                  random.nextInt(256));

            colorChanged(color);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        SubMenu drawingItem = menu.addSubMenu(0, DRAWING_MENU_ID, 0, "Drawing")
                .setIcon(R.drawable.ic_menu_compose);

        drawingItem.add(0, BRUSH_WIDTH_MENU_ID, 0, "Brush width");
        drawingItem.add(0, FIGURE_MENU_ID, 0, "Figure");
        drawingItem.add(0, CLEAR_SHAPE_MENU_ID, 0, "Clear");

        menu.add(0, SHARE_MENU_ID, 0, "Share").setIcon(android.R.drawable.ic_menu_share);

        //SubMenu shareItem = menu.addSubMenu(0, SHARE_MENU_ID, 0, "Share")
        //        .setIcon(android.R.drawable.ic_menu_share);

        //shareItem.add(0, SHARE_FACEBOOK_MENU_ID, 0, "Facebook");
        //shareItem.add(0, SHARE_OTHER_MENU_ID, 0, "Other...");

        menu.add(0, PREFERENCES_MENU_ID, 0, "Preferences").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, OPEN_FILE_MENU_ID, 0, "Open").setIcon(R.drawable.ic_menu_archive);
        menu.add(0, SAVE_FILE_MENU_ID, 0, "Save").setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, ABOUT_MENU_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);

        return true;
    }

    private void onBrushWidthMenuItemSelected() {
        final String[] items = new String[MAX_BRUSH_WIDTH_VALUE / BRUSH_VALUE_STEP];
        for (int i = MIN_BRUSH_WIDTH_VALUE; i <= MAX_BRUSH_WIDTH_VALUE; i += BRUSH_VALUE_STEP) {
            items[i / BRUSH_VALUE_STEP - 1] = String.valueOf(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select brush width");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mPaint.setStrokeWidth(Float.valueOf(items[item]));
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void onBrushMenuItemSelected() {

        // TODO: Change on more suatable implementation.

        final CharSequence[] items = {
                "Simple",
                "Eraser",
                "Emboss",
                "Blur",
                "SrcATop"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a brush style");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: {
                        mPaint.setMaskFilter(null);
                        mPaint.setXfermode(null);
                        mPaint.setAlpha(0xFF);
                        break;
                    }
                    case 1: {
                        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        break;
                    }
                    case 2: {
                        if (mPaint.getMaskFilter() != mEmboss) {
                            mPaint.setMaskFilter(mEmboss);
                        } else {
                            mPaint.setMaskFilter(null);
                        }
                        break;
                    }
                    case 3: {
                        if (mPaint.getMaskFilter() != mBlur) {
                            mPaint.setMaskFilter(mBlur);
                        } else {
                            mPaint.setMaskFilter(null);
                        }
                        break;
                    }
                    case 4: {
                        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                        mPaint.setAlpha(0x80);
                        break;
                    }
                }

                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void onFigureMenuItemSelected() {

        // TODO: Change on more suatable implementation.

        final CharSequence[] items = {
                "Path",
                "Rectangle",
                "Round Rectangle",
                "Oval",
                "Triangle",
                "Diamond"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a figure");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case 0: {
                        _paintView.onNewShapeSelect(new PathShape());
                        break;
                    }
                    case 1: {
                        _paintView.onNewShapeSelect(new RectShape());
                        break;
                    }
                    case 2: {
                        _paintView.onNewShapeSelect(new RoundRectShape());
                        break;
                    }
                    case 3: {
                        _paintView.onNewShapeSelect(new OvalShape());
                        break;
                    }
                    case 4: {
                        _paintView.onNewShapeSelect(new TriangleShape());
                        break;
                    }
                    case 5: {
                        _paintView.onNewShapeSelect(new DiamondShape());
                        break;
                    }
                }
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case COLOR_MENU_ID: {
                if (resultCode == RESULT_OK) {
                    int color = data.getExtras().getInt(ColorPickerIntents.EXTRA_COLOR);
                    colorChanged(color);
                }
                break;
            }
            case OPEN_FILE_MENU_ID: {
                if (resultCode == RESULT_OK) {
                    Uri fileUri = data.getData();

                    try {

                        File openFile = new File(fileUri.getPath());

                        if (!openFile.isFile()) {
                            WarningAlert.show(this, "Please select a valid image file.");
                        } else {
                            Bitmap bitmapImage = BitmapFactory.decodeFile(fileUri.getPath());
                            _paintView.setDrawableBitmap(bitmapImage);
                        }
                    }
                    catch (Exception ex) {
                        WarningAlert.show(this, "Cannot open image: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                break;
            }
            case SAVE_FILE_MENU_ID: {
                if (resultCode == RESULT_OK) {

                    Uri fileUri = data.getData();

                    File openFile = new File(fileUri.getPath());

                    if (openFile.isDirectory()) {
                        WarningAlert.show(this, "Please specify a valid image path.");
                    } else {
                        FileUtils.saveImage(this,
                                _paintView.getDrawableBitmap(), fileUri.getPath());
                    }
                }
                break;
            }
            case PREFERENCES_MENU_ID: {
                if (resultCode == RESULT_OK) {
                }
                break;
            }
            case ABOUT_MENU_ID: {
                if (resultCode == RESULT_OK) {
                }
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case DRAWING_MENU_ID: {
                return true;
            }
            case BRUSH_WIDTH_MENU_ID: {
                onBrushWidthMenuItemSelected();
                return true;
            }
            case FIGURE_MENU_ID: {
                onFigureMenuItemSelected();
                return true;
            }
            case CLEAR_SHAPE_MENU_ID: {
                _paintView.onClear();
                break;
            }
            case OPEN_FILE_MENU_ID: {
                Intent intent = new Intent(this, FileManagerActivity.class);
                intent.setAction(FileManagerIntents.ACTION_PICK_FILE);
                intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select image to open");
                startActivityForResult(intent, OPEN_FILE_MENU_ID);
                return true;
            }
            case SAVE_FILE_MENU_ID: {
                Intent intent = new Intent(this, FileManagerActivity.class);
                intent.setAction(FileManagerIntents.ACTION_PICK_FILE);
                intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select image to save");
                startActivityForResult(intent, SAVE_FILE_MENU_ID);
                return true;
            }
            case PREFERENCES_MENU_ID: {
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivityForResult(intent, PREFERENCES_MENU_ID);
                return true;
            }
            case ABOUT_MENU_ID: {
                Intent intent = new Intent(this, AboutActivity.class);
                intent.setAction(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);
                startActivityForResult(intent, ABOUT_MENU_ID);
                return true;
            }
            case SHARE_MENU_ID: {
                new ShareToAction().doAction(this, _paintView);
                // TODO: Add dynamic subitems list composition
                return true;
            }
            case SHARE_FACEBOOK_MENU_ID: {
                new FacebookShareAction().doAction(this, _paintView);
                return true;
            }
            case SHARE_OTHER_MENU_ID: {
                new ShareToAction().doAction(this, _paintView);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void undo() {
        if (_currentAction < _paintActions.size()) {
            _paintView.repaintAction(_paintActions.get(_currentAction));
            _currentAction++;
        }
    }

    private void redo() {
        if (_currentAction > 1) {
            _currentAction--;
            _paintView.repaintAction(_paintActions.get(_currentAction - 1));
        }
    }
}
