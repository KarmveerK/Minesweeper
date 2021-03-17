package com.akaalistudios.minesweeper // the main activity for showing the first screen

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    var difficulty: String = ""
    lateinit var activity_main_enter_rows: EditText
    lateinit var activity_main_enter_columns: EditText
    lateinit var activity_main_enter_mines: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadData()

        val activity_main_radioGroup = findViewById<RadioGroup>(R.id.activity_main_radioGroup)
        val help = findViewById<ImageView>(R.id.help_icon)
        val share = findViewById<ImageView>(R.id.share_icon)
        val activity_main_make_custom_board = findViewById<Button>(R.id.activity_main_make_custom_board)
        activity_main_enter_rows = findViewById(R.id.activity_main_enter_rows)
        activity_main_enter_columns = findViewById(R.id.activity_main_enter_columns)
        activity_main_enter_mines = findViewById(R.id.activity_main_enter_mines)
        val activity_main_start = findViewById<Button>(R.id.activity_main_start)


        activity_main_make_custom_board.setOnClickListener {
            //clears the checks in the radio group and makes the edit texts visible
            activity_main_radioGroup.clearCheck()
            activity_main_enter_rows.visibility = View.VISIBLE
            activity_main_enter_columns.visibility = View.VISIBLE
            activity_main_enter_mines.visibility = View.VISIBLE
            difficulty = " "
        }

        activity_main_radioGroup.setOnCheckedChangeListener { group, checkedId ->
            //makes the edit texts invisible and turns the radio buttons
            activity_main_enter_rows.visibility = View.INVISIBLE
            activity_main_enter_columns.visibility = View.INVISIBLE
            activity_main_enter_mines.visibility = View.INVISIBLE
            when (checkedId) {
                R.id.activity_main_easy -> difficulty = "easy"
                R.id.activity_main_medium -> difficulty = "medium"
                R.id.activity_main_hard -> difficulty = "hard"
            }
        }
        activity_main_start.setOnClickListener {
            startGame(difficulty)
        }

        help.setOnClickListener {
            //shows the instructions of the game
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("INSTRUCTIONS")
            builder.setMessage("Minesweeper for Android lets you play the classic logic game where you have to use your wit to clean mines from Gameboard." +
                    "\nThe game is played in two modes: mine mode, where you can click to open tiles, and flag mode where you can flag tiles. Use long click to mark flags in mine mode and again long click to remove the flag." +
                    "\nTips: \n\n-Try to press open tile where number of flags around the tile equals the number of tile ! Its's by far the fastest way to play the game. And to hit the high scores...")
            builder.setPositiveButton("OK"
            ) { dialog, which ->
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
        share.setOnClickListener {
            //shares the high scores of the game
            val activity_main_best_time = findViewById<TextView>(R.id.activity_main_best_time)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Hey there! This is my highscore in Minesweeper game for android : ${activity_main_best_time.text}")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun loadData() {
        //this methods handles the highscore and last win time of the players using shared preferences
        val sharedPref: SharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)//gets the high score and last win time from BoardActivity
        val lastTime = sharedPref.getString("lastTime", "00:00").toString()
        val bestTime = sharedPref.getString("bestTime", "00:00").toString()
        val activity_main_best_time = findViewById<TextView>(R.id.activity_main_best_time)
        val activity_main_last_game_time = findViewById<TextView>(R.id.activity_main_last_game_time)
        activity_main_last_game_time.text = lastTime
        activity_main_best_time.text = bestTime
        val sharedPref2: SharedPreferences = getSharedPreferences("sharedPref2", Context.MODE_PRIVATE)//sends the initial highscore to the BoardActivity and compares them
        val editor = sharedPref2.edit().apply {
            putString("highscore", bestTime)

        }.apply()
    }

    private fun startGame(difficulty: String?) {
        if (difficulty == " ") {
            //checking if the entries are empty
            if (TextUtils.isEmpty(activity_main_enter_rows.text.toString()) || TextUtils.isEmpty(activity_main_enter_columns.text.toString()) || TextUtils.isEmpty(activity_main_enter_mines.text.toString())) {
                Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_LONG).show()
            } else {
                val rows = activity_main_enter_rows.text.toString().toInt()
                val columns = activity_main_enter_columns.text.toString().toInt()
                val mines = activity_main_enter_mines.text.toString().toInt()
                //declaring all the constraints of the game
                if (rows > 25 || columns > 25 || rows < 5 || columns < 5) {
                    Toast.makeText(this, "The number of rows and columns should be more than 4 and less than 25", Toast.LENGTH_SHORT).show()
                } else if (mines < 3) {
                    Toast.makeText(this, "The number of mines should be more than 2", Toast.LENGTH_SHORT).show()
                } else if (mines > (rows * columns / 4)) {
                    Toast.makeText(this, "The number of mines should be less to avoid overcrowding", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, BoardActivity::class.java).apply {
                        putExtra("Rows", rows)
                        putExtra("Columns", columns)
                        putExtra("Mines", mines)
                        putExtra("mode", 2)
                    }
                    startActivity(intent)
                }
            }


        } else {
            if (difficulty == "") {
                Toast.makeText(this, "Select the difficulty.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, BoardActivity::class.java).apply {
                    putExtra("selectedLevel", difficulty)
                    putExtra("mode", 1)
                }
                startActivity(intent)
            }
        }
    }
}