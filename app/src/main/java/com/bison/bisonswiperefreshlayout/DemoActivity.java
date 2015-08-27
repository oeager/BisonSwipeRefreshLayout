package com.bison.bisonswiperefreshlayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.bison.library.ISwipeView;
import com.bison.library.SwipeRefreshLayout;
import com.bison.library.SwipeMode;
import com.bison.library.extras.PulltoRefreshSwipeView;
import com.bison.library.imp.DefaultSwipeView;
import com.bison.library.imp.DefaultRefreshDrawable;


public class DemoActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        String[] array = new String[50];
        for (int i = 0; i < array.length; i++) {
            array[i] = "string " + i;
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));

//        DefaultRefreshDrawable drawable = new DefaultRefreshDrawable(this);
//        DefaultRefreshDrawable drawable1 = new DefaultRefreshDrawable(this);
//        ISwipeView swipeView=new DefaultSwipeView(drawable,drawable1);
        ISwipeView swipeView = new PulltoRefreshSwipeView(this);
        final SwipeRefreshLayout layout = SwipeRefreshLayout.attach(listView, swipeView, SwipeMode.BOTH);

        layout.setOnSwipeRefreshListener(new SwipeRefreshLayout.SwipeRefreshListener() {
            @Override
            public void onRefresh(final boolean isTopRefresh) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String s = isTopRefresh?"刷新完成":"加载完成";
                                Toast.makeText(DemoActivity.this,s,Toast.LENGTH_SHORT).show();
                                layout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });
    }


}
