package org.linnaeus.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import org.linnaeus.ShakeManager;
import org.linnaeus.dialog.ColorPickerDialog;
import org.linnaeus.drawing.*;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.WarningAlert;
import org.openintents.about.AboutActivity;
import org.openintents.colorpicker.ColorPickerActivity;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.intents.AboutIntents;
import org.openintents.intents.ColorPickerIntents;
import org.openintents.intents.FileManagerIntents;

import java.io.File;

public class PaintAreaActivity extends GraphicsActivity
                        implements ColorPickerDialog.OnColorChangedListener,
                                   ShakeManager.ShakeEventListener {

    private PaintCanvas _paintCanvas;
    private ShakeManager _shakeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _paintCanvas = new PaintCanvas(this);
        setContentView(_paintCanvas);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                                       0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

        registerForContextMenu(_paintCanvas);

        _shakeManager = new ShakeManager(this);
        _shakeManager.addListener(this);
    }

    @Override
    protected void onPause() {

        super.onPause();
        if(_shakeManager != null){
            _shakeManager.onPause();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        _shakeManager.onResume();
    }

    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void onShakeEvent() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to clear drawing?")
            .setCancelable(false)
            .setTitle("Drawing shake")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    _paintCanvas.onClear();
                    dialog.cancel();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        builder.create().show();
    }

    public class PaintCanvas extends View {

        //private static final float MINP = 0.25f;
        //private static final float MAXP = 0.75f;

        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Shape mShape;
        private Paint   mBitmapPaint;

        public PaintCanvas(Context c) {
            super(c);

            mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mShape = new PathShape();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        public void onNewShapeSelect(Shape shape){
            mShape = shape;
        }

        public void onClear(){
            mCanvas.drawColor(0xFFFFFFFF);
            invalidate();
        }

        public Bitmap getDrawableBitmap(){
            return Bitmap.createBitmap(mBitmap);
        }

        public void setDrawableBitmap(Bitmap bitmap){
            mCanvas.drawBitmap(bitmap, 0,0, mBitmapPaint);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            mShape.draw(canvas, mPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            int action = event.getAction();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {

                    mX = x;
                    mY = y;

                    mShape.onTouchEvent((int)x, (int)y, action);
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
                    mShape.onTouchEvent((int)x, (int)y, action);
                    mShape.draw(mCanvas, mPaint);
                    mShape.reset();
                    invalidate();
                    break;
                }
            }
            return true;
        }
    }

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int OPEN_FILE_MENU_ID = Menu.FIRST + 5;
    private static final int SAVE_FILE_MENU_ID = Menu.FIRST + 6;
    private static final int ABOUT_MENU_ID = Menu.FIRST + 7;
    private static final int DRAWING_MENU_ID = Menu.FIRST + 8;
    private static final int SHARE_MENU_ID = Menu.FIRST + 9;
    private static final int CHILD_MODE_MENU_ID = Menu.FIRST + 10;

    private static final int CLEAR_SHAPE_MENU_ID = Menu.FIRST + 14;

    private static final int PREFERENCES_MENU_ID = Menu.FIRST + 18;
    private static final int BRUSH_MENU_ID = Menu.FIRST + 19;
    private static final int FIGURE_MENU_ID = Menu.FIRST + 20;

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
                                   .setIcon(android.R.drawable.ic_menu_edit);

        drawingItem.add(0, COLOR_MENU_ID, 0, "Color");
        drawingItem.add(0, BRUSH_MENU_ID, 0, "Brush style");
        drawingItem.add(0, BRUSH_MENU_ID, 0, "Brush width");
        drawingItem.add(0, FIGURE_MENU_ID, 0, "Figure");
        drawingItem.add(0, CLEAR_SHAPE_MENU_ID, 0, "Clear");

        menu.add(0, CHILD_MODE_MENU_ID, 0, "Child Mode").setIcon(R.drawable.happy);
        menu.add(0, SHARE_MENU_ID, 0, "Share").setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, OPEN_FILE_MENU_ID, 0, "Open").setIcon(R.drawable.ic_menu_archive);
        menu.add(0, SAVE_FILE_MENU_ID, 0, "Save").setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, PREFERENCES_MENU_ID, 0, "Preferences").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, ABOUT_MENU_ID, 0, "About...").setIcon(android.R.drawable.ic_menu_info_details);

        return true;
    }

    private void onBrushMenuItemSelected(){

        // TODO: Change on more suatable implementation.

        final CharSequence[] items = {
                "Eraser",
                "Emboss",
                "Blur",
                "SrcATop"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a brush style");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                // TODO: refactor two lines above.

                mPaint.setXfermode(null);
                mPaint.setAlpha(0xFF);

                switch (item) {
                    case 0: {
                        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        break;
                    }
                    case 1: {
                        if (mPaint.getMaskFilter() != mEmboss) {
                            mPaint.setMaskFilter(mEmboss);
                        } else {
                            mPaint.setMaskFilter(null);
                        }
                        break;
                    }
                    case 2: {
                        if (mPaint.getMaskFilter() != mBlur) {
                            mPaint.setMaskFilter(mBlur);
                        } else {
                            mPaint.setMaskFilter(null);
                        }
                        break;
                    }
                    case 3: {
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

    private void onFigureMenuItemSelected(){

        // TODO: Change on more suatable implementation.

        final CharSequence[] items = {
                "Path",
                "Rectangle",
                "Round Rectangle",
                "Oval",
                "Triangle",
                "Diamond" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a figure");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch(item) {
                    case 0: {
                        _paintCanvas.onNewShapeSelect(new PathShape());
                        break;
                   }
                    case 1: {
                        _paintCanvas.onNewShapeSelect(new RectShape());
                        break;
                    }
                    case 2: {
                        _paintCanvas.onNewShapeSelect(new RoundRectShape());
                        break;
                    }
                    case 3: {
                        _paintCanvas.onNewShapeSelect(new OvalShape());
                        break;
                    }
                    case 4: {
                        _paintCanvas.onNewShapeSelect(new TriangleShape());
                        break;
                    }
                    case 5: {
                        _paintCanvas.onNewShapeSelect(new DiamondShape());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data){

       switch(requestCode){
           case COLOR_MENU_ID: {
               if(resultCode == RESULT_OK){
                   int color = data.getExtras().getInt(ColorPickerIntents.EXTRA_COLOR);
                   colorChanged(color);
               }
               break;
           }
           case CLEAR_SHAPE_MENU_ID: {
                _paintCanvas.onClear();
               break;
           }
           case OPEN_FILE_MENU_ID: {
               if(resultCode == RESULT_OK){
                   Uri fileUri = data.getData();

                   try{

                       File openFile = new File(fileUri.getPath());

                       if(!openFile.isFile()){
                            WarningAlert.show(this, "Please select a valid image file.");
                       }
                       else {
                           Bitmap bitmapImage = BitmapFactory.decodeFile(fileUri.getPath());
                           _paintCanvas.setDrawableBitmap(bitmapImage);
                       }
                   }
                   catch(Exception ex){
                       WarningAlert.show(this, "Cannot open image: " + ex.getMessage());
                       ex.printStackTrace();
                   }
               }
               break;
           }
           case SAVE_FILE_MENU_ID: {
               if(resultCode == RESULT_OK){

                   Uri fileUri = data.getData();

                   File openFile = new File(fileUri.getPath());

                   if(openFile.isDirectory()){
                        WarningAlert.show(this, "Please specify a valid image path.");
                   }
                   else {
                        FileUtils.saveImage(this,
                                _paintCanvas.getDrawableBitmap(), fileUri.getPath());
                   }
               }
               break;
           }
           case PREFERENCES_MENU_ID: {
               if(resultCode == RESULT_OK){
               }
               break;
           }
           case ABOUT_MENU_ID: {
               if(resultCode == RESULT_OK){
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
            case BRUSH_MENU_ID: {
                onBrushMenuItemSelected();
                return true;
            }
            case FIGURE_MENU_ID: {
                onFigureMenuItemSelected();
                return true;
            }
            case COLOR_MENU_ID: {
                Intent intent = new Intent(this, ColorPickerActivity.class);
                startActivityForResult(intent, COLOR_MENU_ID);
                return true;
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
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
