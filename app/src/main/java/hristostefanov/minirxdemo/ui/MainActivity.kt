package hristostefanov.minirxdemo.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
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
        swipeRefreshLayout.refreshes().subscribe(viewModel.refreshObserver)
    }

    override fun onStart() {
        super.onStart()

        viewModel.progressIndicator.observeOn(AndroidSchedulers.mainThread()).subscribe {
            swipeRefreshLayout.isRefreshing = it
        }.also {
            compositeDisposable.add(it)
        }

        viewModel.errorMessage.observeOn(AndroidSchedulers.mainThread()).subscribe{
            messageTextView.text = it
            messageTextView.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
        }.also {
            compositeDisposable.add(it)
        }

        viewModel.postList.observeOn(AndroidSchedulers.mainThread()).subscribe {
            recyclerView.adapter = PostAdapter(it)
        }.also {
            compositeDisposable.add(it)
        }
    }

    override fun onStop() {
        // Note: using #clear clear because the container will be reused between start-stop-start
        // transitions
        compositeDisposable.clear()
        super.onStop()
    }
}
