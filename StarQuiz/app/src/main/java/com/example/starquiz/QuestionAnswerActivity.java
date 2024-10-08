package com.example.starquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;
import java.util.logging.Logger;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionAnswerActivity extends AppCompatActivity {

    private QuizController quizControl;
    private TextView timerTextView;
    private TextView graceTimerTextView;
    private TextView scoreTextView;
    private TextView levelTextView;
    private TextView questionTextView;
    private TextView userGreeting;
    private Button[] answerButtons = new Button[4];
    private Button trueAnswer;
    private Button[] lifelineButtons = new Button[6];
    private String username;
    private int modeNumUsing = 0;
    private int score;
    private int level;
    private int questionIndex;
    private boolean recheckButtons;
    private boolean timerIsRestored = false;
    private double timerStuff;
    private double timeTracked;
    private boolean picking2 = false;
    private boolean secondChancing = false;
    private Button[] picking2Buttons = new Button[2];
    private int buttonsPressed = 0;
    private Button PickedButton;
    private boolean buttonChecked;
    private difficultyQuestions DQ;
    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);
        Bundle extras = getIntent().getExtras();
        Intent intent = getIntent();
        modeNumUsing = intent.getIntExtra("modeVer", modeNumUsing);
        getDiffMode modeUsing = new getDiffMode();
        DQ = modeUsing.returnQuestions(modeNumUsing);

        recheckButtons = false;
        buttonsPressed = 0;
        // Initialize views by ID
        timerTextView = findViewById(R.id.timer);
        graceTimerTextView = findViewById(R.id.timer2);
        scoreTextView = findViewById(R.id.score);
        levelTextView = findViewById(R.id.level);
        questionTextView = findViewById(R.id.question);
        userGreeting = findViewById(R.id.questionAnswerUserGreeting);

        username = extras.getString("username");
        userGreeting.setText("Hello " + username);

        graceTimerTextView.setText("");



        for (int i = 0; i < 4; i++) {
            int buttonId = getResources().getIdentifier("answer" + (i + 1), "id", getPackageName());
            answerButtons[i] = findViewById(buttonId);
        }
        for (int i = 0; i < 6; i++) {
            int buttonId = getResources().getIdentifier("lifeline" + (i + 1), "id", getPackageName());
            lifelineButtons[i] = findViewById(buttonId);
        }

        // Initialize score and level
        score = 0;
        level = 1;
        timeTracked = 0;
        questionIndex = 0;

        scoreTextView.setText("Score: " + score);
        levelTextView.setText("Q" + level);
        // Set up a sample question and answers
        //displayQuestion("Sample question", new String[]{"Answer 1", "Answer 2", "Answer 3", "Answer 4"});
        runQuestions(questionIndex);


        // Set up answer button click listeners
        for (Button button : answerButtons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle answer selection and move to next question or end the quiz

                    if(picking2)
                    {
                        button.setBackgroundColor(getColor(R.color.titleBackground));
                        picking2Buttons[buttonsPressed] = button;
                        buttonsPressed++;
                        if(buttonsPressed == 2)
                        {
                            checkAnswer();
                        }

                    }
                    else if(secondChancing)
                    {
                        if(button != trueAnswer)
                        {
                            button.setBackgroundColor(getColor(R.color.teal_700));
                        }
                        secondChancing = false;
                    }
                    else
                    {
                        if(PickedButton == null)
                        {
                            PickedButton = button;
                        }
                        checkAnswer();
                    }
                }
            });
        }

        lifelineButtons[0].setOnClickListener(new View.OnClickListener() { //lifeline 1 remove 1
                @Override
                public void onClick(View view) {
                    for (Button button : answerButtons) {
                        if(button != trueAnswer)
                        {
                            button.setEnabled(false);
                            button.setBackgroundColor(getResources().getColor(R.color.teal_700));
                            recheckButtons = true;
                            break;
                        }
                    }

                    lifelineButtons[0].setEnabled(false);
                    lifelineButtons[0].setBackgroundColor(getResources().getColor(R.color.teal_700));
                }
            });

        lifelineButtons[1].setOnClickListener(new View.OnClickListener() { //lifeline 2 skip question
            @Override
            public void onClick(View view) {
                // skip question in database
                stopTimer();
                lifelineButtons[1].setEnabled(false);
                lifelineButtons[1].setBackgroundColor(getResources().getColor(R.color.teal_700));
            }
        });

        lifelineButtons[2].setOnClickListener(new View.OnClickListener() { //lifeline 3 restore timer
            @Override
            public void onClick(View view) {
                timerIsRestored = true;
                stopTimer();
                timerIsRestored = false;
                startTimer(10000, timerTextView, false);
                lifelineButtons[2].setEnabled(false);
                lifelineButtons[2].setBackgroundColor(getResources().getColor(R.color.teal_700));
            }
        });

        lifelineButtons[3].setOnClickListener(new View.OnClickListener() { //lifeline 4 50/50
            @Override
            public void onClick(View view) {
                int l = 0;
                do {
                    Random rand = new Random();
                    int n = rand.nextInt(3);
                    if(answerButtons[n] != trueAnswer && answerButtons[n].isEnabled())
                    {
                        answerButtons[n].setEnabled(false);
                        answerButtons[n].setBackgroundColor(getResources().getColor(R.color.teal_700));
                        recheckButtons = true;
                        l++;
                    }
                }
                while(l != 2);

                lifelineButtons[3].setEnabled(false);
                lifelineButtons[3].setBackgroundColor(getResources().getColor(R.color.teal_700));
            }
        });

        lifelineButtons[4].setOnClickListener(new View.OnClickListener() { //lifeline 5 2nd chance
            @Override
            public void onClick(View view) {
                secondChancing = true;
                lifelineButtons[4].setEnabled(false);
                lifelineButtons[4].setBackgroundColor(getResources().getColor(R.color.teal_700));
            }
        });

        lifelineButtons[5].setOnClickListener(new View.OnClickListener() { //lifeline 6 pick 2
            @Override
            public void onClick(View view) {
                picking2 = true;
                lifelineButtons[5].setEnabled(false);
                lifelineButtons[5].setBackgroundColor(getResources().getColor(R.color.teal_700));
            }
        });

        // Start a sample timer





