package com.example.umc_flo

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.umc_flo.databinding.ActivitySongBinding
import com.google.gson.Gson

class SongActivity : AppCompatActivity() {

    //전역 변수
    lateinit var binding : ActivitySongBinding
    lateinit var timer: Timer
    private var mediaPlayer: MediaPlayer? = null
    private var gson: Gson = Gson()

    val songs = arrayListOf<Song>()
    lateinit var songDB: SongDatabase
    var nowPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayList()
        initSong()
        initClickListener()


    }

    override fun onPause() {
        super.onPause()


        songs[nowPos].second = ((binding.songProgressSb.progress * songs[nowPos].playTime)/100)/1000
        songs[nowPos].isPlaying = false
        setPlayerStatus(false)

        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit() // 에디터

        editor.putInt("songId",songs[nowPos].id)

        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.interrupt()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun initPlayList(){
        songDB = SongDatabase.getInstance(this)!!
        songs.addAll(songDB.songDao().getSongs())
    }

    private fun initClickListener(){
        binding.songDownIb.setOnClickListener {
            finish()
        }

        binding.songMiniplayerIv.setOnClickListener {
            setPlayerStatus(true)
        }

        binding.songPauseIv.setOnClickListener {
            setPlayerStatus(false)
        }

        binding.songNextIv.setOnClickListener {
            moveSong(+1)
        }

        binding.songPreviousIv.setOnClickListener {
            moveSong(-1)
        }

        binding.songLikeIv.setOnClickListener {
            setLike(songs[nowPos].isLike)
        }
    }

    private fun initSong(){
        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId",0)

        nowPos = getPlayingSongPosition(songId)

        Log.d("now Song ID",songs[nowPos].id.toString())

        startTimer()
        setPlayer(songs[nowPos])
    }

    private fun setLike(isLike: Boolean){
        songs[nowPos].isLike = !isLike
        songDB.songDao().updateIsLikeById(!isLike,songs[nowPos].id)

        if (!isLike){
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_on)
        } else{
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_off)
        }

    }

    private fun moveSong(direct: Int){
        if (nowPos + direct < 0){
            Toast.makeText(this,"first song", Toast.LENGTH_SHORT).show()
            return
        }

        if (nowPos + direct >= songs.size){
            Toast.makeText(this,"last song", Toast.LENGTH_SHORT).show()
            return
        }

        nowPos += direct

        timer.interrupt()
        startTimer()

        mediaPlayer?.release()
        mediaPlayer = null

        setPlayer(songs[nowPos])
    }

    private fun getPlayingSongPosition(songId: Int): Int{
        for (i in 0 until songs.size){
            if (songs[i].id == songId){
                return i
            }
        }
        return 0
    }


    private fun setPlayer(song: Song){
        binding.songMusicTitleTv.text = song.title
        binding.songSingerNameTv.text = song.singer
        binding.songStartTimeTv.text = String.format("%02d:%02d",song.second / 60, song.second % 60)
        binding.songEndTimeTv.text = String.format("%02d:%02d",song.playTime / 60, song.playTime % 60)
        binding.songAlbumIv.setImageResource(song.coverImg!!)
        binding.songProgressSb.progress = (song.second * 1000 / song.playTime)

        val music = resources.getIdentifier(song.music, "raw", this.packageName)
        mediaPlayer = MediaPlayer.create(this, music)

        if (song.isLike){
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_on)
        } else{
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_off)
        }

        setPlayerStatus(song.isPlaying)

    }


    private fun setPlayerStatus (isPlaying : Boolean){
        songs[nowPos].isPlaying = isPlaying
        timer.isPlaying = isPlaying

        if(isPlaying){
            binding.songMiniplayerIv.visibility = View.GONE
            binding.songPauseIv.visibility = View.VISIBLE
            mediaPlayer?.start()
        } else {
            binding.songMiniplayerIv.visibility = View.VISIBLE
            binding.songPauseIv.visibility = View.GONE
            if(mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
            }
        }

    }

    private fun startTimer(){
        timer = Timer(songs[nowPos].playTime,songs[nowPos].isPlaying)
        timer.start()
    }

    inner class Timer(private val playTime: Int,var isPlaying: Boolean = true):Thread(){

        private var second : Int = 0
        private var mills: Float = 0f

        override fun run() {
            super.run()
            try {
                while (true){

                    if (second >= playTime){
                        break
                    }

                    if (isPlaying){
                        sleep(50)
                        mills += 50

                        runOnUiThread {
                            binding.songProgressSb.progress = ((mills / playTime)*100).toInt()
                        }

                        if (mills % 1000 == 0f){
                            runOnUiThread {
                                binding.songStartTimeTv.text = String.format("%02d:%02d",second / 60, second % 60)
                            }
                            second++
                        }

                    }

                }

            }catch (e: InterruptedException){
                Log.d("Song","쓰레드가 죽었습니다. ${e.message}")
            }

        }
    }



}


