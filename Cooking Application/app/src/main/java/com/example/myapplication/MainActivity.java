
package com.example.gptvoicevoxapplication;

import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView recipeName;
    private TextView stepNumber;
    private TextView stepTitle;
    private TextView stepDetail;
    private TextView timerText;

    private Button nextButton;
    private Button backButton;
    private Button voiceButton;
    private Button readButton;

    private TextToSpeech tts;

    private int currentStep = 0;
    private int seconds = 0;

    private String[][] steps = {
            {
                    "材料を切ります",
                    "鶏肉はひと口大に、玉ねぎとパプリカは食べやすい大きさに切ります。"
            },
            {
                    "鶏肉を焼きます",
                    "フライパンに油を入れ、中火で鶏肉を焼きます。"
            },
            {
                    "野菜を炒めます",
                    "玉ねぎとパプリカを加えて炒めます。"
            },
            {
                    "トマトで煮込みます",
                    "トマトを加えて弱火で10分ほど煮込んだら完成です。"
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeName = findViewById(R.id.recipeName);
        stepNumber = findViewById(R.id.stepNumber);
        stepTitle = findViewById(R.id.stepTitle);
        stepDetail = findViewById(R.id.stepDetail);
        timerText = findViewById(R.id.timerText);

        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
        voiceButton = findViewById(R.id.voiceButton);
        readButton = findViewById(R.id.readButton);

        recipeName.setText("鶏肉と野菜のトマト煮");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.JAPANESE);
            }
        });

        showStep();

        nextButton.setOnClickListener(v -> {
            if (currentStep < steps.length - 1) {
                currentStep++;
                showStep();
            } else {
                Toast.makeText(this, "料理が完成しました！",
                        Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                showStep();
            }
        });

        readButton.setOnClickListener(v -> readStep());

        voiceButton.setOnClickListener(v -> startVoiceRecognition());
    }

    private void showStep() {
        stepNumber.setText("STEP " + (currentStep + 1)
                + " / " + steps.length);

        stepTitle.setText(steps[currentStep][0]);
        stepDetail.setText(steps[currentStep][1]);
    }

    private void readStep() {
        String text = steps[currentStep][0] + "。" +
                steps[currentStep][1];

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cooking");
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "ja-JP");

        try {
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            Toast.makeText(this, "音声認識を利用できません",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                String command = results.get(0);

                if (command.contains("次へ")
                        || command.contains("進んで")) {
                    nextButton.performClick();

                } else if (command.contains("戻る")
                        || command.contains("前へ")) {
                    backButton.performClick();

                } else if (command.contains("もう一度")
                        || command.contains("読んで")) {
                    readStep();

                } else {
                    Toast.makeText(this, command,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