//        if(!timerStatus && givenGrace)
//        {
//            // next question
//            timerStatus = true;
//            givenGrace = false;
//        }
    }


    public void recheckAllButtons() //make sure to run this after choice is made/before next question is pulled
    {
        PickedButton = null;

        disableButtons(true);
        //buttonChecked = false;

        answerButtons[0].setBackgroundColor(getResources().getColor(R.color.buttonBackground));
        answerButtons[1].setBackgroundColor(getResources().getColor(R.color.buttonBackground));
        answerButtons[2].setBackgroundColor(getResources().getColor(R.color.buttonBackground));
        answerButtons[3].setBackgroundColor(getResources().getColor(R.color.buttonBackground));

    }

    public void disableButtons(boolean b)
    {
        answerButtons[0].setEnabled(b);
        answerButtons[1].setEnabled(b);
        answerButtons[2].setEnabled(b);
        answerButtons[3].setEnabled(b);

//        lifelineButtons[0].setEnabled(b);
//        lifelineButtons[1].setEnabled(b);
//        lifelineButtons[2].setEnabled(b);
//        lifelineButtons[3].setEnabled(b);
//        lifelineButtons[4].setEnabled(b);
//        lifelineButtons[5].setEnabled(b);

    }

    // Assigns background color to show the correct answer and incorrect answers
    private void checkAnswer() {
        if(picking2)
        {
            if(picking2Buttons.length == 2)
            {
                for (Button but: picking2Buttons) {
                    if(but == trueAnswer)
                    {
                        disableButtons(false);
                        //assign points chose right answer
                        score += modeNumUsing;
                        //buttonChecked = true;
                        break;
                    }
                }
                revealAnswer();
                picking2 = false;
            }
        }
        else
        {
            if(PickedButton == trueAnswer)
            {
                disableButtons(false);
                score += modeNumUsing;
            }
            revealAnswer();
        }

    }

    private void revealAnswer()
    {
//        for (Button button : answerButtons) {
//            if (button == trueAnswer) {
//                button.setBackgroundColor(getColor(R.color.green));
//            } else {
//                button.setBackgroundColor(getColor(R.color.red));
//            }
//        }

        buttonsPressed = 0;
        secondChancing = false;

        score += assignLifelinePoints(5); //whatever points they gets for each lifeline send here
        scoreTextView.setText("Score: " + score);

        stopTimer();

        //startTimer(3000, graceTimerTextView, false);
//        questionIndex++;
//        runQuestions(questionIndex);

    }

    private int assignLifelinePoints(int lifelinePointby)
    {
        int additionalPoints = 0;

        for (Button lifeBut: lifelineButtons
        ) {
            if(lifeBut.isEnabled())
            {
                additionalPoints += lifelinePointby;
            }
        }

        return additionalPoints;
    }

    private void displayQuestion(String questionText, String[] WrongAnswers, String RightAnswer) {
        questionTextView.setText(questionText);

        Random rand = new Random();
        int rA = rand.nextInt(3); //0-4
        trueAnswer = answerButtons[rA];
        trueAnswer.setText(RightAnswer);

        int displayIndex = 0;
        for (int i = 0; i < 4; i++) {

            if(i != rA)
            {
                answerButtons[i].setText(WrongAnswers[displayIndex]);
                displayIndex++;
            }
            else
            {
                continue;
            }

        }

    }

    private void runQuestions(int index)
    {

        displayQuestion(DQ.questionAnswerList[index].QuestionPrompting, DQ.questionAnswerList[index].IncorrectAnswers, DQ.questionAnswerList[index].CorrectAnswer);
        //buttonChecked = false;
        startTimer(10000, timerTextView, true);
    }

    private void startTimer(long timeMillis, TextView textV, boolean usingMainTimer) {
        countDownTimer = new CountDownTimer(timeMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

                timerStuff = (millisUntilFinished / 1000.0);
                textV.setText(String.format("%.1f", timerStuff));
            }

            @Override
            public void onFinish() {
                // Handle timer expiration

//                if(usingMainTimer)//automatically start grace  period
//                {
//                    startTimer(3000, graceTimerTextView, false);
//                    timerIsRestored = false;
//                }

                //move to next question
//                recheckAllButtons();
//                level++;
//                levelTextView.setText("Q" + level);
//                questionIndex++;
//                runQuestions(questionIndex);
                stopTimer();

            }
        }.start();
    }

    private void stopTimer()
    {
        if(!timerIsRestored)
        {
            timeTracked += timerStuff;
            level++;
            levelTextView.setText("Q" + level);
            recheckAllButtons();
            questionIndex++;
        }

        countDownTimer.cancel();

        if(questionIndex < 5)
        {
            runQuestions(questionIndex);
        }
        else {
            Intent intent = new Intent(QuestionAnswerActivity.this, SpecificLeaderboard.class);
            intent.putExtra("playerName", username);//username
            intent.putExtra("playerScore", score);//score
            intent.putExtra("playerTime", timeTracked);//time
            intent.putExtra("categoryNum", modeNumUsing); //mode


            startActivity(intent);
        }

    }