//class SongActivity : AppCompatActivity(){
//
//    private lateinit var binding: ActivitySongBinding
//    private lateinit var timer : Timer
//    private var mediaPlayer : MediaPlayer? = null
//    private var gson: Gson = Gson()
//
//    val songs = arrayListOf<Song>()
//    lateinit var songDB: SongDatabase
//    var nowPos = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySongBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        initPlayList()
//        initSong()
//        initClickListener()
//
//
//        if(intent.hasExtra("title") && intent.hasExtra("singer")){
//            binding.songMusicTitleTv.text=intent.getStringExtra("title")!!
//            binding.songSingerNameTv.text=intent.getStringExtra("singer")!!
//            //텍스트뷰의 텍스트를 바꿔줄건데 인텐트라는 택배 상자에서
//            //타이틀이라는 키값 혹은 싱어라는 키값을 가진 스트링으로 바꿔줄 것이라는 뜻
//        }
//
//        binding.songDownIb.setOnClickListener{
////            val intent = Intent(this, MainActivity::class.java)
////            intent.putExtra("title", song.title)
////            intent.putExtra("title", song.singer)
////            setResult(RESULT_OK,intent)
////            finish()
//
//            val song = Song(binding.songMusicTitleTv.text.toString(),
//                binding.songSingerNameTv.text.toString())
//
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("title", song.title)
//            intent.putExtra("title", song.singer)
//
//            setResult(Activity.RESULT_OK, intent)
//            finish()
//        }
//        binding.songMiniplayerIv.setOnClickListener {
//            setPlayerStatus(true)
//        }
//        binding.songPauseIv.setOnClickListener{
//            setPlayerStatus(false)
//        }
//        binding.songLikeIv.setOnClickListener{
//            setLikeStatus(songs[nowPos].isLike)
//        }
//        binding.songRepeatIv.setOnClickListener{
//            setPlaybackStatus(true)
////            setPlayer(song)
////            restartTimer()
//        }
//        binding.songFullIv.setOnClickListener{
//            setPlaybackStatus(false)
//        }
//
//        binding.songRandomIv.setOnClickListener{
//            setRandomStatus(true)
//        }
//        binding.songFullRandomIv.setOnClickListener{
//            setRandomStatus(false)
//        }
//
//
//
//    }
//    //사용자가 포커스를 잃었을 때 음악이 중지
//    override fun onPause(){
//        super.onPause()
//        setPlayerStatus(false)
//        songs[nowPos].second = ((binding.songProgressSb.progress * songs[nowPos].playTime)/100)/1000
//        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
//        val editor = sharedPreferences.edit() //에디터
//
//
//        editor.putInt("songId", songs[nowPos].id)
//
//        editor.apply()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        timer.interrupt()
//        mediaPlayer?.release() //미디어플레이어가 갖고 있던 리소스 해제
//        mediaPlayer = null //미디어 플레이어 해제
//    }
//
////DB에서 모든 노래를 가져와서 songs 리스트에 추가
//    private fun initPlayList(){
//        songDB = SongDatabase.getInstance(this)!!
//        songs.addAll(songDB.songDao().getSongs())
//    }
//
//
//
//    private fun initSong(){
//        val spf = getSharedPreferences("song", MODE_PRIVATE)
//        val songId = spf.getInt("songId",0)
//
//        nowPos = getPlayingSongPosition(songId)
//
//        Log.d("now Song ID",songs[nowPos].id.toString())
//
//        startTimer()
//        setPlayer(songs[nowPos])
//    }
//
//    private fun getPlayingSongPosition(songId: Int): Int{
//        for (i in 0 until songs.size){
//            if (songs[i].id == songId){
//                return i
//            }
//        }
//        return 0
//    }
//
//    private fun setPlayer(song: Song) {
//        binding.songMusicTitleTv.text = song.title
//        binding.songSingerNameTv.text = song.singer
//        binding.songMusicTitleTv.text=intent.getStringExtra("title")!!
//        binding.songSingerNameTv.text=intent.getStringExtra("singer")!!
//        binding.songAlbumIv.setImageResource(song.coverImg!!)
//        binding.songStartTimeTv.text = String.format("%02d:%02d", this.songs[nowPos].second / 60, this.songs[nowPos].second % 60)
//        binding.songEndTimeTv.text = String.format("%02d:%02d", this.songs[nowPos].playTime / 60, this.songs[nowPos].playTime % 60)
//        binding.songProgressSb.progress = (this.songs[nowPos].second * 1000 / this.songs[nowPos].playTime)
//
//        val music = resources.getIdentifier(song.music, "raw", this.packageName)
//        mediaPlayer = MediaPlayer.create(this, music)
//
//        if (song.isLike){
//            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_on)
//        } else{
//            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_off)
//        }
//
//        setPlayerStatus(song.isPlaying)
//    }
//
//    private fun setLikeStatus(isLike : Boolean) {
//        songs[nowPos].isLike = !isLike
//        songDB.songDao().updateIsLikeById(!isLike,songs[nowPos].id)
//
//        if (!isLike){
//            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_on)
//        } else{
//            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_off)
//        }
//    }
//
//    private fun setPlayerStatus(isPlaying : Boolean){
//        songs[nowPos].isPlaying = isPlaying
//        timer.isPlaying = isPlaying
//
//        if(isPlaying){
//            binding.songMiniplayerIv.visibility = View.GONE
//            binding.songPauseIv.visibility = View.VISIBLE
//            mediaPlayer?.start()
//        }
//        else{
//            binding.songMiniplayerIv.visibility = View.VISIBLE
//            binding.songPauseIv.visibility = View.GONE
//            if(mediaPlayer?.isPlaying == true){
//                mediaPlayer?.pause()
//            }
//        }
//    }
//
////    Full playback, repeat playback
//    private fun setPlaybackStatus(playback : Boolean) {
//        if(playback){
//            binding.songFullIv.visibility = View.VISIBLE
//            binding.songRepeatIv.visibility = View.GONE
//
//
//
//        }
//        else{
//            binding.songFullIv.visibility = View.GONE
//            binding.songRepeatIv.visibility = View.VISIBLE
//        }
//    }
//
//    private fun setRandomStatus(playback : Boolean) {
//        if(playback){
//            binding.songFullRandomIv.visibility = View.VISIBLE
//            binding.songRandomIv.visibility = View.GONE
//        }
//        else{
//            binding.songFullRandomIv.visibility = View.GONE
//            binding.songRandomIv.visibility = View.VISIBLE
//        }
//    }
//
//    private fun startTimer(){
//        timer = Timer(songs[nowPos].playTime, songs[nowPos].isPlaying)
//        timer.start()
//    }
//
////    private fun restartTimer() {
////        try {
////            if (timer.isAlive) {
////                timer.join() // 현재 타이머가 종료될 때까지 대기
////            }
////        } catch (e: InterruptedException) {
////            Log.e("SongActivity", "타이머 종료 대기 중 오류 발생: ${e.message}")
////        }
////        startTimer() // 새로운 타이머 시작
////    }
//
//    inner class Timer(private val playTime: Int, var isPlaying: Boolean = true):Thread(){
//        private var second : Int = 0
//        private var mills: Float = 0f
//
//        override fun run() {
//            super.run()
//            try {
//                while (true){
//                    if (second >= playTime){
//                        break
//                    }
//                    if (isPlaying){
//                        sleep(50)
//                        mills += 50
//
//                        runOnUiThread {
//                            binding.songProgressSb.progress = ((mills / playTime)*100).toInt()
//                        }
//
//                        if (mills % 1000 == 0f){
//                            runOnUiThread {
//                                binding.songStartTimeTv.text = String.format("%02d:%02d",second / 60, second % 60)
//                            }
//                            second++
//                        }
//                    }
//                }
//            }catch (e: InterruptedException){
//                Log.d("Song","쓰레드가 죽었습니다. ${e.message}")
//            }
//        }
//    }
//
//
//}