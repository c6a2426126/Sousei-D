package com.example.kikicook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private LinearLayout layoutVoice, layoutRecommend, layoutCookingStep;
    private Button btnTabVoice, btnTabRecommend, btnStartCooking, btnNextStep;
    private TextView tvVoiceStatus, tvRecognizedText, tvRecipeTitle, tvRecipeTime, tvRecipeIngredients;
    private TextView tvStepCount, tvStepDescription;
    private FloatingActionButton fabMic;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    // 簡易的な調理ステップデータ
    private String[] currentSteps = {
            "1. じゃがいもを一口大に切り、耐熱ボウルに入れてラップをし600Wで3分加熱します。",
            "2. フライパンにバターを溶かし、じゃがいもに焼き色がつくまで中火で炒めます。",
            "3. 仕上げに醤油を回し入れ、全体に絡めたら完成です！"
    };
    private int currentStepIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSpeechRecognizer();
        setupListeners();
    }

    private void initViews() {
        layoutVoice = findViewById(R.id.layoutVoice);
        layoutRecommend = findViewById(R.id.layoutRecommend);
        layoutCookingStep = findViewById(R.id.layoutCookingStep);

        btnTabVoice = findViewById(R.id.btnTabVoice);
        btnTabRecommend = findViewById(R.id.btnTabRecommend);
        btnStartCooking = findViewById(R.id.btnStartCooking);
        btnNextStep = findViewById(R.id.btnNextStep);

        tvVoiceStatus = findViewById(R.id.tvVoiceStatus);
        tvRecognizedText = findViewById(R.id.tvRecognizedText);
        tvRecipeTitle = findViewById(R.id.tvRecipeTitle);
        tvRecipeTime = findViewById(R.id.tvRecipeTime);
        tvRecipeIngredients = findViewById(R.id.tvRecipeIngredients);

        tvStepCount = findViewById(R.id.tvStepCount);
        tvStepDescription = findViewById(R.id.tvStepDescription);

        fabMic = findViewById(R.id.fabMic);
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPANESE.toString());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                tvVoiceStatus.setText("お話しください...");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    tvVoiceStatus.setText("タップして話す");
                    handleVoiceResult(spokenText);
                }
            }

            @Override
            public void onError(int error) {
                tvVoiceStatus.setText("認識できませんでした。もう一度タップしてください");
            }

            // 以下、必須のオーバーライドメソッド
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void setupListeners() {
        // タブ切り替え
        btnTabVoice.setOnClickListener(v -> switchTab(true));
        btnTabRecommend.setOnClickListener(v -> switchTab(false));

        // マイクボタン押下
        fabMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
            } else {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });

        // 「この料理を作る」ボタン
        btnStartCooking.setOnClickListener(v -> {
            currentStepIndex = 0;
            layoutCookingStep.setVisibility(View.VISIBLE);
            updateStepUI();
        });

        // 「次のステップへ」ボタン
        btnNextStep.setOnClickListener(v -> {
            if (currentStepIndex < currentSteps.length - 1) {
                currentStepIndex++;
                updateStepUI();
            } else {
                Toast.makeText(this, "料理が完成しました！", Toast.LENGTH_SHORT).show();
                layoutCookingStep.setVisibility(View.GONE);
            }
        });
    }

    // 音声認識結果に応じたレシピマッチングと画面遷移
    private void handleVoiceResult(String spokenText) {
        tvRecognizedText.setText("認識した言葉：「" + spokenText + "」");

        if (spokenText.contains("卵") || spokenText.contains("たまご")) {
            tvRecipeTitle.setText("ふんわりカニ玉あんかけ");
            tvRecipeTime.setText("調理時間：10分");
            tvRecipeIngredients.setText("材料：卵 3個、カニカマ 4本、鶏ガラスープの素 小さじ1");
            currentSteps = new String[]{
                    "1. 卵を溶きほぐし、ほぐしたカニカマを混ぜます。",
                    "2. フライパンで卵を半熟状に焼き、お皿に盛ります。",
                    "3. 甘酢あんを作って上からかけたら完成です！"
            };
        } else if (spokenText.contains("鶏") || spokenText.contains("チキン")) {
            tvRecipeTitle.setText("鶏もも肉の照り焼きチキン");
            tvRecipeTime.setText("調理時間：15分");
            tvRecipeIngredients.setText("材料：鶏もも肉 1枚、醤油 大さじ2、みりん 大さじ2、砂糖 大さじ1");
            currentSteps = new String[]{
                    "1. 鶏もも肉の皮目から中火でパリッと焼きます。",
                    "2. 裏返して蓋をし、弱火で5分蒸し焼きにします。",
                    "3. 調味料を加えて煮絡めたら完成です！"
            };
        } else {
            // デフォルト（じゃがいも等の場合）
            tvRecipeTitle.setText("ほくほくじゃがいものバター醤油炒め");
            tvRecipeTime.setText("調理時間：15分");
            tvRecipeIngredients.setText("材料：じゃがいも 2個、バター 10g、醤油 大さじ1");
        }

        layoutCookingStep.setVisibility(View.GONE);
        switchTab(false); // 自動で「おすすめ」タブへ移動
    }

    private void switchTab(boolean isVoiceTab) {
        if (isVoiceTab) {
            layoutVoice.setVisibility(View.VISIBLE);
            layoutRecommend.setVisibility(View.GONE);
            btnTabVoice.setBackgroundColor(Color.WHITE);
            btnTabRecommend.setBackgroundColor(Color.TRANSPARENT);
        } else {
            layoutVoice.setVisibility(View.GONE);
            layoutRecommend.setVisibility(View.VISIBLE);
            btnTabVoice.setBackgroundColor(Color.TRANSPARENT);
            btnTabRecommend.setBackgroundColor(Color.WHITE);
        }
    }

    private void updateStepUI() {
        tvStepCount.setText("ステップ " + (currentStepIndex + 1) + " / " + currentSteps.length);
        tvStepDescription.setText(currentSteps[currentStepIndex]);
        btnNextStep.setText(currentStepIndex == currentSteps.length - 1 ? "完了する" : "次のステップへ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}