//    private void useDifficulty()
//    {
//
//        //database 10
////        questionAnswerOptions[] d10List = new questionAnswerOptions[10];
////        String[] Q1W = {"B", "C", "D"};
////        questionAnswerOptions Q1 = new questionAnswerOptions("Answer is A", "A", Q1W);
////        d10List[0] = Q1;
////        String[] Q2W = {"B", "C", "D"};
////        questionAnswerOptions Q2 = new questionAnswerOptions("Answer is A", "A", Q2W);
////        d10List[1] = Q2;
////
////        difficultyQuestions D10;
//
//    }
}

class difficultyQuestions
{
    int Num;
    questionAnswerOptions[] questionAnswerList = new questionAnswerOptions[10];

    public difficultyQuestions(int n, questionAnswerOptions[] QAOs)
    {
        Num = n;
        questionAnswerList = QAOs;
    }

}
class questionAnswerOptions
{
    String QuestionPrompting;
    String CorrectAnswer;
    String[] IncorrectAnswers = new String[3];

    public questionAnswerOptions(String question, String correct, String[] wrongs)
    {
        QuestionPrompting = question;
        CorrectAnswer = correct;

        for (int i = 0; i < wrongs.length; i++) {
            IncorrectAnswers[i] = wrongs[i];
        }
    }
}

class getDiffMode
{
    //difficulty Easy

