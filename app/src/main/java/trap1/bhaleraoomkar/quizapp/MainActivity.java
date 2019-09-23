package trap1.bhaleraoomkar.quizapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.widget.*;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    Button button1;
    Button resetButton;
    EditText entry1;
    TextView text1;
    TextView hello;
    TextView scorebox;
    TextView timeLeft;
    TextView rules;
    TextView points;
    ConstraintLayout layout;

    String[] questions = new String[]{"What is 1 + 1?",
            "What state is Chicago in?",
            "What year did the Civil War end?",
            "How many protons are in an an atom of mercury?",
            "What was Juliet's last name in Shakespeare's 'Romeo and Juliet?'",
            "What is the decimal representation of 1110110?",
            "What section of Article I of the U.S. Constitution describes the purpose of the federal government?",
            "Positive feedback loops occur when there are a __ number of negative connectors.",
            "The authors of now-ubiquitous writing style guide were __ and White.",
            "The best Mobile teacher at TJ is Mr. __"
    };
    String[] answers = new String[]{"2","Illinois","1865","80","Capulet","118","8","Even","Strunk", "Tra"};
    TreeSet<Entry> leaderboard = new TreeSet<Entry>(Collections.<Entry>reverseOrder());
    String playerName = "";
    int[] pointVals = new int[]{100,200,300,400,500,1000,1750,3750,7000,11500};
    int currIdx = -1;
    String currAns = "";
    int score = 0;
    Timer timer = new Timer();
    int max_duration = 9100;
    int duration = max_duration;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;



    View.OnClickListener reset = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            score = 0;
            currIdx = -1;
            duration = max_duration;
            timer = new Timer();
            resetButton.setVisibility(View.GONE);
            rules.setVisibility(View.GONE);
            timeLeft.setText("\n");
            points.setText("");
            scorebox.setText(String.format("Score: %s", score));
            name.onClick(v);
        }
    };

    View.OnClickListener resetLeaderboard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            leaderboard.clear();
            editor.clear();
            editor.commit();
            scorebox.setText(String.format("Final Score: %s\n\nTop 5 scores:\n", score));
            resetButton.setVisibility(View.GONE);
        }
    };

    View.OnClickListener name = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hello.setVisibility(View.INVISIBLE);
            text1.setText(getString(R.string.name_enter));
            entry1.setVisibility(View.VISIBLE);
            entry1.setEnabled(true);
            button1.setText(getString(R.string.click_button));
            button1.setOnClickListener(getName);

        }
    };

    View.OnClickListener getName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerName = entry1.getText().toString();
            entry1.setText("");
            timeLeft.setText(getString(R.string.time, duration/100));
            points.setText(getString(R.string.points, pointVals[currIdx+1]));
            newQ.onClick(v);
        }
    };

    View.OnClickListener end = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            layout.setBackgroundColor(Color.WHITE);
            timeLeft.setText("\n");
            points.setText("");
            entry1.setVisibility(View.GONE);
            if(leaderboard.size() == 0 || score >= leaderboard.first().getScore()) {
                Context context = getApplicationContext();
                String text = "New High Score!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            resetButton.setVisibility(View.VISIBLE);
            long time = System.currentTimeMillis();
            String endString = String.format("Final Score: %s\n\nTop 5 scores:\n", score);
            String saveString = "";
            leaderboard.add(new Entry(playerName, score, time));
            int i = 0;
            for(Entry e: leaderboard){
                endString = endString + (i+1) + ". " + e.toString() + "\n";
                saveString = saveString + "a" + e.toFullString() + "\n";
                i++;
                if(i==5) break;
            }
            editor.putString(getString(R.string.high_scores_key), saveString);
            editor.commit();
            text1.setText(getString(R.string.game_over));
            scorebox.setText(endString);
            button1.setText(getString(R.string.play_again));
            button1.setOnClickListener(reset);
        }
    };

    View.OnClickListener newQ = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            layout.setBackgroundColor(Color.WHITE);
            currIdx++;
            entry1.setVisibility(View.VISIBLE);
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(currIdx >= questions.length){
                                timer.cancel();
                            }
                            else if(currIdx >= 0) {
                                String currentTime = getString(R.string.time,--duration/100);
                                timeLeft.setText(currentTime);
                                String currentPoints = getString(R.string.points, Math.round((double) pointVals[currIdx] * (.6 + .4 * ((double) duration / (double) (max_duration)))));
                                points.setText(currentPoints);
                            }
                            if(duration<=0){
                                button1.setOnClickListener(end);
                                button1.setText(getString(R.string.end));
                                text1.setText(getString(R.string.final_screen));
                                entry1.setText("");
                                timer.cancel();
                            }
                        }
                    });
                }
            }, 0, 10);
            if(currIdx >= questions.length){
                end.onClick(v);
            }else {
                entry1.setEnabled(true);
                text1.setText(String.format("Question %s (%s points):\n%s", currIdx + 1, pointVals[currIdx], questions[currIdx]));
                button1.setText(getString(R.string.click_button));
                button1.setOnClickListener(response);
            }
        }
    };

    View.OnClickListener response = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            timer.cancel();
            timer.purge();

            currAns = entry1.getText().toString().toLowerCase().trim();
            entry1.setVisibility(View.GONE);
            entry1.setEnabled(false);
            entry1.setText("");
            if (currAns.equals(answers[currIdx].toLowerCase())) {
                layout.setBackgroundColor(Color.GREEN);
                long points = Math.round((double)pointVals[currIdx] * (.6 + .4*((double)duration/(double)(max_duration))));
                text1.setText(getString(R.string.right, points));
                score += points;

            } else {
                layout.setBackgroundColor(Color.RED);
                text1.setText(String.format("%s The correct answer is %s.", getString(R.string.wrong), answers[currIdx]));
            }
            scorebox.setText(String.format("Score: %s", score));
            button1.setText(getString(R.string.click_button_continue));
            button1.setOnClickListener(newQ);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences(getString(R.string.high_scores_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();



        String high_scores = sharedPref.getString(getString(R.string.high_scores_key), "");

        String[] split_vals = high_scores.trim().split("\n");

        for(String s: split_vals){
            if(s.equals("")) continue;
            StringTokenizer st = new StringTokenizer(s);
            String name = st.nextToken().substring(1);
            int score = Integer.parseInt(st.nextToken());
            long time = Long.parseLong(st.nextToken());
            leaderboard.add(new Entry(name, score, time));
        }

        button1 = (Button)findViewById(R.id.clickButton);
        entry1 = (EditText)findViewById(R.id.answer);
        text1 = (TextView)findViewById(R.id.textBox);
        scorebox = (TextView)findViewById(R.id.score);
        timeLeft = (TextView)findViewById(R.id.timeLeft);
        resetButton = (Button)findViewById(R.id.resetLeaderboard);
        rules = (TextView)findViewById(R.id.rules);
        points = (TextView)findViewById(R.id.points);
        layout = (ConstraintLayout)findViewById(R.id.layout);
        hello = (TextView)findViewById(R.id.hello);
        button1.setOnClickListener(reset);
        resetButton.setOnClickListener(resetLeaderboard);



    }





}
class Entry implements Comparable<Entry>{
    private String name;
    private int score;
    private long time;

    public Entry(String n, int s, long t){
        name = n;
        score = s;
        time = t;
    }

    public int compareTo(Entry e){
        if(score < e.score) return -1;
        else if(score > e.score) return 1;
        else if(time < e.time) return -1;
        else if(time > e.time) return 1;
        else return 0;
    }

    public String toString(){
        return name + ": " + score;
    }

    public String toFullString(){
        return name + " " + score + " " + time;
    }

    public String getName(){return name;}
    public int getScore(){return score;}
}
