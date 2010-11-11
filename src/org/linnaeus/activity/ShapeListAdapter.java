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
 * Time: 16:22:55
 */

public class ShapeListAdapter extends ArrayAdapter {

    private static final int RESOURCE = R.layout.image_list_item;
    private ArrayList<Shape> _objects;
    private Context _context;

    public ShapeListAdapter(Context context, ArrayList<Shape> objects)
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

        Shape shapeItem = _objects.get(position);

        if (shapeItem != null) {

            TextView textView = (TextView)view.findViewById(R.id.text);
            textView.setText(shapeItem.getName());

            ImageView imageView = (ImageView)view.findViewById(R.id.image);

            Bitmap image = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(image);

            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(3);

            shapeItem.draw(canvas, mPaint,
                    new Region(0,0, image.getWidth(), image.getHeight()));

            imageView.setImageBitmap(image);
        }

        return view;
    }
}
