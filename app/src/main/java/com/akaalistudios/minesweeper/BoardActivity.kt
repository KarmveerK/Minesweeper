package com.akaalistudios.minesweeper

import android.R.attr
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View.OnLongClickListener
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class BoardActivity : AppCompatActivity() {

    lateinit var activity_board_restart:ImageView
    private lateinit var chronometer : Chronometer
    lateinit var activity_board_mines:TextView
    var status = Status.ONGOING
    var choice : Int = 1
    var flaggedMines = 0
    var bestTime = "00:00"
    var lastTime = "00:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        val intent = intent
        val mode = intent.getIntExtra("mode", 0)
        when(mode){
            1 -> {
                val difficulty = intent.getStringExtra("selectedLevel")
                if (difficulty.equals("easy")) {
                    setUpBoard(10, 10, 10)
                } else if (difficulty.equals("medium")) {
                    setUpBoard(13, 13, 20)
                } else if (difficulty.equals("hard")) {
                    setUpBoard(16, 16, 40)
                }
            }
            2 -> {
                val rows = intent.getIntExtra("Rows", 0)
                val columns = intent.getIntExtra("Columns", 0)
                val mines = intent.getIntExtra("Mines", 0)
                setUpBoard(rows, columns, mines)
            }
        }
        activity_board_restart = findViewById<ImageView>(R.id.activity_board_restart)
        activity_board_restart.setImageResource(R.drawable.smile)
        activity_board_restart.setOnClickListener{
            gameRestart()
        }
    }

    private fun gameRestart() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setMessage("Are you sure you want to restart the game?")
        builder.setCancelable(false)

        builder.setPositiveButton("Yes"
        ){ dialog, which ->
            val intent = intent
            finish()
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, which -> }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun setUpBoard(rows: Int, columns: Int, mines: Int) {
        activity_board_mines = findViewById<TextView>(R.id.activity_board_mines)
        activity_board_mines.text = mines.toString()

        val board = findViewById<LinearLayout>(R.id.board)
        //array containing mice cells
        val cellBoard = Array(rows) { Array(columns) { MineCell(this) } }

        //parameters of linear layout
        val params1 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        )
        //parameters of buttons
        val params2 = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        var counter = 1
        var isFirstClick = true

        for (i in 0 until rows) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            params1.weight = 1.0F

            for (j in 0 until columns) {
                val button = MineCell(this)
                button.id = counter
                button.textSize = 18.0F
                button.layoutParams = params2
                params2.weight = 1.0F
                button.setBackgroundResource(R.drawable.minesweeper_unopened_square)
                cellBoard[i][j] = button
                button.setOnClickListener {
                    // Checking for first click
                    if (isFirstClick) {
                        setMines(j,rows, columns, mines, cellBoard )
                        isFirstClick = false
                        startTimer()
                    }
                    choice = 1
                    move(choice, i, j, cellBoard, rows, columns, mines)
                    display(cellBoard)
                }

                button.setOnLongClickListener{
                    choice = 2
                    move(choice, i, j, cellBoard, rows, columns, mines)
                    display(cellBoard)
                    true
                }
                linearLayout.addView(button)
                counter++
            }
            board.addView(linearLayout)
        }

    }

    private fun display(cellBoard: Array<Array<MineCell>>) {
        cellBoard.forEach { row ->
            row.forEach {
                if(it.isRevealed)
                    setNumberImage(it)
                else if (it.isMarked)
                    it.setBackgroundResource(R.drawable.flag)
                else if (status == Status.LOST && it.value == MINE) {
                    it.setBackgroundResource(R.drawable.bomb)
                }
                //To show that mine is not present here but it is marked
                if(status == Status.LOST && it.isMarked && !it.isMine){
                    it.setBackgroundResource(R.drawable.crossedflag)
                }
                else if (status == Status.WON && it.value == MINE) {
                    it.setBackgroundResource(R.drawable.flag)
                }
                else if(status == Status.LOST){
                    activity_board_restart.setImageResource(R.drawable.sad)
                    it.isEnabled = false

                }
            }

        }
    }

    private fun setMines(j:Int,rows: Int, columns: Int, mines: Int, cellBoard: Array<Array<MineCell>>) {

        val mineCount:Int= mines
        var i:Int = 1
        while (i<=mineCount){
            var r = (Random(System.nanoTime()).nextInt(0, rows))
            var c = (Random(System.nanoTime()).nextInt(0, columns))
            if (c==j ||cellBoard[r][c].isMine){
                continue
            }
            cellBoard[r][c].isMine = true
            cellBoard[r][c].value = MINE
            updateNeighbours(r, c, cellBoard, rows, columns)
            i++
        }

    }

    private fun updateNeighbours(r: Int, c: Int, cellBoard: Array<Array<MineCell>>, rows: Int, columns: Int) {
        for (i in movement) {
            for (j in movement) {
                if(((r+i) in 0 until rows) && ((c+j) in 0 until columns) && cellBoard[r + i][c + j].value != MINE)
                    cellBoard[r + i][c + j].value++
            }
        }
    }

    private fun move(choice: Int, x: Int, y: Int, cellBoard: Array<Array<MineCell>>, rows: Int, columns: Int, mines: Int):Boolean{
        if(choice==1){
            if(cellBoard[x][y].isMarked || cellBoard[x][y].isRevealed){
                return false
            }
            if(cellBoard[x][y].value == MINE){
                status = Status.LOST
                updateScore()
                return true
            }
            moveRecursion(x, y, cellBoard, rows, columns)
            checkStatus(cellBoard, rows, columns)
            return true
        }
        if(choice == 2){

            if(cellBoard[x][y].isRevealed) return false

            else if(cellBoard[x][y].isMarked){
                flaggedMines--
                cellBoard[x][y].setBackgroundResource(R.drawable.minesweeper_unopened_square)
                cellBoard[x][y].isMarked = false
                checkStatus(cellBoard, rows, columns)
            }
            else {
                if(flaggedMines==mines){
                    Toast.makeText(this, "Maximum number of mines reached", Toast.LENGTH_LONG).show()
                    return false
                }
                flaggedMines++
                cellBoard[x][y].isMarked = true
                checkStatus(cellBoard, rows, columns)
            }
            var finalMineCount = mines-flaggedMines
            activity_board_mines.text = finalMineCount.toString()
            return true
        }

        return false
    }

    private fun isValid(x:Int, y:Int,rowSize: Int,colSize: Int):Boolean{
        return ((x>=0)&&(x<=rowSize-1))&&((y>=0)&&(y<=colSize-1))
    }

    private fun moveRecursion(x: Int, y: Int, cellBoard: Array<Array<MineCell>>, rowSize: Int, colSize: Int) {
        if(isValid(x,y,rowSize,colSize) && !cellBoard[x][y].isMarked && !cellBoard[x][y].isRevealed){
            cellBoard[x][y].isRevealed = true
            if (cellBoard[x][y].value == 0){
                for (i in movement) {
                    for (j in movement) {
                        if (isValid(x , y ,rowSize,colSize)) {
                            if (cellBoard[x ][y ].value != -1) moveRecursion( x+i , y+j ,cellBoard,rowSize,colSize)
                        }
                    }
                }

            }
        }

    }


    private fun checkStatus(cellBoard: Array<Array<MineCell>>, rowSize: Int, colSize: Int) {
        var flag1=0
        var flag2=0
        for(i in 0 until rowSize){
            for(j in 0 until colSize){
                if(cellBoard[i][j].value==MINE && !cellBoard[i][j].isMarked){
                    flag1=1
                }
                if(cellBoard[i][j].value!=MINE && !cellBoard[i][j].isRevealed){
                    flag2=1
                }
            }
        }
        if(flag1==0 || flag2==0) status = Status.WON
        else status = Status.ONGOING

        if(status==Status.WON) updateScore()
    }

    private fun gameLost() {
        //activity_board_restart.setImageResource(R.drawable.sad)
        Toast.makeText(this,"GAME LOST! \nRestart and try again!",Toast.LENGTH_LONG).show()
        val mp : MediaPlayer = MediaPlayer.create(this, R.raw.defeat)
        mp.start()
    }

    private fun startTimer() {
        chronometer = findViewById<Chronometer>(R.id.timer)
        chronometer.start()
    }

    private fun updateScore() {
        chronometer.stop()
        if(status == Status.WON){
            val time = (chronometer.text).toString()


            val sharedPref2 :SharedPreferences = getSharedPreferences("sharedPref2",Context.MODE_PRIVATE)
            val highscore = sharedPref2.getString("highscore","00:00")
            if (highscore != null) {
                bestTime = highscore
                Log.d("score","highscore initially $bestTime")
            }


            val sharedPref :SharedPreferences = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
            val editor = sharedPref.edit().apply {
                lastTime = time
                putString("lastTime",time)

                if (bestTime>lastTime){
                    bestTime = lastTime
                    putString("bestTime",bestTime)
                }
                else if (bestTime == "00:00"){
                    bestTime = lastTime
                    putString("bestTime",bestTime)
                }
                else{
                    putString("bestTime",bestTime)
                }
                Log.d("score","$bestTime    $lastTime")
            }.apply()
            gameWon()
        }
        else if (status ==Status.LOST){
            gameLost()
        }
    }
    private fun setNumberImage(button: MineCell) {
        if(button.value==0) button.setBackgroundResource(R.drawable.zero)
        if(button.value==1) button.setBackgroundResource(R.drawable.one)
        if(button.value==2) button.setBackgroundResource(R.drawable.two)
        if(button.value==3) button.setBackgroundResource(R.drawable.three)
        if(button.value==4) button.setBackgroundResource(R.drawable.four)
        if(button.value==5) button.setBackgroundResource(R.drawable.five)
        if(button.value==6) button.setBackgroundResource(R.drawable.six)
        if(button.value==7) button.setBackgroundResource(R.drawable.seven)
        if(button.value==8) button.setBackgroundResource(R.drawable.eight)

    }

    private fun gameWon(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val mp : MediaPlayer = MediaPlayer.create(this, R.raw.victory)
        mp.start()
        builder.setTitle("Congratulations! You Won")
        builder.setCancelable(false)

        builder.setPositiveButton("Restart Game"
        ){ dialog, which ->
            val intent = intent
            finish()
            startActivity(intent)
        }

        builder.setNegativeButton("Main Page") { dialog, which ->
            val intent = Intent(this@BoardActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
    companion object{
        const val MINE = -1
        val movement = intArrayOf(-1, 0, 1)
    }
    enum class Status{
        WON,
        ONGOING,
        LOST
    }
}