    String[] D10W1 = {"Sega", "SquareEnix", "Xbox"};
    questionAnswerOptions D10Q1 = new questionAnswerOptions("Which company released the iconic video game 'Donkey Kong'?", "Nintendo", D10W1);
    String[] D10W2 = {"Football", "FPS", "Baseball"};
    questionAnswerOptions D10Q2 = new questionAnswerOptions("What kind of sport is played on the Gran Turismo video games series?", "Racing", D10W2);
    String[] D10W3 = {"15", "6", "5"};
    questionAnswerOptions D10Q3 = new questionAnswerOptions("How many Grand Theft Auto games have been released?", "16", D10W3);
    String[] D10W4 = {"343", "Microsoft", "Sega"};
    questionAnswerOptions D10Q4 = new questionAnswerOptions("What is the name of the original developers of the 'Halo' series?", "Bungie", D10W4);
    String[] D10W5 = {"Industrial Era", "Medieval Era", "Modern Era"};
    questionAnswerOptions D10Q5 = new questionAnswerOptions("Red Dead Redemption is based in what historical time?", "Western Era", D10W5);



    String[] D20W1 = {"Australia", "Russia", "France"};
    questionAnswerOptions D20Q1 = new questionAnswerOptions("The Minecraft game was created by a developer from which country?", "Sweden", D20W1);
    String[] D20W2 = {"Coins", "Onion Rings", "Money"};
    questionAnswerOptions D20Q2 = new questionAnswerOptions("what must the player collect in the game 'Sonic the Hedgehog'?", "Rings", D20W2);
    String[] D20W3 = {"Vampires", "Mutants", "Cryptids"};
    questionAnswerOptions D20Q3 = new questionAnswerOptions("Dead Rising 2 was a 2010 game about killing which type of evil force?", "Zombies", D20W3);
    String[] D20W4 = {"Epona", "Zelda", "Gannon"};
    questionAnswerOptions D20Q4 = new questionAnswerOptions("What is the name of the hero of 'The Legend of Zelda'?", "Link", D20W4);
    String[] D20W5 = {"Side Scroller", "Platformer", "Visual Novel"};
    questionAnswerOptions D20Q5 = new questionAnswerOptions("\n" +
            "Doom' is considered what type of video game?", "First Person Shooter", D20W5);



    String[] D30W1 = {"Gamecube", "Sega Genesis", "Gameboy"};
    questionAnswerOptions D30Q1 = new questionAnswerOptions("\n" +
            "What was the name of the video game console launched by Sega in 1998?", "Dreamcast", D30W1);
    String[] D30W2 = {"Mario Mario", "Plumber", "Hero"};
    questionAnswerOptions D30Q2 = new questionAnswerOptions("What was 'Mario' first known as in 'Donkey Kong'?", "Jumpman", D30W2);
    String[] D30W3 = {"AC3", "AC2", "AC1"};
    questionAnswerOptions D30Q3 = new questionAnswerOptions("Which 'Assassin's Creed' game is set in Florence, Italy?", "AC Brotherhood", D30W3);
    String[] D30W4 = {"Save The Town", "Purge The Undead", "Clear The Castle"};
    questionAnswerOptions D30Q4 = new questionAnswerOptions("What is the main goal in the first 'Castlevania'?", "Kill Dracula", D30W4);
    String[] D30W5 = {"Lord of the Rings Online", "Final Fantasy 14", "Elder Scrolls Online"};
    questionAnswerOptions D30Q5 = new questionAnswerOptions("\n" +
            "Wrath of the Lich King' was a 2008 expansion pack for which series?", "World of Warcraft", D30W5);



    String[] D40W1 = {"Tokyo", "Pyongyang", "California"};
    questionAnswerOptions D40Q1 = new questionAnswerOptions("Where are Nintendo Original headquarters located?", "Kyoto", D40W1);
    String[] D40W2 = {"California", "New York", "Wisconsin"};
    questionAnswerOptions D40Q2 = new questionAnswerOptions("Watch Dogs 2' is set in which are of the United States?", "San Francisco", D40W2);
    String[] D40W3 = {"Dead Island", "Mad World", "Cyberpunk"};
    questionAnswerOptions D40Q3 = new questionAnswerOptions("In which game can you find Raccoon City?", "Resident Evil", D40W3);
    String[] D40W4 = {"Warframe", "Destiny", "Starcraft"};
    questionAnswerOptions D40Q4 = new questionAnswerOptions("What video game franchise does Rooster Teeth use for their show “Red vs. Blue?”", "Halo", D40W4);
    String[] D40W5 = {"Popcorn", "Orange", "Cheese"};
    questionAnswerOptions D40Q5 = new questionAnswerOptions("Pacman was designed to resemble which food?", "Pizza", D40W5);



