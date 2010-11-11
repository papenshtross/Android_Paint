package org.linnaeus.activity;

import android.content.Context;
import android.graphics.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 11.11.2010
 * Time: 18:11:18
 */

public class BrushStyleListAdapter extends ArrayAdapter {

    private static final int RESOURCE = R.layout.image_list_item;
    private String[] _objects;
    private Context _context;

    public BrushStyleListAdapter(Context context, String[] objects)
    {
        super(context, RESOURCE, objects);
        _context = context;
        _objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;

        if (view == null) {

            LayoutInflater inflater = (LayoutInflater)
                    _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(RESOURCE, null);
        }

        String styleItem = _objects[position];

        if (styleItem != null) {

            TextView textView = (TextView)view.findViewById(R.id.text);
            textView.setText(styleItem);

            ImageView imageView = (ImageView)view.findViewById(R.id.image);

            Bitmap image = Bitmap.createBitmap(150, 48, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(image);

            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.DKGRAY);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(12);

            switch (position) {
                case 0: {
                    mPaint.setMaskFilter(null);
                    mPaint.setXfermode(null);
                    mPaint.setAlpha(0xFF);
                    break;
                }
                case 1: {
                    mPaint.setColor(Color.LTGRAY);
                    mPaint.setAlpha(0xFF);
                    //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    break;
                }
                case 2: {
                    MaskFilter mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
                    mPaint.setMaskFilter(mEmboss);
                    break;
                }
                case 3: {
                    MaskFilter mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
                    mPaint.setMaskFilter(mBlur);
                    break;
                }
                case 4: {
                    //mPaint.setColor(Color.LTGRAY);
                    //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                    mPaint.setAlpha(0x80);
                    break;
                }
            }

            float margin = 10;

            float startX = margin + mPaint.getStrokeWidth() / 2;
            float startY = image.getHeight() / 2;
            float stopX = image.getWidth() - startX;
            float stopY = startY;

            canvas.drawLine(startX, startY, stopX, stopY, mPaint);

            imageView.setImageBitmap(image);
        }

        return view;
    }
}
