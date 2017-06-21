package com.catalinjujiu.testrubikrenderer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.catalinjurjiu.animcubeandroid.CubeConstants;
import com.catalinjurjiu.originalcoderemade.AnimCube;

public class MainActivity extends Activity {

    private AnimCube animCube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        animCube = (AnimCube) findViewById(R.id.animcube);
        animCube.setMoveSequence("R2' U M U' R2' U M' U'");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_moves:
                animCube.startAnimation(CubeConstants.ANIMATION_PLAY_FORWARD);
                break;
            case R.id.stop_moves:
                animCube.stopAnimation();
                break;
            case R.id.one_move_forward:
                animCube.startAnimation(CubeConstants.ANIMATION_STEP_FORWARD);
                break;
            case R.id.one_move_backward:
                animCube.startAnimation(CubeConstants.ANIMATION_STEP_BACKWARD);
                break;
            case R.id.reset_to_initial:
                animCube.resetToInitialState();
                break;
            case R.id.freeze:
                //TODO
                break;
            case R.id.save_state:
                //TODO
                break;
            case R.id.restore_state:
                //TODO
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