    String[] D50W1 = {"XIV", "II", "I"};
    questionAnswerOptions D50Q1 = new questionAnswerOptions("The character 'Cloud' was from which of the Final Fantasy game?", "VII", D50W1);
    String[] D50W2 = {"Nephew", "Rival", "Brother"};
    questionAnswerOptions D50Q2 = new questionAnswerOptions("What was the relation of Kratos with Zeus in the game God of War?", "Son", D50W2);
    String[] D50W3 = {"Raccoon", "Badger", "Tasmanian Devil"};
    questionAnswerOptions D50Q3 = new questionAnswerOptions("Crash is a video game character who is a genetically mutated type of what Animal?", "Bandicoot", D50W3);
    String[] D50W4 = {"James", "Ridley", "John"};
    questionAnswerOptions D50Q4 = new questionAnswerOptions("What is Solid Snake’s real name?", "David", D50W4);
    String[] D50W5 = {"Rocky", "Buddy", "Mister"};
    questionAnswerOptions D50Q5 = new questionAnswerOptions("What is the name of the recurring dog NPC in the Fallout series?", "Dogmeat", D50W5);



    String[] D100W1 = {"Monster Hunter", "Jet Set Radio", "Crazy Taxi"};
    questionAnswerOptions D100Q1 = new questionAnswerOptions("What was the last game to be released on the Sega Dreamcast?", "Puyo Puyo Fever", D100W1);
    String[] D100W2 = {"Killer 7", "Resident Evil 1", "Sonic"};
    questionAnswerOptions D100Q2 = new questionAnswerOptions("What was the first game to introduce a proper saving system?", "The Legend of Zelda", D100W2);
    String[] D100W3 = {"Dog", "Hawk", "Donkey"};
    questionAnswerOptions D100Q3 = new questionAnswerOptions("What animal was Yoshi originally supposed to be?", "Horse", D100W3);
    String[] D100W4 = {"Exact", "20x", "5x"};
    questionAnswerOptions D100Q4 = new questionAnswerOptions("How much bigger than Earth is Minecraft’s map?", "18x", D100W4);
    String[] D100W5 = {"Pac-Man", "MineSweeper", "Pong"};
    questionAnswerOptions D100Q5 = new questionAnswerOptions("What game has been ported the most?", "Tetris", D100W5);

    public difficultyQuestions returnQuestions(int mode)
    {
        questionAnswerOptions[] dList = new questionAnswerOptions[5];
        switch(mode)
        {
            case(10):
                dList[0] = D10Q1;
                dList[1] = D10Q2;
                dList[2] = D10Q3;
                dList[3] = D10Q4;
                dList[4] = D10Q5;
                break;
            case(20):
                dList[0] = D20Q1;
                dList[1] = D20Q2;
                dList[2] = D20Q3;
                dList[3] = D20Q4;
                dList[4] = D20Q5;
                break;
            case(30):
                dList[0] = D30Q1;
                dList[1] = D30Q2;
                dList[2] = D30Q3;
                dList[3] = D30Q4;
                dList[4] = D30Q5;
                break;
            case(40):
                dList[0] = D40Q1;
                dList[1] = D40Q2;
                dList[2] = D40Q3;
                dList[3] = D40Q4;
                dList[4] = D40Q5;
                break;
            case(50):
                dList[0] = D50Q1;
                dList[1] = D50Q2;
                dList[2] = D50Q3;
                dList[3] = D50Q4;
                dList[4] = D50Q5;
                break;
            case(100):
                dList[0] = D100Q1;
                dList[1] = D100Q2;
                dList[2] = D100Q3;
                dList[3] = D100Q4;
                dList[4] = D100Q5;
                break;
        }

        return new difficultyQuestions(mode, dList);
    }
}