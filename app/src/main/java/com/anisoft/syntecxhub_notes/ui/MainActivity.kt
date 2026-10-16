package com.anisoft.syntecxhub_notes.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anisoft.syntecxhub_notes.R
import com.anisoft.syntecxhub_notes.databinding.ActivityMainBinding

/**
 * Host activity that contains the fragment container for the note list and detail screens.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NoteListFragment())
                .commit()
        }
    }
}
