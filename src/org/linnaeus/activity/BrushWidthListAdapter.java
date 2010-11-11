package org.linnaeus.activity;

import android.content.Context;
import android.graphics.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.linnaeus.drawing.Shape;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 10.11.2010
 * Time: 22:43:19
 */

public class BrushWidthListAdapter extends ArrayAdapter {

    private static final int RESOURCE = R.layout.image_list_item;
    private String[] _objects;
    private Context _context;

    public BrushWidthListAdapter(Context context, String[] objects)
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

        String widthItem = _objects[position];

        if (widthItem != null) {

            TextView textView = (TextView)view.findViewById(R.id.text);
            textView.setText(String.valueOf(widthItem));

            ImageView imageView = (ImageView)view.findViewById(R.id.image);

            Bitmap image = Bitmap.createBitmap(250, 48, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(image);

            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(Float.valueOf(widthItem));

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
