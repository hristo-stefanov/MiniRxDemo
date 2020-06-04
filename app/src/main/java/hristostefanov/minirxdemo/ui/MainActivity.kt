package hristostefanov.minirxdemo.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import hristostefanov.minirxdemo.App
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.presentation.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((application as App).component)
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        refreshButton.clicks().subscribe(viewModel.refreshObserver)
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable.add(viewModel.errorMessage.observeOn(AndroidSchedulers.mainThread()).subscribe{
            messageTextView.text = it
            messageTextView.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
        })

        compositeDisposable.add(viewModel.postList.observeOn(AndroidSchedulers.mainThread()).subscribe {
            recyclerView.adapter = PostAdapter(it)
        })
    }

    override fun onStop() {
        // TODO should #clear it onStop?
        compositeDisposable.dispose()
        super.onStop()
    }
}
