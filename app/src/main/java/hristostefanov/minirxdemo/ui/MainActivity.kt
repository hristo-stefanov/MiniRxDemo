package hristostefanov.minirxdemo.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import hristostefanov.minirxdemo.App
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.presentation.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val _compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val factory = (application as App).component.getViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        refreshButton.setOnClickListener {
            viewModel.onRefresh()
        }
    }

    override fun onStart() {
        super.onStart()

        _compositeDisposable.add(viewModel.errorMessage.observeOn(AndroidSchedulers.mainThread()).subscribe{
            messageTextView.text = it
            messageTextView.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
        })

        _compositeDisposable.add(viewModel.postList.observeOn(AndroidSchedulers.mainThread()).subscribe {
            recyclerView.adapter = PostAdapter(it)
        })
    }

    override fun onStop() {
        _compositeDisposable.dispose()
        super.onStop()
    }
}
