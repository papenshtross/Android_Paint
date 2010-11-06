package org.linnaeus.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import org.linnaeus.dialog.ColorPickerDialog;
import org.linnaeus.drawing.*;
import org.openintents.about.AboutActivity;
import org.openintents.colorpicker.ColorPickerActivity;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.intents.AboutIntents;
import org.openintents.intents.ColorPickerIntents;
import org.openintents.intents.FileManagerIntents;

public class PaintArea  extends GraphicsActivity
                        implements ColorPickerDialog.OnColorChangedListener {

    private PaintCanvas _mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _mainView = new PaintCanvas(this);
        setContentView(_mainView);

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

        registerForContextMenu(_mainView);
    }

    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public class PaintCanvas extends View {

        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;

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
            mCanvas.drawColor(0xFFAAAAAA);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

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
                    mShape.onTouchEvent((int)x, (int)y, action);
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {

                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);

                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mShape.onTouchEvent((int)x, (int)y, action);
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
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
    private static final int OPEN_FILE_MENU_ID = Menu.FIRST + 5;
    private static final int SAVE_FILE_MENU_ID = Menu.FIRST + 6;
    private static final int ABOUT_MENU_ID = Menu.FIRST + 7;
    private static final int DRAWING_MENU_ID = Menu.FIRST + 8;
    private static final int SHARE_MENU_ID = Menu.FIRST + 9;
    private static final int CHILD_MODE_MENU_ID = Menu.FIRST + 10;

    private static final int PATH_SHAPE_MENU_ID = Menu.FIRST + 11;
    private static final int RECT_SHAPE_MENU_ID = Menu.FIRST + 12;
    private static final int OVAL_SHAPE_MENU_ID = Menu.FIRST + 13;
    private static final int CLEAR_SHAPE_MENU_ID = Menu.FIRST + 14;
    private static final int TRIANGLE_SHAPE_MENU_ID = Menu.FIRST + 15;
    private static final int ROUND_RECT_SHAPE_MENU_ID = Menu.FIRST + 16;
    private static final int DIAMOND_SHAPE_MENU_ID = Menu.FIRST + 17;

    @Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenu.ContextMenuInfo menuInfo) {

        menu.add(0, COLOR_MENU_ID, 0, "Color");
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss");
        menu.add(0, BLUR_MENU_ID, 0, "Blur");
        menu.add(0, ERASE_MENU_ID, 0, "Eraser");
        menu.add(0, CLEAR_SHAPE_MENU_ID, 0, "Clear");
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop");
        menu.add(0, PATH_SHAPE_MENU_ID, 0, "Shape: PATH");
        menu.add(0, RECT_SHAPE_MENU_ID, 0, "Shape: RECTANGLE");
        menu.add(0, ROUND_RECT_SHAPE_MENU_ID, 0, "Shape: ROUND RECTANGLE");
        menu.add(0, OVAL_SHAPE_MENU_ID, 0, "Shape: OVAL");
        menu.add(0, TRIANGLE_SHAPE_MENU_ID, 0, "Shape: TRIANGLE");
        menu.add(0, DIAMOND_SHAPE_MENU_ID, 0, "Shape: DIAMOND");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID: {
                Intent intent = new Intent(this, ColorPickerActivity.class);
                startActivityForResult(intent, COLOR_MENU_ID);
                return true;
            }
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
            case PATH_SHAPE_MENU_ID:
                _mainView.onNewShapeSelect(new PathShape());
                return true;
            case RECT_SHAPE_MENU_ID:
                _mainView.onNewShapeSelect(new RectShape());
            case ROUND_RECT_SHAPE_MENU_ID:
                _mainView.onNewShapeSelect(new RoundRectShape());
                return true;
            case OVAL_SHAPE_MENU_ID:
                _mainView.onNewShapeSelect(new OvalShape());
                return true;
            case TRIANGLE_SHAPE_MENU_ID:
                _mainView.onNewShapeSelect(new TriangleShape());
                return true;
            case DIAMOND_SHAPE_MENU_ID:
                _mainView.onNewShapeSelect(new DiamondShape());
                return true;
            case CLEAR_SHAPE_MENU_ID:
                _mainView.onClear();
                return true;
        }
		return false;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, DRAWING_MENU_ID, 0, null).setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, CHILD_MODE_MENU_ID, 0, null).setIcon(R.drawable.happy);      
        menu.add(0, SHARE_MENU_ID, 0, null).setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, OPEN_FILE_MENU_ID, 0, null).setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(0, SAVE_FILE_MENU_ID, 0, null).setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, ABOUT_MENU_ID, 0, null).setIcon(android.R.drawable.ic_menu_info_details);

        return true;
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
           case OPEN_FILE_MENU_ID: {
               if(resultCode == RESULT_OK){
                   Uri fileUri = data.getData();
                   // TODO:
                   Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
               }
               break;
           }
           case SAVE_FILE_MENU_ID: {
               if(resultCode == RESULT_OK){
                   Uri fileUri = data.getData();
                   // TODO:
                   Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
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
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
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
            case ABOUT_MENU_ID: {
                Intent intent = new Intent(this, AboutActivity.class);
                intent.setAction(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);
                startActivityForResult(intent, ABOUT_MENU_ID);
                return true;
            }
            case DRAWING_MENU_ID: {
                openContextMenu(_mainView);
                return true;
            }
            case SHARE_MENU_ID: {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
