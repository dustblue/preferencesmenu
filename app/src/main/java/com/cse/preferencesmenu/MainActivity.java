package com.cse.preferencesmenu;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private android.view.ActionMode mActionMode;
    ConstraintLayout parent;
    EditText text;
    Button pop, contextButton;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        parent = findViewById(R.id.main_activity);
        text = findViewById(R.id.text);
        pop = findViewById(R.id.button);
        contextButton = findViewById(R.id.context);

        registerForContextMenu(contextButton);

        contextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActionMode != null) {
                    return;
                }
                mActionMode = MainActivity.this.startActionMode(mActionModeCallback);
                view.setSelected(true);
            }
        });

        pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        text.setText(prefs.getString("text", "Not found"));
        text.setTypeface(null, prefs.getInt("typeface", 0));
        text.setTextColor(prefs.getInt("color", Color.RED));
        parent.setBackgroundColor(prefs.getInt("background", Color.RED));

        radioGroup = (RadioGroup) findViewById(R.id.collapsing_layout);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                text.setTypeface(null, i-1);
                editor.putInt("typeface", i-1);
                editor.apply();
            }
        });
    }

    public void animate(final View v, int duration) {
        final boolean expand = v.getVisibility() != View.VISIBLE;

        int prevHeight = v.getHeight();
        int height = 0;
        if (expand) {
            int measureSpecParams = View.MeasureSpec.getSize(View.MeasureSpec.UNSPECIFIED);
            v.measure(measureSpecParams, measureSpecParams);
            height = v.getMeasuredHeight();
        } else {
            ((RadioGroup) v).clearCheck();
        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, height);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (expand) {
                    v.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!expand) {
                    v.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int color;

        switch (item.getItemId()) {
            case R.id.text:
                Snackbar.make(parent, "Saved Text!!", Snackbar.LENGTH_SHORT).show();
                editor.putString("text", text.getText().toString());
                editor.apply();
                return true;
            case R.id.style:
                color = getRandomColor();
                text.setTextColor(color);
                editor.putInt("style", color);
                editor.apply();
                Snackbar.make(parent, "New Text Color Set!!", Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.background:
                color = getRandomColor();
                parent.setBackgroundColor(color);
                editor.putInt("background", color);
                editor.apply();
                Snackbar.make(parent, "New Background Set!!", Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.dropdown:
                animate(radioGroup, 500);
                return true;
            case R.id.exit:
                Snackbar.make(parent, "Exiting App!!", Snackbar.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup, popup.getMenu());
        popup.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose Text Style");
        menu.add(0, v.getId(), 0, "Normal");
        menu.add(0, v.getId(), 0, "Bold");
        menu.add(0, v.getId(), 0, "Italic");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Snackbar.make(parent, "Text changed to : " + item.getTitle(), Snackbar.LENGTH_SHORT).show();
        if (item.getTitle().equals("Normal")) {
            text.setTypeface(null, 0);
            editor.putInt("typeface", 0);
        } else if (item.getTitle().equals("Bold")) {
            text.setTypeface(null, 1);
            editor.putInt("typeface", 1);
        } else if (item.getTitle().equals("Italic")) {
            text.setTypeface(null, 2);
            editor.putInt("typeface", 2);
        }
        editor.apply();
        return true;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_background:

                    int color = getRandomColor();
                    parent.setBackgroundColor(color);
                    editor.putInt("backgrounhd", color);
                    editor.apply();
                    Snackbar.make(parent, "Context Action Bar, Color Changed", Snackbar.LENGTH_LONG).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
}
