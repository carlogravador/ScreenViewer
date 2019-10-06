package com.example.screenviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CreateLobbyActivity extends AppCompatActivity {

    Button mScreenMirroringButton;
    ViewGroup mViewGroup;
    View mCardLayoutView;

    public void initUI()
    {
        mScreenMirroringButton = findViewById(R.id.cl_start_button);
        mViewGroup = findViewById(R.id.layout_create_lobby);
        mCardLayoutView = findViewById(R.id.cl_card_view_input);

        mScreenMirroringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScreenMirroringButton.getText().equals("Start Screen Mirroring"))
                {
                    moveInputCardViewTo(RelativeLayout.CENTER_HORIZONTAL, true);
                    mScreenMirroringButton.setText("Stop Screen Mirroring");
                }
                else
                {
                    moveInputCardViewTo(RelativeLayout.CENTER_IN_PARENT, true);
                    mScreenMirroringButton.setText("Start Screen Mirroring");
                }
            }
        });
    }

    public void moveInputCardViewTo(int idPos, boolean withTransition) {

        if (withTransition) {
            TransitionManager.beginDelayedTransition(mViewGroup);
        }

        RelativeLayout.LayoutParams positionRules = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        positionRules.addRule(idPos, RelativeLayout.TRUE);
        final int margins = this.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        final int top = this.getResources().getDimensionPixelSize(R.dimen.top_margin);
        positionRules.setMargins(margins, top, margins, margins);
        mCardLayoutView.setLayoutParams(positionRules);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lobby);

        initUI();
    }
}
