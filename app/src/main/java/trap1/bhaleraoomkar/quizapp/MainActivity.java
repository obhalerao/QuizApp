package trap1.bhaleraoomkar.quizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
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
    TextView scorebox;
    TextView timeLeft;
    TextView rules;
    TextView points;

    String[] questions = new String[]{"What is 1 + 1?",
            "What state is Chicago in?",
            "What year did the Civil War end?",
            "How many protons are in an an atom of mercury?",
            "What was Juliet's last name in Shakespeare's 'Romeo and Juliet?'"};
    String[] answers = new String[]{"2","Illinois","1865","80","Capulet"};
    TreeSet<Entry> leaderboard = new TreeSet<Entry>(Collections.<Entry>reverseOrder());
    String playerName = "";
    int[] pointVals = new int[]{100,200,300,400,500};
    int currIdx = -1;
    String currAns = "";
    int score = 0;
    Timer timer = new Timer();
    int max_duration = 60;
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
            text1.setText("Please enter your name.");
            entry1.setEnabled(true);
            button1.setText("Submit");
            button1.setOnClickListener(getName);

        }
    };

    View.OnClickListener getName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerName = entry1.getText().toString();
            entry1.setText("");
            timeLeft.setText(getString(R.string.time, duration));
            points.setText(getString(R.string.points, pointVals[currIdx+1]));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String currentTime = getString(R.string.time,--duration);
                            timeLeft.setText(currentTime);
                            if(currIdx >= 0) {
                                String currentPoints = getString(R.string.points, Math.round((double) pointVals[currIdx] * (.6 + .4 * ((double) duration / (double) (max_duration)))));
                                points.setText(currentPoints);
                            }
                            if(duration<=0){
                                button1.setOnClickListener(end);
                                button1.setText("End");
                                text1.setText("Game over! Press 'End' to go to the final screen.");
                                entry1.setText("");
                                timer.cancel();
                            }
                        }
                    });
                }
            }, 1000, 1000);
            newQ.onClick(v);
        }
    };

    View.OnClickListener end = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            timer.cancel();
            timeLeft.setText("\n");
            points.setText("");
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
                saveString = saveString + e.toFullString() + "\n";
                i++;
                if(i==5) break;
            }
            editor.putString(getString(R.string.high_scores_key), saveString);
            editor.commit();
            text1.setText("Game over!");
            scorebox.setText(endString);
            button1.setText("Play again?");
            button1.setOnClickListener(reset);
        }
    };

    View.OnClickListener newQ = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currIdx++;
            if(currIdx >= questions.length){
                end.onClick(v);
            }else {
                entry1.setEnabled(true);
                text1.setText(String.format("Question %s (%s points):\n%s", currIdx + 1, pointVals[currIdx], questions[currIdx]));
                button1.setText("Submit");
                button1.setOnClickListener(response);
            }
        }
    };

    View.OnClickListener response = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currAns = entry1.getText().toString().toLowerCase().trim();
            entry1.setEnabled(false);
            entry1.setText("");
            if (currAns.equals(answers[currIdx].toLowerCase())) {
                long points = Math.round((double)pointVals[currIdx] * (.6 + .4*((double)duration/(double)(max_duration))));
                text1.setText(getString(R.string.right, points));
                score += points;

            } else {
                text1.setText(String.format("%s The correct answer is %s.", getString(R.string.wrong), answers[currIdx]));
            }
            scorebox.setText(String.format("Score: %s", score));
            button1.setText("Continue");
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
            String name = st.nextToken();
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
        button1.setText("Start");
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
