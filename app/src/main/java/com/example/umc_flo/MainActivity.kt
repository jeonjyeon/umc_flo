package com.example.umc_flo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.umc_flo.databinding.ActivityMainBinding
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var song: Song = Song()
    private var gson: Gson = Gson()
//    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
//    private var REQUEST_CODE = 100

    //예전 구현 방법으로 해보기 main액티랑 song액티 연결해보기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setTheme(R.style.Theme_FLO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val song = Song(
//            binding.mainMiniplayerTitleTv.text.toString(),
//            binding.mainMiniplayerSingerTv.text.toString(), 0, 60, false, "music_lilac"
//        )

        binding.mainPlayerCl.setOnClickListener {
            val editor = getSharedPreferences("song", MODE_PRIVATE).edit()
            editor.putInt("songId",song.id)
            editor.apply()

            val intent = Intent(this,SongActivity::class.java)
            startActivity(intent)
        }

        inputDummySongs()
//        inputDummyAlbums()
        initBottomNavigation()

        Log.d("Song", song.title + song.singer)


    }


    private fun initBottomNavigation() {


        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        var fragData = "home"

        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.to_left, R.anim.from_left)
                        //앞의 것은 기존 화면에서 넘어갈 때, 뒤의 것은 해당 페이지로 올때
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()

                    fragData = "home"
                    return@setOnItemSelectedListener true
                }

                R.id.lookFragment -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.to_left, R.anim.from_right)
                        .replace(R.id.main_frm, LookFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.searchFragment -> {
                    if (fragData == "locker") {

                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.to_left, R.anim.from_left)
                            .replace(R.id.main_frm, SearchFragment())
                            .commitAllowingStateLoss()

                    } else {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.to_right, R.anim.from_right)
                            .replace(R.id.main_frm, SearchFragment())
                            .commitAllowingStateLoss()
                    }

                    fragData = "search"
                    return@setOnItemSelectedListener true
                }

                R.id.lockerFragment -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.to_right, R.anim.from_right)
                        .replace(R.id.main_frm, LockerFragment())
                        .commitAllowingStateLoss()

                    fragData = "locker"
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun setMiniPlayer(song : Song){
        binding.mainMiniplayerTitleTv.text = song.title
        binding.mainMiniplayerSingerTv.text = song.singer
        binding.mainMiniplayerProgressSb.progress = (song.second*100000)/song.playTime
    }

    fun updateMainPlayerCl(album : Album) {
        binding.mainMiniplayerTitleTv.text = album.title
        binding.mainMiniplayerSingerTv.text = album.singer
        binding.mainMiniplayerProgressSb.progress = 0
    }

    override fun onStart() {
        super.onStart()
//        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
//        val songJson = sharedPreferences.getString("songData", null)
//
//        song = if (songJson == null) {
//            Song("라일락", "아이유(IU)", 0, 60, false, "music_lilac")
//        } else {
//            gson.fromJson(songJson, Song::class.java)
//        }

        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId",0)

        val songDB = SongDatabase.getInstance(this)!!

        song = if (songId == 0){
            songDB.songDao().getSong(1)
        } else{
            songDB.songDao().getSong(songId)
        }

        Log.d("song ID", song.id.toString())


        setMiniPlayer(song)

    }

    //DB에 더미데이터 insert
    private fun inputDummySongs(){
        val songDB = SongDatabase.getInstance(this)!!
        val songs = songDB.songDao().getSongs()

        if (songs.isNotEmpty()) return

        songDB.songDao().insert(
            Song(
                "Lilac",
                "아이유 (IU)",
                0,
                200,
                false,
                "music_lilac",
                R.drawable.img_album_exp2,
                false,
            )
        )

        songDB.songDao().insert(
            Song(
                "Flu",
                "아이유 (IU)",
                0,
                200,
                false,
                "music_flu",
                R.drawable.img_album_exp2,
                false,
            )
        )

        songDB.songDao().insert(
            Song(
                "Butter",
                "방탄소년단 (BTS)",
                0,
                190,
                false,
                "music_butter",
                R.drawable.img_album_exp,
                false,
            )
        )

        songDB.songDao().insert(
            Song(
                "Next Level",
                "에스파 (AESPA)",
                0,
                210,
                false,
                "music_next",
                R.drawable.img_album_exp3,
                false,
            )
        )


        songDB.songDao().insert(
            Song(
                "Boy with Luv",
                "music_boy",
                0,
                230,
                false,
                "music_lilac",
                R.drawable.img_album_exp4,
                false,
            )
        )


        songDB.songDao().insert(
            Song(
                "BBoom BBoom",
                "모모랜드 (MOMOLAND)",
                0,
                240,
                false,
                "music_bboom",
                R.drawable.img_album_exp5,
                false,
            )
        )

        val _songs = songDB.songDao().getSongs()
        Log.d("DB data", _songs.toString())
    }

//    //ROOM_DB
//    private fun inputDummyAlbums() {
//        val songDB = SongDatabase.getInstance(this)!!
//        val albums = songDB.albumDao().getAlbums()
//
//        if (albums.isNotEmpty()) return
//
//        songDB.albumDao().insert(
//            Album(
//                0,
//                "IU 5th Album 'LILAC'", "아이유 (IU)", R.drawable.img_album_exp2
//            )
//        )
//
//        songDB.albumDao().insert(
//            Album(
//                1,
//                "Butter", "방탄소년단 (BTS)", R.drawable.img_album_exp
//            )
//        )
//
//        songDB.albumDao().insert(
//            Album(
//                2,
//                "iScreaM Vol.10 : Next Level Remixes", "에스파 (AESPA)", R.drawable.img_album_exp3
//            )
//        )
//
//        songDB.albumDao().insert(
//            Album(
//                3,
//                "MAP OF THE SOUL : PERSONA", "방탄소년단 (BTS)", R.drawable.img_album_exp4
//            )
//        )
//
//        songDB.albumDao().insert(
//            Album(
//                4,
//                "GREAT!", "모모랜드 (MOMOLAND)", R.drawable.img_album_exp5
//            )
//        )
//
//    }

}