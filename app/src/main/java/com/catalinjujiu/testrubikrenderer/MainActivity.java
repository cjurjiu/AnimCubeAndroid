package com.catalinjujiu.testrubikrenderer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.catalinjurjiu.animcubeandroid.AnimCube;

public class MainActivity extends Activity {

    private AnimCube animCube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        animCube = (AnimCube) findViewById(R.id.animcube);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_demo_moves:
//                animCube.stopAnimation();
//                //TODO make demo moves.
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
